package org.oauth.fake_oauth_canvas;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;
import jakarta.servlet.http.HttpSession;

import java.net.URI;
import java.util.UUID;

@RestController
public class AuthController {

    private final TokenStore tokenStore;

    public AuthController(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @GetMapping("/login/oauth2/auth")
    public ResponseEntity<Void> authorize(
            @RequestParam("client_id") String clientId,
            @RequestParam("response_type") String responseType,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "purpose", required = false) String purpose,
            @RequestParam(value = "force_login", required = false) String forceLogin,
            @RequestParam(value = "unique_id", required = false) String uniqueId,
            @RequestParam(value = "prompt", required = false) String prompt,
            HttpSession session) {

        if (!"code".equals(responseType)) {
            return ResponseEntity.badRequest().build();
        }

        session.setAttribute("client_id", clientId);
        session.setAttribute("redirect_uri", redirectUri);
        session.setAttribute("state", state);
        session.setAttribute("scope", scope);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/login/oauth2/select-role"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping("/login/oauth2/select-role")
    public ResponseEntity<Void> selectRole(@RequestParam("role") String role, HttpSession session) {
        String redirectUri = (String) session.getAttribute("redirect_uri");
        String state = (String) session.getAttribute("state");

        String code = tokenStore.generateCode(role);
        String location = redirectUri + "?code=" + code + "&state=" + state;

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(location));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping("/login/oauth2/token")
    public ResponseEntity<String> token(
        @RequestParam("grant_type") String grantType,
        @RequestParam(value = "client_id", required = false) String clientId,
        @RequestParam(value = "client_secret", required = false) String clientSecret,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri,
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "refresh_token", required = false) String refreshToken,
        @RequestParam(value = "client_assertion_type", required = false) String clientAssertionType,
        @RequestParam(value = "client_assertion", required = false) String clientAssertion,
        @RequestParam(value = "scope", required = false) String scope) {

    if ("authorization_code".equals(grantType)) {
        String role = tokenStore.getRoleForCode(code);
        if (role == null) {
            return ResponseEntity.badRequest().body("Invalid code");
        }

        JSONObject response = new JSONObject();
        response.put("access_token", tokenStore.getAccessToken(role));
        response.put("token_type", "Bearer");
        response.put("user", tokenStore.getUserDetails(role));
        response.put("refresh_token", tokenStore.getRefreshToken(role));
        response.put("expires_in", 3600);
        response.put("canvas_region", "us-east-1");
        return ResponseEntity.ok(response.toString());
    } else if ("refresh_token".equals(grantType)) {
        // This part needs a way to map refresh_token back to a role.
        // For now, let's assume instructor for demonstration.
        // A real implementation would store a mapping.
        String role = TokenStore.INSTRUCTOR_ROLE; 
        JSONObject response = new JSONObject();
        response.put("access_token", tokenStore.getAccessToken(role));
        response.put("token_type", "Bearer");
        response.put("user", tokenStore.getUserDetails(role));
        response.put("expires_in", 3600);
        return ResponseEntity.ok(response.toString());
    } else if ("client_credentials".equals(grantType)) {
        JSONObject response = new JSONObject();
        response.put("access_token", UUID.randomUUID().toString());
        response.put("token_type", "Bearer");
        response.put("expires_in", 3600);
        response.put("scope", scope);
        return ResponseEntity.ok(response.toString());
    }

    return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/login/oauth2/token")
    public ResponseEntity<String> logout(
            @RequestParam(value = "expire_sessions", required = false) String expireSessions) {
        if ("1".equals(expireSessions)) {
            JSONObject response = new JSONObject();
            response.put("forward_url", "https://idp.school.edu/opaque_url");
            return ResponseEntity.ok(response.toString());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/session_token")
    public ResponseEntity<String> sessionToken(
            @RequestParam(value = "return_to", required = false) String returnTo) {
        JSONObject response = new JSONObject();
        response.put("session_url", "https://canvas.instructure.com/opaque_url");
        return ResponseEntity.ok(response.toString());
    }

    /**
     * Validates Canvas access token and returns user information
     * This endpoint is called by the VT-AI-Teaching-Assistant-Backend to validate OAuth tokens
     */
    @GetMapping("/api/v1/users/self")
    public ResponseEntity<String> getUserInfo(@RequestParam(value = "access_token", required = false) String accessToken) {
        // Check if token is valid by looking it up in our token store
        String role = tokenStore.getRoleForAccessToken(accessToken);
        
        if (role == null) {
            // Check header for Bearer token
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid access token");
        }

        JSONObject userDetails = tokenStore.getUserDetails(role);
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid access token");
        }

        // Add email based on role (matching the existing system)
        if (TokenStore.INSTRUCTOR_ROLE.equals(role)) {
            userDetails.put("email", "kashyapk@vt.edu");
            userDetails.put("login_id", "kashyapk@vt.edu");
        } else if (TokenStore.STUDENT_ROLE.equals(role)) {
            userDetails.put("email", "sarthakr@vt.edu");
            userDetails.put("login_id", "sarthakr@vt.edu");
        }

        return ResponseEntity.ok(userDetails.toString());
    }
}
