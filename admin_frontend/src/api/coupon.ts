import request from '@/utils/request'

export const getCouponPageAPI = (params: any) => {
  return request({
    url: '/coupon/page',
    method: 'get',
    params,
  })
}

export const getCouponByIdAPI = (id: number) => {
  return request({
    url: `/coupon/${id}`,
    method: 'get',
  })
}

export const addCouponAPI = (data: any) => {
  return request({
    url: '/coupon',
    method: 'post',
    data,
  })
}

export const updateCouponAPI = (data: any) => {
  return request({
    url: '/coupon',
    method: 'put',
    data,
  })
}

export const updateCouponStatusAPI = (id: number, status: number) => {
  return request({
    url: `/coupon/${id}/status`,
    method: 'put',
    data: { status },
  })
}

export const getCouponRecordsAPI = (id: number, params: any) => {
  return request({
    url: `/coupon/${id}/records`,
    method: 'get',
    params,
  })
}
