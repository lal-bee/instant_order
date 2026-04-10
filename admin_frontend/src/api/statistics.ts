import request from '@/utils/request' // 引入自定义的axios函数

/**
 * chart数据
 */

// 营业额统计
export const getTurnoverStatisticsAPI = (params: any) => {
  return request({
    url: `/report/turnoverStatistics`,
    method: 'get',
    params
  })
}

// 用户统计
export const getUserStatisticsAPI = (params: any) => {
  return request({
    url: `/report/userStatistics`,
    method: 'get',
    params
  })
}

// 订单统计
export const getOrderStatisticsAPI = (params: any) => {
  return request({
    url: `/report/orderStatistics`,
    method: 'get',
    params
  })
}

// 销量排名TOP10
export const getTop10StatisticsAPI = (params: any) => {
  return request({
    url: `/report/top10Statistics`,
    method: 'get',
    params
  })
}

// 导出
export const exportInforAPI = () => {
  return request({
    url: '/report/export',
    method: 'get',
    responseType: "blob" // 告诉浏览器预期响应的数据类型为二进制数据，而不是默认的 JSON 或纯文本
    // blob 表示二进制大对象，可以是图像、视频、音频、PDF 文件等
  })
}
