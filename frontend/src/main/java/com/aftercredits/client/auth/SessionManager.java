package com.aftercredits.client.auth;

import java.util.UUID;
import com.aftercredits.repository.WatchlistManager;

/**
 * Singleton that holds the current user session for AfterCredits.
 * All UI components should read from here to know who is logged in.
 */
public class SessionManager {

    public enum AuthProvider { LOCAL, GOOGLE, FACEBOOK, INSTAGRAM, GUEST }

    public static final class Session {
        private final long userId;
        private final String displayName;
        private final String email;   // may be null for guest
        private final String token;
        private final boolean isGuest;
        private final String guestId; // automatically generated for guest users
        private final AuthProvider provider;

        public Session(long userId, String displayName, String email,
                       String token, boolean isGuest, String guestId, AuthProvider provider) {
            this.userId = userId;
            this.displayName = displayName;
            this.email = email;
            this.token = token;
            this.isGuest = isGuest;
            this.guestId = guestId;
            this.provider = provider;
        }

        public Session(long userId, String displayName, String email,
                       String token, boolean isGuest, AuthProvider provider) {
            this(userId, displayName, email, token, isGuest, isGuest ? generateGuestId() : null, provider);
        }

        public long getUserId()        { return userId; }
        public String getDisplayName() { return displayName; }
        public String getEmail()       { return email; }
        public String getToken()       { return token; }
        public boolean isGuest()       { return isGuest; }
        public String getGuestId()     { return guestId; }
        public AuthProvider getProvider() { return provider; }

        public String getFormattedDisplayName() {
            if (isGuest && guestId != null) {
                return displayName + " (" + guestId + ")";
            }
            return displayName;
        }
    }

    private static volatile Session currentSession = null;

    private SessionManager() {}

    public static String generateGuestId() {
        return "GUEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static void setSession(Session session) {
        currentSession = session;

        if (session != null) {
            String storageKey;
            if (session.isGuest() && session.getGuestId() != null) {
                storageKey = "guest-" + session.getGuestId();
            } else if (session.getUserId() > 0) {
                storageKey = "account-" + session.getUserId();
            } else {
                storageKey = "guest-" + generateGuestId();
            }
            WatchlistManager.getInstance().setActiveUserKey(storageKey);
        }
        // Mirror into System properties for legacy code that reads them
        if (session != null) {
            System.setProperty("aftercredits.user.name", session.getDisplayName());
            if (session.getEmail() != null) {
                System.setProperty("aftercredits.user.email", session.getEmail());
            } else {
                System.clearProperty("aftercredits.user.email");
            }
            System.setProperty("aftercredits.auth.token", session.getToken());
            if (session.getGuestId() != null) {
                System.setProperty("aftercredits.user.guest_id", session.getGuestId());
            } else {
                System.clearProperty("aftercredits.user.guest_id");
            }
        } else {
            System.clearProperty("aftercredits.user.name");
            System.clearProperty("aftercredits.user.email");
            System.clearProperty("aftercredits.auth.token");
            System.clearProperty("aftercredits.user.guest_id");
        }
    }

    public static Session getSession() {
        return currentSession;
    }

    public static boolean isLoggedIn() {
        return currentSession != null;
    }

    public static boolean isGuest() {
        return currentSession != null && currentSession.isGuest();
    }

    public static void clearSession() {
        setSession(null);
    }
}
