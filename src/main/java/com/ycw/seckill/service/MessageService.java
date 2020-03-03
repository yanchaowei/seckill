package com.ycw.seckill.service;

import com.ycw.seckill.domain.MiaoShaMessageInfo;
import com.ycw.seckill.domain.MiaoShaMessageUser;
import com.ycw.seckill.mybatis.mapper.MiaoshaMessageMapper;
import com.ycw.seckill.vo.MiaoshaMessageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author ycw
 */
@Service
public class MessageService {

    @Autowired
    private MiaoshaMessageMapper MessageMapper;

    public void insertMessage(MiaoshaMessageVo miaoshaMessageVo) {
        // 构建 MiaoShaMessageUser 并插入数据库
        MiaoShaMessageUser messageUser = new MiaoShaMessageUser();
        messageUser.setGoodId(miaoshaMessageVo.getGoodId());
        messageUser.setMessageId(miaoshaMessageVo.getMessageId());
        MessageMapper.insertMiaoshaMessageUser(messageUser);

        // 构建 MiaoShaMessageInfo 并插入数据库
        MiaoShaMessageInfo messageInfo = new MiaoShaMessageInfo();
        messageInfo.setContent(miaoshaMessageVo.getContent());
        messageInfo.setCreateTime(new Date());
        messageInfo.setGoodName(miaoshaMessageVo.getGoodName());
        messageInfo.setMessageHead(miaoshaMessageVo.getMessageHead());
        messageInfo.setMessageId(miaoshaMessageVo.getMessageId());
        messageInfo.setMessageType(miaoshaMessageVo.getMessageType());
        messageInfo.setOverTime(miaoshaMessageVo.getOverTime());
        messageInfo.setPrice(miaoshaMessageVo.getPrice());
        messageInfo.setStatus(miaoshaMessageVo.getStatus());
        messageInfo.setUserId(miaoshaMessageVo.getUserId());
        MessageMapper.insertMiaoshaMessageInfo(messageInfo);
    }
}
