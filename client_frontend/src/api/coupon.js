import request from './request'

export function getReceiveCouponListAPI(storeId) {
  return request({
    method: 'GET',
    url: '/user/coupon/receive-list',
    params: { storeId },
  })
}

export function receiveCouponAPI(couponId) {
  return request({
    method: 'POST',
    url: `/user/coupon/${couponId}/receive`,
  })
}

export function getMyCouponAPI(statusType) {
  return request({
    method: 'GET',
    url: '/user/coupon/my',
    params: { statusType },
  })
}

export function getOrderAvailableCouponAPI(params) {
  return request({
    method: 'GET',
    url: '/user/coupon/order-available',
    params,
  })
}
