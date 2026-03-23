export type UserInfo = {
  id: number
  account: string
  role: 0 | 1 | 2 | '0' | '1' | '2' | 'CHAIRMAN' | 'MANAGER' | 'EMPLOYEE'
  storeId: number
  token: string
}