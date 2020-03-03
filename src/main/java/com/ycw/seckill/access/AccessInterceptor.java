package com.ycw.seckill.access;

import com.alibaba.fastjson.JSON;
import com.ycw.seckill.common.enums.ResultStatus;
import com.ycw.seckill.controller.ResultGeekQ;
import com.ycw.seckill.domain.MiaoshaUser;
import com.ycw.seckill.service.RedisService;
import com.ycw.seckill.service.impl.MiaoshaUserviceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author ycw
 */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private MiaoshaUserviceImpl miaoshaUservice;

    @Autowired
    private RedisService redisService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            // 获得请求方法
            HandlerMethod method = (HandlerMethod) handler;
            /**
             * 【获取user】思路：
             * 我们在登陆的时候，将user存在了redis，主键是 token 的值，而token放在了cookie返回到浏览器；
             * 所以我们还是要从redis获得登陆用户的信息，前提是拿到 token 的值；
             * token的值只能从用户的浏览器端带来，所以不是在request的域中就是在cookie中。
             * 拿到token就可以调用 MiaoshaUserService 来获取redis中的用户信息。
             */
            MiaoshaUser user = getUser(request, response);
            // 线程隔离，为每一个登陆的用户创建一个单独的user变量副本
            UserContext.setUser(user);
            AccessLimit accessLimit = method.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            int second = accessLimit.second();
            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    render(response, ResultStatus.SESSION_ERROR);
                    return false;
                }
                key = key + "_" + user.getNickname();
            } else {
                // 无权限要求，不做任何事情
            }
            // 现在构造 key，从redis中拿到该请求已经访问的次数,并进行判断是否超过限制次数
            AccessKey accessKey = AccessKey.withExpire(second);
            Integer count = redisService.get(accessKey, key, Integer.class);
            if (count == null) {
                redisService.set(accessKey, key, 1);
            } else if (count < maxCount) {
                redisService.incr(accessKey, key);
            } else {
                render(response, ResultStatus.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        UserContext.removeUser();
    }

    private void render(HttpServletResponse response, ResultStatus sessionError) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        ServletOutputStream outputStream = response.getOutputStream();
        String jsonString = JSON.toJSONString(ResultGeekQ.error(sessionError));
        outputStream.write(jsonString.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String requestToken = request.getParameter(MiaoshaUserviceImpl.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieToken(request, MiaoshaUserviceImpl.COOKIE_NAME_TOKEN);
        if (requestToken == null && cookieToken == null) {
            return null;
        }
        String token = requestToken != null ? requestToken : cookieToken;
        MiaoshaUser user = miaoshaUservice.getUserByToken(response, token);
        return user;
    }

    private String getCookieToken(HttpServletRequest request, String tokenName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(tokenName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
