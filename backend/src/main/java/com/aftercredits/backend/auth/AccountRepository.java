package com.aftercredits.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    Optional<Account> findByDisplayName(String displayName);

    Optional<Account> findByDisplayNameIgnoreCase(String displayName);

    boolean existsByEmail(String email);

    Optional<Account> findByProviderAndProviderId(Account.Provider provider, String providerId);
}
