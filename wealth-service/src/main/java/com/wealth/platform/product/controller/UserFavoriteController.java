package com.wealth.platform.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.product.dto.UserFavoriteDTO;
import com.wealth.platform.product.entity.WeaUserFavorite;
import com.wealth.platform.product.service.UserFavoriteService;
import com.wealth.platform.product.vo.UserFavoriteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户自选关注表控制器（从 wealth-account 迁移合并）
 */
@RestController
@Tag(name = "用户自选管理", description = "wea_user_favorite 用户自选相关接口")
@RequestMapping("/product/wea-user-favorite")
@RequiredArgsConstructor
@Validated
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @Operation(summary = "根据ID查询用户自选关注信息")
    @GetMapping("/{id}")
    public Result<UserFavoriteVO> getById(@PathVariable Long id) {
        UserFavoriteVO vo = userFavoriteService.getFavoriteById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询用户自选关注列表")
    @GetMapping
    public Result<List<UserFavoriteVO>> list(
            @RequestParam(required = false) Long userId,
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userFavoriteService.getFavoriteList(userId, pageNum, pageSize));
    }

    @Operation(summary = "分页查询用户自选关注")
    @GetMapping("/page")
    public Result<IPage<UserFavoriteVO>> page(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String productCode) {
        IPage<WeaUserFavorite> page = userFavoriteService.pageWithFilter(pageNum, pageSize, userId, productCode);
        return Result.success(BeanConvertUtil.convertPage(page, UserFavoriteVO.class));
    }

    @Operation(summary = "创建用户自选关注")
    @PostMapping
    @AuditLog(module = "自选管理", operation = "添加自选关注")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody UserFavoriteDTO dto) {
        boolean success = userFavoriteService.createFavorite(dto);
        if (!success) {
            return Result.error(ResultCode.FAIL.getCode(), "已关注该产品，请勿重复添加");
        }
        return Result.success(true);
    }

    @Operation(summary = "更新用户自选关注信息")
    @PutMapping("/{id}")
    @AuditLog(module = "自选管理", operation = "更新自选关注")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UserFavoriteDTO dto) {
        return Result.success(userFavoriteService.updateFavorite(id, dto));
    }

    @Operation(summary = "删除用户自选关注（物理删除）")
    @DeleteMapping("/{id}")
    @AuditLog(module = "自选管理", operation = "删除自选关注")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userFavoriteService.deleteFavorite(id));
    }
}
