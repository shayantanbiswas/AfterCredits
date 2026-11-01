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
        String token = jwtService.generateToken(
            savedAccount.getId(),
            savedAccount.getEmail()
        );

        return AuthResponse.from(savedAccount, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        Account account = accountRepository.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
            request.password(),
            account.getPasswordHash()
        )) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(
            account.getId(),
            account.getEmail()
        );

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
