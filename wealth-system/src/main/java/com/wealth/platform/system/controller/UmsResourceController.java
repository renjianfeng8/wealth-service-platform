package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.system.dto.UmsResourceDTO;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.service.UmsResourceService;
import com.wealth.platform.system.vo.UmsResourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 鍚庡彴璧勬簮琛ㄦ帶鍒跺櫒銆?
 */
@RestController
@Tag(name = "璧勬簮绠＄悊", description = "ums_resource 鍚庡彴璧勬簮鐩稿叧鎺ュ彛")
@RequestMapping("/umsResource")
public class UmsResourceController {

    private final UmsResourceService umsResourceService;

    /**
     * 鍚庡彴璧勬簮琛ㄦ帶鍒跺櫒鏋勯€犲櫒銆?
     *
     * @param umsResourceService 鍚庡彴璧勬簮涓氬姟鏈嶅姟
     */
    public UmsResourceController(UmsResourceService umsResourceService) {
        this.umsResourceService = umsResourceService;
    }

    /**
     * 鏍规嵁 ID 鏌ヨ鍚庡彴璧勬簮淇℃伅銆?
     *
     * @param id 鍚庡彴璧勬簮 ID
     * @return 鏌ヨ缁撴灉
     */
    @Operation(summary = "鏍规嵁ID鏌ヨ鍚庡彴璧勬簮淇℃伅")
    @GetMapping("/{id}")
    public Result<UmsResourceVO> getById(@PathVariable Long id) {
        UmsResource resource = umsResourceService.getById(id);
        if (resource == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(BeanConvertUtil.convert(resource, UmsResourceVO.class));
    }

    /**
     * 鏌ヨ鍚庡彴璧勬簮鍒楄〃锛堜笉鍒嗛〉锛夈€?
     *
     * @return 鍚庡彴璧勬簮鍒楄〃
     */
    @Operation(summary = "鏌ヨ鍚庡彴璧勬簮鍒楄〃")
    @GetMapping
    public Result<List<UmsResourceVO>> list() {
        List<UmsResource> list = umsResourceService.list();
        return Result.success(BeanConvertUtil.convertList(list, UmsResourceVO.class));
    }

    /**
     * 鍒嗛〉鏌ヨ鍚庡彴璧勬簮鍒楄〃銆?
     *
     * @param pageNum 椤电爜
     * @param pageSize 姣忛〉鏉℃暟
     * @return 鍒嗛〉缁撴灉
     */
    @Operation(summary = "鍒嗛〉鏌ヨ鍚庡彴璧勬簮鍒楄〃")
    @GetMapping("/page")
    public Result<IPage<UmsResourceVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<UmsResource> page = new Page<>(pageNum, pageSize);
        IPage<UmsResource> resourcePage = umsResourceService.page(page);

        Page<UmsResourceVO> voPage = new Page<>();
        voPage.setCurrent(resourcePage.getCurrent());
        voPage.setSize(resourcePage.getSize());
        voPage.setTotal(resourcePage.getTotal());
        voPage.setPages(resourcePage.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(resourcePage.getRecords(), UmsResourceVO.class));

        return Result.success(voPage);
    }

    /**
     * 鍒涘缓鍚庡彴璧勬簮銆?
     *
     * @param dto 鍚庡彴璧勬簮鍏ュ弬
     * @return 鏄惁鍒涘缓鎴愬姛
     */
    @Operation(summary = "鍒涘缓鍚庡彴璧勬簮")
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody UmsResourceDTO dto) {
        UmsResource resource = BeanConvertUtil.convert(dto, UmsResource.class);
        boolean saved = umsResourceService.save(resource);
        return Result.success(saved);
    }

    /**
     * 鏇存柊鍚庡彴璧勬簮淇℃伅銆?
     *
     * @param id 鍚庡彴璧勬簮 ID
     * @param dto 鍚庡彴璧勬簮鍏ュ弬
     * @return 鏄惁鏇存柊鎴愬姛
     */
    @Operation(summary = "鏇存柊鍚庡彴璧勬簮淇℃伅")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsResourceDTO dto) {
        UmsResource resource = BeanConvertUtil.convert(dto, UmsResource.class);
        resource.setId(id);
        boolean updated = umsResourceService.updateById(resource);
        return Result.success(updated);
    }

    /**
     * 鍒犻櫎鍚庡彴璧勬簮锛堥€昏緫鍒犻櫎锛夈€?
     *
     * @param id 鍚庡彴璧勬簮 ID
     * @return 鏄惁鍒犻櫎鎴愬姛
     */
    @Operation(summary = "鍒犻櫎鍚庡彴璧勬簮")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = umsResourceService.removeById(id);
        return Result.success(removed);
    }
}