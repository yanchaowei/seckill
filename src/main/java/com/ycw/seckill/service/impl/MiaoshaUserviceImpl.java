package com.ycw.seckill.service.impl;

import com.ycw.seckill.common.SnowflakeIdWorker;
import com.ycw.seckill.common.enums.MessageStatus;
import com.ycw.seckill.common.enums.ResultStatus;
import com.ycw.seckill.domain.MiaoshaUser;
import com.ycw.seckill.exception.GlobleException;
import com.ycw.seckill.mybatis.mapper.MiaoshaUserMapper;
import com.ycw.seckill.rabbitmq.MQSender;
import com.ycw.seckill.redis.MiaoShaUserKey;
import com.ycw.seckill.service.IMiaoshaUserService;
import com.ycw.seckill.service.RedisService;
import com.ycw.seckill.utils.MD5Utils;
import com.ycw.seckill.vo.LoginVo;
import com.ycw.seckill.vo.MiaoshaMessageVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.tools.ToolProvider;
import java.util.Date;
import java.util.UUID;

/**
 * @author ycw
 */
@Service
public class MiaoshaUserviceImpl implements IMiaoshaUserService {

    public static final String COOKIE_NAME_TOKEN = "token" ;
    private static Logger logger = LoggerFactory.getLogger(MiaoshaUserviceImpl.class);


    @Autowired
    MiaoshaUserMapper miaoshaUserMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    private MQSender sender;

    @Override
    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobleException(ResultStatus.SYSTEM_ERROR);
        }

        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        MiaoshaUser user = getByNickName(mobile);
        if (user == null) {
            throw new GlobleException(ResultStatus.MOBILE_NOT_EXIST);
        }

        String dbPass = user.getPassword();
        String saltDb = user.getSalt();
        String calPass = MD5Utils.formPassToDBPass(password, saltDb);
        if (!calPass.equals(dbPass)) {
            throw new GlobleException(ResultStatus.PASSWORD_ERROR);
        }
        String token = UUID.randomUUID().toString();
        addCookie(response, token, user);
        return true;
    }

    @Override
    public boolean register(HttpServletResponse response, String mobile, String passWord, String salt) {

        MiaoshaUser user = new MiaoshaUser();
        user.setNickname(mobile);
        String dbpass = MD5Utils.formPassToDBPass(passWord, salt);
        user.setPassword(dbpass);
        user.setSalt(salt);
        user.setRegisterDate(new Date());

        try {
            miaoshaUserMapper.insertMiaoShaUser(user);
            MiaoshaUser userByUserName = miaoshaUserMapper.getByUserName(mobile);
            if (userByUserName == null) {
                return false;
            }

            MiaoshaMessageVo vo = new MiaoshaMessageVo();
            vo.setContent("尊敬的用户您好，您已经成功登陆！");
            vo.setCreateTime(new Date());
            vo.setMessageId(SnowflakeIdWorker.getOrderId(0,0));
            vo.setSendType(0);
            vo.setStatus(0);
            vo.setMessageType(MessageStatus.messageType.system_message.ordinal());
            vo.setUserId(user.getId());
            vo.setMessageHead(MessageStatus.ContentEnum.system_message_register_head.getMessage());

            sender.sendMiaoshaMessageVo(vo);

        } catch (Exception e) {
            logger.error("注册失败！", e);
            return false;
        }
        return true;
    }

    @Override
    public MiaoshaUser getUserByToken(HttpServletResponse response, String token) {
        if (token == null || token.length() == 0) {
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoShaUserKey.token, token, MiaoshaUser.class);
        addCookie(response, token, user);
        return user;
    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        // 将用户信息放入缓存
        redisService.set(MiaoShaUserKey.token, token, user);
        // cookie
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        // 设置过期时间
        cookie.setMaxAge(MiaoShaUserKey.TOKEN_EXPIRE);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private MiaoshaUser getByNickName(String mobile) {
        MiaoshaUser user = redisService.get(MiaoShaUserKey.getByNickName, mobile, MiaoshaUser.class);
        if (user != null) {
            return user;
        }
        user = miaoshaUserMapper.getByUserName(mobile);
        if (user != null) {
            redisService.set(MiaoShaUserKey.getByNickName, mobile, user);
        }
        return user;
    }
}
