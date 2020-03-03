package com.ycw.seckill.exception;

import com.ycw.seckill.common.enums.ResultStatus;

/**
 * @author ycw
 */
public class GlobleException extends RuntimeException {

    private ResultStatus status;

    public GlobleException(ResultStatus status) {
        super();
        this.status = status;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }
}
