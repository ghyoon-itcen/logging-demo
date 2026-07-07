package org.springframework.samples.petclinic.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 로그 수집 데모를 위한 컨트롤러. 다양한 레벨의 로그와 HTTP 상태 코드를 발생시킵니다.
 */
@RestController
@RequestMapping("/demo/logs")
public class LogDemoController {

	private static final Logger logger = LoggerFactory.getLogger(LogDemoController.class);

	/**
	 * 모든 유형의 로그를 한번에 발생시킵니다.
	 */
	@GetMapping("/all")
	public String generateAllLogs() {
		generateInfo();
		generateWarn();
		generateError();
		generateDebug();
		logger.info("HTTP 200 - 정상 응답");
		logger.warn("HTTP 400 - 잘못된 요청 시뮬레이션");
		logger.error("HTTP 500 - 서버 에러 시뮬레이션");
		return "All log types generated. Check Grafana/Loki for results.";
	}

	/**
	 * INFO 레벨 로그 발생
	 */
	@GetMapping("/info")
	public String generateInfo() {
		logger.info("사용자 요청 처리 완료 - 정상 동작");
		logger.info("캐시 갱신 완료 - 항목 수: 42");
		logger.info("스케줄러 작업 실행 - 다음 실행: 15초 후");
		return "INFO logs generated";
	}

	/**
	 * WARN 레벨 로그 발생
	 */
	@GetMapping("/warn")
	public String generateWarn() {
		logger.warn("DB 커넥션 풀 사용률 높음 - 현재 80%");
		logger.warn("응답 시간 임계치 초과 - 요청 처리 3200ms 소요");
		logger.warn("디스크 사용량 경고 - 남은 공간 15%");
		return "WARN logs generated";
	}

	/**
	 * ERROR 레벨 로그 발생
	 */
	@GetMapping("/error")
	public String generateError() {
		logger.error("데이터베이스 연결 실패 - timeout after 30000ms");
		logger.error("외부 API 호출 실패 - HTTP 503 Service Unavailable");
		try {
			Object obj = null;
			obj.toString();
		}
		catch (NullPointerException e) {
			logger.error("NullPointerException 발생 - 주문 처리 중 오류", e);
		}
		return "ERROR logs generated";
	}

	/**
	 * DEBUG 레벨 로그 발생
	 */
	@GetMapping("/debug")
	public String generateDebug() {
		logger.debug("요청 파라미터: page=1, size=20, sort=name");
		logger.debug("SQL 실행: SELECT * FROM owners WHERE last_name LIKE ?");
		logger.debug("HTTP 요청 헤더: Accept=application/json, Content-Type=text/html");
		return "DEBUG logs generated (enable debug level to see)";
	}

	// ============================================================
	// HTTP 상태 코드별 엔드포인트
	// ============================================================

	/**
	 * HTTP 200 정상 응답
	 */
	@GetMapping("/http/200")
	public ResponseEntity<String> http200() {
		logger.info("HTTP 200 OK - 요청 정상 처리");
		return ResponseEntity.ok("200 OK - 정상 응답");
	}

	/**
	 * HTTP 400 Bad Request
	 */
	@GetMapping("/http/400")
	public ResponseEntity<String> http400() {
		logger.warn("HTTP 400 Bad Request - 필수 파라미터 누락: ownerId");
		return ResponseEntity.badRequest().body("400 Bad Request - 잘못된 요청");
	}

	/**
	 * HTTP 403 Forbidden
	 */
	@GetMapping("/http/403")
	public ResponseEntity<String> http403() {
		logger.warn("HTTP 403 Forbidden - 권한 없는 리소스 접근 시도: /admin/config");
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("403 Forbidden - 접근 권한 없음");
	}

	/**
	 * HTTP 404 Not Found
	 */
	@GetMapping("/http/404")
	public ResponseEntity<String> http404() {
		logger.warn("HTTP 404 Not Found - 존재하지 않는 리소스: /owners/99999");
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("404 Not Found - 리소스를 찾을 수 없음");
	}

	/**
	 * HTTP 500 Internal Server Error (예외 발생)
	 */
	@GetMapping("/http/500")
	public String http500() {
		logger.error("HTTP 500 Internal Server Error - 예기치 않은 서버 오류 발생");
		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "의도적 500 에러 - 데모용");
	}

	/**
	 * HTTP 503 Service Unavailable
	 */
	@GetMapping("/http/503")
	public String http503() {
		logger.error("HTTP 503 Service Unavailable - 외부 서비스 연결 불가");
		throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "외부 서비스 응답 없음 - 데모용");
	}

}
