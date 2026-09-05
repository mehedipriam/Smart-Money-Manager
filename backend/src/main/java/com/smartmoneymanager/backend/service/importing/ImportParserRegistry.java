package com.smartmoneymanager.backend.service.importing;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.smartmoneymanager.backend.exception.InvalidOperationException;

import lombok.RequiredArgsConstructor;

/** Picks the first registered {@link ImportParser} that recognizes a file. Never guesses from the filename. */
@Component
@RequiredArgsConstructor
public class ImportParserRegistry {

    private final List<ImportParser> parsers;

    public ImportParser detect(MultipartFile file) {
        return parsers.stream()
                .filter(parser -> parser.supports(file))
                .findFirst()
                .orElseThrow(() -> new InvalidOperationException(
                        "Unsupported or unrecognized spreadsheet format. We could not safely determine the transaction fields."));
    }
}
