import request from './request'

/**
 * 添加购物车 body: { dishId?, setmealId?, dishFlavor? }
 */
export function addToCartAPI(data) {
  return request({
    method: 'POST',
    url: '/user/cart/add',
    data,
  })
}

/**
 * 购物车数量 -1，body 同上
 */
export function subCartAPI(data) {
  return request({
    method: 'PUT',
    url: '/user/cart/sub',
    data,
  })
}

/**
 * 当前购物车列表
 */
export function getCartAPI() {
  return request({
    method: 'GET',
    url: '/user/cart/list',
  })
}

/**
 * 清空购物车
 */
export function cleanCartAPI() {
  return request({
    method: 'DELETE',
    url: '/user/cart/clean',
  })
}
