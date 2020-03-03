package com.ycw.seckill.controller;

import com.ycw.seckill.domain.MiaoshaUser;
import com.ycw.seckill.redis.GoodsKey;
import com.ycw.seckill.service.IGoodsService;
import com.ycw.seckill.vo.GoodsDetailVo;
import com.ycw.seckill.vo.GoodsVo;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author ycw
 */
@Controller
@RequestMapping("/goods")
public class GoodsController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    private IGoodsService goodsService;

    @RequestMapping(path = "/to_list", produces = "text/html")
    public String toList(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user) {
        model.addAttribute("user", user);
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsVoList);

        return render(request, response, model, "goods_list", GoodsKey.getGoodsList, "");
    }

    @RequestMapping(path = "/detail/{goodsId}")
    @ResponseBody
    public ResultGeekQ<GoodsDetailVo> goodsDetail(HttpServletRequest request, HttpServletResponse response, Model model,
                              MiaoshaUser user, @PathVariable("goodsId") Long goodsId) {
        ResultGeekQ<GoodsDetailVo> result = ResultGeekQ.build();
        GoodsVo goods = goodsService.getByGoodsId(goodsId);
        Long start = goods.getStartDate().getTime();
        Long end = goods.getEndDate().getTime();
        Long now = System.currentTimeMillis();
        int miaoshaStatus;
        int remainSeconds;
        if (now < start) {      // 秒杀还没开始
            miaoshaStatus = 0;
            remainSeconds = (int) ((start - now)/1000);
        } else if (now > end) {
            miaoshaStatus = 2;
            remainSeconds = -1;
        } else {
            miaoshaStatus = 1;
            remainSeconds = 0;
        }

        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setMiaoshaStatus(miaoshaStatus);
        vo.setRemainSeconds(remainSeconds);
        result.setData(vo);
        return result;

    }
}
