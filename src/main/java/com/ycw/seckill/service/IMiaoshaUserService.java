package com.ycw.seckill.service;

import com.ycw.seckill.domain.MiaoshaUser;
import com.ycw.seckill.vo.LoginVo;

import javax.servlet.http.HttpServletResponse; /**
 * @author ycw
 */
public interface IMiaoshaUserService {
    boolean login(HttpServletResponse response, LoginVo loginVo);

    boolean register(HttpServletResponse response, String userName, String passWord, String salt);

    MiaoshaUser getUserByToken(HttpServletResponse response, String token);
}
