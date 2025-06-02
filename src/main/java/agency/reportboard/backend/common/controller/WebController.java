package agency.reportboard.backend.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class WebController {
    @GetMapping({"/dashboard", "/worklog-dashboard", "/index", "/login"})
    public String forwardToHtml(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.equals("/dashboard")) return "forward:/worklog-dashboard.html";
        if (path.equals("/worklog-dashboard")) return "forward:/worklog-dashboard.html";
        if (path.equals("/index") || path.equals("/login")) return "forward:/index.html";
        return "forward:/index.html";
    }
}
