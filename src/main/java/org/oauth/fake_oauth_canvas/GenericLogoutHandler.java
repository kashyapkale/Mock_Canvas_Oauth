package org.oauth.fake_oauth_canvas;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class GenericLogoutHandler implements LogoutSuccessHandler {

    private final String oidcLogoutUrl = "YOUR_OIDC_LOGOUT_URL"; 

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            String idToken = ((OAuth2AuthenticationToken) authentication).getPrincipal().getAttribute("id_token");
            String logoutUrl = oidcLogoutUrl + "?id_token_hint=" + idToken + "&post_logout_redirect_uri=YOUR_POST_LOGOUT_REDIRECT_URI";
            response.sendRedirect(logoutUrl);
        } else {
            response.sendRedirect("/");
        }
    }
}
