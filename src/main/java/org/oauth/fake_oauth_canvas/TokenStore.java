package org.oauth.fake_oauth_canvas;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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
            // Instructor configuration
            @Value("${oauth.instructor.access-token:}") String instructorAccessToken,
            @Value("${oauth.instructor.refresh-token:}") String instructorRefreshToken,
            @Value("${oauth.instructor.id:101}") int instructorId,
            @Value("${oauth.instructor.name:Kashyap Kale}") String instructorName,
            // Student configuration
            @Value("${oauth.student.access-token:}") String studentAccessToken,
            @Value("${oauth.student.refresh-token:}") String studentRefreshToken,
            @Value("${oauth.student.id:202}") int studentId,
            @Value("${oauth.student.name:Sarthak Raut}") String studentName) {
        
        // Validate required tokens
        if (instructorAccessToken == null || instructorAccessToken.isEmpty()) {
            throw new IllegalStateException("oauth.instructor.access-token is required in application.properties");
        }
        if (studentAccessToken == null || studentAccessToken.isEmpty()) {
            throw new IllegalStateException("oauth.student.access-token is required in application.properties");
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
