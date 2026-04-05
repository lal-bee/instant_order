import request from '@/utils/request'

export const getStoreMenuDishListAPI = (storeId: number) => {
  return request({
    url: '/store-menu/dishes',
    method: 'get',
    params: { storeId },
  })
}

export const getStoreMenuCategoryListAPI = () => {
  return request({
    url: '/store-menu/categories',
    method: 'get',
  })
}

export const saveStoreMenuConfigAPI = (params: { storeId: number; dishIds: number[] }) => {
  return request({
    url: '/store-menu/config',
    method: 'put',
    data: params,
  })
}

export const addStoreSpecialDishAPI = (params: {
  storeId: number
  name: string
  pic: string
  detail: string
  price: number
  categoryId: number
}) => {
  return request({
    url: '/store-menu/special-dish',
    method: 'post',
    data: params,
  })
}

export const updateStoreSpecialDishAPI = (
  dishId: number,
  params: {
    storeId: number
    name: string
    pic: string
    detail: string
    price: number
    categoryId: number
  },
) => {
  return request({
    url: `/store-menu/special-dish/${dishId}`,
    method: 'put',
    data: params,
  })
}

export const deleteStoreSpecialDishAPI = (dishId: number) => {
  return request({
    url: `/store-menu/special-dish/${dishId}`,
    method: 'delete',
  })
}
