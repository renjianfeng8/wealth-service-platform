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

export function formatNumber(val: number | null | undefined): string {
  if (val === null || val === undefined || Number.isNaN(val)) return '-'
  const abs = Math.abs(val)
  if (abs >= 1e8) return `${(val / 1e8).toFixed(2)}亿`
  if (abs >= 1e4) return `${(val / 1e4).toFixed(2)}万`
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
  const map: Record<number, string> = { 1: '已提交', 2: '已成交', 3: '已撤销' }
  return status !== null && status !== undefined ? map[status] || '-' : '-'
}

export function orderStatusTag(status: number | undefined | null): string {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: 'info' }
  return status !== null && status !== undefined ? map[status] || '' : ''
}

export function productTypeText(type: number | undefined | null): string {
  const map: Record<number, string> = { 1: '黄金', 2: '白银', 3: '理财' }
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
  const map: Record<number, string> = { 1: '行情快讯', 2: '行业公告', 3: '理财资讯' }
  return type !== null && type !== undefined ? map[type] || '-' : '-'
}

export function msgTypeText(type: number | undefined | null): string {
  const map: Record<number, string> = { 1: '行情提醒', 2: '资讯推送', 3: '委托通知', 4: '活动通知' }
  return type !== null && type !== undefined ? map[type] || '-' : '-'
}
