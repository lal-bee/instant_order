import request from '@/utils/request'

export const getUserPageAPI = (params: any) => {
  return request({
    url: '/user/page',
    method: 'get',
    params,
  })
}

export const getUserByIdAPI = (id: number) => {
  return request({
    url: `/user/${id}`,
    method: 'get',
  })
}

export const addUserAPI = (data: any) => {
  return request({
    url: '/user',
    method: 'post',
    data,
  })
}

export const updateUserAPI = (data: any) => {
  return request({
    url: '/user',
    method: 'put',
    data,
  })
}

export const updateUserStatusAPI = (data: any) => {
  return request({
    url: '/user/status',
    method: 'put',
    data,
  })
}

export const deleteUserAPI = (id: number) => {
  return request({
    url: `/user/${id}`,
    method: 'delete',
  })
}

export const openUserMemberAPI = (data: any) => {
  return request({
    url: '/user/member/open',
    method: 'put',
    data,
  })
}

export const updateUserMemberAPI = (data: any) => {
  return request({
    url: '/user/member/update',
    method: 'put',
    data,
  })
}

export const closeUserMemberAPI = (data: any) => {
  return request({
    url: '/user/member/close',
    method: 'put',
    data,
  })
}

