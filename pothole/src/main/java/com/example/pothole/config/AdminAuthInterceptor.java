package com.example.pothole.config;

import com.example.pothole.Entity.AdminUser;
import com.example.pothole.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminService adminService;

    public AdminAuthInterceptor(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String user = request.getHeader("X-Admin-Username");
        String pass = request.getHeader("X-Admin-Password");
        if (user == null || pass == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing admin credentials");
            return false;
        }
        Optional<AdminUser> au = adminService.findByUsername(user);
        if (au.isPresent() && pass.equals(au.get().getPassword())) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Invalid admin credentials");
        return false;
    }
}
