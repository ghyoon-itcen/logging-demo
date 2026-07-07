# Logging Demo

Spring PetClinic 앱에 Prometheus(메트릭) + Loki(로그) + Grafana(시각화) 스택을 연동하는 데모 프로젝트입니다.
앱이 기동되면 5초마다 자동으로 랜덤 로그가 발생하여, 별도 조작 없이도 Grafana에서 실시간 로그를 확인할 수 있습니다.

---

## 사전 준비

### Docker Desktop 설치

1. https://www.docker.com/products/docker-desktop/ 에서 **Docker Desktop** 다운로드 및 설치
2. 설치 후 Docker Desktop 실행 (시스템 트레이에 고래 아이콘이 보이면 정상)
3. 터미널에서 확인:
   ```bash
   docker --version
   docker compose version
   ```
   버전 정보가 출력되면 준비 완료입니다.

> Docker Desktop은 Docker Engine + Docker Compose를 함께 포함합니다. 별도 설치 불필요.

---

## 실행 방법

### 1단계: 소스코드 빌드

Windows 터미널(또는 cmd)에서 `logging-demo` 폴더로 이동 후:

```bat
scripts\deploy.bat
```

> ⏱ 최초 1회는 라이브러리를 모두 다운로드하므로 5~10분 정도 소요됩니다. 이후 빌드부터는 캐시를 사용하여 1~2분 내에 완료됩니다.

### 2단계: 전체 서비스 실행

```bash
docker compose up -d
```

> `-d`는 백그라운드 실행 옵션입니다. 터미널을 닫아도 서비스가 계속 실행됩니다.

### 3단계: 동작 확인

브라우저에서 아래 주소를 열어보세요:

| 서비스 | URL | 설명 |
|---|---|---|
| PetClinic | http://localhost:8080 | 앱 웹 UI |
| Grafana | http://localhost:3000 | 로그/메트릭 통합 대시보드 (ID: admin / PW: admin) |
| Prometheus | http://localhost:9090 | 메트릭 조회 (Grafana에서도 확인 가능) |

---

## 중지 및 재시작

```bash
# 전체 서비스 중지
docker compose down

# 전체 서비스 재시작
docker compose up -d

# 특정 서비스만 재시작
docker compose restart petclinic
```

---

## 로그 확인 (터미널)

```bash
# PetClinic 로그 실시간 확인 (Ctrl+C로 종료)
docker logs -f petclinic-log

# 최근 50줄만 보기
docker logs --tail 50 petclinic-log
```

로그 파일도 호스트에 저장됩니다: `logging-demo/data/logs/petclinic.log`

---

## Grafana 사용법

http://localhost:3000 접속 → ID `admin` / PW `admin` (초기 비밀번호 변경은 Skip 가능)

### 로그 확인 (Loki)

1. 좌측 메뉴 → **Explore**
2. 상단 데이터소스를 **Loki**로 선택
3. Label filter에서 `container` = `petclinic-log` 선택
4. **Run query** 클릭

**필터링 팁:**
- 에러만 보기: `|= "ERROR"`
- 경고만 보기: `|= "WARN"`
- 자동 생성 로그만 보기: `|= "[AUTO]"`

### 메트릭 확인 (Prometheus)

1. 좌측 메뉴 → **Explore**
2. 데이터소스를 **Prometheus**로 선택
3. 쿼리 예시:
   - `http_server_requests_seconds_count` - HTTP 요청 수
   - `jvm_memory_used_bytes` - JVM 메모리 사용량
   - `process_cpu_usage` - CPU 사용률

---

## 로그 데모 API

브라우저 주소창에 아래 URL을 입력하면 다양한 로그를 즉시 발생시킬 수 있습니다.

### 로그 레벨별

| URL | 설명 |
|---|---|
| http://localhost:8080/demo/logs/all | 모든 유형 한번에 발생 |
| http://localhost:8080/demo/logs/info | INFO 레벨 로그 |
| http://localhost:8080/demo/logs/warn | WARN 레벨 로그 |
| http://localhost:8080/demo/logs/error | ERROR + 스택트레이스 |

### HTTP 상태 코드별

| URL | 응답 코드 | 설명 |
|---|---|---|
| http://localhost:8080/demo/logs/http/200 | 200 | 정상 응답 |
| http://localhost:8080/demo/logs/http/400 | 400 | 잘못된 요청 |
| http://localhost:8080/demo/logs/http/403 | 403 | 접근 거부 |
| http://localhost:8080/demo/logs/http/404 | 404 | 리소스 없음 |
| http://localhost:8080/demo/logs/http/500 | 500 | 서버 에러 |
| http://localhost:8080/demo/logs/http/503 | 503 | 서비스 불가 |

---

## 설정 변경

### 자동 로그 스케줄러 주기

앱이 기동되면 5초마다 자동으로 랜덤 로그가 발생합니다 (INFO 50%, WARN 30%, ERROR 20%).
`[AUTO]` 접두사가 붙어 수동 호출 로그와 구분됩니다.

주기를 변경하려면 `spring-petclinic-main/src/main/java/org/springframework/samples/petclinic/system/LogDemoScheduler.java`에서:

```java
@Scheduled(fixedRate = 5000)  // 밀리초 단위 (5000 = 5초)
```

예시: 1초(`1000`), 10초(`10000`), 1분(`60000`)

### 로그 레벨

`spring-petclinic-main/src/main/resources/application.properties`에서 설정합니다:

```properties
# 기본값: INFO (INFO, WARN, ERROR 출력)
logging.level.org.springframework=INFO

# DEBUG 로그도 보고 싶을 때 (데모 컨트롤러/스케줄러만 적용)
logging.level.org.springframework.samples.petclinic.system=DEBUG
```

| 설정값 | 출력되는 로그 |
|---|---|
| DEBUG | DEBUG, INFO, WARN, ERROR 전부 |
| INFO | INFO, WARN, ERROR |
| WARN | WARN, ERROR만 |
| ERROR | ERROR만 |

> 주의: `logging.level.root=DEBUG`로 전체 설정하면 Spring 내부 로그가 대량 출력됩니다. 패키지 단위로 지정하세요.

### 설정 변경 후 적용

위 설정들은 소스코드에 포함되므로, 변경 후 다시 빌드해야 적용됩니다:

```bat
scripts\deploy.bat
```

### 인프라 설정 파일 변경

`alloy/`, `loki/`, `prometheus/`, `grafana/` 폴더의 설정 파일을 수정한 경우, 빌드 없이 해당 컨테이너만 재시작하면 됩니다:

```bash
# 예: prometheus/prometheus.yml 수정 후
docker compose restart prometheus

# 예: alloy/config.alloy 수정 후
docker compose restart alloy

# 예: loki/loki-config.yml 수정 후
docker compose restart loki
```

> `grafana/datasources.yml`은 Grafana 최초 기동 시에만 읽힙니다. 변경 시:
> ```bash
> docker compose down grafana
> docker compose up -d grafana
> ```

---

## 아키텍처

```
┌──────────────────────────────────────────────────────────────┐
│  Docker Host                                                 │
│                                                              │
│  ┌──────────────────────┐                                    │
│  │  petclinic-log       │                                    │
│  │   :8080              │                                    │
│  │   java -jar app.jar  │                                    │
│  └──────────┬───────────┘                                    │
│             │                                                │
│        stdout 로그         /actuator/prometheus               │
│             │                      │                         │
│             ▼                      ▼                         │
│  ┌──────────────┐       ┌──────────────┐                     │
│  │    Alloy     │       │  Prometheus  │                     │
│  │  (로그 수집)  │       │   :9090      │                     │
│  └──────┬───────┘       └──────┬───────┘                     │
│         │                      │                             │
│         ▼                      │                             │
│  ┌──────────────┐              │                             │
│  │    Loki      │              │                             │
│  │   :3100      │              │                             │
│  └──────┬───────┘              │                             │
│         │                      │                             │
│         ▼                      ▼                             │
│  ┌─────────────────────────────────────┐                     │
│  │             Grafana                 │                     │
│  │              :3000                  │                     │
│  │   (메트릭 + 로그 통합 대시보드)       │                     │
│  └─────────────────────────────────────┘                     │
└──────────────────────────────────────────────────────────────┘
```

---

## 프로젝트 구조

```
logging-demo/
├── docker-compose.yml          # 전체 서비스 정의
├── README.md
├── scripts/
│   └── deploy.bat              # 빌드 및 배포 스크립트
├── alloy/
│   └── config.alloy            # Alloy 로그 수집 설정
├── prometheus/
│   └── prometheus.yml          # Prometheus scrape 설정
├── loki/
│   └── loki-config.yml         # Loki 저장소 설정
├── grafana/
│   └── datasources.yml         # Grafana 데이터소스 자동 프로비저닝
└── data/                       # 볼륨 데이터 (자동 생성됨)
    ├── deploy/                 # 빌드된 jar
    ├── maven-cache/            # Maven 의존성 캐시
    ├── alloy/                  # Alloy 위치 기록
    ├── prometheus/             # Prometheus 데이터
    ├── loki/                   # Loki 로그 데이터
    └── grafana/                # Grafana 설정/대시보드
```

## 이미지 버전

| 서비스 | 이미지 | 버전 |
|---|---|---|
| PetClinic | eclipse-temurin | 17-jre |
| Prometheus | prom/prometheus | v3.13.0 |
| Loki | grafana/loki | 3.7.3 |
| Alloy | grafana/alloy | v1.17.1 |
| Grafana | grafana/grafana | 13.1.0 |

---

## 문제 해결

### 서비스가 안 뜰 때

```bash
# 서비스 상태 확인
docker compose ps

# 특정 컨테이너 로그 확인
docker compose logs petclinic
docker compose logs loki
```

### 포트 충돌

```bash
# 어떤 프로그램이 8080을 쓰는지 확인 (Windows)
netstat -ano | findstr :8080
```

### 처음부터 다시 시작

```bash
# 모든 컨테이너와 데이터 삭제 후 재시작
docker compose down -v
docker compose up -d
```

> `-v` 옵션은 데이터 볼륨도 함께 삭제합니다. Prometheus/Loki에 쌓인 데이터가 초기화됩니다.

---

## 소스코드

- Spring PetClinic: https://github.com/spring-projects/spring-petclinic
- 커스텀 로그 컨트롤러: `spring-petclinic-main/src/main/java/org/springframework/samples/petclinic/system/LogDemoController.java`
- 자동 로그 스케줄러: `spring-petclinic-main/src/main/java/org/springframework/samples/petclinic/system/LogDemoScheduler.java`
