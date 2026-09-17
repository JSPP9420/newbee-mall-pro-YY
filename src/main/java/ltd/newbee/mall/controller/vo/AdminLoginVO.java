package ltd.newbee.mall.controller.vo;

/**
 * 管理端登录成功返回 VO（pro 版新增）
 * token 给前端保存，profile 是管理员资料（复用原项目的 AdminUserProfileVO）
 */
public class AdminLoginVO {

    private String token;

    private AdminUserProfileVO profile;

    public static AdminLoginVO of(String token, AdminUserProfileVO profile) {
        AdminLoginVO vo = new AdminLoginVO();
        vo.token = token;
        vo.profile = profile;
        return vo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AdminUserProfileVO getProfile() {
        return profile;
    }

    public void setProfile(AdminUserProfileVO profile) {
        this.profile = profile;
    }
}
