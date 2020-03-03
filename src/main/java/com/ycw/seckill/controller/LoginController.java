package com.ycw.seckill.controller;

import com.ycw.seckill.service.IMiaoshaUserService;
import com.ycw.seckill.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * @author ycw
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/hello")
    public String hello(Model model) {
//        model.addAttribute("msg", "成功到达页面");
        return "success";
    }

    @Autowired
    private IMiaoshaUserService miaoshaUserService;

    @RequestMapping("/to_login")
    public String toLogin(Model model) {
        model.addAttribute("count",0);
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public ResultGeekQ<Boolean> do_login(HttpServletResponse response, LoginVo loginVo) {
        ResultGeekQ<Boolean> result = ResultGeekQ.build();
        miaoshaUserService.login(response, loginVo);
        return result;
    }
}
