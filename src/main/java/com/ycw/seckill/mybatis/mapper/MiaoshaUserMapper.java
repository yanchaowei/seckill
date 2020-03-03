package com.ycw.seckill.mybatis.mapper;

import com.ycw.seckill.domain.MiaoshaUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * @author ycw
 */
@Mapper
public interface MiaoshaUserMapper {

    @Select("select * from miaosha_user where nickname = #{mobile}")
    MiaoshaUser getByUserName(String mobile);

    @Insert("insert into miaosha_user (id , nickname ,password , salt ,head,register_date,last_login_date)value (#{id},#{nickname},#{password},#{salt},#{head},#{registerDate},#{lastLoginDate}) ")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    public void insertMiaoShaUser(MiaoshaUser miaoshaUser);
}
