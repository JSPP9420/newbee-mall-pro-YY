package ltd.newbee.mall.service.dto;

import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.vo.MallUserVO;

public class UserLoginResult {
    private final String result;
    private final MallUserVO user;

    private UserLoginResult(String result, MallUserVO user) {
        this.result = result;
        this.user = user;
    }

    public static UserLoginResult success(MallUserVO user) {
        return new UserLoginResult(ServiceResultEnum.SUCCESS.getResult(), user);
    }

    public static UserLoginResult failure(String result) {
        return new UserLoginResult(result, null);
    }

    public String getResult() {
        return result;
    }

    public MallUserVO getUser() {
        return user;
    }
}
