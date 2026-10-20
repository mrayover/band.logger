# Architecture

## Foundation

Kotlin 2.2.20, Compose Multiplatform 1.9.3, kotlinx.serialization 1.9.0, Gradle Wrapper 8.14.3 and JDK 21. The application module has a desktop JVM target and platform-neutral commonMain/commonTest source sets. This preserves the requested Kotlin/Compose direction.

commonMain owns Project, Theme, Member, Workspace, schema validation, JSON encoding and forScore generation. IDs are stable strings; desktop supplies UUIDs. Every project owns its members and theme. Updates replace the selected project by ID; project selection is persisted. Future tune, chart and export records must refer to stable project IDs.

WorkspaceStore abstracts persistence. ChartSource and ExportDestination establish future provider boundaries without implementing Chunk 2. Android must implement these with app-private storage and URI/provider access. No filesystem paths, Windows classes or Java libraries enter commonMain.

desktopMain owns the Compose presentation, native save dialog, UUID source and FileWorkspaceStore. It stores workspace.json in %LOCALAPPDATA%\band.logger. A schemaVersion prevents unsupported future data from being silently interpreted. Writes validate first, write a sibling temporary file, back up the previous save, and replace the file atomically where supported. An unreadable workspace displays a blocking error and is not silently reset.

The UI currently performs small local metadata writes synchronously. Larger chart IO belongs in background coroutines in future chunks. No source chart file is read or changed in Chunk 1.

## Windows delivery

Compose createDistributable uses jpackage/jlink to produce band.logger.exe, app libraries and a bundled Java runtime. scripts/package-windows.ps1 tests the application, builds the image, runs the packaged executable's diagnostic mode and creates a ZIP plus SHA-256 hash. It bundles the manual checklist and font license. The runtime's legal notices remain included.

GitHub Actions runs this script on Windows for pushes, pull requests and manual dispatch, then uploads the ZIP and test report. Workflow execution requires publication by the owner; adding the workflow locally does not execute it on GitHub. Artifacts expire after 90 days and are not permanent releases.

## forScore proof

The common generator writes UTF-8 XML with forScore kind="setlist", version="1.0" and title. Ordered score elements carry escaped title/path attributes and Base64 PDF data. The synthetic PDF uses calculated object offsets and contains original test text. Desktop tests parse XML and compare decoded bytes. The save dialog creates a new file only; an existing destination is never overwritten.

Specification: https://forscore.co/developers-file-types/. Embedded PDFs require forScore 14.3+. Real iPad import remains a separate owner gate. No annotation-preservation promise is made.
A process-level file lock prevents concurrent application instances from overwriting the same workspace. Package verification extracts the ZIP into a path with spaces and executes it with JAVA_HOME cleared and developer tools removed from PATH.
