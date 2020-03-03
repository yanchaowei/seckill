package com.ycw.seckill.common.enums;

/**
 * @author ycw
 *
 * 普通返回类
 * 1打头 系统系列错误
 * 2 注册登录系列错误
 * 3 check 系列错误
 * 4 秒杀错误
 * 5 商品错误
 * 6 订单错误
 */

public enum ResultStatus {

    SUCCESS(0, "成功"),
    SYSTEM_ERROR(10001, "系统错误"),

    /**
     * 注册登录
     */
    RESIGETR_SUCCESS(20000,"注册成功!"),
    RESIGETER_FAIL(200001,"注册失败!"),
    CODE_FAIL(200002,"验证码不一致!"),



    // check
    ACCESS_LIMIT_REACHED (30002,"请求非法!"),
    REQUEST_ILLEGAL (30004,"访问太频繁!"),
    SESSION_ERROR (30005,"Session不存在或者已经失效!"),
    MOBILE_NOT_EXIST (30009,"手机号不存在!"),
    PASSWORD_ERROR (30010,"密码错误!"),





    FAIL(-1, "失败"),

    // 秒杀模块
    MIAO_SHA_OVER(40001,"商品已经秒杀完毕"),
    REPEATE_MIAOSHA(40002,"不能重复秒杀"),
    MIAOSHA_FAIL(40003,"秒杀失败");

    private String msg;
    private Integer code;

    ResultStatus(Integer code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public String getName() {
        return this.name();
    }

    public String getOutputName() {
        return this.name();
    }

    private ResultStatus(Object... args) {
        this.msg = String.format(this.msg, args);
    }


}
