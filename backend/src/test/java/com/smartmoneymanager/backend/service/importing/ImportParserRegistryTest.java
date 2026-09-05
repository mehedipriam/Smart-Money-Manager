package com.smartmoneymanager.backend.service.importing;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.smartmoneymanager.backend.entity.enums.ImportSource;
import com.smartmoneymanager.backend.exception.InvalidOperationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImportParserRegistryTest {

    private static class StubParser implements ImportParser {
        private final boolean supportsResult;

        StubParser(boolean supportsResult) {
            this.supportsResult = supportsResult;
        }

        @Override
        public ImportSource getSource() {
            return ImportSource.DARAZ_SALES_TRACKER;
        }

        @Override
        public String getSuggestedCategoryName() {
            return "Stub";
        }

        @Override
        public boolean supports(MultipartFile file) {
            return supportsResult;
        }

        @Override
        public ImportParseResult parse(MultipartFile file) {
            return new ImportParseResult(List.of(), 0, 0, List.of());
        }
    }

    private final MultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/octet-stream", new byte[] { 1 });

    @Test
    void picksTheFirstParserThatSupportsTheFile() {
        StubParser first = new StubParser(false);
        StubParser second = new StubParser(true);
        ImportParserRegistry registry = new ImportParserRegistry(List.of(first, second));

        assertThat(registry.detect(file)).isSameAs(second);
    }

    @Test
    void throwsAClearErrorWhenNoParserRecognizesTheFile() {
        ImportParserRegistry registry = new ImportParserRegistry(List.of(new StubParser(false), new StubParser(false)));

        assertThatThrownBy(() -> registry.detect(file))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Unsupported or unrecognized spreadsheet format");
    }
}
