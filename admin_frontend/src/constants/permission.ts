export enum RoleEnum {
  EMPLOYEE = '0',
  STORE_MANAGER = '1',
  CHAIRMAN = '2',
}

export type RoleLike = RoleEnum | string | number | null | undefined

export const DEFAULT_ROLE: RoleEnum = RoleEnum.EMPLOYEE

export const ROLE_LABEL_MAP: Record<RoleEnum, string> = {
  [RoleEnum.CHAIRMAN]: '董事长',
  [RoleEnum.STORE_MANAGER]: '店长',
  [RoleEnum.EMPLOYEE]: '普通员工',
}

export const ROLE_ORDER_MAP: Record<RoleEnum, number> = {
  [RoleEnum.CHAIRMAN]: 3,
  [RoleEnum.STORE_MANAGER]: 2,
  [RoleEnum.EMPLOYEE]: 1,
}

export const ROLE_ALIAS_MAP: Record<string, RoleEnum> = {
  '2': RoleEnum.CHAIRMAN,

  '1': RoleEnum.STORE_MANAGER,

  '0': RoleEnum.EMPLOYEE,

  // 兼容历史存量值
  CHAIRMAN: RoleEnum.CHAIRMAN,
  BOSS: RoleEnum.CHAIRMAN,
  STORE_MANAGER: RoleEnum.STORE_MANAGER,
  STOREMANAGER: RoleEnum.STORE_MANAGER,
  MANAGER: RoleEnum.STORE_MANAGER,
  EMPLOYEE: RoleEnum.EMPLOYEE,
  STAFF: RoleEnum.EMPLOYEE,
}
