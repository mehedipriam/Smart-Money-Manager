package com.smartmoneymanager.backend.service.importing;

import java.util.List;

/**
 * @param rows            valid transaction candidates — every one of these is shown in the preview.
 * @param totalDetected   total data rows the parser looked at (valid + skipped).
 * @param skippedRowCount rows that didn't produce a candidate, for any reason.
 * @param warnings        human-readable notes about *anomalous* skips worth a user's attention —
 *                        not populated for the ordinary/expected skip case (e.g. a non-delivered
 *                        order), only when something otherwise-valid-looking was still unusable.
 */
public record ImportParseResult(
        List<NormalizedTransactionRow> rows,
        int totalDetected,
        int skippedRowCount,
        List<String> warnings) {
}
