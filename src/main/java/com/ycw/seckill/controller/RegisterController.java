package com.ycw.seckill.controller;

import com.ycw.seckill.common.enums.ResultStatus;
import com.ycw.seckill.service.IMiaoshaUserService;
import com.ycw.seckill.service.MiaoshaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * @author ycw
 */

@Controller
@RequestMapping("/user")
public class RegisterController {

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private IMiaoshaUserService miaoshaUserService;

    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello, seckill";
    }

    @RequestMapping("/do_register")
    public String registerIndex()
    {
        return "register";
    }

    /**
     * 注册网站
     * @param mobile
     * @param passWord
     * @param salt
     * @return
     */
    @RequestMapping("/register")
    @ResponseBody
    public ResultGeekQ<String> register(@RequestParam("username") String mobile ,
                                        @RequestParam("password") String passWord,
                                        @RequestParam("verifyCode") String verifyCode,
                                        @RequestParam("salt") String salt,HttpServletResponse response ){

        ResultGeekQ<String> result = ResultGeekQ.build();

        // 校验验证码
        boolean checked = miaoshaService.checkVerifyCodeRegister(Integer.valueOf(verifyCode));
        if (!checked) {
            result.withError(ResultStatus.CODE_FAIL.getCode(), ResultStatus.CODE_FAIL.getMsg());
        }

        boolean registerInfo = miaoshaUserService.register(response, mobile, passWord, salt);
        if (!registerInfo) {
            result.withError(ResultStatus.FAIL.getCode(), ResultStatus.FAIL.getMsg());
            return result;
        }
        return result;
    }

}

