package com.finance.platform.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.message.dto.FinNewsDTO;
import com.finance.platform.message.entity.FinNews;
import com.finance.platform.message.mapper.FinNewsMapper;
import com.finance.platform.message.service.FinNewsService;
import com.finance.platform.message.vo.FinNewsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinNewsServiceImpl extends ServiceImpl<FinNewsMapper, FinNews>
        implements FinNewsService {

    @Override
    public FinNewsVO getNewsById(Long id) {
        FinNews entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinNewsVO vo = new FinNewsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<FinNewsVO> getNewsList() {
        List<FinNews> list = list();
        return list.stream().map(entity -> {
            FinNewsVO vo = new FinNewsVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean createNews(FinNewsDTO dto) {
        FinNews entity = new FinNews();
        BeanUtils.copyProperties(dto, entity);
        return save(entity);
    }

    @Override
    public boolean updateNews(Long id, FinNewsDTO dto) {
        FinNews entity = getById(id);
        if (entity == null) {
            return false;
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    public boolean deleteNews(Long id) {
        return removeById(id);
    }
}