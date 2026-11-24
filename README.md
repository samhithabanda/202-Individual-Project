# Event Stream Analyzer

A command-line tool that reads a plain-text file of IoT / smart-building event records,
classifies each line as a **Sensor Reading**, **Device Event** or **Access Record**, and writes
aggregated statistics for each type to its own JSON file.

- Java 17+, Maven, Gson (JSON output), JUnit 5 (tests)
- Design document (problem statement, patterns, UML class diagram, extensibility):
  [`docs/Design_Document.pdf`](docs/Design_Document.pdf)

## Build

Requires JDK 17 or newer and Maven 3.8+.

```bash
mvn clean package
```

This compiles the code, runs all JUnit tests and produces a runnable jar at `target/analyzer.jar`.
To run only the tests: `mvn test`.

## Run

```bash
java -jar target/analyzer.jar --file samples/events.txt --outdir results/
```

| Argument | Required | Description |
|---|---|---|
| `--file <filename.txt>` | Yes | Path to the input file |
| `--outdir <directory>` | No | Output directory (default: current directory; created if missing) |

Output files (always created, `{}` when there are no records of that type):

| File | Contents |
|---|---|
| `sensors.json` | Per sensor: `count`, `minimum`, `median`, `average`, `maximum`, `std_dev` |
| `devices.json` | `by_severity` counts and `by_device` (`total`, `most_frequent_severity`) |
| `access.json` | Per door: `decision_times` (min, 50/90/99 percentiles, max), `results` (GRANTED/DENIED), `flagged_users` |

Exit codes: `0` success, `1` invalid arguments / missing or unreadable input file, `2` I/O error while
reading or writing. Errors are printed to stderr with a usage line.

## Input format

Each line is a sequence of space-separated `key=value` pairs; values containing spaces are wrapped in
double quotes (`msg="Heartbeat OK"`). Every line starts with `ts`, which is parsed but not used.

| Type | Required keys |
|---|---|
| Sensor Reading | `sensor`, `reading` (a number) |
| Device Event | `severity` (`CRITICAL`, `ERROR`, `WARN`, `INFO`), `device` |
| Access Record | `user`, `door`, `result` (`GRANTED`/`DENIED`), `duration_ms` (integer) |

Lines that are corrupted, miss required keys or have invalid values are **skipped silently**.
Extra keys are ignored.

## Calculation rules (for reproducible results)

- **Percentiles: nearest-rank method.** For a sorted list of N values, the p-th percentile is the value at
  0-based index `ceil(p/100 * N) - 1`. (Computed with integer arithmetic to avoid floating-point error.)
- **Median:** middle value for odd N; average of the two middle values for even N.
  The 50th percentile (nearest rank) and the median can therefore differ for even N — this is intentional.
- **Standard deviation:** population standard deviation (divide by N).
- **Rounding:** all decimal outputs are rounded half-up to 1 decimal place. Results that are whole
  numbers are written without a decimal point (`40`, not `40.0`).
- **Most frequent severity:** ties go to the higher severity (CRITICAL > ERROR > WARN > INFO).
- **Flagged users:** users with 3 or more `DENIED` attempts on the same door (sorted by name).

## Assumptions

- A line containing any malformed token (no `=`, empty key, unterminated quote) is treated as corrupted.
- If a key appears twice on a line, the last value wins.
- Classification is tried in the order Sensor Reading → Device Event → Access Record; the first match wins.
- Enum values (`severity`, `result`) are case-sensitive. `duration_ms` must be a non-negative integer;
  `reading` must be a finite decimal number (e.g. `21.5`, `-3`, `1e3`; not `abc` or `NaN`).

## Design overview

```
input ─► LineParser ─► RecordClassifier chain ─► Aggregator(s) ─► OutputWriter(s) ─► *.json
```

| Pattern | Where |
|---|---|
| **Strategy** | `LineParser`, `Aggregator`, `OutputWriter` interfaces |
| **Chain of Responsibility** | `RecordClassifier` → `SensorReadingClassifier` → `DeviceEventClassifier` → `AccessRecordClassifier` |
| **Template Method** | `RecordClassifier.classify()` and `TypedAggregator.accept()` |
| **Factory** | `AnalyzerFactory` builds and wires all components |

See the design document for the full UML diagram and how to add a new record type, input format or
output format.

## Project layout

```
src/main/java/com/eventanalyzer/
  Main.java                 entry point
  cli/                      CliArguments, CliException
  core/                     AnalyzerApplication, EventStreamAnalyzer, AnalyzerFactory
  parser/                   LineParser, KeyValueLineParser
  classify/                 RecordClassifier + one classifier per record type
  aggregate/                Aggregator, TypedAggregator, Statistics + one aggregator per record type
  output/                   OutputWriter, JsonOutputWriter
  model/                    EventRecord, SensorReading, DeviceEvent, AccessRecord, Severity, AccessResult
src/test/java/...           JUnit 5 tests
samples/events.txt          sample input from the assignment
docs/                       design document (PDF) and class diagram (SVG)
```
