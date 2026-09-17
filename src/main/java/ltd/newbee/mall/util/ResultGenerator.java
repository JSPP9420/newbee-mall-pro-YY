package ltd.newbee.mall.util;

import org.springframework.util.StringUtils;

public final class ResultGenerator {
    private static final String DEFAULT_SUCCESS_MESSAGE = "SUCCESS";
    private static final String DEFAULT_FAIL_MESSAGE = "FAIL";
    private static final int RESULT_CODE_SUCCESS = 200;
    private static final int RESULT_CODE_SERVER_ERROR = 500;

    private ResultGenerator() {
    }

    public static Result<Void> genSuccessResult() {
        return new Result<>(RESULT_CODE_SUCCESS, DEFAULT_SUCCESS_MESSAGE);
    }

    public static Result<Void> genSuccessResult(String message) {
        return new Result<>(RESULT_CODE_SUCCESS, message);
    }

    public static <T> Result<T> genSuccessResult(T data) {
        Result<T> result = new Result<>(RESULT_CODE_SUCCESS, DEFAULT_SUCCESS_MESSAGE);
        result.setData(data);
        return result;
    }

    public static Result<Void> genFailResult(String message) {
        return new Result<>(RESULT_CODE_SERVER_ERROR,
                StringUtils.hasText(message) ? message : DEFAULT_FAIL_MESSAGE);
    }

    public static <T> Result<T> genErrorResult(int code, String message) {
        return new Result<>(code, message);
    }
}
