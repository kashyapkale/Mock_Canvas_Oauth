package org.oauth.fake_oauth_canvas;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login/oauth2/select-role")
    public String selectRole() {
        return "login-selection";
    }
}
