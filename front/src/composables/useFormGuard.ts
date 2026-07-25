export function useFormGuard<T extends Record<string, any>>(form: T) {
  const initial = JSON.parse(JSON.stringify(form)) as T
  let snapshot = JSON.stringify(form)

  function isDirty(): boolean {
    return JSON.stringify(form) !== snapshot
  }

  function reset() {
    snapshot = JSON.stringify(form)
  }

  function resetToInitial() {
    Object.assign(form, initial)
    snapshot = JSON.stringify(form)
  }

  return { isDirty, reset, resetToInitial }
}
