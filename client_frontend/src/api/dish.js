import request from './request'

/**
 * 根据分类 id 获取菜品列表
 */
export function getDishListAPI(id, storeId) {
  return request({
    method: 'GET',
    url: `/user/dish/list/${id}`,
    params: { storeId },
  })
}

/**
 * 根据 id 获取菜品详情（含 flavors）
 */
export function getDishByIdAPI(id) {
  return request({
    method: 'GET',
    url: `/user/dish/dish/${id}`,
  })
}
