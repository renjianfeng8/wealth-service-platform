# Admin Operations Console Redesign

## Scope

This design targets the admin-side operations console for `wealth-service-platform`.
The selected direction is A: rebuild the admin dashboard as an operations-first surface,
then extract reusable admin page primitives for CRUD-heavy pages.

The visual prototype is available in the Superpowers companion at:

`http://localhost:49919`

The source HTML prototype is:

`.superpowers/brainstorm/stable-20260607132407/content/admin-console-v1.html`

Figma write tools were not exposed in this Codex session, so this document and the
HTML prototype are the Figma-ready blueprint. When `use_figma` is available, create
one desktop frame named `Admin Operations Console / v1` and map the sections below
to design-system components.

## Findings

Frontend findings:

- `front/src/views/admin/dashboard/index.vue` is 813 lines and combines data loading,
  chart lifecycle, metric cards, product cards, market list, K-line controls, and page
  styling in one component.
- Admin pages repeat a common pattern: search/filter controls, `el-table`, pagination,
  row actions, and `el-dialog` forms.
- `front/src/layouts/Navbar.vue` and `front/src/layouts/UserLayout.vue` duplicate the
  unread-message popover, message detail dialog, read logic, and type formatting.
- API modules still use loose `any` in several write/read functions, which weakens the
  frontend-to-backend contract for generated admin forms.

Backend findings:

- `DashboardController` exposes three dashboard endpoints:
  `/system/dashboard/overview`, `/system/dashboard/trend`, and
  `/system/dashboard/kline/{productCode}`.
- `DashboardServiceImpl#getKline` currently sets product name to product code, so the
  dashboard cannot show a reliable display name without frontend fallback logic.
- Permission checks are resource-URL based and cached for 5 minutes in
  `PermissionInterceptor`, so new admin dashboard endpoints must be registered in RBAC
  resources unless intentionally placed in `AuthConstant.PERMIT_ALL_URLS`.
- Current public whitelist includes product page, market list, news page, login,
  register, identify login, SSE, actuator, and Swagger/Knife4j paths. Admin dashboard
  optimization endpoints should remain protected by default.

## Prototype Structure

The optimized admin console has these sections:

1. Sidebar navigation grouped by operations, business, and governance.
2. Topbar with global search, unread-alert count, and admin account entry.
3. KPI strip with total asset estimate, completed order amount, pending orders, and
   unread/risk messages.
4. Trend panel combining asset and income trend controls.
5. Latest orders table with status emphasis and quick entry to trade management.
6. Action queue for risk, RBAC, product, and content tasks.
7. Realtime market panel with rise/fall segmentation.
8. Permission health panel surfacing admins without roles, resources without URLs, and
   permit-all endpoint count.

## Vue Refactor Design

Create focused admin dashboard components under `front/src/views/admin/dashboard/components/`:

- `DashboardMetricGrid.vue`: renders metric cards from a typed array.
- `TrendPanel.vue`: owns ECharts setup, resize, disposal, and period switching.
- `MarketSnapshot.vue`: renders realtime products and rise/fall filters.
- `LatestOrdersPanel.vue`: renders recent trade orders and status tags.
- `ActionQueuePanel.vue`: displays operational tasks from dashboard health data.
- `PermissionHealthPanel.vue`: displays RBAC health metrics.

Create shared admin primitives under `front/src/components/admin/`:

- `AdminPageShell.vue`: standard title, description, toolbar, and content spacing.
- `AdminFilterBar.vue`: schema-driven filter form with reset/search events.
- `AdminDataTable.vue`: table wrapper for loading, empty state, pagination, and slots.
- `AdminFormDialog.vue`: create/update dialog wrapper with validation and submit state.
- `MessageNoticePopover.vue`: extracted from `Navbar.vue` and `UserLayout.vue`.

Move dashboard data orchestration into `front/src/composables/useAdminDashboard.ts`:

- `loadOverview`
- `loadTrend`
- `loadKline`
- `loadMarketSnapshot`
- `loadLatestOrders`
- `loadPermissionHealth`

The page component should become a thin composition shell that wires the sections.

## API Adaptation

Keep existing dashboard endpoints and add protected endpoints only when the UI needs
data that is currently inferred or unavailable:

- `GET /system/dashboard/market-snapshot`
  - Returns product code, product name, latest price, rise/fall, and status for the
    realtime market panel.
- `GET /system/dashboard/latest-orders`
  - Query params: `limit`, optional `orderStatus`.
  - Returns order number, product code/name, trade type, amount, status, create time.
- `GET /system/dashboard/operation-health`
  - Returns counts for stale pending orders, unread risk messages, resources without
    URL, admins without roles, and permit-all endpoint count.

Backend implementation rules:

- Add Swagger `@Operation` annotations for all new endpoints.
- Do not add these admin endpoints to `AuthConstant.PERMIT_ALL_URLS` unless the user
  explicitly approves a public dashboard requirement.
- Use MyBatis-Plus service/mapper patterns and avoid full-table `list()` reads.
- For any new DTO accepted by `@RequestBody`, add `@Valid`.
- Any write operation must use `@Transactional(rollbackFor = Exception.class)`.

## Rollout Plan

1. Extract `MessageNoticePopover.vue` first because it removes duplicated layout logic
   without changing dashboard behavior.
2. Split `admin/dashboard/index.vue` into read-only presentation components while
   keeping the existing three dashboard endpoints.
3. Add `useAdminDashboard.ts` and typed API response interfaces.
4. Add the three optional dashboard aggregation endpoints if the UI needs true backend
   data instead of frontend placeholders.
5. Apply `AdminPageShell`, `AdminFilterBar`, `AdminDataTable`, and `AdminFormDialog`
   to `admin/product`, `admin/trade`, then system RBAC pages.

## Validation

Frontend:

- `npm run build`
- Playwright smoke for `/admin/dashboard` after login.
- Visual check at desktop and mobile widths for dashboard layout, chart sizing, and
  table overflow.

Backend:

- `mvn test -pl wealth-service -DskipTests=false`
- MockMvc tests for any new dashboard endpoints.
- RBAC verification that protected dashboard endpoints return 401/403 without valid
  admin credentials and 200 for an authorized admin.

