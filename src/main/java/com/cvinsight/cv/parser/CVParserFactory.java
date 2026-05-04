package com.cvinsight.cv.parser;

import java.io.File;
import java.util.List;

/**
 * PATTERN: Factory Method
 *
 * Decides which CVParser implementation to return based on the file's extension.
 * Callers never instantiate PDFCVParser or PlainTextCVParser directly — they
 * always go through this factory, so adding a new format (e.g. DOCX) means
 * adding one new parser class and one line here, nothing else changes.
 *
 * Usage:
 *   CVParser parser = CVParserFactory.getParser(file);
 *   CV cv = parser.parse(file);
 */
public class CVParserFactory {

    private static final List<CVParser> PARSERS = List.of(
        new PDFCVParser(),
        new PlainTextCVParser()
    );

    private CVParserFactory() {}

    /**
     * Returns the appropriate parser for the given file.
     *
     * @throws CVParseException if no parser supports the file's extension
     */
    public static CVParser getParser(File file) throws CVParseException {
        String extension = getExtension(file);
        return PARSERS.stream()
            .filter(p -> p.supports(extension))
            .findFirst()
            .orElseThrow(() -> new CVParseException(
                "Unsupported file type: ." + extension + ". Please upload a PDF or TXT file."
            ));
    }

    private static String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return (dot >= 0) ? name.substring(dot + 1).toLowerCase() : "";
    }
}
