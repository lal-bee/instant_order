import request from '@/utils/request'

export const getStockPageAPI = (params: {
  page: number
  pageSize: number
  storeId?: number
  dishName?: string
  warningOnly?: number
}) => {
  return request({
    url: '/stock/page',
    method: 'get',
    params,
  })
}

export const updateStockAPI = (data: {
  storeId: number
  dishId: number
  stock?: number
  warningStock?: number
}) => {
  return request({
    url: '/stock/update',
    method: 'put',
    data,
  })
}

export const getStockWarningAPI = (params?: {
  storeId?: number
  dishName?: string
}) => {
  return request({
    url: '/stock/warning',
    method: 'get',
    params,
  })
}
