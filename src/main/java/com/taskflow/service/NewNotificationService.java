package com.taskflow.service;

import com.taskflow.util.ConfigManager;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.util.Properties;

/**
 * Notification service - supposed to handle all notifications
 * Reality: Half-implemented, the other half is scattered across TaskService
 * 
 * FIXME: Has been "in progress" since 2020
 * Last commit: 2021-02-14 "WIP: notification service"
 */
@Service
public class NotificationService {
    
    // FIXME: These should be in config, not hardcoded
    private static final String SMTP_HOST = "smtp.taskflow.local";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USER = "notifications@taskflow.local";
    private static final String SMTP_PASS = "N0t1fy2021!";
    
    // Slack webhook - SECURITY: hardcoded webhook URL
    // FIXME: move to environment variable
    private static final String SLACK_WEBHOOK = System.getenv("SLACK_WEBHOOK_URL") != null 
        ? System.getenv("SLACK_WEBHOOK_URL") 
        : "https://hooks.example.com/services/REPLACE_ME";
    
    private boolean enabled;
    
    public NotificationService() {
        // Check if notifications are enabled
        this.enabled = ConfigManager.getInstance().getBoolean("app.notification.enabled", false);
    }
    
    /**
     * Send email notification
     * FIXME: Never actually works because SMTP is not configured
     */
    public boolean sendEmail(String to, String subject, String body) {
        if (!enabled) {
            System.out.println("Notifications disabled. Would have sent email to: " + to);
            return false;
        }
        
        try {
            // "Implementation" - just logs and returns
            // The actual JavaMail code was deleted because it "caused too many exceptions"
            System.out.println("EMAIL: To=" + to + ", Subject=" + subject);
            System.out.println("EMAIL BODY: " + body);
            
            // Pretend it worked
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Send Slack notification
     * FIXME: URL connection is never closed
     */
    public boolean sendSlackNotification(String channel, String message) {
        if (!enabled) {
            System.out.println("Notifications disabled. Would have sent Slack to: " + channel);
            return false;
        }
        
        try {
            URL url = new URL(SLACK_WEBHOOK);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            
            // Manually building JSON - should use Jackson
            String payload = "{\"channel\":\"" + channel + "\",\"text\":\"" + message + "\"}";
            // SECURITY: No escaping of message content - JSON injection possible
            
            OutputStream os = conn.getOutputStream();
            os.write(payload.getBytes("UTF-8"));
            os.flush();
            // BUG: OutputStream never closed
            
            int responseCode = conn.getResponseCode();
            // Connection never closed
            
            return responseCode == 200;
            
        } catch (Exception e) {
            // Swallow all exceptions
            System.err.println("Slack notification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send notification to user
     * TODO: Determine notification channel per user preference
     */
    public void notifyUser(Long userId, String title, String message) {
        // Just log for now
        System.out.println("NOTIFY USER " + userId + ": [" + title + "] " + message);
        // TODO: Look up user preferences
        // TODO: Send via preferred channel (email, slack, in-app)
        // FIXME: This has been a TODO for 2 years
    }
    
    /**
     * Send batch notifications
     * PERFORMANCE: Sends one by one, no batching
     */
    public void notifyUsers(java.util.List<Long> userIds, String title, String message) {
        for (Long userId : userIds) {
            try {
                notifyUser(userId, title, message);
            } catch (Exception e) {
                // Continue on error
                System.err.println("Failed to notify user " + userId);
            }
        }
    }
}
