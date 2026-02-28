# Library Management System

A console-based Java library management project with file-backed storage.

## Project Layout

- `src/main/java/library` - core application source
- `data` - local datastore files (`books.txt`, `students.txt`, `issues.txt`)
- `scripts` - local utility scripts
- `run.bat` - convenience launcher (delegates to scripts)
- `pom.xml` - Maven build config

## Run Locally

### Option 1: Script

```bat
run.bat
```

### Option 2: Maven

```bat
mvn -q clean compile
mvn -q exec:java -Dexec.mainClass=library.Main
```

## Notes

- Data files are intentionally excluded from Git tracking.
- Build output directories are ignored.
