package com.shortener.service;

import static org.junit.jupiter.api.Assertions.*;

import com.shortener.repository.LongToShortRepository;
import com.shortener.repository.ShortToLongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class CodeGeneratorTest {

    private LongToShortRepository longRepo;
    private ShortToLongRepository shortRepo;
    private CodeGenerator codeGenerator;

    @BeforeEach
    void setUp() {
        longRepo = mock(LongToShortRepository.class);
        shortRepo = mock(ShortToLongRepository.class);
        codeGenerator = new CodeGenerator(longRepo, shortRepo);
    }

    @Test
    void testGenerateCodeReturnsValidLength() {
        String code = codeGenerator.generateCode();
        assertNotNull(code);
        assertEquals(8, code.length());
    }

    @Test
    void testGenerateReturnsUniqueCode() {
        when(shortRepo.existsById(anyString()))
                .thenReturn(true)
                .thenReturn(false);
        String generatedCode = codeGenerator.generate("https://example.com/test");
        assertNotNull(generatedCode);
        verify(shortRepo, atLeast(2)).existsById(anyString());
    }

    @Test
    void testGenerateReturnsCodeWhenFirstIsUnique() {
        when(shortRepo.existsById(anyString())).thenReturn(false);
        String code = codeGenerator.generate("https://example.com/unique");
        assertNotNull(code);
        verify(shortRepo, times(1)).existsById(code);
    }
}