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

export interface UserInfo {
  id?: number
  username: string
  nickname?: string
  phone?: string
  avatar?: string
  status?: number
}

export interface UmsAdmin {
  id?: number
  username: string
  password?: string
  email?: string
  nickName?: string
  status?: number
  avatar?: string
}

export interface UmsRole {
  id?: number
  name: string
  description?: string
  status?: number
  sort?: number
}

export interface UmsResource {
  id?: number
  name: string
  url: string
  description?: string
  categoryId?: number
}

export interface WeaProduct {
  id?: number
  productName: string
  productCode: string
  productType?: number
  price: number
  riseFall?: number
  riseFallRate?: number
  status?: number
  sort?: number
  createTime?: string
}

export interface WeaMarketData {
  id?: number
  productCode: string
  currentPrice: number
  openPrice?: number
  closePrice?: number
  highestPrice?: number
  lowestPrice?: number
  riseFall?: number
  riseFallRate?: number
  marketTime?: string
}

export interface WeaTradeOrder {
  id?: number
  orderNo?: string
  userId: number
  productCode: string
  tradeType: number
  entrustPrice: number
  entrustNum: number
  orderStatus?: number
  createTime?: string
}

export interface WeaUserFavorite {
  id?: number
  userId: number
  productCode: string
  createTime?: string
}

export interface WeaNews {
  id?: number
  title: string
  content?: string
  newsType?: number
  source?: string
  status?: number
  publishTime?: string
}

export interface WeaMessage {
  id?: number
  userId: number
  msgType?: number
  msgTitle: string
  msgContent: string
  readFlag?: number
  createTime?: string
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
  { label: '行情提醒', value: 1 },
  { label: '资讯推送', value: 2 },
  { label: '委托通知', value: 3 },
  { label: '活动通知', value: 4 },
]

export const PRODUCT_TYPE_OPTIONS: DictItem[] = [
  { label: '黄金', value: 1 },
  { label: '白银', value: 2 },
  { label: '理财', value: 3 },
]

export const NEWS_TYPE_OPTIONS: DictItem[] = [
  { label: '行情快讯', value: 1 },
  { label: '行业公告', value: 2 },
  { label: '理财资讯', value: 3 },
]
