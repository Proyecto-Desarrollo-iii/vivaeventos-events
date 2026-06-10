package co.empresa.vivaeventos.events.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLoggingInterceptorTest {

    @Mock
    private AuditEventClient auditEventClient;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private AuditLoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AuditLoggingInterceptor(auditEventClient);
    }

    @Test
    void preHandleShouldSetStartTimeAttribute() {
        interceptor.preHandle(request, response, null);
        verify(request).setAttribute(eq("auditStartTime"), anyLong());
    }

    @Test
    void preHandleShouldReturnTrue() {
        boolean result = interceptor.preHandle(request, response, null);
        assert result;
    }

    @Test
    void afterCompletionShouldLogHttpRequestWithHeaders() {
        when(request.getAttribute("auditStartTime")).thenReturn(100L);
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-User-Email")).thenReturn("admin@test.com");
        when(request.getHeader("X-User-Role")).thenReturn("ADMIN");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, null, null);

        verify(auditEventClient).logEvent(
                argThat(req ->
                        "events".equals(req.serviceName()) &&
                        "admin@test.com".equals(req.userId()) &&
                        "ADMIN".equals(req.userRole()) &&
                        "HTTP_REQUEST".equals(req.action()) &&
                        "GET".equals(req.entityType()) &&
                        req.oldValues() == null &&
                        req.newValues().contains("\"method\":\"GET\"")
                )
        );
    }

    @Test
    void afterCompletionShouldSkipExcludedPaths() {
        when(request.getRequestURI()).thenReturn("/actuator/health");

        interceptor.afterCompletion(request, response, null, null);

        verify(auditEventClient, never()).logEvent(any());
    }
}
