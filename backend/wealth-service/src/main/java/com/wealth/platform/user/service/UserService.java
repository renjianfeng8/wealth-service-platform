package com.wealth.platform.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.common.dto.LoginDTO;
import com.wealth.platform.user.dto.UserDTO;
import com.wealth.platform.user.entity.User;
import com.wealth.platform.user.vo.LoginVO;
import com.wealth.platform.user.vo.UserVO;

import java.util.List;

public interface UserService extends IService<User> {

    // 新增用户（含密码加密）
    boolean createUser(UserDTO dto);

    // 用户注册
    Boolean register(UserDTO dto);

    // 用户登录
    LoginVO login(LoginDTO dto);

    // 统一登录（自动识别用户类型）
    LoginVO identifyLogin(LoginDTO dto);

    // 重置密码
    Boolean resetPassword(User user, String oldPassword);

    // 分页条件查询
    IPage<User> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status);

    List<UserVO> getUserList(Integer pageNum, Integer pageSize);

    UserVO getUserById(Long id);

    boolean updateUser(Long id, UserDTO dto);

    boolean deleteUser(Long id);
}
