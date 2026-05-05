export interface Result<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface PageParam {
  pageNum?: number
  pageSize?: number
}

export interface LoginForm {
  username: string
  password: string
}

export interface DictItem {
  label: string
  value: any
}

export const STATUS_OPTIONS: DictItem[] = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 },
]

export const TRADE_TYPE_OPTIONS: DictItem[] = [
  { label: '买入', value: 1 },
  { label: '卖出', value: 2 },
]

export const ORDER_STATUS_OPTIONS: DictItem[] = [
  { label: '待成交', value: 0 },
  { label: '已成交', value: 1 },
  { label: '已撤销', value: 2 },
]

export const MSG_TYPE_OPTIONS: DictItem[] = [
  { label: '系统通知', value: 1 },
  { label: '交易提醒', value: 2 },
  { label: '风控通知', value: 3 },
  { label: '活动通知', value: 4 },
]
