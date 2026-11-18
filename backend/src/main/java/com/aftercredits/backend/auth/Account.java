package com.aftercredits.backend.auth;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_accounts_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_accounts_provider", columnNames = {"provider", "providerId"})
    }
)
public class Account {

    public enum Provider { LOCAL, GOOGLE, FACEBOOK, INSTAGRAM, GUEST }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = true, length = 60)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider = Provider.LOCAL;

    @Column(nullable = true, length = 255)
    private String providerId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Account() {}

    public Account(String email, String displayName, String passwordHash) {
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.provider = Provider.LOCAL;
    }

    public Account(String displayName, Provider provider, String providerId, String email) {
        this.displayName = displayName;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPasswordHash() { return passwordHash; }
    public Provider getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public Instant getCreatedAt() { return createdAt; }
}
