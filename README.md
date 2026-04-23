# tsvresultset

Small utilities to **export a JDBC `ResultSet` to TSV** and to **read a TSV file back as a `ResultSet`**, so you can process tabular data with the same `ResultSet` API you already use against a database.

- **Write:** [`TsvWriter`](src/main/java/jp/engawa/tsvresultset/TsvWriter.java) — serializes rows from a `ResultSet` to UTF-8, tab-delimited text.
- **Read:** [`TsvResultSet`](src/main/java/jp/engawa/tsvresultset/TsvResultSet.java) — parses that format and implements `java.sql.ResultSet` for forward-only string access.
- **Metadata:** [`TsvMetaData`](src/main/java/jp/engawa/tsvresultset/TsvMetaData.java) — provides `ResultSetMetaData` from the header row (all columns as `VARCHAR`).

## Requirements

- **Java 17+**
- **Maven 3.6+** (to build; no extra runtime dependencies beyond the JDK and JDBC where you use `ResultSet`)

## Add as a dependency

If you install the JAR to your local repository:

```bash
mvn clean install
```

Use in another Maven project:

```xml
<dependency>
  <groupId>jp.engawa</groupId>
  <artifactId>tsvresultset</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

(Adjust the version when you publish a release.)

## TSV format

| Aspect | Rule |
|--------|------|
| Encoding | UTF-8 |
| First line | Column names (header), tab-separated |
| Data lines | One record per line, tab-separated fields |
| SQL `NULL` | Empty field when writing |
| Tab / newline inside a cell | **Not allowed** in the on-disk model used here. On write, `TsvWriter` replaces U+0009, U+000A, and U+000D with spaces (irreversible) so the reader’s simple `split("\t", -1)` stays consistent. |

## Usage

### Export a `ResultSet` to a file

The cursor is typically **before the first row** (no `next()` yet). The writer advances with `next()` until the result set is exhausted.

```java
import jp.engawa.tsvresultset.TsvWriter;
import java.nio.file.Path;

try (Connection conn = /* ... */;
     Statement st = conn.createStatement();
     ResultSet rs = st.executeQuery("SELECT id, name FROM items")) {
    TsvWriter.write(rs, Path.of("items.tsv"));
}
```

To write to standard output, use `TsvWriter.write(rs, System.out)`.

### Read a TSV as a `ResultSet`

```java
import jp.engawa.tsvresultset.TsvResultSet;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

try (ResultSet rs = new TsvResultSet(new File("items.tsv"))) {
    while (rs.next()) {
        String name = rs.getString("name");
        // ...
    }
}
```

Column lookup by label is **case-insensitive** (see `findColumn`).

## Limitations

- **`TsvResultSet` is not a full JDBC `ResultSet`.** It is **forward-only** (`TYPE_FORWARD_ONLY`); methods for scrollable cursors, updates, and many typed getters still throw `UnsupportedOperationException` (or return stubs). Use **`getString`**, or **`getInt` / `getLong` / `getFloat` / `getDouble`** (lenient parse from the cell text), and **`getObject` for `String.class`**.
- **Empty fields** are treated like SQL `NULL` for `wasNull()`.
- **Row/column count mismatch** between header and a data line is handled leniently (extra/missing cells); rely on your own validation if you need strict rectangular data.

## Build

```bash
mvn clean verify
```

Produces `target/tsvresultset-1.0.0-SNAPSHOT.jar`.

A copy of the same artifact (built with `mvn clean package`) is also committed under [`dist/`](dist/) for quick download without running Maven. Regenerate and copy it when you change the project version.

## License

No license file is included in this repository; add one if you redistribute the library.
