# Admin Figma Prototype

## Status

Figma write tools were not exposed in this Codex session. This file is the
Figma-ready prototype specification for the implemented admin redesign. When
the Figma connector exposes `create_new_file` and `use_figma`, create a file
named `wealth-service-platform / Admin Operations Prototype` and reproduce the
frames below.

## Frames

Create these desktop frames at `1440 x 960`:

- `01 / Dashboard`: operations console with metric grid, trend chart, K-line
  chart, latest orders, action queue, quick entries, mini product strip, and
  market snapshot.
- `02 / CRUD Page Template`: generic `AdminPageShell` layout with title,
  description, filter bar, table toolbar, data table, pagination, and form
  dialog state.
- `03 / Product Management`: product-specific CRUD page with product name,
  product code, type, price, rise/fall, status, and create time columns.
- `04 / Trade Management`: trade order CRUD page with order number, user ID,
  product code, trade direction, entrust price, quantity, status, and create
  time columns.
- `05 / Governance Pages`: system management pattern for admin, role,
  resource, admin-role relation, and role-resource relation pages.

## Design Tokens

Use a quiet operations palette:

- Page background: `#f5f7fb`
- Surface background: `#ffffff`
- Border: `#e5e7eb`
- Primary action: Element Plus primary blue
- Success: `#16a34a`
- Warning: `#d97706`
- Danger: `#dc2626`
- Text primary: `#1f2937`
- Text secondary: `#64748b`

Radii:

- Page primitives and table containers: `8px`
- Tags and small status chips: Element Plus defaults

Spacing:

- Page vertical gap: `16px`
- Card/table inner padding: `16px`
- Form item horizontal rhythm: Element Plus inline form defaults

## Dashboard

The Dashboard frame represents `/admin/dashboard`.

Sections:

1. Header: `运营控制台`, refresh action, and operations status summary.
2. Metric grid: total products, active users, order amount, pending orders, and
   unread messages.
3. Trend panel: asset and balance trend chart.
4. K-line panel: selected product, period segmented control, candlestick canvas.
5. Latest orders: compact table linked to `/admin/trade`.
6. Action queue: pending operational tasks with severity.
7. Quick entries: shortcuts to product, trade, market, news, message, and RBAC
   pages.
8. Market snapshot: realtime price list with rise/fall emphasis.

## CRUD Pages

All CRUD Pages use this common hierarchy:

```text
AdminPageShell
  Header
    Title
    Description
    Optional toolbar
  AdminFilterBar
    Schema-driven fields
    Query button
    Reset button
  AdminDataTable
    Toolbar slot
    Element Plus table columns
    Pagination
  AdminFormDialog
    Validated Element Plus form
    Cancel / Save footer
```

Implemented pages:

- User management
- Product management
- Market data
- Trade order management
- Favorite management
- Message management
- News management
- Product search
- Admin management
- Role management
- Resource management
- Admin-role relation
- Role-resource relation

## Components To Map

Map these Vue components to Figma components:

- `AdminPageShell`: page header, description, toolbar slot, content stack.
- `AdminFilterBar`: input, select, number input, search/reset actions.
- `AdminDataTable`: toolbar, table, empty state, loading state, pagination.
- `AdminFormDialog`: modal shell, validated form, loading save button.
- `MessageNoticePopover`: bell trigger, unread badge, message list, detail
  dialog.

## Interaction Notes

- Filter submit resets `pageNum` to `1`.
- Pagination is owned by `AdminDataTable` and emits `page-change`.
- Dialog save validates before emitting `submit`.
- Dirty form close uses the page's existing `useFormGuard` confirmation.
- Relation pages use filter select options loaded from admin/role/resource list
  APIs.

## Source References

- Prototype HTML blueprint:
  `.superpowers/brainstorm/stable-20260607132407/content/admin-console-v1.html`
- Current screenshots:
  `output/playwright/admin-dashboard-kline-panel.png`
  `output/playwright/admin-product-primitives.png`
  `output/playwright/admin-system-role-resource-primitives.png`

