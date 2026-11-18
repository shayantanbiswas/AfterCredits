package com.aftercredits.backend.auth;

import com.aftercredits.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        AccountRepository accountRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (accountRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        Account account = new Account(
            email,
            request.displayName().trim(),
            passwordEncoder.encode(request.password())
        );

        Account savedAccount = accountRepository.save(account);
        String token = jwtService.generateToken(savedAccount.getId(), savedAccount.getEmail());

        return AuthResponse.from(savedAccount, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.email().trim();

        Account account = accountRepository.findByEmail(identifier.toLowerCase(Locale.ROOT))
            .or(() -> accountRepository.findByDisplayNameIgnoreCase(identifier))
            .orElseThrow(InvalidCredentialsException::new);

        if (account.getProvider() != Account.Provider.LOCAL) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String subject = account.getEmail() != null ? account.getEmail() : account.getDisplayName();
        String token = jwtService.generateToken(account.getId(), subject);
        return AuthResponse.from(account, token);
    }

    @Transactional
    public AuthResponse oauthLogin(OAuthLoginRequest request) {
        Account.Provider provider = Account.Provider.valueOf(request.provider().toUpperCase());

        Account account = accountRepository
            .findByProviderAndProviderId(provider, request.providerId())
            .orElseGet(() -> {
                Account newAccount = new Account(
                    request.displayName().trim(),
                    provider,
                    request.providerId(),
                    request.email() != null ? normalizeEmail(request.email()) : null
                );
                return accountRepository.save(newAccount);
            });

        String subject = account.getEmail() != null
            ? account.getEmail()
            : "oauth:" + provider + ":" + request.providerId();
        String token = jwtService.generateToken(account.getId(), subject);
        return AuthResponse.from(account, token);
    }

    @Transactional
    public AuthResponse guestLogin(GuestLoginRequest request) {
        Account account = accountRepository
            .findByProviderAndProviderId(Account.Provider.GUEST, request.guestId())
            .orElseGet(() -> {
                Account newAccount = new Account(
                    request.displayName(),
                    Account.Provider.GUEST,
                    request.guestId(),
                    null
                );
                return accountRepository.save(newAccount);
            });

        String token = jwtService.generateToken(account.getId(), "guest:" + request.guestId());
        return AuthResponse.from(account, token);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public CurrentAccountResponse getCurrentAccount(String email) {
        Account account = accountRepository.findByEmail(normalizeEmail(email))
            .orElseThrow(InvalidCredentialsException::new);
        return CurrentAccountResponse.from(account);
    }
}
