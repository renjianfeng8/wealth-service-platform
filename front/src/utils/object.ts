/**
 * B3: 仅从 source 复制 target 已有的属性，排除 createTime/updateTime 等只读字段。
 * 替代 Object.assign(form, row) 的方式，防止只读字段污染 PUT 请求。
 *
 * @param target - 目标对象（如 form reactive），其 keys 定义可编辑字段白名单
 * @param source - 源对象（如 API 返回的 row），含 createTime 等只读字段
 */
export function assignEditable<T extends Record<string, any>>(
  target: T,
  source: Record<string, any>,
): void {
  const targetKeys = new Set(Object.keys(target))
  for (const key of targetKeys) {
    if (key in source) {
      target[key as keyof T] = source[key]
    }
  }
}
