# front-user — 已废弃

用户前台已于 2026-05-26 合并到 `front/` 项目。

管理后台和用户前台现在统一由 `front/` 构建和部署，通过 `/admin/` 和 `/user/` 路由前缀区分。

请删除此目录：

```bash
git rm -r front-user
```
