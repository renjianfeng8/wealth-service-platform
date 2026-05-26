import dayjs from 'dayjs'

export function formatDateTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

export function formatDate(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD')
}

export function statusTag(status: number | undefined | null): string {
  return status === 1 ? 'success' : 'danger'
}

export function statusText(status: number | undefined | null): string {
  return status === 1 ? '正常' : '禁用'
}

export function formatPrice(val: number | undefined | null): string {
  if (val === null || val === undefined) return '-'
  return val.toFixed(2)
}

export function formatRate(val: number | undefined | null): string {
  if (val === null || val === undefined) return '-'
  const sign = val >= 0 ? '+' : ''
  return `${sign}${(val * 100).toFixed(2)}%`
}

export function tradeTypeText(type: number | undefined | null): string {
  const map: Record<number, string> = { 1: '买入', 2: '卖出' }
  return type !== null && type !== undefined ? map[type] || '-' : '-'
}

export function orderStatusText(status: number | undefined | null): string {
  const map: Record<number, string> = { 0: '待成交', 1: '已成交', 2: '已撤销' }
  return status !== null && status !== undefined ? map[status] || '-' : '-'
}

export function orderStatusTag(status: number | undefined | null): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'info' }
  return status !== null && status !== undefined ? map[status] || '' : ''
}

export function productTypeText(type: number | undefined | null): string {
  const map: Record<number, string> = { 1: '贵金属', 2: '理财产品', 3: '基金', 4: '股票' }
  return type !== null && type !== undefined ? map[type] || '-' : '-'
}

export function formatRelativeTime(time: string | undefined | null): string {
  if (!time) return '-'
  const now = dayjs()
  const t = dayjs(time)
  const diffMin = now.diff(t, 'minute')
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHour = now.diff(t, 'hour')
  if (diffHour < 24) return `${diffHour}小时前`
  const diffDay = now.diff(t, 'day')
  if (diffDay < 7) return `${diffDay}天前`
  return dayjs(time).format('MM-DD HH:mm')
}

export function newsTypeText(type: number | undefined | null): string {
  const map: Record<number, string> = { 1: '行业动态', 2: '市场分析', 3: '政策解读', 4: '公司公告' }
  return type !== null && type !== undefined ? map[type] || '-' : '-'
}

export function msgTypeText(type: number | undefined | null): string {
  const map: Record<number, string> = { 1: '系统通知', 2: '交易提醒', 3: '风控通知', 4: '活动通知' }
  return type !== null && type !== undefined ? map[type] || '-' : '-'
}
