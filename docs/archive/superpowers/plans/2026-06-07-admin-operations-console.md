# Admin Operations Console Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the admin operations experience by extracting shared notification and admin page primitives, then splitting the large admin dashboard into focused components.

**Architecture:** Implement the redesign incrementally. First remove duplicated layout behavior with a shared message popover, then move dashboard orchestration into a composable and presentation into small components, then add optional protected backend aggregation endpoints only when the UI needs data not already available.

**Tech Stack:** Vue 3, TypeScript, Element Plus, Pinia, ECharts, Spring Boot 3, MyBatis-Plus.

---

## File Structure

- Create `front/src/components/MessageNoticePopover.vue`: shared unread-message popover and detail dialog.
- Modify `front/src/layouts/Navbar.vue`: replace duplicated notice logic with `MessageNoticePopover`.
- Modify `front/src/layouts/UserLayout.vue`: replace duplicated notice logic with `MessageNoticePopover`.
- Create `front/tests/message-notice-structure.test.mjs`: static structure test proving the extraction is wired into both layouts.
- Later create `front/src/views/admin/dashboard/components/*.vue`: dashboard presentation components.
- Later create `front/src/composables/useAdminDashboard.ts`: dashboard data orchestration.
- Later modify `front/src/api/dashboard.ts`: add typed client functions for optional aggregation endpoints.
- Later modify `wealth-service/src/main/java/com/wealth/platform/system/controller/DashboardController.java`: add protected read endpoints if needed.

## Task 1: Extract Shared Message Notice Popover

**Files:**
- Create: `front/src/components/MessageNoticePopover.vue`
- Create: `front/tests/message-notice-structure.test.mjs`
- Modify: `front/src/layouts/Navbar.vue`
- Modify: `front/src/layouts/UserLayout.vue`

- [ ] **Step 1: Write the failing static structure test**

Create `front/tests/message-notice-structure.test.mjs` with assertions that:

```js
import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const componentPath = join(root, 'src/components/MessageNoticePopover.vue');
const navbarPath = join(root, 'src/layouts/Navbar.vue');
const userLayoutPath = join(root, 'src/layouts/UserLayout.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(componentPath), 'MessageNoticePopover.vue should exist');

const component = readFileSync(componentPath, 'utf8');
const navbar = readFileSync(navbarPath, 'utf8');
const userLayout = readFileSync(userLayoutPath, 'utf8');

assert(component.includes('defineProps'), 'MessageNoticePopover should expose props');
assert(component.includes('targetPath'), 'MessageNoticePopover should accept targetPath');
assert(component.includes('getMessagePage'), 'MessageNoticePopover should load unread messages');
assert(component.includes('readMessage'), 'MessageNoticePopover should mark messages read');
assert(navbar.includes('<MessageNoticePopover target-path="/admin/message" />'), 'Navbar should render shared message popover');
assert(userLayout.includes('<MessageNoticePopover target-path="/user/message" />'), 'UserLayout should render shared message popover');
assert(!navbar.includes('getMessagePage, readMessage'), 'Navbar should not own message API calls');
assert(!userLayout.includes('getMessagePage, readMessage'), 'UserLayout should not own message API calls');

console.log('Message notice structure checks passed');
```

- [ ] **Step 2: Run the test and verify it fails**

Run:

```bash
cd front
node tests/message-notice-structure.test.mjs
```

Expected: fails because `MessageNoticePopover.vue` does not exist yet.

- [ ] **Step 3: Create `MessageNoticePopover.vue`**

Move unread count, popover, message detail dialog, `getMessagePage`, `readMessage`,
`markAllRead`, and message type/time formatting into the shared component. The component
accepts:

```ts
const props = defineProps<{
  targetPath: string
}>()
```

- [ ] **Step 4: Replace Navbar duplicated logic**

In `Navbar.vue`, import `MessageNoticePopover` and replace the old popover/dialog block
with:

```vue
<MessageNoticePopover target-path="/admin/message" />
```

Remove message-specific refs, functions, imports, and global notice styles from the layout.

- [ ] **Step 5: Replace UserLayout duplicated logic**

In `UserLayout.vue`, import `MessageNoticePopover` and replace the old popover/dialog block
with:

```vue
<MessageNoticePopover target-path="/user/message" />
```

Remove message-specific refs, functions, imports, and global notice styles from the layout.

- [ ] **Step 6: Verify the refactor**

Run:

```bash
cd front
node tests/message-notice-structure.test.mjs
npm run build
```

Expected: structure test passes and build exits 0.

## Task 2: Split Dashboard Data Orchestration

**Files:**
- Create: `front/src/composables/useAdminDashboard.ts`
- Modify: `front/src/views/admin/dashboard/index.vue`

- [ ] Move `loadOverview`, `loadTrend`, `loadKline`, and `loadProducts` into the composable.
- [ ] Keep existing endpoints unchanged.
- [ ] Run `npm run build`.

## Task 3: Extract Dashboard Presentation Components

**Files:**
- Create: `front/src/views/admin/dashboard/components/DashboardMetricGrid.vue`
- Create: `front/src/views/admin/dashboard/components/TrendPanel.vue`
- Create: `front/src/views/admin/dashboard/components/MarketSnapshot.vue`
- Create: `front/src/views/admin/dashboard/components/LatestOrdersPanel.vue`
- Modify: `front/src/views/admin/dashboard/index.vue`

- [ ] Extract one component at a time.
- [ ] Run `npm run build` after each extraction.
- [ ] Keep the dashboard route and public API behavior unchanged.

## Task 4: Optional Backend Aggregation Endpoints

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/controller/DashboardController.java`
- Modify/Create service and VO classes under `wealth-service/src/main/java/com/wealth/platform/system/`
- Modify: `front/src/api/dashboard.ts`

- [ ] Add only protected `GET` endpoints.
- [ ] Do not add them to `AuthConstant.PERMIT_ALL_URLS`.
- [ ] Add Swagger `@Operation`.
- [ ] Use MyBatis-Plus and aggregate queries instead of full-table reads.
- [ ] Run `mvn test -pl wealth-service -DskipTests=false`.

## Self-Review

- Spec coverage: Task 1 covers duplicated notification logic; Tasks 2 and 3 cover dashboard decomposition; Task 4 covers optional backend interface adaptation.
- Placeholder scan: no `TBD` or undefined implementation steps.
- Type consistency: `MessageNoticePopover` uses `targetPath`, matching both layout call sites.

