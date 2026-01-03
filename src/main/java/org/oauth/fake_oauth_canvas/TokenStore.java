package org.oauth.fake_oauth_canvas;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenStore {

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
        
        // Get access tokens directly from environment variables (REQUIRED)
        String instructorAccessToken = env.getProperty("OAUTH_INSTRUCTOR_ACCESS_TOKEN");
        String studentAccessToken = env.getProperty("OAUTH_STUDENT_ACCESS_TOKEN");
        
        // Validate required tokens - MUST be set via environment variables
        if (instructorAccessToken == null || instructorAccessToken.isEmpty()) {
            throw new IllegalStateException(
                "OAUTH_INSTRUCTOR_ACCESS_TOKEN environment variable is REQUIRED. " +
                "Please set it in Railway's environment variables."
            );
        }
        if (studentAccessToken == null || studentAccessToken.isEmpty()) {
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

        // Student
        JSONObject student = new JSONObject();
        student.put("id", studentId);
        student.put("name", studentName);
        userDetails.put(STUDENT_ROLE, student);
        accessTokens.put(STUDENT_ROLE, studentAccessToken);
        refreshTokens.put(STUDENT_ROLE, studentRefreshToken.isEmpty() ? "refresh-token-student-fghij" : studentRefreshToken);
    }

    public String generateCode(String role) {
        String code = UUID.randomUUID().toString();
        codes.put(code, role);
        return code;
    }

    public String getRoleForCode(String code) {
        return codes.get(code);
    }

    public JSONObject getUserDetails(String role) {
        return userDetails.get(role);
    }

    public String getAccessToken(String role) {
        return accessTokens.get(role);
    }

    public String getRefreshToken(String role) {
        return refreshTokens.get(role);
    }

    /**
     * Gets the role associated with an access token
     * Used for token validation
     */
    public String getRoleForAccessToken(String accessToken) {
        if (accessToken == null) {
            return null;
        }
        
        // Check instructor token
        if (accessToken.equals(accessTokens.get(INSTRUCTOR_ROLE))) {
            return INSTRUCTOR_ROLE;
        }
        
        // Check student token
        if (accessToken.equals(accessTokens.get(STUDENT_ROLE))) {
            return STUDENT_ROLE;
        }
        
        return null;
    }
}
