import request from './request'

/**
 * 用户登录
 */
export function loginAPI(data) {
  return request({
    method: 'POST',
    url: '/user/user/login',
    data,
  })
}

/**
 * 用户注册
 */
export function registerAPI(data) {
  return request({
    method: 'POST',
    url: '/user/user/register',
    data,
  })
}
