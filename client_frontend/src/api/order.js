import request from './request'

/**
 * 提交订单
 */
export function submitOrderAPI(data) {
  return request({
    method: 'POST',
    url: '/user/order/submit',
    data,
  })
}

/**
 * 支付订单 body: { orderNumber, payMethod }
 */
export function payOrderAPI(data) {
  return request({
    method: 'PUT',
    url: '/user/order/payment',
    data,
  })
}

/**
 * 未支付订单数量
 */
export function getUnPayOrderAPI() {
  return request({
    method: 'GET',
    url: '/user/order/unPayOrderCount',
  })
}

/**
 * 根据订单 id 获取订单详情
 */
export function getOrderAPI(id) {
  return request({
    method: 'GET',
    url: `/user/order/orderDetail/${id}`,
  })
}

/**
 * 历史订单分页 params: { page, pageSize, status? }
 */
export function getOrderPageAPI(params) {
  return request({
    method: 'GET',
    url: '/user/order/historyOrders',
    params,
  })
}

/**
 * 取消订单
 */
export function cancelOrderAPI(id) {
  return request({
    method: 'PUT',
    url: `/user/order/cancel/${id}`,
  })
}

/**
 * 再来一单（批量加入购物车）
 */
export function reOrderAPI(id) {
  return request({
    method: 'POST',
    url: `/user/order/reOrder/${id}`,
  })
}

/**
 * 催单
 */
export function urgeOrderAPI(id) {
  return request({
    method: 'GET',
    url: `/user/order/reminder/${id}`,
  })
}
