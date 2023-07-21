package com.atguigu.common.exphandler;

import com.atguigu.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/6/28 19:54
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {


    //当接口中出现exception异常时,最后会执行此方法
    //方法的参数, 目标接口所出现的异常
    @ExceptionHandler(Exception.class)
    public R error(Exception e) {
        return R.error().message(e.getMessage());
    }

    @ExceptionHandler(ArithmeticException.class)
    public R error(ArithmeticException e) {
        return R.error().message(e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public R error(NullPointerException e) {
        return R.error().message(e.getMessage());
    }

    @ExceptionHandler(YyghException.class)
    public R error(YyghException e) {

        log.error("自定义异常" + e.getMsg());
        //自定义异常
        return R.error().message("自定义异常"+e.getMsg());
    }
}
