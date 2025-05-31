package agency.reportboard.backend.common.controller;

import agency.reportboard.backend.common.dto.ApiResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ApiResponse<Object>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String message = (String) request.getAttribute("jakarta.servlet.error.message");
        
        if (statusCode == null) {
            statusCode = 500;
        }
        
        if (message == null || message.isEmpty()) {
            message = getDefaultMessage(statusCode);
        }
        
        return ResponseEntity.status(statusCode)
                .body(ApiResponse.error(message));
    }
    
    private String getDefaultMessage(int statusCode) {
        return switch (statusCode) {
            case 401 -> "Not authenticated";
            case 403 -> "Access denied";
            case 404 -> "Not found";
            case 500 -> "Internal server error";
            default -> "Error occurred";
        };
    }
}
