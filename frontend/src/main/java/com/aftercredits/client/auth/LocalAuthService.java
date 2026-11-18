package com.aftercredits.client.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Robust local authentication and account persistence service.
 * Performs real cryptographic password hashing (SHA-256 + salt) and verifies
 * credentials against persisted accounts.
 */
public class LocalAuthService {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final LocalAuthService INSTANCE = new LocalAuthService();

    private final File storageFile;
    private final List<StoredAccount> accounts = new ArrayList<>();

    public static LocalAuthService getInstance() {
        return INSTANCE;
    }

    private LocalAuthService() {
        File dir = new File(System.getProperty("user.dir"), ".aftercredits");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.canWrite()) {
            dir = new File(System.getProperty("java.io.tmpdir"), ".aftercredits");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        this.storageFile = new File(dir, "accounts.json");
        loadAccounts();
    }

    private synchronized void loadAccounts() {
        accounts.clear();
        if (storageFile.exists() && storageFile.length() > 0) {
            try (Reader reader = new FileReader(storageFile, StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<List<StoredAccount>>() {}.getType();
                List<StoredAccount> loaded = GSON.fromJson(reader, listType);
                if (loaded != null) {
                    accounts.addAll(loaded);
                }
            } catch (Exception e) {
                System.err.println("[AfterCredits] Could not read accounts file: " + e.getMessage());
            }
        }

        // Seed default accounts if empty
        if (accounts.isEmpty()) {
            seedDefaultAccounts();
            saveAccounts();
        }
    }

    private synchronized void saveAccounts() {
        try (Writer writer = new FileWriter(storageFile, StandardCharsets.UTF_8)) {
            GSON.toJson(accounts, writer);
        } catch (Exception e) {
            System.err.println("[AfterCredits] Could not persist accounts: " + e.getMessage());
        }
    }

    private void seedDefaultAccounts() {
        // Default verified accounts:
        // admin / Password123
        // demo / Password123
        // cinephile / Password123
        createAccountInternal("admin", "admin@aftercredits.com", "Password123", "LOCAL", null);
        createAccountInternal("demo", "demo@aftercredits.com", "Password123", "LOCAL", null);
        createAccountInternal("cinephile", "cinephile@aftercredits.com", "Password123", "LOCAL", null);
    }

    private StoredAccount createAccountInternal(String username, String email, String password, String provider, String providerId) {
        String salt = generateSalt();
        String hash = (password != null) ? hashPassword(password, salt) : null;
        long id = System.currentTimeMillis() + accounts.size();
        StoredAccount acc = new StoredAccount(id, username, email, hash, salt, provider, providerId, System.currentTimeMillis());
        accounts.add(acc);
        return acc;
    }

    /**
     * Registers a new account with username, email, and password.
     *
     * @throws AuthException if email or username is already taken or invalid.
     */
    public synchronized AuthApiClient.AuthResult register(String username, String email, String password) throws AuthException {
        if (username == null || username.trim().length() < 2) {
            throw new AuthException("Username must be at least 2 characters long.");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new AuthException("Please enter a valid email address.");
        }
        if (password == null || password.length() < 8) {
            throw new AuthException("Password must be at least 8 characters long.");
        }

        String cleanUsername = username.trim();
        String cleanEmail = email.trim().toLowerCase();

        // Check uniqueness
        for (StoredAccount acc : accounts) {
            if (acc.email != null && acc.email.equalsIgnoreCase(cleanEmail)) {
                throw new AuthException("An account with email \"" + cleanEmail + "\" already exists.");
            }
            if (acc.username != null && acc.username.equalsIgnoreCase(cleanUsername)) {
                throw new AuthException("Username \"" + cleanUsername + "\" is already taken.");
            }
        }

        StoredAccount created = createAccountInternal(cleanUsername, cleanEmail, password, "LOCAL", null);
        saveAccounts();

        String token = "local-token-" + created.id + "-" + System.currentTimeMillis();
        return new AuthApiClient.AuthResult(created.id, created.email, created.username, token);
    }

    /**
     * Authenticates a user by email or username, and verifies their password.
     *
     * @param identifier Username or email
     * @param password   Raw entered password
     * @return AuthResult if credentials match
     * @throws AuthException if user does not exist or password is wrong
     */
    public synchronized AuthApiClient.AuthResult login(String identifier, String password) throws AuthException {
        if (identifier == null || identifier.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthException("Please enter your email/username and password.");
        }

        String cleanIdentifier = identifier.trim();

        // Find account matching either email or username
        Optional<StoredAccount> matchingAccount = accounts.stream()
                .filter(a -> (a.email != null && a.email.equalsIgnoreCase(cleanIdentifier))
                          || (a.username != null && a.username.equalsIgnoreCase(cleanIdentifier)))
                .findFirst();

        if (matchingAccount.isEmpty()) {
            throw new AuthException("Account not found for \"" + cleanIdentifier + "\". Please sign up first.");
        }

        StoredAccount acc = matchingAccount.get();

        if (acc.passwordHash == null || acc.salt == null) {
            throw new AuthException("This account uses social sign-in (" + acc.provider + "). Please sign in using " + acc.provider + ".");
        }

        // Verify password
        String computedHash = hashPassword(password, acc.salt);
        if (!computedHash.equals(acc.passwordHash)) {
            throw new AuthException("Incorrect password. Please try again.");
        }

        String token = "local-token-" + acc.id + "-" + System.currentTimeMillis();
        return new AuthApiClient.AuthResult(acc.id, acc.email, acc.username, token);
    }

    /**
     * Authenticates or creates an account using a social provider (Google, Facebook, Instagram).
     */
    public synchronized AuthApiClient.AuthResult oauthLogin(String provider, String providerId, String displayName, String email) {
        String cleanProvider = (provider != null) ? provider.toUpperCase() : "LOCAL";
        String cleanId = (providerId != null) ? providerId.trim() : "user-" + System.currentTimeMillis();
        String cleanName = (displayName != null && !displayName.isBlank()) ? displayName.trim() : "Social User";
        String cleanEmail = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : null;

        Optional<StoredAccount> existing = accounts.stream()
                .filter(a -> cleanProvider.equalsIgnoreCase(a.provider) && cleanId.equalsIgnoreCase(a.providerId))
                .findFirst();

        StoredAccount acc;
        if (existing.isPresent()) {
            acc = existing.get();
        } else {
            acc = createAccountInternal(cleanName, cleanEmail, null, cleanProvider, cleanId);
            saveAccounts();
        }

        String token = "oauth-token-" + cleanProvider + "-" + acc.id;
        return new AuthApiClient.AuthResult(acc.id, acc.email, acc.username, token);
    }

    /**
     * Authenticates or creates a Guest account.
     */
    public synchronized AuthApiClient.AuthResult guestLogin(String guestId, String displayName) {
        String finalGuestId = (guestId != null) ? guestId : SessionManager.generateGuestId();
        String name = (displayName != null) ? displayName : "Guest";

        Optional<StoredAccount> existing = accounts.stream()
                .filter(a -> "GUEST".equalsIgnoreCase(a.provider) && finalGuestId.equalsIgnoreCase(a.providerId))
                .findFirst();

        StoredAccount acc;
        if (existing.isPresent()) {
            acc = existing.get();
        } else {
            acc = createAccountInternal(name, null, null, "GUEST", finalGuestId);
            saveAccounts();
        }

        String token = "guest-token-" + finalGuestId;
        return new AuthApiClient.AuthResult(acc.id, null, acc.username, token);
    }

    // ── Cryptographic Hashing Helpers ─────────────────────────────────

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm missing from JVM", e);
        }
    }

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // ── Data Class ───────────────────────────────────────────────────

    public static class StoredAccount {
        public long id;
        public String username;
        public String email;
        public String passwordHash;
        public String salt;
        public String provider;
        public String providerId;
        public long createdAt;

        public StoredAccount() {}

        public StoredAccount(long id, String username, String email, String passwordHash,
                             String salt, String provider, String providerId, long createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.passwordHash = passwordHash;
            this.salt = salt;
            this.provider = provider;
            this.providerId = providerId;
            this.createdAt = createdAt;
        }
    }

    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}
