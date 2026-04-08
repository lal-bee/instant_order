import request from './request'

export function getTableInfoAPI(tableId) {
  return request({
    method: 'GET',
    url: `/user/table/${tableId}`,
  })
}
