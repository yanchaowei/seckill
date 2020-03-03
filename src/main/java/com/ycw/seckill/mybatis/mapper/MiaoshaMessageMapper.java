package com.ycw.seckill.mybatis.mapper;

import com.ycw.seckill.domain.MiaoShaMessageInfo;
import com.ycw.seckill.domain.MiaoShaMessageUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ycw
 */
@Mapper
public interface MiaoshaMessageMapper {
    void insertMiaoshaMessageUser(MiaoShaMessageUser messageUser);

    void insertMiaoshaMessageInfo(MiaoShaMessageInfo messageInfo);
}
