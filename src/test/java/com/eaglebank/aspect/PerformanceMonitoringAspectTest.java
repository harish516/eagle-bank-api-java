package com.eaglebank.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceMonitoringAspect Tests")
class PerformanceMonitoringAspectTest {

    @InjectMocks
    private PerformanceMonitoringAspect performanceMonitoringAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        // Set up log capture
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = loggerContext.getLogger(PerformanceMonitoringAspect.class);
        logAppender = new ListAppender<>();
        logAppender.setContext(loggerContext);
        logAppender.start();
        logger.addAppender(logAppender);
        logger.setLevel(Level.DEBUG);

        // Setup common mock behavior
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.getDeclaringType()).thenReturn(TestClass.class);
        lenient().when(signature.getName()).thenReturn("testMethod");
    }

    @Nested
    @DisplayName("MonitorPerformance Annotation Tests")
    class MonitorPerformanceAnnotationTests {

        @Test
        @DisplayName("Should log execution time when method completes successfully")
        void shouldLogExecutionTimeWhenMethodCompletesSuccessfully() throws Throwable {
            // Given
            PerformanceMonitoringAspect.MonitorPerformance annotation = createMonitorPerformanceAnnotation("", false, 0);
            String expectedResult = "test result";
            when(joinPoint.proceed()).thenReturn(expectedResult);

            // When
            Object result = performanceMonitoringAspect.monitorExecutionTime(joinPoint, annotation);

            // Then
            assertThat(result).isEqualTo(expectedResult);
            assertThat(logAppender.list)
                    .hasSize(1)
                    .allSatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.INFO);
                        assertThat(event.getFormattedMessage())
                                .contains("PERFORMANCE_MONITOR")
                                .contains("TestClass.testMethod completed")
                                .contains("duration:");
                    });
        }

        @Test
        @DisplayName("Should use custom operation name when provided")
        void shouldUseCustomOperationNameWhenProvided() throws Throwable {
            // Given
            PerformanceMonitoringAspect.MonitorPerformance annotation = createMonitorPerformanceAnnotation("Custom Operation", false, 0);
            when(joinPoint.proceed()).thenReturn("result");

            // When
            performanceMonitoringAspect.monitorExecutionTime(joinPoint, annotation);

            // Then
            assertThat(logAppender.list)
                    .hasSize(1)
                    .allSatisfy(event -> {
                        assertThat(event.getFormattedMessage())
                                .contains("Custom Operation completed");
                    });
        }

        @Test
        @DisplayName("Should log parameters when logParameters is true")
        void shouldLogParametersWhenLogParametersIsTrue() throws Throwable {
            // Given
            PerformanceMonitoringAspect.MonitorPerformance annotation = createMonitorPerformanceAnnotation("", true, 0);
            Object[] args = {"param1", "param2"};
            when(joinPoint.getArgs()).thenReturn(args);
            when(joinPoint.proceed()).thenReturn("result");

            // When
            performanceMonitoringAspect.monitorExecutionTime(joinPoint, annotation);

            // Then
            assertThat(logAppender.list)
                    .hasSize(1)
                    .allSatisfy(event -> {
                        assertThat(event.getFormattedMessage())
                                .contains("parameters: [param1, param2]");
                    });
        }

        @Test
        @DisplayName("Should not log when execution time is below threshold")
        void shouldNotLogWhenExecutionTimeIsBelowThreshold() throws Throwable {
            // Given
            PerformanceMonitoringAspect.MonitorPerformance annotation = createMonitorPerformanceAnnotation("", false, 1000); // 1 second threshold
            when(joinPoint.proceed()).thenReturn("result");

            // When
            performanceMonitoringAspect.monitorExecutionTime(joinPoint, annotation);

            // Then
            assertThat(logAppender.list).isEmpty();
        }

        @Test
        @DisplayName("Should log error when method throws exception")
        void shouldLogErrorWhenMethodThrowsException() throws Throwable {
            // Given
            PerformanceMonitoringAspect.MonitorPerformance annotation = createMonitorPerformanceAnnotation("", false, 0);
            RuntimeException exception = new RuntimeException("Test exception");
            when(joinPoint.proceed()).thenThrow(exception);

            // When & Then
            assertThatThrownBy(() -> performanceMonitoringAspect.monitorExecutionTime(joinPoint, annotation))
                    .isEqualTo(exception);

            assertThat(logAppender.list)
                    .hasSize(1)
                    .allSatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                        assertThat(event.getFormattedMessage())
                                .contains("PERFORMANCE_MONITOR")
                                .contains("TestClass.testMethod failed")
                                .contains("duration:")
                                .contains("error: Test exception");
                    });
        }

        @Test
        @DisplayName("Should handle method with no parameters when logParameters is true")
        void shouldHandleMethodWithNoParametersWhenLogParametersIsTrue() throws Throwable {
            // Given
            PerformanceMonitoringAspect.MonitorPerformance annotation = createMonitorPerformanceAnnotation("", true, 0);
            when(joinPoint.getArgs()).thenReturn(new Object[0]);
            when(joinPoint.proceed()).thenReturn("result");

            // When
            performanceMonitoringAspect.monitorExecutionTime(joinPoint, annotation);

            // Then
            assertThat(logAppender.list)
                    .hasSize(1)
                    .allSatisfy(event -> {
                        assertThat(event.getFormattedMessage())
                                .contains("TestClass.testMethod completed")
                                .doesNotContain("parameters:");
                    });
        }
    }

    @Nested
    @DisplayName("Service Method Monitoring Tests")
    class ServiceMethodMonitoringTests {

        @Test
        @DisplayName("Should log debug for fast service operations")
        void shouldLogDebugForFastServiceOperations() throws Throwable {
            // Given
            when(joinPoint.proceed()).thenReturn("result");

            // When
            Object result = performanceMonitoringAspect.monitorServiceMethods(joinPoint);

            // Then
            assertThat(result).isEqualTo("result");
            assertThat(logAppender.list)
                    .hasSize(1)
                    .allSatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                        assertThat(event.getFormattedMessage())
                                .contains("SERVICE_OPERATION")
                                .contains("TestClass.testMethod")
                                .contains("duration:");
                    });
        }

        @Test
        @DisplayName("Should log warning for slow service operations")
        void shouldLogWarningForSlowServiceOperations() throws Throwable {
            // Given
            when(joinPoint.proceed()).thenAnswer(invocation -> {
                // Simulate slow operation without Thread.sleep for testing
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 150) {
                    // Busy wait to simulate slow operation
                }
                return "result";
            });

            // When
            Object result = performanceMonitoringAspect.monitorServiceMethods(joinPoint);

            // Then
            assertThat(result).isEqualTo("result");
            assertThat(logAppender.list)
                    .hasSize(1)
                    .allSatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.WARN);
                        assertThat(event.getFormattedMessage())
                                .contains("SLOW_SERVICE_OPERATION")
                                .contains("TestClass.testMethod")
                                .contains("duration:");
                    });
        }

        @Test
        @DisplayName("Should log error when service method throws exception")
        void shouldLogErrorWhenServiceMethodThrowsException() throws Throwable {
            // Given
            RuntimeException exception = new RuntimeException("Service error");
            when(joinPoint.proceed()).thenThrow(exception);

            // When & Then
            assertThatThrownBy(() -> performanceMonitoringAspect.monitorServiceMethods(joinPoint))
                    .isEqualTo(exception);

            assertThat(logAppender.list)
                    .hasSize(1)
                    .allSatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                        assertThat(event.getFormattedMessage())
                                .contains("SERVICE_OPERATION_FAILED")
                                .contains("TestClass.testMethod")
                                .contains("duration:")
                                .contains("error: Service error");
                    });
        }
    }

    @Nested
    @DisplayName("MonitorPerformance Annotation Tests")
    class MonitorPerformanceAnnotationDefinitionTests {

        @Test
        @DisplayName("Should have correct annotation properties")
        void shouldHaveCorrectAnnotationProperties() {
            // Given
            PerformanceMonitoringAspect.MonitorPerformance annotation = createMonitorPerformanceAnnotation("test", true, 100);

            // Then
            assertThat(annotation.value()).isEqualTo("test");
            assertThat(annotation.logParameters()).isTrue();
            assertThat(annotation.thresholdMs()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should have correct default values")
        void shouldHaveCorrectDefaultValues() {
            // Given
            PerformanceMonitoringAspect.MonitorPerformance annotation = createMonitorPerformanceAnnotation("", false, 0);

            // Then
            assertThat(annotation.value()).isEmpty();
            assertThat(annotation.logParameters()).isFalse();
            assertThat(annotation.thresholdMs()).isZero();
        }
    }

    // Helper methods
    private PerformanceMonitoringAspect.MonitorPerformance createMonitorPerformanceAnnotation(
            String value, boolean logParameters, long thresholdMs) {
        return new PerformanceMonitoringAspect.MonitorPerformance() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return PerformanceMonitoringAspect.MonitorPerformance.class;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public boolean logParameters() {
                return logParameters;
            }

            @Override
            public long thresholdMs() {
                return thresholdMs;
            }
        };
    }

    // Test class for mocking
    private static class TestClass {
        public void testMethod() {
            // Test method
        }
    }
}
