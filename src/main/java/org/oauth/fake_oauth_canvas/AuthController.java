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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;

@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final TokenStore tokenStore;

    public AuthController(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
        logger.info("======================================");
        logger.info("AuthController initialized");
        logger.info("======================================");
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

        logger.info("======================================");
        logger.info("[AUTHORIZE] GET /login/oauth2/auth");
        logger.info("[AUTHORIZE] Session ID: {}", session.getId());
        logger.info("[AUTHORIZE] client_id: {}", clientId);
        logger.info("[AUTHORIZE] response_type: {}", responseType);
        logger.info("[AUTHORIZE] redirect_uri: {}", redirectUri);
        logger.info("[AUTHORIZE] state: {}", state);
        logger.info("[AUTHORIZE] scope: {}", scope);
        logger.info("======================================");

        if (!"code".equals(responseType)) {
            logger.error("[AUTHORIZE] Invalid response_type: {}. Expected 'code'", responseType);
            return ResponseEntity.badRequest().build();
        }

        session.setAttribute("client_id", clientId);
        session.setAttribute("redirect_uri", redirectUri);
        session.setAttribute("state", state);
        session.setAttribute("scope", scope);

        logger.info("[AUTHORIZE] Stored in session - client_id: {}, redirect_uri: {}, state: {}",
            clientId, redirectUri, state);
        logger.info("[AUTHORIZE] Redirecting to /login/oauth2/select-role");

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/login/oauth2/select-role"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping("/login/oauth2/select-role")
    public ResponseEntity<Void> selectRole(@RequestParam("role") String role, HttpSession session) {
        logger.info("======================================");
        logger.info("[SELECT-ROLE] POST /login/oauth2/select-role");
        logger.info("[SELECT-ROLE] Session ID: {}", session.getId());
        logger.info("[SELECT-ROLE] Selected role: {}", role);

        String redirectUri = (String) session.getAttribute("redirect_uri");
        String state = (String) session.getAttribute("state");
        String clientId = (String) session.getAttribute("client_id");

        logger.info("[SELECT-ROLE] Retrieved from session - redirect_uri: {}", redirectUri);
        logger.info("[SELECT-ROLE] Retrieved from session - state: {}", state);
        logger.info("[SELECT-ROLE] Retrieved from session - client_id: {}", clientId);

        if (redirectUri == null) {
            logger.error("[SELECT-ROLE] ERROR: redirect_uri is NULL in session!");
            logger.error("[SELECT-ROLE] Session attributes: client_id={}, state={}", clientId, state);
            return ResponseEntity.badRequest().build();
        }

        String code = tokenStore.generateCode(role);
        logger.info("[SELECT-ROLE] Generated authorization code: {}", code);
        logger.info("[SELECT-ROLE] Code mapped to role: {}", role);

        String location = redirectUri + "?code=" + code + "&state=" + state;
        logger.info("[SELECT-ROLE] Redirecting to: {}", location);
        logger.info("======================================");

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

        logger.info("======================================");
        logger.info("[TOKEN] POST /login/oauth2/token");
        logger.info("[TOKEN] grant_type: {}", grantType);
        logger.info("[TOKEN] client_id: {}", clientId);
        logger.info("[TOKEN] client_secret: {}", clientSecret != null ? "[REDACTED - length=" + clientSecret.length() + "]" : "null");
        logger.info("[TOKEN] redirect_uri: {}", redirectUri);
        logger.info("[TOKEN] code: {}", code);
        logger.info("[TOKEN] refresh_token: {}", refreshToken != null ? "[REDACTED]" : "null");
        logger.info("======================================");

        if ("authorization_code".equals(grantType)) {
            logger.info("[TOKEN] Processing authorization_code grant");
            logger.info("[TOKEN] Looking up code in TokenStore: {}", code);

            String role = tokenStore.getRoleForCode(code);
            logger.info("[TOKEN] TokenStore returned role: {}", role);

            if (role == null) {
                logger.error("======================================");
                logger.error("[TOKEN] ERROR: Invalid code - not found in TokenStore!");
                logger.error("[TOKEN] Code attempted: {}", code);
                logger.error("[TOKEN] Current codes in store: {}", tokenStore.getAllCodes());
                logger.error("======================================");
                return ResponseEntity.badRequest().body("Invalid code");
            }

            logger.info("[TOKEN] Code is valid! Role: {}", role);

            JSONObject response = new JSONObject();
            String accessToken = tokenStore.getAccessToken(role);
            response.put("access_token", accessToken);
            response.put("token_type", "Bearer");
            response.put("user", tokenStore.getUserDetails(role));
            response.put("refresh_token", tokenStore.getRefreshToken(role));
            response.put("expires_in", 3600);
            response.put("canvas_region", "us-east-1");

            logger.info("[TOKEN] SUCCESS! Returning tokens for role: {}", role);
            logger.info("[TOKEN] Access token (first 20 chars): {}...",
                accessToken != null && accessToken.length() > 20 ? accessToken.substring(0, 20) : accessToken);
            logger.info("======================================");

            return ResponseEntity.ok(response.toString());

        } else if ("refresh_token".equals(grantType)) {
            logger.info("[TOKEN] Processing refresh_token grant");
            String role = TokenStore.INSTRUCTOR_ROLE;
            JSONObject response = new JSONObject();
            response.put("access_token", tokenStore.getAccessToken(role));
            response.put("token_type", "Bearer");
            response.put("user", tokenStore.getUserDetails(role));
            response.put("expires_in", 3600);
            logger.info("[TOKEN] SUCCESS! Returning refreshed tokens");
            return ResponseEntity.ok(response.toString());

        } else if ("client_credentials".equals(grantType)) {
            logger.info("[TOKEN] Processing client_credentials grant");
            JSONObject response = new JSONObject();
            response.put("access_token", UUID.randomUUID().toString());
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);
            response.put("scope", scope);
            return ResponseEntity.ok(response.toString());
        }

        logger.error("[TOKEN] ERROR: Unsupported grant_type: {}", grantType);
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/login/oauth2/token")
    public ResponseEntity<String> logout(
            @RequestParam(value = "expire_sessions", required = false) String expireSessions) {
        logger.info("[LOGOUT] DELETE /login/oauth2/token, expire_sessions={}", expireSessions);
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
        logger.info("[SESSION-TOKEN] GET /login/session_token, return_to={}", returnTo);
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
        logger.info("======================================");
        logger.info("[USER-INFO] GET /api/v1/users/self");
        logger.info("[USER-INFO] access_token: {}", accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : "null");

        // Check if token is valid by looking it up in our token store
        String role = tokenStore.getRoleForAccessToken(accessToken);
        logger.info("[USER-INFO] Token lookup returned role: {}", role);

        if (role == null) {
            logger.error("[USER-INFO] ERROR: Invalid access token - not found in TokenStore");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid access token");
        }

        JSONObject userDetails = tokenStore.getUserDetails(role);
        if (userDetails == null) {
            logger.error("[USER-INFO] ERROR: No user details found for role: {}", role);
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

        logger.info("[USER-INFO] SUCCESS! Returning user details for role: {}", role);
        logger.info("======================================");
        return ResponseEntity.ok(userDetails.toString());
    }
}
