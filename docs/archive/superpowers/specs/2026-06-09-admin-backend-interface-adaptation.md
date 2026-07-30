# Admin Backend Interface Adaptation

## Scope

This document records backend interface recommendations for the implemented
admin frontend redesign. No backend code was changed in this frontend rollout.

## Pagination Contract

All admin list pages work best when paginated endpoints return a consistent
shape:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 0,
    "size": 10,
    "current": 1,
    "pages": 0
  }
}
```

Recommended query parameters:

- `pageNum`: frontend current page.
- `pageSize`: frontend page size.
- Text filters such as `username`, `productCode`, `title`, and `url`.
- Numeric enum filters such as `status`, `productType`, `orderStatus`,
  `msgType`, `adminId`, and `roleId`.

Backend pages should normalize `pageNum/pageSize` into MyBatis-Plus `Page`
objects and avoid full-table reads followed by in-memory pagination.

## Status Enum

The frontend now uses readable shared enum labels:

- Generic status: `1 = 正常`, `0 = 禁用`
- Trade type: `1 = 买入`, `2 = 卖出`
- Order status: `0 = 待成交`, `1 = 已成交`, `2 = 已撤销`
- Product type: `1 = 贵金属`, `2 = 理财产品`, `3 = 基金`, `4 = 股票`
- Message type: `1 = 系统通知`, `2 = 交易提醒`, `3 = 风控通知`,
  `4 = 活动通知`
- News type: `1 = 行业动态`, `2 = 市场分析`, `3 = 政策解读`,
  `4 = 公司公告`

Backend DTO/VO names should align with these values to avoid page-local mapping
drift.

## CRUD Contracts

For create/update endpoints, keep the current REST style:

- `POST /resource`
- `PUT /resource/{id}`
- `DELETE /resource/{id}`
- `GET /resource/page`

Recommendations:

- Add Swagger `@Tag` and `@Operation` on all controllers and methods.
- Add `@Valid` to every `@RequestBody` DTO.
- Add bean validation annotations to create/update DTOs.
- Add `@Transactional(rollbackFor = Exception.class)` to write operations.
- Return validation errors in the same `Result` envelope.

## RBAC

The redesigned system pages depend on these protected resources:

- `/system/umsAdmin/page`
- `/system/umsRole/page`
- `/system/umsResource/page`
- `/system/umsAdminRoleRelation/page`
- `/system/umsRoleResourceRelation/page`
- `/system/umsAdmin`
- `/system/umsRole`
- `/system/umsResource`

Do not add admin management endpoints to `AuthConstant.PERMIT_ALL_URLS`.
Instead, seed them as RBAC resources and assign them to administrator roles.

When adding future dashboard aggregation APIs, keep them protected unless the
user explicitly asks for public dashboard data.

## Dashboard API Suggestions

The current frontend can run on existing APIs. Optional backend improvements:

- `GET /system/dashboard/latest-orders`
  - Query: `limit`, optional `orderStatus`
  - Returns compact order rows for the dashboard order panel.
- `GET /system/dashboard/operation-health`
  - Returns stale pending order count, unread risk message count, admins
    without roles, resources without URLs, and permit-all count.
- `GET /system/dashboard/market-snapshot`
  - Returns realtime products with code, name, price, rise/fall, and status.

Implementation guidance:

- Use MyBatis-Plus query wrappers and aggregate queries.
- Avoid loading all rows into Java memory for counts.
- Add MockMvc tests for protected access and success responses.
- Keep `AuthConstant.PERMIT_ALL_URLS` unchanged unless explicitly approved.

## Frontend Adapter Notes

The common `AdminDataTable` assumes:

- `records` is always an array.
- `total` is a number.
- Page changes trigger a fresh query.
- Delete/update failures are surfaced by the global Axios interceptor.

The common `AdminFormDialog` validates before submit. Backend validation should
mirror required fields so users see consistent failures on both sides.

