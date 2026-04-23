package jp.engawa.tsvresultset;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Writes a JDBC {@link ResultSet} to TSV in a form {@link TsvResultSet} can read.
 * <ul>
 *   <li>UTF-8; first line is column names (header); tab-delimited.</li>
 *   <li>Cell values are taken from {@link ResultSet#getString(int)} in general; SQL {@code NULL} becomes an empty field.</li>
 *   <li>Values containing tab or line breaks are replaced with spaces (irreversible) so they stay compatible
 *   with {@link TsvResultSet}'s simple line split.</li>
 * </ul>
 */
public final class TsvWriter {

    private TsvWriter() {}

    /**
     * Overwrite a file in UTF-8.
     *
     * @param rs cursor usually before the first row (not yet {@code next}); all rows are written by iterating {@code next()}.
     */
    public static void write(ResultSet rs, Path path) throws SQLException, IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            write(rs, w);
        }
    }

    /**
     * Write to an {@link OutputStream}. The stream is not closed (e.g. for {@code System.out}); only flushes at the end.
     */
    public static void write(ResultSet rs, OutputStream out) throws SQLException, IOException {
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        write(rs, w);
    }

    /**
     * Write to a {@link Writer}. Whether to close the writer is the caller's responsibility.
     */
    public static void write(ResultSet rs, Writer writer) throws SQLException, IOException {
        BufferedWriter buf = writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
        ResultSetMetaData meta = rs.getMetaData();
        int n = meta.getColumnCount();
        String[] headers = new String[n];
        for (int i = 0; i < n; i++) {
            String label = meta.getColumnLabel(i + 1);
            if (label == null || label.isBlank()) {
                label = meta.getColumnName(i + 1);
            }
            if (label == null) {
                label = "";
            }
            headers[i] = label.trim();
        }
        writeLine(buf, headers);
        while (rs.next()) {
            String[] row = new String[n];
            for (int i = 0; i < n; i++) {
                String v = rs.getString(i + 1);
                if (rs.wasNull()) {
                    row[i] = "";
                } else {
                    row[i] = sanitizeCellForTsv(v);
                }
            }
            writeLine(buf, row);
        }
        buf.flush();
    }

    /**
     * Replaces tab, LF, and CR with spaces so the same line/tab delimiter assumptions as {@link TsvResultSet} hold.
     */
    public static String sanitizeCellForTsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\t', ' ')
                .replace('\n', ' ')
                .replace('\r', ' ');
    }

    private static void writeLine(BufferedWriter w, String[] cells) throws IOException {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                w.write('\t');
            }
            String c = cells[i] == null ? "" : cells[i];
            w.write(c);
        }
        w.write('\n');
    }
}
