package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resource Authorization Service
 * Handles resource-level authorization checks
 */
@Slf4j
@Service("resourceAuthService")
public class ResourceAuthorizationService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Check if current user owns the resource
     */
    public boolean isOwner(Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long currentUserId = userPrincipal.getId();
            
            return currentUserId.equals(userId);
        } catch (Exception e) {
            log.error("Error checking resource ownership: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if current user owns the resource and has required role
     */
    public boolean isOwnerWithRole(Long userId, String requiredRole) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long currentUserId = userPrincipal.getId();
            
            // Check ownership
            if (!currentUserId.equals(userId)) {
                return false;
            }

            // Check role
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + requiredRole));
                    
        } catch (Exception e) {
            log.error("Error checking resource ownership with role: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if current user is admin or owns the resource
     */
    public boolean isAdminOrOwner(Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            // Check if user is admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            if (isAdmin) {
                return true;
            }

            // Check ownership
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long currentUserId = userPrincipal.getId();
            
            return currentUserId.equals(userId);
            
        } catch (Exception e) {
            log.error("Error checking admin or owner access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get current authenticated user ID
     */
    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
            
        } catch (Exception e) {
            log.error("Error getting current user ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if current user exists and is enabled
     */
    public boolean isValidUser(Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            return user != null && user.getEnabled();
        } catch (Exception e) {
            log.error("Error checking user validity: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if current user can access student document
     */
    public boolean canAccessStudentDocument(Long userId, Long documentId) {
        // For now, only check ownership - could be extended to check actual document ownership in DB
        return isOwner(userId);
    }

    /**
     * Check if current user can access teacher resource (qualification, experience, etc.)
     */
    public boolean canAccessTeacherResource(Long userId, Long resourceId) {
        // For now, only check ownership - could be extended to check actual resource ownership in DB
        return isOwner(userId);
    }

    /**
     * Check if current user can access student profile
     */
    public boolean canAccessStudentProfile(Long userId) {
        return isOwner(userId);
    }

    /**
     * Check if current user can access teacher profile
     */
    public boolean canAccessTeacherProfile(Long userId) {
        return isOwner(userId);
    }
}
