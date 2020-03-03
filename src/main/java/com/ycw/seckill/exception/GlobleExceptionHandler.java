package com.ycw.seckill.exception;

import com.ycw.seckill.common.enums.ResultStatus;
import com.ycw.seckill.controller.ResultGeekQ;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author ycw
 */
@ControllerAdvice
@ResponseBody
public class GlobleExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResultGeekQ<String> exceptionHandler(Exception e) {
        if (e instanceof GlobleException) {
            GlobleException globleException = (GlobleException) e;
            return ResultGeekQ.error(globleException.getStatus());
        } else if (e instanceof BindException) {
//            BindException bindException = (BindException) e;
//            List<ObjectError> errors = bindException.getAllErrors();
//            ObjectError objectError = errors.get(0);
            return ResultGeekQ.error(ResultStatus.SESSION_ERROR);
        } else {
            return ResultGeekQ.error(ResultStatus.SYSTEM_ERROR);
        }
    }
}
