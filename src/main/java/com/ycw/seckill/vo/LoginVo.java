package com.ycw.seckill.vo;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * @author ycw
 */
@Getter
@Setter
public class LoginVo {

    @NonNull
//    @MobileCheck
    private String mobile;

    @NonNull
    @Length(min = 32)
    private String password;

    @Override
    public String toString() {
        return "LoginVo{" +
                "mobile='" + mobile + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
