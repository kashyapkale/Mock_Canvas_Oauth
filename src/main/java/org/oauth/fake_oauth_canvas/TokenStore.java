package org.oauth.fake_oauth_canvas;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenStore {

    private static final Logger logger = LoggerFactory.getLogger(TokenStore.class);

    public static final String INSTRUCTOR_ROLE = "instructor";
    public static final String STUDENT_ROLE = "student";

    private final Map<String, String> codes = new ConcurrentHashMap<>();
    private final Map<String, JSONObject> userDetails = new ConcurrentHashMap<>();
    private final Map<String, String> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, String> refreshTokens = new ConcurrentHashMap<>();

    public TokenStore(
            Environment env,
            // Instructor configuration
            @Value("${oauth.instructor.refresh-token:refresh-token-instructor-67890}") String instructorRefreshToken,
            @Value("${oauth.instructor.id:101}") int instructorId,
            @Value("${oauth.instructor.name:Kashyap Kale}") String instructorName,
            // Student configuration
            @Value("${oauth.student.refresh-token:refresh-token-student-fghij}") String studentRefreshToken,
            @Value("${oauth.student.id:202}") int studentId,
            @Value("${oauth.student.name:Sarthak Raut}") String studentName) {

        logger.info("======================================");
        logger.info("[TokenStore] Initializing TokenStore...");
        logger.info("[TokenStore] Instance hash: {}", System.identityHashCode(this));
        logger.info("======================================");

        // Get access tokens directly from environment variables (REQUIRED)
        String instructorAccessToken = env.getProperty("OAUTH_INSTRUCTOR_ACCESS_TOKEN");
        String studentAccessToken = env.getProperty("OAUTH_STUDENT_ACCESS_TOKEN");

        logger.info("[TokenStore] OAUTH_INSTRUCTOR_ACCESS_TOKEN: {}",
            instructorAccessToken != null ? instructorAccessToken.substring(0, Math.min(20, instructorAccessToken.length())) + "..." : "NULL");
        logger.info("[TokenStore] OAUTH_STUDENT_ACCESS_TOKEN: {}",
            studentAccessToken != null ? studentAccessToken.substring(0, Math.min(20, studentAccessToken.length())) + "..." : "NULL");

        // Validate required tokens - MUST be set via environment variables
        if (instructorAccessToken == null || instructorAccessToken.isEmpty()) {
            logger.error("[TokenStore] ERROR: OAUTH_INSTRUCTOR_ACCESS_TOKEN is not set!");
            throw new IllegalStateException(
                "OAUTH_INSTRUCTOR_ACCESS_TOKEN environment variable is REQUIRED. " +
                "Please set it in Railway's environment variables."
            );
        }
        if (studentAccessToken == null || studentAccessToken.isEmpty()) {
            logger.error("[TokenStore] ERROR: OAUTH_STUDENT_ACCESS_TOKEN is not set!");
            throw new IllegalStateException(
                "OAUTH_STUDENT_ACCESS_TOKEN environment variable is REQUIRED. " +
                "Please set it in Railway's environment variables."
            );
        }

        // Instructor
        JSONObject instructor = new JSONObject();
        instructor.put("id", instructorId);
        instructor.put("name", instructorName);
        userDetails.put(INSTRUCTOR_ROLE, instructor);
        accessTokens.put(INSTRUCTOR_ROLE, instructorAccessToken);
        refreshTokens.put(INSTRUCTOR_ROLE, instructorRefreshToken.isEmpty() ? "refresh-token-instructor-67890" : instructorRefreshToken);

        logger.info("[TokenStore] Instructor configured - id: {}, name: {}", instructorId, instructorName);

        // Student
        JSONObject student = new JSONObject();
        student.put("id", studentId);
        student.put("name", studentName);
        userDetails.put(STUDENT_ROLE, student);
        accessTokens.put(STUDENT_ROLE, studentAccessToken);
        refreshTokens.put(STUDENT_ROLE, studentRefreshToken.isEmpty() ? "refresh-token-student-fghij" : studentRefreshToken);

        logger.info("[TokenStore] Student configured - id: {}, name: {}", studentId, studentName);
        logger.info("[TokenStore] TokenStore initialization complete!");
        logger.info("======================================");
    }

    public String generateCode(String role) {
        String code = UUID.randomUUID().toString();
        codes.put(code, role);

        logger.info("======================================");
        logger.info("[TokenStore] generateCode() called");
        logger.info("[TokenStore] Generated code: {}", code);
        logger.info("[TokenStore] Mapped to role: {}", role);
        logger.info("[TokenStore] Total codes in store: {}", codes.size());
        logger.info("[TokenStore] All codes: {}", codes.keySet());
        logger.info("[TokenStore] Instance hash: {}", System.identityHashCode(this));
        logger.info("======================================");

        return code;
    }

    public String getRoleForCode(String code) {
        logger.info("======================================");
        logger.info("[TokenStore] getRoleForCode() called");
        logger.info("[TokenStore] Looking for code: {}", code);
        logger.info("[TokenStore] Instance hash: {}", System.identityHashCode(this));
        logger.info("[TokenStore] Total codes in store: {}", codes.size());
        logger.info("[TokenStore] All codes in store: {}", codes.keySet());

        String role = codes.get(code);

        if (role != null) {
            logger.info("[TokenStore] FOUND! Code maps to role: {}", role);
        } else {
            logger.error("[TokenStore] NOT FOUND! Code does not exist in store");
            logger.error("[TokenStore] This could mean:");
            logger.error("[TokenStore]   1. The code was never generated (different instance?)");
            logger.error("[TokenStore]   2. The server restarted and lost in-memory codes");
            logger.error("[TokenStore]   3. The code is malformed or truncated");
        }
        logger.info("======================================");

        return role;
    }

    public Set<String> getAllCodes() {
        return codes.keySet();
    }

    public JSONObject getUserDetails(String role) {
        logger.info("[TokenStore] getUserDetails() for role: {}", role);
        return userDetails.get(role);
    }

    public String getAccessToken(String role) {
        String token = accessTokens.get(role);
        logger.info("[TokenStore] getAccessToken() for role: {} -> {}", role,
            token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");
        return token;
    }

    public String getRefreshToken(String role) {
        logger.info("[TokenStore] getRefreshToken() for role: {}", role);
        return refreshTokens.get(role);
    }

    /**
     * Gets the role associated with an access token
     * Used for token validation
     */
    public String getRoleForAccessToken(String accessToken) {
        logger.info("[TokenStore] getRoleForAccessToken() called");

        if (accessToken == null) {
            logger.warn("[TokenStore] Access token is null");
            return null;
        }

        // Check instructor token
        if (accessToken.equals(accessTokens.get(INSTRUCTOR_ROLE))) {
            logger.info("[TokenStore] Token matches INSTRUCTOR");
            return INSTRUCTOR_ROLE;
        }

        // Check student token
        if (accessToken.equals(accessTokens.get(STUDENT_ROLE))) {
            logger.info("[TokenStore] Token matches STUDENT");
            return STUDENT_ROLE;
        }

        logger.warn("[TokenStore] Token does not match any known access token");
        return null;
    }
}
