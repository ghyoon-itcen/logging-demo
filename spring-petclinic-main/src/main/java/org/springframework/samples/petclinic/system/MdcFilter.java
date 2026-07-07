package org.springframework.samples.petclinic.system;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * HTTP 요청마다 MDC에 추적 정보를 설정하는 서블릿 필터. 요청 ID, HTTP 메서드, URI를 로그에 자동으로 포함시킵니다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcFilter implements Filter {

	private static final String REQ_ID = "reqId";

	private static final String METHOD = "method";

	private static final String URI = "uri";

	private static final String CLIENT_IP = "clientIp";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			HttpServletRequest httpRequest = (HttpServletRequest) request;

			MDC.put(REQ_ID, UUID.randomUUID().toString().substring(0, 8));
			MDC.put(METHOD, httpRequest.getMethod());
			MDC.put(URI, httpRequest.getRequestURI());
			MDC.put(CLIENT_IP, httpRequest.getRemoteAddr());

			chain.doFilter(request, response);
		}
		finally {
			MDC.clear();
		}
	}

}
