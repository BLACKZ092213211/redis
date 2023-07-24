package com.cf.pms;

import com.cf.api.response.APIResponse;
import com.cf.pms.common.Constant;
import com.cf.pms.error.BizErrorCode;
import com.cf.pms.exception.BusinessException;
import com.cf.pms.exception.CallbackException;
import com.cf.pms.exception.YundingCallbackException;
import com.cf.utils.log.LogHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.List;

/**
 * GlobalExceptionHandler
 *
 * @author liwei
 * @date 2017/3/29
 * @description 统一的异常处理类
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        // 转成蛇形
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ResponseEntity<Object> businessExceptionHandler(HttpServletRequest request,Exception e) {
        BusinessException ex = (BusinessException) e;
        LogHelper.error(logger, "GlobalExceptionHandler异常,errorcode={0}, message={1}", ex.getErrorCode(), ex.getMessage());
        HttpStatus httpStatus = this.httpStatusConvert(ex.getHttpStatus());
		APIResponse<Void> model = getAPIResponse(request, ex.getErrorCode(),
				ex.getMessage());
        return new ResponseEntity<>(objectMapper.convertValue(model, Object.class), httpStatus);
    }

	@ExceptionHandler(value = IllegalArgumentException.class)
	@ResponseBody
	public ResponseEntity<Object> illegalArgumentExceptionHandler(HttpServletRequest request, Exception e) {
		IllegalArgumentException ex = (IllegalArgumentException) e;
		LogHelper.error(logger, "GlobalExceptionHandler异常,errorCode={0}, message={1}",
				BizErrorCode.REQUEST_PARAM_EMPTY_ERROR, ex.getMessage());
		APIResponse<Void> model = getAPIResponse(request, BizErrorCode.REQUEST_PARAM_EMPTY_ERROR.getCode(),
				ex.getMessage());
		return new ResponseEntity<>(model, HttpStatus.EXPECTATION_FAILED);
	}


	@ExceptionHandler(value = { HttpMessageConversionException.class, JsonMappingException.class })
	@ResponseBody
	public ResponseEntity<Object> bindExceptionHandler(HttpServletRequest request, Exception e) {
		InvalidFormatException ex = (InvalidFormatException) e.getCause();
		String[] exNames = StringUtils.split(ex.getTargetType().getName(), ".");
		String exName = exNames[exNames.length - 1].toLowerCase();
		LogHelper.error(logger, "JSONException异常，errorcode={0}", BizErrorCode.REQUEST_PARAM_EMPTY_ERROR.getCode());
		APIResponse<Void> model = getAPIResponse(request, BizErrorCode.REQUEST_PARAM_EMPTY_ERROR.getCode(),
				MessageFormat.format("枚举类型传参错误:参数名：{0}，参数值：{1}", exName, ex.getValue()));
		return new ResponseEntity<>(model, HttpStatus.EXPECTATION_FAILED);
	}

    /**
     * Http 码转换
     *
     * @param status
     * @return
     */
    private HttpStatus httpStatusConvert(Integer status) {
        HttpStatus httpStatus;
        if (status != null) {
            try {
                httpStatus = HttpStatus.valueOf(status);
            } catch (Exception ex) {
                httpStatus = HttpStatus.EXPECTATION_FAILED;
            }
        } else {
            //如果业务异常并且没有定义http状态吗 这里默认是200 尽量不要使用500 否则前端拿到这个状态码会认为是不允许跨域
            httpStatus = HttpStatus.OK;
        }
        return httpStatus;
    }


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<Object> errorHandler(HttpServletRequest request, Exception e) {
        // 异常走错误流程 HttpStatus.EXPECTATION_FAILED
        HttpStatus httpStatus;
        String statusCode = null;
        String message = null;
        if(e instanceof CallbackException){
        	httpStatus = HttpStatus.EXPECTATION_FAILED;
        	message = e.getMessage();
        }else if (e instanceof YundingCallbackException) {
            httpStatus = HttpStatus.EXPECTATION_FAILED;
            message = "云丁回调业务处理异常";
        } else if (e instanceof BindException) {
            List<FieldError> fieldErrors = ((BindException) e).getBindingResult().getFieldErrors();
            for (FieldError error : fieldErrors) {
            	statusCode = BizErrorCode.REQUEST_PARAM_EMPTY_ERROR.getCode();
                message = error.getDefaultMessage();
                logger.info(error.getField() + ":" + error.getDefaultMessage());
            }
            httpStatus = HttpStatus.EXPECTATION_FAILED;
        } else if (e instanceof MethodArgumentNotValidException) {
            List<FieldError> fieldErrors = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors();
            for (FieldError error : fieldErrors) {
            	statusCode = BizErrorCode.REQUEST_PARAM_EMPTY_ERROR.getCode();
                message = error.getDefaultMessage();
                logger.info(error.getField() + ":" + error.getDefaultMessage());
            }
            httpStatus = HttpStatus.EXPECTATION_FAILED;
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
        	message = "此API的请求方式不正确，请转换正确的请求方式！";
            httpStatus = HttpStatus.EXPECTATION_FAILED;
        } else if (e instanceof DuplicateKeyException) {
            // 主键冲突异常 2018.11.23 yinqiang
            LogHelper.exception(e, logger, "发生主键冲突异常");
            message = "请勿重复操作，或稍后重试";
            httpStatus = HttpStatus.NOT_ACCEPTABLE;
        } else if (e instanceof NullPointerException) {
            // 空引用异常特殊处理 2018.11.23 yinqiang
            LogHelper.exception(e, logger, "发生NPE异常");
            message = "系统无法正确处理当前操作，错误码：NPE0001,请联系供应商排查";
            httpStatus = HttpStatus.EXPECTATION_FAILED;
        } else {
            LogHelper.exception(e, logger, "发生运行时异常");
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            // 修改异常文案 2018.11.23 yinqiang
            message = "检测到当前操作无法执行,请联系供应商处理";
            statusCode = httpStatus.toString();
        }
        APIResponse<Void> model = getAPIResponse(request, statusCode, message);
        return new ResponseEntity<>(objectMapper.convertValue(model, Object.class), httpStatus);
    }
    
    private APIResponse<Void> getAPIResponse(HttpServletRequest request,String statusCode,String message) {
    	String traceId = ThreadContext.get(Constant.TRACE_ID);
    	APIResponse<Void> model = new APIResponse<Void>();
    	// 标记请求出现异常(幂等拦截器会用到此值)
    	request.setAttribute(Constant.TRACE_ID, traceId);
    	// 错误输出增加traceid 18-12-20 yinqiang
    	model.setTraceId(traceId);
    	model.setStatusCode(statusCode);
    	model.setMessage(message);
    	return model;
    }
    
}
