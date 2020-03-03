package com.ycw.seckill.service;

import com.ycw.seckill.domain.MiaoshaOrder;
import com.ycw.seckill.domain.MiaoshaUser;
import com.ycw.seckill.domain.OrderInfo;
import com.ycw.seckill.redis.GoodsKey;
import com.ycw.seckill.redis.MiaoshaKey;
import com.ycw.seckill.utils.MD5Utils;
import com.ycw.seckill.utils.UUIDUtil;
import com.ycw.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * @author ycw
 */
@Service
public class MiaoshaService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IGoodsService goodsService;


    public BufferedImage createVerifyCodeRegister() {
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCodeRegister,"register",rnd);
        //输出图片
        return image;
    }

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            Integer catch1 = (Integer)engine.eval(exp);
            return catch1.intValue();
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static char[] ops = new char[] {'+', '-', '*'};
    /**
     * + - *
     * */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    public boolean checkVerifyCodeRegister(Integer verifyCode) {
        Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCodeRegister, "register", Integer.class);
        if (codeOld == null || codeOld - verifyCode != 0) {
            return false;
        }
        return redisService.delete(MiaoshaKey.getMiaoshaVerifyCodeRegister, "register");
    }

    public String createMiaoshaPath(MiaoshaUser user, String goodsId) {
        if (user == null || goodsId == null) {
            return null;
        }
        String str = MD5Utils.md5(UUIDUtil.uuid() + "123456");
        redisService.set(MiaoshaKey.getMiaoshaPath, user.getNickname() + "_" + goodsId, str);
        return str;
    }

    public boolean checkPath(MiaoshaUser user, Long goodsId, String path) {
        if (user == null || goodsId == null) {
            return false;
        }
        String cachePath = redisService.get(MiaoshaKey.getMiaoshaPath, user.getNickname() + "_" + goodsId, String.class);
        return path.equals(cachePath);
    }

    public long getMiaoshaResult(MiaoshaUser user, Long goodsId) {
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(user.getNickname(), goodsId);
        if (miaoshaOrder != null) {
            return miaoshaOrder.getOrderId();   // >0 代表秒杀成功
        } else {
            Boolean over = getGoodsOver(goodsId);
            if (over) {
                return -1;  // -1 代表商品已经被秒杀完
            } else {
                return 0;   // 0 代表未秒杀成功，但商品还有库存
            }
        }
    }

    private Boolean getGoodsOver(Long goodsId) {
        return redisService.exists(GoodsKey.isGoodsOver, "" + goodsId);
    }

    public BufferedImage getMiaoshaVerifyCode(MiaoshaUser user, Long goodsId) {
        if (user == null || goodsId == null) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode,user.getNickname() + "_" + goodsId,rnd);
        //输出图片
        return image;
    }

    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        boolean success = goodsService.reduceStock(goods);
        if (success) {
            OrderInfo order = orderService.createOrder(user, goods);
            return order;
        } else {
            setGoodsOver(goods.getId());
            return null;
        }
    }

    private void setGoodsOver(Long goodsId) {
        redisService.set(GoodsKey.isGoodsOver, "" + goodsId, true);
    }

    public boolean checkVerifyCode(MiaoshaUser user, String goodsId, Integer verifyCode) {
        if (user == null || goodsId == null) {
            return false;
        }
        Integer oldCode = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getNickname() + "_" + goodsId, Integer.class);
        if (oldCode == null || oldCode - verifyCode != 0) {
            return false;
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getNickname() + "_" + goodsId);
        return true;
    }
}
