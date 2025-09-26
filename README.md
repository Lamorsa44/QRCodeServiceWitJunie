# QRCodeServiceJunie

A small Kotlin + Spring Boot web service for generating QR codes on the fly using ZXing. It exposes simple HTTP endpoints to:

- Check service health
- Generate a QR code image (PNG or JPEG) and return it in the response
- Generate a QR code image and save it to disk (temporary folder), returning a confirmation message

Current local date/time: 2025-09-26 04:44

## Tech stack
- Kotlin 2.0
- Spring Boot 3.3 (spring-boot-starter-web)
- ZXing (com.google.zxing:core and :javase) for QR generation
- Gradle (Kotlin DSL)
- Java 17 (toolchain)

## Project layout
- build.gradle.kts — dependencies and build config
- src/main/kotlin/com/example/qrservice/QrCodeServiceApplication.kt — Spring Boot bootstrap
- src/main/kotlin/com/example/qrservice/QrController.kt — HTTP endpoints and QR logic
- src/main/resources/application.properties — Spring app configuration

## Prerequisites
- JDK 17+
- Internet access to download Gradle wrapper dependencies on first run

The repo includes the Gradle Wrapper, so you do not need to install Gradle.

## Build and run

Using the Gradle Wrapper:

- On Windows:
  - Run: `gradlew.bat bootRun`
- On macOS/Linux:
  - Run: `./gradlew bootRun`

By default, Spring Boot will start on http://localhost:12345.

To build a runnable JAR:

- Windows: `gradlew.bat bootJar`
- macOS/Linux: `./gradlew bootJar`

The JAR will be in `build/libs/`.

## Configuration

- Server port and other Spring settings can be set in `src/main/resources/application.properties` or via environment variables.
- The "/qr/save" endpoint writes files to the system temporary directory under a subfolder named `qr`. The exact base folder depends on the OS:
  - Windows: typically `%TEMP%` (e.g., `C:\\Users\\<YOU>\\AppData\\Local\\Temp\\qr`)
  - macOS/Linux: typically `/tmp/qr`

## API

Base URL: `http://localhost:12345`

Common query parameters used by QR endpoints:
- `size` (optional, int): Pixel width/height of the QR image. Default 250. Allowed range 100..1000.
- `type` (optional, string): Image format, `png` or `jpeg`. Default `png`.
- `contents` (optional, string): Text to encode. Default empty string.

### 1) Health check
- Method: GET
- Path: `/health`
- Produces: `text/plain`
- Example:
  - `curl -i http://localhost:12345/health`
- Success response: `200 OK` with body `Service is running`.

### 2) Generate QR as image
- Method: GET
- Path: `/qr`
- Produces: `image/png` or `image/jpeg` depending on `type`
- Example (PNG):
  - `curl -i "http://localhost:12345/qr?size=300&type=png&contents=Hello%20World"`
- Example (JPEG):
  - `curl -i "http://localhost:12345/qr?size=300&type=jpeg&contents=https%3A%2F%2Fexample.com"`
- Success response: `200 OK` with binary image in the response body.
- Error response: `400 Bad Request` with a plain-text error explanation.

### 3) Generate QR and save to disk
- Method: GET
- Path: `/qr/save`
- Produces: `text/plain`
- Example:
  - `curl -i "http://localhost:12345/qr/save?size=400&type=png&contents=Saved%20QR"`
- Success response: `200 OK` with a message like:
  - `Saved QR code to <system-temp>/qr/qr_YYYYMMDD_HHMMSS.png`
- Error response: `4xx/5xx` with plain-text error.

## Validation and error handling
- `size` must be within 100..1000; otherwise you receive `400 Bad Request`.
- `type` must be either `png` or `jpeg`; otherwise you receive `400 Bad Request`.
- Any internal failure to generate or save a QR yields an error message explaining the issue.

## Notes
- Contents can be any UTF-8 string. For shell usage, remember to URL-encode the `contents` value.
- The response `Content-Type` header is determined by the `type` parameter for the `/qr` endpoint.
- Files saved via `/qr/save` are overwritten only if you manually reuse the exact generated name, which includes a timestamp; otherwise each request produces a new file name.

## Development
- Run tests: `gradlew.bat test` (Windows) or `./gradlew test` (macOS/Linux)
- Update dependencies: edit `build.gradle.kts` and run `gradlew.bat build` or `./gradlew build`

## Example

### http://localhost:12345/qr?contents=Awua%20De%20Limon%20Con%20Chia
![img.png](img.png)

## License
This project is provided as-is; no explicit license specified. Add one if needed for your use case.