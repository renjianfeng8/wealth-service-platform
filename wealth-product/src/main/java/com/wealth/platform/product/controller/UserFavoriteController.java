package com.wealth.platform.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.product.dto.FinUserFavoriteDTO;
import com.wealth.platform.product.entity.WeaUserFavorite;
import com.wealth.platform.product.service.FinUserFavoriteService;
import com.wealth.platform.product.vo.FinUserFavoriteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户自选关注表控制器（从 wealth-account 迁移合并）
 */
@RestController
@Tag(name = "用户自选管理", description = "wea_user_favorite 用户自选相关接口")
@RequestMapping("/wea-user-favorite")
@RequiredArgsConstructor
@Validated
public class UserFavoriteController {

    private final FinUserFavoriteService finUserFavoriteService;

    @Operation(summary = "根据ID查询用户自选关注信息")
    @GetMapping("/{id}")
    public Result<FinUserFavoriteVO> getById(@PathVariable Long id) {
        FinUserFavoriteVO vo = finUserFavoriteService.getFavoriteById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询用户自选关注列表")
    @GetMapping
    public Result<List<FinUserFavoriteVO>> list(@RequestParam(required = false) Long userId) {
        return Result.success(finUserFavoriteService.getFavoriteList(userId));
    }

    @Operation(summary = "分页查询用户自选关注")
    @GetMapping("/page")
    public Result<IPage<FinUserFavoriteVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<WeaUserFavorite> page = new Page<>(pageNum, pageSize);
        return Result.success(BeanConvertUtil.convertPage(finUserFavoriteService.page(page), FinUserFavoriteVO.class));
    }

    @Operation(summary = "创建用户自选关注")
    @PostMapping
    @AuditLog(module = "自选管理", operation = "添加自选关注")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody FinUserFavoriteDTO dto) {
        boolean success = finUserFavoriteService.createFavorite(dto);
        if (!success) {
            return Result.error(ResultCode.FAIL.getCode(), "已关注该产品，请勿重复添加");
        }
        return Result.success(true);
    }

    @Operation(summary = "更新用户自选关注信息")
    @PutMapping("/{id}")
    @AuditLog(module = "自选管理", operation = "更新自选关注")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody FinUserFavoriteDTO dto) {
        return Result.success(finUserFavoriteService.updateFavorite(id, dto));
    }

    @Operation(summary = "删除用户自选关注（物理删除）")
    @DeleteMapping("/{id}")
    @AuditLog(module = "自选管理", operation = "删除自选关注")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(finUserFavoriteService.deleteFavorite(id));
    }
}
