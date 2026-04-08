import request from './request'

/**
 * 根据套餐分类 id 获取套餐列表
 */
export function getSetmealListAPI(id, storeId) {
  return request({
    method: 'GET',
    url: `/user/setmeal/list/${id}`,
    params: { storeId },
  })
}

/**
 * 获取套餐详情（含 setmealDishes）
 */
export function getSetmealAPI(id) {
  return request({
    method: 'GET',
    url: `/user/setmeal/${id}`,
  })
}
