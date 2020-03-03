package com.ycw.seckill.common.resultBean;

import com.ycw.seckill.common.enums.ResultStatus;

/**
 * @author ycw
 */
public class AbstractResult {
    private ResultStatus status;
    private int code;
    private String message;

    public AbstractResult(ResultStatus status, String message) {
        this.status = status;
        this.message = message;
        this.code = status.getCode();
    }

    public AbstractResult(ResultStatus status) {
        this.status = status;
        this.code = status.getCode();
        this.message = status.getMsg();
    }

    public boolean isSuccess(AbstractResult result) {
        return result != null && result.getStatus() == ResultStatus.SUCCESS && result.getStatus().getCode() == ResultStatus.SUCCESS.getCode();
    }

    public AbstractResult success() {
        this.status = ResultStatus.SUCCESS;
        return this;
    }

    public AbstractResult withError(int code, String message) {
        this.code = code;
        this.message = message;
        return this;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
