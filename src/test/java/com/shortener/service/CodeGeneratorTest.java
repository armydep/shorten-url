package com.shortener.service;

import static org.junit.jupiter.api.Assertions.*;

import com.shortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class CodeGeneratorTest {

    private UrlMappingRepository repository;
    private CodeGenerator codeGenerator;

    @BeforeEach
    void setUp() {
        repository = mock(UrlMappingRepository.class);
        codeGenerator = new CodeGenerator(repository);
    }

    @Test
    void testGenerateCodeReturnsValidLength() {
        String code = codeGenerator.generateCode();
        assertNotNull(code);
        assertEquals(8, code.length());
    }

    @Test
    void testGenerateReturnsUniqueCode() {
        when(repository.existsById(anyString()))
                .thenReturn(true)
                .thenReturn(false);
        String generatedCode = codeGenerator.generate("https://example.com/test");
        assertNotNull(generatedCode);
        verify(repository, atLeast(2)).existsById(anyString());
    }

    @Test
    void testGenerateReturnsCodeWhenFirstIsUnique() {
        when(repository.existsById(anyString())).thenReturn(false);
        String code = codeGenerator.generate("https://example.com/unique");
        assertNotNull(code);
        verify(repository, times(1)).existsById(code);
    }
}