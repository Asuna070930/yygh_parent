package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/10 15:20
 */
public interface UserInfoService extends IService<UserInfo> {
    Map login(LoginVo loginVo);

    UserInfo selectByOpenid(String openid);

    Map bundle(LoginVo loginVo);

    void userAuth(Long userId, UserAuthVo userAuthVo);

    UserInfo getUserInfo(Long userId);

    Page<UserInfo> selectList(Integer page, Integer limit, UserInfoQueryVo userInfoQueryVo);

    void updateStatus(Long id, Integer status);

    Map show(Long userId);

    void updateAuthStatus(Long userId, Integer authStatus);
}
