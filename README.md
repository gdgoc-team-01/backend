## 로컬 Docker 실행

Docker Desktop 실행 및 환경변수 설정 후
다음 명령어를 실행합니다.

```bash
./gradlew clean bootJar && docker compose up -d --build
```

Dockerfile은 Gradle에서 생성한 JAR 파일을 사용하므로
소스 코드 변경 시 반드시 Gradle 빌드를 먼저 수행해야 합니다.