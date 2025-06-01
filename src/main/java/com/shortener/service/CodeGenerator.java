package com.shortener.service;

import com.shortener.repository.LongToShortRepository;
import com.shortener.repository.ShortToLongRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@RequiredArgsConstructor
@Service
public class CodeGenerator {
    private final LongToShortRepository longRepo;
    private final ShortToLongRepository shortRepo;

    private final SecureRandom random = new SecureRandom();
    private final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final Integer CODE_SIZE = 8;

    public String generate(@NotBlank String longUrl) {
        String code;
        do {
            code = generateCode();
        }
        while (shortRepo.existsById(code));
        return code;
    }

    public String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_SIZE; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }
}
