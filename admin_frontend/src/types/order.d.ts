// 提交订单返回的信息
export type OrderSubmitVO = Partial<{
  id: number // 订单ID
  orderAmount: number // 订单金额
  orderNumber: string // 订单编号
  orderTime: Date // 下单时间
}>

// 订单信息
export type Order = {
  id: number // 订单id
  number: string // 订单号
  status: number // 订单状态 1待付款 2已支付(待制作) 3制作中 4待取餐 5已完成 6已取消
  userId: number // 下单用户id
  orderTime: Date // 下单时间
  checkoutTime: Date // 结账时间
  payMethod: number // 支付方式 1微信，2支付宝
  payStatus: number // 支付状态 0未支付 1已支付 2退款
  amount: number // 实收金额
  remark: string // 备注
  userName: string // 用户名
  phone: string // 手机号
  address: string // 就餐信息
  consignee: string // 用户昵称
  cancelReason: string // 订单取消原因
  rejectionReason: string // 订单拒绝原因
  cancelTime: Date // 订单取消时间
  tablewareNumber: number // 餐具数量
  tablewareStatus: number // 餐具数量状态  1按餐量提供  0选择具体数量
  storeId: number // 门店id
  storeName: string // 门店名称
  tableId: number // 餐桌id
  tableNo: string // 桌号
}

// 订单详细菜品信息
export type OrderDetail = Partial<{
  id: number
  name: string // 名称
  orderId: number // 订单id
  dishId: number // 菜品id
  setmealId: number // 套餐id
  dishFlavor: string // 口味
  number: number // 数量
  amount: number // 金额
  pic: string // 图片
}>

// 订单所有信息
export type OrderVO = Order & {
  orderDetailList: OrderDetail[]
}

// 分页接口
export type PageVO<T> = {
  total: number
  records: T[]
}
