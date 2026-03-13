import request from './request'

/**
 * 地址列表
 */
export function getAddressListAPI() {
  return request({
    method: 'GET',
    url: '/user/address/list',
  })
}

/**
 * 默认地址
 */
export function getDefaultAddressAPI() {
  return request({
    method: 'GET',
    url: '/user/address/default',
  })
}

/**
 * 根据 id 查询地址
 */
export function getAddressByIdAPI(id) {
  return request({
    method: 'GET',
    url: `/user/address/${id}`,
  })
}

/**
 * 新增地址
 */
export function addAddressAPI(data) {
  return request({
    method: 'POST',
    url: '/user/address',
    data,
  })
}

/**
 * 修改地址
 */
export function updateAddressAPI(data) {
  return request({
    method: 'PUT',
    url: '/user/address',
    data,
  })
}

/**
 * 设置默认地址 body: { id }
 */
export function updateDefaultAddressAPI(data) {
  return request({
    method: 'PUT',
    url: '/user/address/default',
    data,
  })
}

/**
 * 删除地址
 */
export function deleteAddressAPI(id) {
  return request({
    method: 'DELETE',
    url: `/user/address/${id}`,
  })
}
