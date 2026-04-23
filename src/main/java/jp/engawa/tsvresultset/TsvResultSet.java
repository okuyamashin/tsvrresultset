package jp.engawa.tsvresultset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Reads a TSV file and exposes it as a {@link ResultSet}.
 * Only {@code getString} (and string-oriented accessors) is supported; other types are not.
 */
public class TsvResultSet implements ResultSet {
    protected File tsvFile;
    protected TsvMetaData metaData;
    protected BufferedReader reader;

    /** Values in the current row (already split on tab). */
    protected String[] currentRow;
    /** Whether the last value returned from {@code getString} was SQL {@code NULL} (or treated as such). */
    protected boolean lastWasNull;

    public TsvResultSet(File tsvFile) throws IOException {
        this.tsvFile = tsvFile;
        this.reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(tsvFile), StandardCharsets.UTF_8));

        // First line is the header: column names for metadata
        String headerLine = reader.readLine();
        if (headerLine == null) {
            this.metaData = new TsvMetaData(new String[0]);
        } else {
            String[] columnNames = splitTsv(headerLine);
            // Trim names (extra spaces are common in Excel and similar TSV exports)
            for (int i = 0; i < columnNames.length; i++) {
                if (columnNames[i] != null) {
                    columnNames[i] = columnNames[i].trim();
                }
            }
            this.metaData = new TsvMetaData(columnNames);
        }
        this.currentRow = null;
    }

    private static String[] splitTsv(String line) {
        if (line == null) return new String[0];
        return line.split("\t", -1);
    }

    @Override
    public boolean next() throws SQLException {
        try {
            String line = reader.readLine();
            if (line == null) {
                currentRow = null;
                return false;
            }
            currentRow = splitTsv(line);
            return true;
        } catch (IOException e) {
            throw new SQLException("Failed to read TSV", e);
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException e) {
            throw new SQLException("Failed to close", e);
        }
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        if (currentRow == null) {
            throw new SQLException("No current row; call next() first");
        }
        if (columnIndex < 1 || columnIndex > currentRow.length) {
            lastWasNull = true;
            return null;
        }
        String value = currentRow[columnIndex - 1];
        lastWasNull = (value == null || value.isEmpty());
        return value;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        int index = findColumn(columnLabel);
        return getString(index);
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return getString(columnIndex);
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return getString(columnLabel);
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        String[] names = metaData.columnNames;
        for (int i = 0; i < names.length; i++) {
            if (columnLabel != null && columnLabel.equalsIgnoreCase(names[i])) {
                return i + 1;
            }
        }
        throw new SQLException("Column not found: " + columnLabel);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return lastWasNull;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return metaData;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return reader == null;
    }

    // Getters for types other than String are not supported
    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getBoolean is not supported; use getString");
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getBoolean is not supported; use getString");
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getByte is not supported; use getString");
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getByte is not supported; use getString");
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getShort is not supported; use getString");
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getShort is not supported; use getString");
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getInt is not supported; use getString");
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getInt is not supported; use getString");
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getLong is not supported; use getString");
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getLong is not supported; use getString");
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getFloat is not supported; use getString");
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getFloat is not supported; use getString");
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getDouble is not supported; use getString");
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getDouble is not supported; use getString");
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getBytes is not supported; use getString");
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getBytes is not supported; use getString");
    }

    @Override
    public java.sql.Date getDate(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getDate is not supported; use getString");
    }

    @Override
    public java.sql.Date getDate(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getDate is not supported; use getString");
    }

    @Override
    public java.sql.Time getTime(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getTime is not supported; use getString");
    }

    @Override
    public java.sql.Time getTime(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getTime is not supported; use getString");
    }

    @Override
    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getTimestamp is not supported; use getString");
    }

    @Override
    public java.sql.Timestamp getTimestamp(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getTimestamp is not supported; use getString");
    }

    @Override
    public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getUnicodeStream is deprecated; use getCharacterStream");
    }
    @Override
    public java.io.InputStream getUnicodeStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getUnicodeStream is deprecated; use getCharacterStream");
    }
    @Override
    public java.net.URL getURL(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getURL is not supported; use getString");
    }
    @Override
    public java.net.URL getURL(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getURL is not supported; use getString");
    }
    @Override
    public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getAsciiStream is not supported");
    }

    @Override
    public java.io.InputStream getAsciiStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getAsciiStream is not supported");
    }

    @Override
    public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getBinaryStream is not supported");
    }

    @Override
    public java.io.InputStream getBinaryStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getBinaryStream is not supported");
    }

    @Override
    public java.io.Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getCharacterStream is not supported");
    }

    @Override
    public java.io.Reader getCharacterStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getCharacterStream is not supported");
    }

    @Override
    public java.math.BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getBigDecimal is not supported; use getString");
    }

    @Override
    public java.math.BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("getBigDecimal is not supported; use getString");
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return getString(columnIndex);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return getString(columnLabel);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("unwrap is not supported");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    // Remaining ResultSet API (cursor navigation, updates, etc.)
    @Override
    public boolean isBeforeFirst() throws SQLException { return false; }
    @Override
    public boolean isAfterLast() throws SQLException { return false; }
    @Override
    public boolean isFirst() throws SQLException { return false; }
    @Override
    public boolean isLast() throws SQLException { return false; }
    @Override
    public void beforeFirst() throws SQLException { throw new UnsupportedOperationException("beforeFirst is not supported"); }
    @Override
    public void afterLast() throws SQLException { throw new UnsupportedOperationException("afterLast is not supported"); }
    @Override
    public boolean first() throws SQLException { throw new UnsupportedOperationException("first is not supported"); }
    @Override
    public boolean last() throws SQLException { throw new UnsupportedOperationException("last is not supported"); }
    @Override
    public int getRow() throws SQLException { return 0; }
    @Override
    public boolean absolute(int row) throws SQLException { throw new UnsupportedOperationException("absolute is not supported"); }
    @Override
    public boolean relative(int rows) throws SQLException { throw new UnsupportedOperationException("relative is not supported"); }
    @Override
    public boolean previous() throws SQLException { throw new UnsupportedOperationException("previous is not supported"); }
    @Override
    public void setFetchDirection(int direction) throws SQLException { }
    @Override
    public int getFetchDirection() throws SQLException { return ResultSet.FETCH_FORWARD; }
    @Override
    public void setFetchSize(int rows) throws SQLException { }
    @Override
    public int getFetchSize() throws SQLException { return 0; }
    @Override
    public int getType() throws SQLException { return ResultSet.TYPE_FORWARD_ONLY; }
    @Override
    public int getConcurrency() throws SQLException { return ResultSet.CONCUR_READ_ONLY; }
    @Override
    public int getHoldability() throws SQLException { return ResultSet.CLOSE_CURSORS_AT_COMMIT; }
    @Override
    public boolean rowUpdated() throws SQLException { return false; }
    @Override
    public boolean rowInserted() throws SQLException { return false; }
    @Override
    public boolean rowDeleted() throws SQLException { return false; }
    @Override
    public void updateNull(int columnIndex) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateShort(int columnIndex, short x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateInt(int columnIndex, int x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateLong(int columnIndex, long x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBigDecimal(int columnIndex, java.math.BigDecimal x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateString(int columnIndex, String x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateDate(int columnIndex, java.sql.Date x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateTime(int columnIndex, java.sql.Time x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBinaryStream(int columnIndex, java.io.InputStream x, int length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNull(String columnLabel) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateShort(String columnLabel, short x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateInt(String columnLabel, int x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateLong(String columnLabel, long x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBigDecimal(String columnLabel, java.math.BigDecimal x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateString(String columnLabel, String x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateDate(String columnLabel, java.sql.Date x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateTime(String columnLabel, java.sql.Time x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateTimestamp(String columnLabel, java.sql.Timestamp x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateAsciiStream(String columnLabel, java.io.InputStream x, int length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBinaryStream(String columnLabel, java.io.InputStream x, int length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateCharacterStream(String columnLabel, java.io.Reader x, int length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void insertRow() throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateRow() throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void deleteRow() throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void refreshRow() throws SQLException { throw new UnsupportedOperationException("refreshRow is not supported"); }
    @Override
    public void cancelRowUpdates() throws SQLException { }
    @Override
    public void moveToInsertRow() throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void moveToCurrentRow() throws SQLException { }
    @Override
    public java.sql.Statement getStatement() throws SQLException { return null; }
    @Override
    public java.sql.SQLWarning getWarnings() throws SQLException { return null; }
    @Override
    public void clearWarnings() throws SQLException { }
    @Override
    public String getCursorName() throws SQLException { return ""; }
    @Override
    public Object getObject(int columnIndex, java.util.Map<String, Class<?>> map) throws SQLException { return getString(columnIndex); }
    @Override
    public java.sql.Ref getRef(int columnIndex) throws SQLException { throw new UnsupportedOperationException("getRef is not supported"); }
    @Override
    public java.sql.Blob getBlob(int columnIndex) throws SQLException { throw new UnsupportedOperationException("getBlob is not supported"); }
    @Override
    public java.sql.Clob getClob(int columnIndex) throws SQLException { throw new UnsupportedOperationException("getClob is not supported"); }
    @Override
    public java.sql.Array getArray(int columnIndex) throws SQLException { throw new UnsupportedOperationException("getArray is not supported"); }
    @Override
    public Object getObject(String columnLabel, java.util.Map<String, Class<?>> map) throws SQLException { return getString(columnLabel); }
    @Override
    public java.sql.Ref getRef(String columnLabel) throws SQLException { throw new UnsupportedOperationException("getRef is not supported"); }
    @Override
    public java.sql.Blob getBlob(String columnLabel) throws SQLException { throw new UnsupportedOperationException("getBlob is not supported"); }
    @Override
    public java.sql.Clob getClob(String columnLabel) throws SQLException { throw new UnsupportedOperationException("getClob is not supported"); }
    @Override
    public java.sql.Array getArray(String columnLabel) throws SQLException { throw new UnsupportedOperationException("getArray is not supported"); }
    @Override
    public java.sql.Date getDate(int columnIndex, java.util.Calendar cal) throws SQLException { throw new UnsupportedOperationException("getDate is not supported"); }
    @Override
    public java.sql.Date getDate(String columnLabel, java.util.Calendar cal) throws SQLException { throw new UnsupportedOperationException("getDate is not supported"); }
    @Override
    public java.sql.Time getTime(int columnIndex, java.util.Calendar cal) throws SQLException { throw new UnsupportedOperationException("getTime is not supported"); }
    @Override
    public java.sql.Time getTime(String columnLabel, java.util.Calendar cal) throws SQLException { throw new UnsupportedOperationException("getTime is not supported"); }
    @Override
    public java.sql.Timestamp getTimestamp(int columnIndex, java.util.Calendar cal) throws SQLException { throw new UnsupportedOperationException("getTimestamp is not supported"); }
    @Override
    public java.sql.Timestamp getTimestamp(String columnLabel, java.util.Calendar cal) throws SQLException { throw new UnsupportedOperationException("getTimestamp is not supported"); }
    @Override
    public java.sql.SQLXML getSQLXML(int columnIndex) throws SQLException { throw new UnsupportedOperationException("getSQLXML is not supported"); }
    @Override
    public java.sql.SQLXML getSQLXML(String columnLabel) throws SQLException { throw new UnsupportedOperationException("getSQLXML is not supported"); }
    @Override
    public java.math.BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException { throw new UnsupportedOperationException("getBigDecimal is not supported"); }
    @Override
    public java.math.BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException { throw new UnsupportedOperationException("getBigDecimal is not supported"); }
    @Override
    public void updateAsciiStream(int columnIndex, java.io.InputStream x, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBinaryStream(int columnIndex, java.io.InputStream x, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateCharacterStream(int columnIndex, java.io.Reader x, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateAsciiStream(String columnLabel, java.io.InputStream x, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBinaryStream(String columnLabel, java.io.InputStream x, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateCharacterStream(String columnLabel, java.io.Reader x, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBlob(int columnIndex, java.io.InputStream inputStream, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBlob(String columnLabel, java.io.InputStream inputStream, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateClob(int columnIndex, java.io.Reader reader, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateClob(String columnLabel, java.io.Reader reader, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNClob(int columnIndex, java.io.Reader reader, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNClob(String columnLabel, java.io.Reader reader, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNClob(int columnIndex, java.sql.NClob nClob) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNClob(String columnLabel, java.sql.NClob nClob) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateSQLXML(int columnIndex, java.sql.SQLXML xmlObject) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateSQLXML(String columnLabel, java.sql.SQLXML xmlObject) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public java.sql.RowId getRowId(int columnIndex) throws SQLException { throw new UnsupportedOperationException("getRowId is not supported"); }
    @Override
    public java.sql.RowId getRowId(String columnLabel) throws SQLException { throw new UnsupportedOperationException("getRowId is not supported"); }
    @Override
    public void updateRowId(int columnIndex, java.sql.RowId x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateRowId(String columnLabel, java.sql.RowId x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateRef(int columnIndex, java.sql.Ref x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateRef(String columnLabel, java.sql.Ref x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBlob(int columnIndex, java.sql.Blob x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBlob(String columnLabel, java.sql.Blob x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateClob(int columnIndex, java.sql.Clob x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateClob(String columnLabel, java.sql.Clob x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateArray(int columnIndex, java.sql.Array x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateArray(String columnLabel, java.sql.Array x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public java.sql.NClob getNClob(int columnIndex) throws SQLException { throw new UnsupportedOperationException("getNClob is not supported"); }
    @Override
    public java.sql.NClob getNClob(String columnLabel) throws SQLException { throw new UnsupportedOperationException("getNClob is not supported"); }
    @Override
    public java.io.Reader getNCharacterStream(int columnIndex) throws SQLException { throw new UnsupportedOperationException("getNCharacterStream is not supported"); }
    @Override
    public java.io.Reader getNCharacterStream(String columnLabel) throws SQLException { throw new UnsupportedOperationException("getNCharacterStream is not supported"); }
    @Override
    public void updateNCharacterStream(int columnIndex, java.io.Reader x, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNCharacterStream(String columnLabel, java.io.Reader reader, long length) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateAsciiStream(int columnIndex, java.io.InputStream x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBinaryStream(int columnIndex, java.io.InputStream x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateCharacterStream(int columnIndex, java.io.Reader x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateAsciiStream(String columnLabel, java.io.InputStream x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBinaryStream(String columnLabel, java.io.InputStream x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBlob(int columnIndex, java.io.InputStream inputStream) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateBlob(String columnLabel, java.io.InputStream inputStream) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateClob(int columnIndex, java.io.Reader reader) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateClob(String columnLabel, java.io.Reader reader) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNClob(int columnIndex, java.io.Reader reader) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNClob(String columnLabel, java.io.Reader reader) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNCharacterStream(int columnIndex, java.io.Reader x) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public void updateNCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException { throw new UnsupportedOperationException("Updates are not supported"); }
    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        if (type == String.class) {
            return type.cast(getString(columnIndex));
        }
        throw new UnsupportedOperationException("getObject(Class) only supports String");
    }
    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        if (type == String.class) {
            return type.cast(getString(columnLabel));
        }
        throw new UnsupportedOperationException("getObject(Class) only supports String");
    }
}
