package com.wealth.platform.account.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.account.dto.FinUserFavoriteDTO;
import com.wealth.platform.account.entity.WeaUserFavorite;
import com.wealth.platform.account.service.FinUserFavoriteService;
import com.wealth.platform.account.vo.FinUserFavoriteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * 用户自选关注表控制器。
 */
@RestController
@Tag(name = "用户自选管理", description = "wea_user_favorite 用户自选相关接口")
@RequestMapping("/wea-user-favorite")
@RequiredArgsConstructor
@Validated
public class UserFavoriteController {

    private final FinUserFavoriteService finUserFavoriteService;

    /**
     * 根据 ID 查询用户自选关注信息。
     *
     * @param id 用户自选关注 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询用户自选关注信息")
    @GetMapping("/{id}")
    public Result<FinUserFavoriteVO> getById(@PathVariable Long id) {
        FinUserFavoriteVO vo = finUserFavoriteService.getFavoriteById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    /**
     * 查询用户自选关注列表（不分页）。
     *
     * @param userId 用户ID（为空时返回空列表，避免数据泄露）
     * @return 用户自选关注列表
     */
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

    /**
     * 创建用户自选关注。
     *
     * @param dto 用户自选关注入参
     * @return 是否创建成功
     */
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

    /**
     * 更新用户自选关注信息。
     *
     * @param id  用户自选关注 ID
     * @param dto 用户自选关注入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新用户自选关注信息")
    @PutMapping("/{id}")
    @AuditLog(module = "自选管理", operation = "更新自选关注")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody FinUserFavoriteDTO dto) {
        return Result.success(finUserFavoriteService.updateFavorite(id, dto));
    }

    /**
     * 删除用户自选关注（物理删除）。
     *
     * @param id 用户自选关注 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除用户自选关注（物理删除）")
    @DeleteMapping("/{id}")
    @AuditLog(module = "自选管理", operation = "删除自选关注")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(finUserFavoriteService.deleteFavorite(id));
    }
}
