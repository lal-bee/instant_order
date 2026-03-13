package fun.cyhgraph.service.serviceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.*;
import fun.cyhgraph.entity.*;
import fun.cyhgraph.exception.AddressBookBusinessException;
import fun.cyhgraph.exception.OrderBusinessException;
import fun.cyhgraph.exception.ShoppingCartBusinessException;
import fun.cyhgraph.mapper.*;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.OrderService;
import fun.cyhgraph.utils.HttpClientUtil;
import fun.cyhgraph.utils.WeChatPayUtil;
import fun.cyhgraph.vo.OrderPaymentVO;
import fun.cyhgraph.vo.OrderStatisticsVO;
import fun.cyhgraph.vo.OrderSubmitVO;
import fun.cyhgraph.vo.OrderVO;
import fun.cyhgraph.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    private Order order;
    @Autowired
    private WebSocketServer webSocketServer;
    // 这个Value是annotation注解的包，不是lombok的！
    @Value("${hanye.shop.address}")
    private String shopAddress;
    @Value("${hanye.baidu.ak}")
    private String ak;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String SCAN_PARAMS_KEY_PREFIX = "scan_params:user:";

    /**
     * 用户下单
     *
     * @param orderSubmitDTO
     * @return
     */
    public OrderSubmitVO submit(OrderSubmitDTO orderSubmitDTO) {
        // 1、地址或堂食（storeId+tableId）
        Integer addressId = orderSubmitDTO.getAddressId();
        Long storeId = orderSubmitDTO.getStoreId();
        String tableId = orderSubmitDTO.getTableId();
        // 堂食但请求未带 storeId/tableId 时，从 Redis 取出（正式发布后扫码参数存 Redis）
        if (addressId == null && (storeId == null || !StringUtils.hasText(tableId))) {
            Integer userId = BaseContext.getCurrentId();
            String key = SCAN_PARAMS_KEY_PREFIX + userId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null && cached.toString().contains(":")) {
                String[] parts = cached.toString().split(":", 2);
                storeId = Long.parseLong(parts[0].trim());
                tableId = parts[1].trim();
                orderSubmitDTO.setStoreId(storeId);
                orderSubmitDTO.setTableId(tableId);
            }
        }
        boolean isInStore = (addressId == null && storeId != null && StringUtils.hasText(tableId));
        if (!isInStore && addressId == null) {
            throw new OrderBusinessException(MessageConstant.SCAN_PARAMS_REQUIRED);
        }
        AddressBook addressBook = null;
        if (!isInStore) {
            addressBook = addressBookMapper.getById(orderSubmitDTO.getAddressId());
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
        }
        // 2、查询校验购物车情况
        Integer userId = BaseContext.getCurrentId();
        Cart cart = new Cart();
        cart.setUserId(userId);
        List<Cart> cartList = cartMapper.list(cart);
        if (cartList == null || cartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.CART_IS_NULL);
        }
        // 3、构建订单数据
        Order order = new Order();
        BeanUtils.copyProperties(orderSubmitDTO, order);
        if (isInStore) {
            order.setAddressBookId(null);
            order.setAddress("堂食-门店" + orderSubmitDTO.getStoreId() + "-桌" + orderSubmitDTO.getTableId());
            order.setPhone("");
            order.setConsignee("堂食");
        } else {
            order.setAddressBookId(orderSubmitDTO.getAddressId());
            order.setPhone(addressBook.getPhone());
            order.setAddress(addressBook.getDetail());
            order.setConsignee(addressBook.getConsignee());
        }
        // 利用时间戳来生成当前订单的编号
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        order.setStatus(Order.PENDING_PAYMENT); // 刚下单提交，此时是待付款状态
        order.setPayStatus(Order.UN_PAID); // 未支付
        order.setOrderTime(LocalDateTime.now());
        this.order = order;
        // 4、向订单表插入1条数据
        orderMapper.insert(order);
        // 订单明细数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        // 遍历购物车中所有的商品，逐个加到订单明细表
        for (Cart c : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(c, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }
        // 5、向明细表插入n条数据
        orderDetailMapper.insertBatch(orderDetailList);
        // 6、清理购物车中的数据
        cartMapper.delete(userId);
        // 7、封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
        return orderSubmitVO;
    }

    /**
     * 当前用户未支付订单数量
     *
     * @return
     */
    public Integer unPayOrderCount() {
        Integer userId = BaseContext.getCurrentId();
        return orderMapper.getUnPayCount(userId);
    }

    /**
     * 根据id查询订单详情
     *
     * @param id
     * @return
     */
    public OrderVO getById(Integer id) {
        Order order = orderMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 用户端条件分页查询历史订单
     *
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult userPage(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        // 要根据当前用户和状态条件来查询订单，因此设计OrderPageDTO来封装信息
        OrderPageDTO orderPageDTO = new OrderPageDTO();
        orderPageDTO.setUserId(BaseContext.getCurrentId());
        orderPageDTO.setStatus(status);
        Page<Order> orderPage = orderMapper.page(orderPageDTO);
        // 查到所有订单orderPage后，封装成OrderVO列表返回
        List<OrderVO> list = new ArrayList<>();
        // 其实就是将每个订单都加上订单详情OrderDetail
        if (orderPage != null && orderPage.getTotal() > 0) {
            for (Order order : orderPage) {
                Integer orderId = order.getId(); // 订单id
                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getById(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }
        return new PageResult(orderPage.getTotal(), list);
    }

    /**
     * 用户根据订单id取消订单
     *
     * @param id
     */
    public void userCancelById(Integer id) throws Exception {
        // 根据id查询订单
        Order ordersDB = orderMapper.getById(id);
        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消  前两个状态才能直接退款，否则要联系商家
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Order order = new Order();
        order.setId(ordersDB.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Order.TO_BE_CONFIRMED)) {
            // 调用微信支付退款接口（refund有异常需要抛出处理）
            // 用不了
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            // 支付状态修改为 退款
            order.setPayStatus(Order.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        order.setStatus(Order.CANCELLED);
        order.setCancelReason("用户取消");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 根据订单id再来一单
     *
     * @param id
     */
    public void reOrder(Integer id) {
        Integer userId = BaseContext.getCurrentId();
        // 1、先拿到这个订单id的所有菜品
        List<OrderDetail> detailList = orderDetailMapper.getById(id);
        // 2、将订单详情对象转换为购物车对象
//        List<Cart> cartList = new ArrayList<>();
//        for (OrderDetail orderDetail : detailList){
//            Cart cart = new Cart();
//            BeanUtils.copyProperties(orderDetail, cart, "id");
//            cart.setUserId(userId);
//            cart.setCreateTime(LocalDateTime.now());
//            cartList.add(cart);
//        }
        List<Cart> cartList = detailList.stream().map(x -> {
            Cart cart = new Cart();
            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, cart, "id");
            cart.setUserId(userId);
            cart.setCreateTime(LocalDateTime.now());
            return cart;
        }).toList();
        // 3、将购物车对象批量添加到数据库
        cartMapper.insertBatch(cartList);
    }

    /**
     * 用户支付订单
     *
     * @param orderPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrderPaymentDTO orderPaymentDTO) {
        // 按订单号查询订单，避免使用实例变量 this.order（多请求会错乱）
        Order order = orderMapper.getByNumber(orderPaymentDTO.getOrderNumber());
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(BaseContext.getCurrentId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (Order.PAID.equals(order.getPayStatus())) {
            throw new OrderBusinessException("订单已支付");
        }
        // 暂不调微信支付，直接更新为已支付
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        Integer OrderPaidStatus = Order.PAID;
        Integer OrderStatus = Order.TO_BE_CONFIRMED;
        LocalDateTime checkOutTime = LocalDateTime.now();
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, checkOutTime, order.getId());

        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", order.getId());
        map.put("content", "订单号：" + order.getNumber());
        String json = JSON.toJSONString(map);
        log.info("发给商家端啊！：{}", map);
        webSocketServer.sendToAllClient(json);

        return vo;
    }

    /**
     * 条件分页查询订单信息
     *
     * @param orderPageDTO
     * @return
     */
    public PageResult conditionSearch(OrderPageDTO orderPageDTO) {
        PageHelper.startPage(orderPageDTO.getPage(), orderPageDTO.getPageSize());
        Page<Order> orders = orderMapper.page(orderPageDTO);
        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(orders);
        return new PageResult(orders.getTotal(), orderVOList);
    }

    /**
     * 不同状态订单数量统计
     *
     * @return
     */
    public OrderStatisticsVO statistics() {
        // 根据状态，分别查询出待接单、已接单/待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.countByStatus(Order.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countByStatus(Order.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByStatus(Order.DELIVERY_IN_PROGRESS);
        // 封装成VO返回
        return OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmed)
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();
    }

    /**
     * 接单
     *
     * @param orderConfirmDTO
     */
    public void confirm(OrderConfirmDTO orderConfirmDTO) {
        Order order = Order.builder()
                .id(orderConfirmDTO.getId())
                .status(Order.CONFIRMED)
                .build();
        orderMapper.update(order);
    }

    /**
     * 拒单
     *
     * @param orderRejectionDTO
     */
    public void reject(OrderRejectionDTO orderRejectionDTO) {
        Integer orderId = orderRejectionDTO.getId();
        Order orderDB = orderMapper.getById(orderId);
        Order order = new Order();
        // 订单只有存在且状态为2（待接单）才可以拒单
        if (orderDB == null || !orderDB.getStatus().equals(Order.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        }
        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        order.setId(orderDB.getId());
        // 调用微信支付退款接口，但关于微信的接口都用不了，支付状态修改为 退款 就行
        order.setPayStatus(Order.REFUND);
        order.setStatus(Order.CANCELLED);
        order.setRejectionReason(orderRejectionDTO.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 取消订单
     *
     * @param orderCancelDTO
     */
    public void cancel(OrderCancelDTO orderCancelDTO) {
        Integer orderId = orderCancelDTO.getId();
        Order orderDB = orderMapper.getById(orderId);
        Order order = new Order();
        // 取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        order.setId(orderDB.getId());
        // 调用微信支付退款接口，但关于微信的接口都用不了，支付状态修改为 退款 就行
        order.setPayStatus(Order.REFUND);
        order.setStatus(Order.CANCELLED);
        order.setCancelReason(orderCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 根据id派送订单
     *
     * @param id
     */
    public void delivery(Integer id) {
        Order orderDB = orderMapper.getById(id);
        // 订单存在 且 状态为3已接单，才能进行派送操作
        if (orderDB == null || !orderDB.getStatus().equals(Order.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Order order = new Order();
        order.setId(orderDB.getId());
        order.setStatus(Order.DELIVERY_IN_PROGRESS);
        orderMapper.update(order);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Integer id) {
        Order orderDB = orderMapper.getById(id);
        // 订单存在 且 状态为4派送中，才能进行完成操作
        if (orderDB == null || !orderDB.getStatus().equals(Order.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Order order = new Order();
        order.setId(orderDB.getId());
        order.setStatus(Order.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now()); // 设置订单完成时间
        orderMapper.update(order);
    }

    /**
     * 用户催单
     * @param id
     */
    public void reminder(Integer id) {
        Order orderDB = orderMapper.getById(id);
        // 订单不存在
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 通过websocket向客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type", 2); // 消息类型，2表示客户催单（1表示来单提醒）
        map.put("orderId", id);
        map.put("content", "订单号：" + orderDB.getNumber());
        String json = JSON.toJSONString(map);
        log.info("发给商家端啊！：{}", map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 抽出page.getResult()的内容，其中的订单菜品需要有详情信息
     *
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVOList(Page<Order> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();
        List<Order> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Order orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);
                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param order
     * @return
     */
    private String getOrderDishesStr(Order order) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(order.getId());
        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3;）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());
        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /**
     * 检查客户的收货地址是否超出配送范围
     *
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address", shopAddress);
        map.put("output", "json");
        map.put("ak", ak);

        // 获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("店铺地址解析失败");
        }

        // 数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        // 店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address", address);

        // 获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("收货地址解析失败");
        }

        // 数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        // 用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin", shopLngLat);
        map.put("destination", userLngLat);
        map.put("steps_info", "0");

        // 路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("配送路线规划失败");
        }

        // 数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if (distance > 5000) {
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

}
