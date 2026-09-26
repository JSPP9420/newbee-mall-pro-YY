package ltd.newbee.mall.security;

import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

public final class CsrfTokenService {

    public static final String SESSION_ATTRIBUTE = CsrfTokenService.class.getName() + ".TOKEN";
    public static final String REQUEST_ATTRIBUTE = "_csrfToken";
    public static final String REQUEST_PARAMETER = "_csrf";
    public static final String REQUEST_HEADER = "X-CSRF-TOKEN";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CsrfTokenService() {
    }

    public static String getOrCreateToken(HttpSession session) {
        Object existingToken = session.getAttribute(SESSION_ATTRIBUTE);
        if (existingToken instanceof String) {
            return (String) existingToken;
        }
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        session.setAttribute(SESSION_ATTRIBUTE, token);
        return token;
    }
}
