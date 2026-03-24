export type UserInfo = {
  id: number
  account: string
  role: 0 | 1 | 2 | '0' | '1' | '2' | 'CHAIRMAN' | 'STORE_MANAGER' | 'MANAGER' | 'EMPLOYEE'
  storeId: number
  token: string
}