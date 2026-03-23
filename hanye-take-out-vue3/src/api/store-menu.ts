import request from '@/utils/request'

export const getStoreMenuDishListAPI = (storeId: number) => {
  return request({
    url: '/store-menu/dishes',
    method: 'get',
    params: { storeId },
  })
}

export const saveStoreMenuConfigAPI = (params: { storeId: number; dishIds: number[] }) => {
  return request({
    url: '/store-menu/config',
    method: 'put',
    data: params,
  })
}
