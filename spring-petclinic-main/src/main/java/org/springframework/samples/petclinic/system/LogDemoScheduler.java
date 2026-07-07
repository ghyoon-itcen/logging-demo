package org.springframework.samples.petclinic.system;

import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 5초마다 랜덤한 로그를 발생시키는 스케줄러. Grafana/Loki 데모용 로그 데이터를 자동으로 생성합니다.
 */
@Component
@EnableScheduling
public class LogDemoScheduler {

	private static final Logger logger = LoggerFactory.getLogger(LogDemoScheduler.class);

	private final Random random = new Random();

	private static final String[] INFO_MESSAGES = { "사용자 로그인 성공 - userId=user01", "페이지 조회 - /owners (응답 120ms)",
			"캐시 적중 - key=owner_list, hit rate=85%", "스케줄러 정상 실행 - 다음 실행까지 5초", "DB 쿼리 완료 - 3건 조회 (15ms)" };

	private static final String[] WARN_MESSAGES = { "DB 커넥션 풀 사용률 높음 - 현재 75%", "응답 시간 임계치 초과 - GET /owners 2800ms",
			"메모리 사용량 경고 - heap 82%", "재시도 발생 - 외부 API 호출 2회차", "세션 만료 임박 - userId=user03 (남은 시간 2분)" };

	private static final String[] ERROR_MESSAGES = { "NullPointerException - 주문 처리 중 오류 발생",
			"DB 연결 실패 - Connection refused (timeout 30s)", "외부 API 응답 실패 - HTTP 503 Service Unavailable",
			"파일 저장 실패 - disk full (/data)", "인증 토큰 검증 실패 - expired token for userId=user05" };

	@Scheduled(fixedRate = 5000)
	public void generateRandomLog() {
		try {
			MDC.put("reqId", UUID.randomUUID().toString().substring(0, 8));
			MDC.put("method", "SCHEDULER");
			MDC.put("uri", "/auto-log");

			int type = random.nextInt(10);

			if (type < 5) {
				// 50% 확률로 INFO
				logger.info("[AUTO] {}", INFO_MESSAGES[random.nextInt(INFO_MESSAGES.length)]);
			}
			else if (type < 8) {
				// 30% 확률로 WARN
				logger.warn("[AUTO] {}", WARN_MESSAGES[random.nextInt(WARN_MESSAGES.length)]);
			}
			else {
				// 20% 확률로 ERROR
				logger.error("[AUTO] {}", ERROR_MESSAGES[random.nextInt(ERROR_MESSAGES.length)]);
			}
		}
		finally {
			MDC.clear();
		}
	}

}
