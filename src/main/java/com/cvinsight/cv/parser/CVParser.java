package com.cvinsight.cv.parser;

import com.cvinsight.model.CV;

import java.io.File;

/**
 * Contract for all CV file parsers.
 * CVParserFactory returns the correct implementation based on file extension.
 */
public interface CVParser {

    /**
     * Parses the given file and returns a populated CV object.
     * The CV will have rawText and sections filled in.
     * id, userId, uploadedAt, and status are set by CVService after parsing.
     */
    CV parse(File file) throws CVParseException;

    /**
     * Returns true if this parser handles the given file extension (e.g. "pdf", "txt").
     */
    boolean supports(String extension);
}
