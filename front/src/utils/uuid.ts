/**
 * 生成 UUID。crypto.randomUUID 仅在安全上下文（HTTPS/localhost）可用，
 * 非安全上下文（生产 http）降级为时间戳+随机串，避免全站写请求在拦截器抛错。
 */
export function randomUUID(): string {
  return typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID()
    : `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}-${Math.random().toString(36).slice(2)}`
}
