// front/src/composables/useFormGuard.ts
import { reactive, toRefs } from 'vue'

export function useFormGuard(form: Record<string, any>) {
  const state = reactive({
    snapshot: JSON.stringify(form),
    clean: true,
  })

  function isDirty(): boolean {
    if (!state.clean) return false
    return JSON.stringify(form) !== state.snapshot
  }

  function reset() {
    state.snapshot = JSON.stringify(form)
  }

  function markClean() {
    state.clean = false
  }

  return { isDirty, reset, markClean }
}
