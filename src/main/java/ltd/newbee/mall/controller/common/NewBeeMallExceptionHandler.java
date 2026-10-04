package ltd.newbee.mall.controller.common;

import ltd.newbee.mall.common.NewBeeMallException;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestControllerAdvice
public class NewBeeMallExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewBeeMallExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                               HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("请求参数校验失败");
        return response(400, message, request);
    }

    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("请求参数校验失败");
        return response(400, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleUnreadableMessage(HttpServletRequest request) {
        return response(400, "请求参数格式错误", request);
    }

    @ExceptionHandler(NewBeeMallException.class)
    public Object handleBusinessException(NewBeeMallException exception,
                                          HttpServletRequest request) {
        return response(400, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception exception, HttpServletRequest request) {
        LOGGER.error("请求处理失败: {} {}", request.getMethod(), request.getRequestURI(), exception);
        return response(500, "系统繁忙，请稍后重试", request);
    }

    private Object response(int code, String message, HttpServletRequest request) {
        Result<Void> result = ResultGenerator.genErrorResult(code, message);
        if (isJsonRequest(request)) {
            return result;
        }
        ModelAndView modelAndView = new ModelAndView("error/error");
        modelAndView.addObject("message", message);
        modelAndView.addObject("url", request.getRequestURL());
        return modelAndView;
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        return (contentType != null && contentType.contains("application/json"))
                || (accept != null && accept.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith);
    }
}
