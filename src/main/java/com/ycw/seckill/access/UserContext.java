package com.ycw.seckill.access;

import com.ycw.seckill.domain.MiaoshaUser;

/**
 * @author ycw
 */
public class UserContext {

    private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<>();

    public static MiaoshaUser getUser() {
        return userHolder.get();
    }

    public static void setUser(MiaoshaUser user) {
        userHolder.set(user);
    }

    public static void removeUser() {
        userHolder.remove();
    }
}
