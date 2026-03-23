import request from '@/utils/request'

export const getStoreListAPI = () => {
  return request({
    url: '/store/list',
    method: 'get',
  })
}

export const getStoreByIdAPI = (id: number) => {
  return request({
    url: `/store/${id}`,
    method: 'get',
  })
}

export const addStoreAPI = (params: any) => {
  return request({
    url: '/store',
    method: 'post',
    data: params,
  })
}

export const updateStoreAPI = (params: any) => {
  return request({
    url: '/store',
    method: 'put',
    data: params,
  })
}

export const updateStoreStatusAPI = (id: number) => {
  return request({
    url: `/store/status/${id}`,
    method: 'put',
  })
}
