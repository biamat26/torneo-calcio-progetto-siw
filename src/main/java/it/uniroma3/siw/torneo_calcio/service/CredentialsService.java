package it.uniroma3.siw.torneo_calcio.service;

import it.uniroma3.siw.torneo_calcio.model.Credentials;
import it.uniroma3.siw.torneo_calcio.repository.CredentialsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CredentialsService {
    private final PasswordEncoder passwordEncoder;
    private final CredentialsRepository credentialsRepository;

    public CredentialsService(PasswordEncoder passwordEncoder, CredentialsRepository credentialsRepository) {
        this.passwordEncoder = passwordEncoder;
        this.credentialsRepository = credentialsRepository;
    }

    @Transactional(readOnly = true)
    public Credentials getCredentials(Long id) {
        return this.credentialsRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public Credentials getCredentials(String username) {
        return this.credentialsRepository.findByUsername(username);
    }

    @Transactional
    public Credentials saveCredentials(Credentials credentials) {
        credentials.setRole(Credentials.DEFAULT_ROLE);
        credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
        return this.credentialsRepository.save(credentials);
    }
}
