package fun.cyhgraph.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 堂食订单状态：
     * 1 待付款
     * 2 已支付(待制作)
     * 3 制作中
     * 4 待取餐
     * 5 已完成
     * 6 已取消
     * 7 已退款
     */
    public static final Integer PENDING_PAYMENT = 1;
    public static final Integer TO_BE_PREPARED = 2;
    public static final Integer PREPARING = 3;
    public static final Integer READY_FOR_PICKUP = 4;
    public static final Integer COMPLETED = 5;
    public static final Integer CANCELLED = 6;
    public static final Integer REFUNDED = 7;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;

    private Integer id;
    private String number;  // 订单号
    private Integer status; // 订单状态 1待付款 2已支付(待制作) 3制作中 4待取餐 5已完成 6已取消 7已退款
    private Integer userId; // 下单用户id
    private LocalDateTime orderTime; // 下单时间
    private LocalDateTime checkoutTime; // 结账时间
    private Integer payMethod; // 支付方式 1微信，2支付宝
    private Integer payStatus; // 支付状态 0未支付 1已支付 2退款
    private BigDecimal amount; // 实收金额
    private BigDecimal originAmount; // 原始金额（优惠前）
    private BigDecimal couponAmount; // 优惠金额
    private Integer couponId; // 使用的优惠券模板id
    private Integer userCouponId; // 使用的用户券id
    private String remark; // 备注
    private String userName; // 用户名
    private String phone; // 手机号
    private String address; // 就餐信息（兼容历史字段名）
    private String consignee; // 用户昵称（兼容历史字段名）
    private String cancelReason; // 订单取消原因
    private String rejectionReason; // 订单拒绝原因
    private LocalDateTime cancelTime; // 订单取消时间
    private int tablewareNumber; // 餐具数量
    private Integer tablewareStatus; // 餐具数量状态  1按餐量提供  0选择具体数量
    private Long storeId; // 门店id
    private Long tableId; // 餐桌id
    private String tableNo; // 桌号
    private String storeName; // 门店名称（查询扩展字段）
}
