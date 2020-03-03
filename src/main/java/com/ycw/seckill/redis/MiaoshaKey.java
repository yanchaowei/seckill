package com.ycw.seckill.redis;

/**
 * @author ycw
 */
public class MiaoshaKey extends BasePrefix {
    public MiaoshaKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(300, "mp");
    public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(300, "vc");
    public static MiaoshaKey getMiaoshaVerifyCodeRegister = new MiaoshaKey(300, "register");
}
