package com.ycw.seckill.controller;

import com.ycw.seckill.access.AccessLimit;
import com.ycw.seckill.common.enums.ResultStatus;
import com.ycw.seckill.domain.MiaoshaOrder;
import com.ycw.seckill.domain.MiaoshaUser;
import com.ycw.seckill.rabbitmq.MQSender;
import com.ycw.seckill.rabbitmq.MiaoshaMessage;
import com.ycw.seckill.redis.GoodsKey;
import com.ycw.seckill.service.IGoodsService;
import com.ycw.seckill.service.IOrderService;
import com.ycw.seckill.service.MiaoshaService;
import com.ycw.seckill.service.RedisService;
import com.ycw.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author ycw
 */
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(MiaoshaController.class);

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private MQSender mqSender;

    private HashMap<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    @RequestMapping(value = "/verifyCodeRegister", method = RequestMethod.GET)
    public ResultGeekQ<String> getMiaoshaVerifyCod(HttpServletResponse response) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        try {
            BufferedImage image = miaoshaService.createVerifyCodeRegister();
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "JPEG", outputStream);
            outputStream.flush();
            outputStream.close();
            return result;
        } catch (IOException e) {
            logger.error("【注册】生成验证码错误。");
            result.withError(ResultStatus.MIAOSHA_FAIL.getCode(), ResultStatus.MIAOSHA_FAIL.getMsg());
            e.printStackTrace();
            return result;
        }

    }

    @AccessLimit(second = 60, maxCount = 5, needLogin = true)
    @RequestMapping(path = "/path", method = RequestMethod.GET)
    @ResponseBody
    public ResultGeekQ<String> getMiaoshaPath(MiaoshaUser user, @RequestParam("goodsId") String goodsId, @RequestParam("verifyCode") Integer verifyCode) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        if (user == null) {
            result.withError(ResultStatus.SESSION_ERROR.getCode(), ResultStatus.SESSION_ERROR.getMsg());
            return result;
        }
        boolean checked = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if (!checked) {
            result.withError(ResultStatus.REQUEST_ILLEGAL.getCode(), ResultStatus.REQUEST_ILLEGAL.getMsg());
            return result;
        }
        String path = miaoshaService.createMiaoshaPath(user, goodsId);
        result.setData(path);
        return result;
    }

    /**
     * * 秒杀思路：
     * =》获取路径
     * =》请求秒杀
     * =》 控制层判断：
     *      1、是否已经秒杀到，若已经秒杀到，则返回重复秒杀错误；
     *      2、库存减-1，注意这里是redis中操作，可能出现负数，但是没有影响，因为仅仅为了限流。
     *      若 >0，说明该用户已经获得“访问数据库中商品的资格”，符合创建订单的条件，发出消息异步进行创建订单。
     * =》获取秒杀结果：根据用户信息和商品id访问数据库，看看上一步的消息任务有没有成功创建订单，若查询到订单，则该用户秒杀成功
     *
     * @param user
     * @param path
     * @param goodsId
     * @return
     */
    @RequestMapping(path = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public ResultGeekQ<Integer> doMiaosha(MiaoshaUser user, @PathVariable("path") String path,
                                          @RequestParam("goodsId") Long goodsId) {
        ResultGeekQ result = ResultGeekQ.build();
        if (user == null) {
            result.withError(ResultStatus.SESSION_ERROR.getCode(), ResultStatus.SESSION_ERROR.getMsg());
            return result;
        }
        //验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);
        if (!check) {
            result.withError(ResultStatus.REQUEST_ILLEGAL.getCode(), ResultStatus.REQUEST_ILLEGAL.getMsg());
            return result;
        }
//		//使用RateLimiter 限流
//		RateLimiter rateLimiter = RateLimiter.create(10);
//		//判断能否在1秒内得到令牌，如果不能则立即返回false，不会阻塞程序
//		if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
//			System.out.println("短期无法获取令牌，真不幸，排队也瞎排");
//			return ResultGeekQ.error(CodeMsg.MIAOSHA_FAIL);
//
//		}

        //是否已经秒杀到
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(user.getNickname(), goodsId);
        if (miaoshaOrder != null) {
            result.withError(ResultStatus.REPEATE_MIAOSHA.getCode(), ResultStatus.REPEATE_MIAOSHA.getMsg());
            return result;
        }

        // 内存标记减少redis访问
        Boolean over = localOverMap.get(goodsId);
        if (over != null && over) {
            result.withError(ResultStatus.MIAO_SHA_OVER.getCode(), ResultStatus.MIAO_SHA_OVER.getMsg());
            return result;
        }

        // 预减库存
        Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            result.withError(ResultStatus.MIAO_SHA_OVER.getCode(), ResultStatus.MIAO_SHA_OVER.getMsg());
            return result;
        }
        // miaoshaService.miaosha(user, goodsService.getByGoodsId(goodsId));
        // 此处应替换为使用消息队列进行流量削峰
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(mm);
        return result;
    }

    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @RequestMapping("/result")
    @ResponseBody
    public ResultGeekQ<Long> getMiaoshaResult(@RequestParam("goodsId") Long goodsId, MiaoshaUser user, Model model) {
        ResultGeekQ<Long> result = ResultGeekQ.build();
        if (user == null) {
            result.withError(ResultStatus.SESSION_ERROR.getCode(), ResultStatus.SESSION_ERROR.getMsg());
            return result;
        }
        model.addAttribute("user", user);
      long miaoshaResult = miaoshaService.getMiaoshaResult(user, goodsId);
        result.setData(miaoshaResult);
        return result;
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    public ResultGeekQ<String> verifyCode(HttpServletResponse response, @RequestParam("goodsId")Long goodsId, MiaoshaUser user) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        if (user == null) {
            result.withError(ResultStatus.SESSION_ERROR.getCode(), ResultStatus.SESSION_ERROR.getMsg());
            return result;
        }
        try {
            BufferedImage image = miaoshaService.getMiaoshaVerifyCode(user, goodsId);
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "JPEG", outputStream);
            outputStream.flush();
            outputStream.close();
            return result;
        } catch (IOException e) {
            logger.error("【秒杀】生成验证码错误。");
            result.withError(ResultStatus.MIAOSHA_FAIL.getCode(), ResultStatus.MIAOSHA_FAIL.getMsg());
            e.printStackTrace();
            return result;
        }

    }

    /**
     * 系统初始化
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        if (goodsVoList == null)
            return;
        for (GoodsVo goodsVo : goodsVoList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goodsVo.getId(), goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(), false);
        }

    }
}
