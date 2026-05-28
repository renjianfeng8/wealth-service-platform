export function useFormGuard<T extends Record<string, any>>(form: T) {
  let snapshot = JSON.stringify(form)

  function isDirty(): boolean {
    return JSON.stringify(form) !== snapshot
  }

  function reset() {
    snapshot = JSON.stringify(form)
  }

  return { isDirty, reset }
}
