package com.taskflow.service;

import com.taskflow.model.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.util.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * User management service
 * 
 * Contains authentication logic that should be in a separate AuthService
 * Contains notification preferences that should be in NotificationService
 * Contains team management that should be in TeamService
 * 
 * "It's fine, we'll refactor later" - everyone, 2019-2022
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create user
     * SECURITY: No password hashing
     */
    public User createUser(String username, String email, String password) {
        // Check for existing user - race condition possible
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }
        
        // No email format validation
        // No password strength validation
        // No username format validation
        
        User user = new User(username, email, password); // plaintext password!
        return userRepository.save(user);
    }
    
    /**
     * Authenticate user
     * SECURITY: Plaintext password comparison, no hashing
     * SECURITY: Timing attack vulnerability (String.equals)
     * SECURITY: No rate limiting on login attempts
     */
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            // SECURITY: Different error message reveals whether username exists
            throw new RuntimeException("User not found: " + username);
        }
        
        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }
        
        // SECURITY: Plaintext password comparison!
        if (user.getPassword().equals(password)) {
            // Reset failed attempts
            user.setFailedLoginAttempts(0);
            user.setLastLogin(new Date());
            userRepository.save(user);
            return user;
        } else {
            // Increment failed attempts but never lock the account
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            userRepository.save(user);
            throw new RuntimeException("Invalid password");
        }
    }
    
    /**
     * Get user by ID
     */
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    /**
     * Get all users - NO PAGINATION
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Update user profile
     */
    public User updateUser(Long id, User updatedUser) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("User not found");
        }
        
        // Update fields - no authorization check!
        // Any authenticated user can update any other user's profile
        if (updatedUser.getUsername() != null) {
            existing.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getEmail() != null) {
            existing.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getFirstName() != null) {
            existing.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existing.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getDepartment() != null) {
            existing.setDepartment(updatedUser.getDepartment());
        }
        if (updatedUser.getRole() != null) {
            // SECURITY: Anyone can change their own role to admin!
            existing.setRole(updatedUser.getRole());
        }
        
        return userRepository.save(existing);
    }
    
    /**
     * Delete user - hard delete!
     * FIXME: Should be soft delete
     * FIXME: Doesn't reassign tasks
     * FIXME: Doesn't check if user has active tasks
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        // Tasks assigned to this user now reference a non-existent user
        System.out.println("AUDIT: User deleted - id=" + id);
    }
    
    /**
     * Password reset - insecure implementation
     * SECURITY: Token never expires
     * SECURITY: Token is predictable (timestamp-based)
     */
    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            // SECURITY: Reveals whether email is registered
            throw new RuntimeException("No account found with email: " + email);
        }
        
        // Generate "token" - just a timestamp. Predictable and guessable.
        String token = "reset-" + System.currentTimeMillis();
        user.setResetToken(token);
        userRepository.save(user);
        
        // "Send" email
        System.out.println("PASSWORD RESET: Token for " + email + " is " + token);
        // TODO: Actually send email
        
        return token; // SECURITY: Returning token in response!
    }
    
    /**
     * Reset password with token
     */
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token);
        if (user == null) {
            throw new RuntimeException("Invalid or expired token");
        }
        
        // No token expiry check!
        // No password strength check!
        
        user.setPassword(newPassword); // Still plaintext!
        user.setResetToken(null);
        userRepository.save(user);
    }
    
    /**
     * Search users
     */
    public List<User> searchUsers(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return new ArrayList<>();
        }
        return userRepository.searchUsers(keyword);
    }
    
    /**
     * Get user display name - DUPLICATE of User.getDisplayName()
     * This version exists because "sometimes User.getDisplayName() returns wrong result"
     */
    public String getUserDisplayName(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "Unknown User";
        
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFull_name() != null) {
            return user.getFull_name();
        } else {
            return user.getUsername();
        }
    }
    
    /**
     * Get users by role with active filter
     * DUPLICATE logic with repository method
     */
    public List<User> getActiveUsersByRole(String role) {
        List<User> users = userRepository.findByRole(role);
        // Manual filtering because the query doesn't filter by active
        List<User> activeUsers = new ArrayList<>();
        for (User user : users) {
            if (user.isActive() && !user.isDeleted()) {
                activeUsers.add(user);
            }
        }
        return activeUsers;
    }
    
    /**
     * Bulk deactivate users - used for "offboarding"
     * No notification, no task reassignment
     */
    public int deactivateUsers(List<Long> userIds) {
        int count = 0;
        for (Long id : userIds) {
            try {
                User user = userRepository.findById(id).orElse(null);
                if (user != null) {
                    user.setActive(false);
                    userRepository.save(user);
                    count++;
                }
            } catch (Exception e) {
                System.err.println("Failed to deactivate user " + id + ": " + e.getMessage());
            }
        }
        return count;
    }
}
