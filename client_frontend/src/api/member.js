import request from './request'

export function getMemberInfoAPI() {
  return request({
    method: 'GET',
    url: '/user/member/info',
  })
}

export function openMemberByPlanAPI(planType) {
  return request({
    method: 'PUT',
    url: '/user/member/open',
    data: { planType },
  })
}
