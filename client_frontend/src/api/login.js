import request from './request'

/**
 * 用户登录，body: { code }（H5 可用模拟 code，由后端兼容返回 token）
 */
export function loginAPI(code) {
  return request({
    method: 'POST',
    url: '/user/user/login',
    data: { code },
  })
}
