package com.atguigu.yygh.user.service.impl;

import com.atguigu.common.exphandler.YyghException;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/10 15:21
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    PatientService patientService;


    //手机号+短信验证码登录接口的需求：
    //1、手机号，验证码 判断非空
    //2、判断验证码是否正确（用户输入的验证码和真正发送的验证码比较），校验失败，抛出自定义异常，提示验证码不正确
    //3、如果验证码正确，接下来判断该手机号是否存在，如果不存在，自动注册（authStatus=0，status=1（正常状态））
    //4、如果存在，判断该用户的status状态是否被锁定，抛出自定义异常，提示用户被锁定
    //5、准备返回值： name + token
    //   name：右上角显示的内容，优先取userInfo中的name，如果为空取出nick_name，依然为空最后取出phone
    //   token: 当前用户登录成功后，服务端颁发的jwt格式的令牌，该令牌是加密的，令牌中会存储该用户的一部分信息，例如：用户的id，用户的name
    //前端收到name和token之后，存储在浏览器的cookie中
    //登录成功后，前端每次发起的请求，都会主动从cookie中获取token令牌，放在请求头中，一些传递给服务端接口
    //服务端接口收到该令牌之后，可以进行校验，可以解析其中的信息
    @Override
    public Map login(LoginVo loginVo) {
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();

        //1、手机号，验证码 判断非空
        if (StringUtils.isEmpty(phone)) {
            throw new YyghException(20001, "手机号为空");
        }
        if (StringUtils.isEmpty(code)) {
            throw new YyghException(20001, "验证码为空");
        }

        //2、判断验证码是否正确
        String codeFromRedis = stringRedisTemplate.opsForValue().get(phone);
        if (StringUtils.isEmpty(codeFromRedis)) {
            throw new YyghException(20001, "验证码已过期，请重新发送");
        }
        if (!code.equals(codeFromRedis)) {
            throw new YyghException(20001, "验证码不正确");
        }

        //3.判断手机号是否存在，不存在就自动注册
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);

        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setPhone(phone);
            userInfo.setAuthStatus(AuthStatusEnum.NO_AUTH.getStatus());//认证状态默认=0，未认证
            userInfo.setStatus(1);//正常状态
            int insert = baseMapper.insert(userInfo);
            if (insert < 0) {
                throw new YyghException(20001, "自动注册失败");
            }
        }

        //4.判断用户的状态是否被锁定
        if (userInfo.getStatus() == 0) {
            throw new YyghException(20001, "用户被锁定，不允许登录");
        }

        //5.返回值name + token
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
        }
        String token = JwtHelper.createToken(userInfo.getId(), name);//jwt格式的令牌，用于存放当前用户的一些信息
        //后端接口可以从请求头中获取令牌，判断当前用户是否是已登录的状态

        Map map = new HashMap();
        map.put("name", name);
        map.put("token", token);

        return map;
    }

    @Override
    public UserInfo selectByOpenid(String openid) {

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);

        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * @param loginVo 1、必要参数非空校验
     *                <p>
     *                <p>
     *                手机号不能重复存在，
     * @return
     */
    @Override
    public Map bundle(LoginVo loginVo) {
        //1、先非空校验
        String code = loginVo.getCode();
        String phone = loginVo.getPhone();
        String openid = loginVo.getOpenid();

        //2、短信验证码的校验
        String codeFromRedis = stringRedisTemplate.opsForValue().get(phone);
        if (StringUtils.isEmpty(codeFromRedis)) {
            throw new YyghException(20001, "请重新发送验证码");
        }
        if (!codeFromRedis.equals(code)) {
            throw new YyghException(20001, "验证码不正确");
        }

        //3、根据openid查询微信用户
        UserInfo userInfo_openid = this.selectByOpenid(openid);


        //4、根据phone查询手机号用户
        UserInfo userInfo_phone = this.selectByPhone(phone);

        //5、判断4的返回值为null，说明，该手机号在数据库中不存在，可以直接绑定
        if (userInfo_phone == null) {
            userInfo_openid.setPhone(phone);
            userInfo_openid.setUpdateTime(new Date());
            baseMapper.updateById(userInfo_openid);


            //判断用户是否被锁定
            if (userInfo_openid.getStatus() == 0) {
                throw new YyghException(20001, "用户被锁定");
            }

            return this.getMap(userInfo_openid);
        } else {
            //6、手机号已经存在，接下来，判断该手机号是否被其它的微信绑定了
            if (StringUtils.isEmpty(userInfo_phone.getOpenid())) {

                //手机号用户删除
                baseMapper.deleteById(userInfo_phone.getId());

                //更新微信用户
                userInfo_openid.setPhone(phone);
                userInfo_openid.setUpdateTime(new Date());
                baseMapper.updateById(userInfo_openid);
            } else {
                //手机号已经存在，并且被其它的微信绑定过了；就不能继续使用该手机号去绑定
                throw new YyghException(20001, "手机号已被占用");
            }
            return this.getMap(userInfo_openid);
        }

    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        UserInfo userInfo = baseMapper.selectById(userId);

        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());

        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());//1认证中
        userInfo.setUpdateTime(new Date());

        int i = baseMapper.updateById(userInfo);
        if (i <= 0) {
            throw new YyghException(20001, "提交认证信息失败");
        }
    }

    @Override
    public UserInfo getUserInfo(Long userId) {
        UserInfo userInfo = baseMapper.selectById(userId);

        //param.authStatusString
        this.packUserInfo(userInfo);
        return userInfo;
    }

    @Override
    public Page<UserInfo> selectList(Integer page, Integer limit, UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> userInfoPage = new Page<>(page, limit);

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        String keyword = userInfoQueryVo.getKeyword();//用户名模糊查询
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();//开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();//结束时间

        Integer authStatus = userInfoQueryVo.getAuthStatus();//认证状态


        //注意：mp条件拼接，一定先判空
        if (!StringUtils.isEmpty(keyword)) {
            queryWrapper.like("name", keyword);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            queryWrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            queryWrapper.le("create_time", createTimeEnd);
        }
        //当前端传递的authStatus不等于空，拼接该条件
        if (!StringUtils.isEmpty(authStatus)) {
            queryWrapper.eq("auth_status", authStatus);
        }

        // userInfoPage1 ==userInfoPage ？ true
        Page<UserInfo> userInfoPage1 = baseMapper.selectPage(userInfoPage, queryWrapper);

        userInfoPage1.getRecords().forEach(userInfo -> {
            this.packUserInfo(userInfo);
        });

        return userInfoPage1;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        UserInfo userInfo = baseMapper.selectById(id);
        userInfo.setStatus(status);
        baseMapper.updateById(userInfo);
    }

    //展示认证列表接口
    @Override
    public Map show(Long userId) {
//        map = userInfo + patientList

        UserInfo userInfo = baseMapper.selectById(userId);
//        this.packUserInfo(userInfo);//param

        //查询就诊人列表
        List<Patient> patientList = patientService.findAll(userId);

        Map map = new HashMap();
        map.put("userInfo",userInfo);
        map.put("patientList",patientList);

        return map;
    }


    //驳回接口
    @Override
    public void updateAuthStatus(Long userId, Integer authStatus) {
        UserInfo userInfo = baseMapper.selectById(userId);
        userInfo.setAuthStatus(authStatus);
        baseMapper.updateById(userInfo);
    }


    private void packUserInfo(UserInfo userInfo) {
        Integer authStatus = userInfo.getAuthStatus();
        String statusNameByStatus = AuthStatusEnum.getStatusNameByStatus(authStatus);
        userInfo.getParam().put("authStatusString", statusNameByStatus);
        userInfo.getParam().put("statusString", userInfo.getStatus() == 0 ? "锁定" : "正常");//锁定和正常
    }

    private UserInfo selectByPhone(String phone) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return baseMapper.selectOne(queryWrapper);
    }


    private Map getMap(UserInfo userInfo) {
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
        }
        String token = JwtHelper.createToken(userInfo.getId(), name);

        Map map = new HashMap();
        map.put("name", name);
        map.put("token", token);
        return map;
    }
}
