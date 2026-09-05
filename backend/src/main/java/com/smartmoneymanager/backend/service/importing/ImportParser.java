package com.smartmoneymanager.backend.service.importing;

import org.springframework.web.multipart.MultipartFile;

import com.smartmoneymanager.backend.entity.enums.ImportSource;

/**
 * One recognizable file template (e.g. a specific seller platform's export). Implementations are
 * Spring {@code @Component} beans — {@link ImportParserRegistry} autowires all of them, so adding
 * a future source (Amazon, Shopify, a bank CSV export, ...) is just adding a new class here, never
 * editing the registry or the preview/creation flow around it.
 */
public interface ImportParser {

    ImportSource getSource();

    /** Category name this source's rows are naturally filed under (e.g. "Daraz Sell") — resolved read-only, never created automatically. */
    String getSuggestedCategoryName();

    /**
     * Must never guess: true only when reliable signals (sheet name, required headers, ...)
     * positively identify this file as this parser's template. Any doubt — false, and let the
     * registry either try another parser or report the file as unrecognized.
     */
    boolean supports(MultipartFile file);

    /** Only called after {@link #supports} returned true for this same file. */
    ImportParseResult parse(MultipartFile file);
}
