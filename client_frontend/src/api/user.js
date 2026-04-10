import request from './request'

/**
 * 根据 id 查询用户信息
 */
export function getUserInfoAPI(id) {
  return request({
    method: 'GET',
    url: `/user/user/${id}`,
  })
}

/**
 * 更新用户信息
 */
export function updateUserAPI(data) {
  return request({
    method: 'PUT',
    url: '/user/user',
    data,
  })
}
