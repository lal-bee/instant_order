import request from '@/utils/request'

export const getHeadquartersListAPI = () => {
  return request({
    url: '/headquarters/list',
    method: 'get',
  })
}

export const addHeadquartersAPI = (params: any) => {
  return request({
    url: '/headquarters',
    method: 'post',
    data: params,
  })
}

export const updateHeadquartersAPI = (params: any) => {
  return request({
    url: '/headquarters',
    method: 'put',
    data: params,
  })
}

export const updateHeadquartersStatusAPI = (id: number) => {
  return request({
    url: `/headquarters/status/${id}`,
    method: 'put',
  })
}
