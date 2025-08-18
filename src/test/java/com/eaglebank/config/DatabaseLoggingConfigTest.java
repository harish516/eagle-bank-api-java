package com.eaglebank.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;

@ExtendWith(MockitoExtension.class)
@DisplayName("Database Logging Configuration Tests")
class DatabaseLoggingConfigTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private SessionFactoryImplementor sessionFactory;

    @Mock
    private ServiceRegistryImplementor serviceRegistry;

    @Mock
    private EventListenerRegistry eventListenerRegistry;

    private DatabaseLoggingConfig databaseLoggingConfig;

    @BeforeEach
    void setUp() {
        databaseLoggingConfig = new DatabaseLoggingConfig(entityManagerFactory);
        
        // Set up lenient mocks to avoid unnecessary stubbings warnings
        lenient().when(entityManagerFactory.unwrap(SessionFactoryImplementor.class)).thenReturn(sessionFactory);
        lenient().when(sessionFactory.getServiceRegistry()).thenReturn(serviceRegistry);
        lenient().when(serviceRegistry.getService(EventListenerRegistry.class)).thenReturn(eventListenerRegistry);
    }

    @Nested
    @DisplayName("Configuration and Annotation Tests")
    class ConfigurationAndAnnotationTests {

        @Test
        @DisplayName("Should have Configuration annotation")
        void shouldHaveConfigurationAnnotation() {
            // Then
            assertThat(DatabaseLoggingConfig.class.isAnnotationPresent(Configuration.class))
                .isTrue();
        }

        @Test
        @DisplayName("Should have PostConstruct annotation on registerEventListeners method")
        void shouldHavePostConstructAnnotationOnRegisterEventListenersMethod() throws Exception {
            // Given
            Method registerEventListenersMethod = DatabaseLoggingConfig.class.getMethod("registerEventListeners");
            
            // Then
            assertThat(registerEventListenersMethod.isAnnotationPresent(PostConstruct.class))
                .isTrue();
        }

        @Test
        @DisplayName("Should be a valid Spring configuration class")
        void shouldBeValidSpringConfigurationClass() {
            // Then
            assertThat(DatabaseLoggingConfig.class.isAnnotationPresent(Configuration.class))
                .isTrue();
            // Class should not be abstract
            assertThat(java.lang.reflect.Modifier.isAbstract(DatabaseLoggingConfig.class.getModifiers()))
                .isFalse();
        }

        @Test
        @DisplayName("Should have proper constructor for dependency injection")
        void shouldHaveProperConstructorForDependencyInjection() {
            // Given
            java.lang.reflect.Constructor<?>[] constructors = DatabaseLoggingConfig.class.getConstructors();
            
            // Then
            assertThat(constructors).hasSize(1);
            assertThat(constructors[0].getParameterCount()).isEqualTo(1); // EntityManagerFactory dependency
            assertThat(constructors[0].getParameterTypes()[0]).isEqualTo(EntityManagerFactory.class);
        }
    }

    @Nested
    @DisplayName("Event Listener Registration Tests")
    class EventListenerRegistrationTests {

        @Test
        @DisplayName("Should register event listeners on post construct")
        void shouldRegisterEventListenersOnPostConstruct() {
            // When
            databaseLoggingConfig.registerEventListeners();

            // Then
            verify(eventListenerRegistry).appendListeners(eq(EventType.POST_INSERT), any(PostInsertEventListener.class));
            verify(eventListenerRegistry).appendListeners(eq(EventType.POST_UPDATE), any(PostUpdateEventListener.class));
            verify(eventListenerRegistry).appendListeners(eq(EventType.POST_DELETE), any(PostDeleteEventListener.class));
        }

        @Test
        @DisplayName("Should unwrap EntityManagerFactory to SessionFactoryImplementor")
        void shouldUnwrapEntityManagerFactoryToSessionFactoryImplementor() {
            // When
            databaseLoggingConfig.registerEventListeners();

            // Then
            verify(entityManagerFactory).unwrap(SessionFactoryImplementor.class);
        }

        @Test
        @DisplayName("Should get ServiceRegistry from SessionFactory")
        void shouldGetServiceRegistryFromSessionFactory() {
            // When
            databaseLoggingConfig.registerEventListeners();

            // Then
            verify(sessionFactory).getServiceRegistry();
        }

        @Test
        @DisplayName("Should get EventListenerRegistry from ServiceRegistry")
        void shouldGetEventListenerRegistryFromServiceRegistry() {
            // When
            databaseLoggingConfig.registerEventListeners();

            // Then
            verify(serviceRegistry).getService(EventListenerRegistry.class);
        }
    }

    @Nested
    @DisplayName("Custom Event Listener Tests")
    class CustomEventListenerTests {

        @Test
        @DisplayName("Should have CustomPostInsertEventListener as static inner class")
        void shouldHaveCustomPostInsertEventListenerAsStaticInnerClass() {
            // Given
            Class<?>[] innerClasses = DatabaseLoggingConfig.class.getDeclaredClasses();
            
            // Then
            assertThat(innerClasses).hasSize(3);
            boolean hasCustomPostInsertEventListener = false;
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("CustomPostInsertEventListener")) {
                    hasCustomPostInsertEventListener = true;
                    assertThat(java.lang.reflect.Modifier.isStatic(innerClass.getModifiers())).isTrue();
                    assertThat(PostInsertEventListener.class.isAssignableFrom(innerClass)).isTrue();
                    break;
                }
            }
            assertThat(hasCustomPostInsertEventListener).isTrue();
        }

        @Test
        @DisplayName("Should have CustomPostUpdateEventListener as static inner class")
        void shouldHaveCustomPostUpdateEventListenerAsStaticInnerClass() {
            // Given
            Class<?>[] innerClasses = DatabaseLoggingConfig.class.getDeclaredClasses();
            
            // Then
            boolean hasCustomPostUpdateEventListener = false;
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("CustomPostUpdateEventListener")) {
                    hasCustomPostUpdateEventListener = true;
                    assertThat(java.lang.reflect.Modifier.isStatic(innerClass.getModifiers())).isTrue();
                    assertThat(PostUpdateEventListener.class.isAssignableFrom(innerClass)).isTrue();
                    break;
                }
            }
            assertThat(hasCustomPostUpdateEventListener).isTrue();
        }

        @Test
        @DisplayName("Should have CustomPostDeleteEventListener as static inner class")
        void shouldHaveCustomPostDeleteEventListenerAsStaticInnerClass() {
            // Given
            Class<?>[] innerClasses = DatabaseLoggingConfig.class.getDeclaredClasses();
            
            // Then
            boolean hasCustomPostDeleteEventListener = false;
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("CustomPostDeleteEventListener")) {
                    hasCustomPostDeleteEventListener = true;
                    assertThat(java.lang.reflect.Modifier.isStatic(innerClass.getModifiers())).isTrue();
                    assertThat(PostDeleteEventListener.class.isAssignableFrom(innerClass)).isTrue();
                    break;
                }
            }
            assertThat(hasCustomPostDeleteEventListener).isTrue();
        }
    }

    @Nested
    @DisplayName("Event Listener Behavior Tests")
    class EventListenerBehaviorTests {

        @Test
        @DisplayName("Should have all three custom event listener inner classes")
        void shouldHaveAllThreeCustomEventListenerInnerClasses() {
            // Given
            Class<?>[] innerClasses = DatabaseLoggingConfig.class.getDeclaredClasses();
            
            // Then
            assertThat(innerClasses).hasSize(3);
            
            String[] expectedClassNames = {
                "CustomPostInsertEventListener",
                "CustomPostUpdateEventListener", 
                "CustomPostDeleteEventListener"
            };
            
            for (String expectedClassName : expectedClassNames) {
                boolean found = false;
                for (Class<?> innerClass : innerClasses) {
                    if (innerClass.getSimpleName().equals(expectedClassName)) {
                        found = true;
                        assertThat(java.lang.reflect.Modifier.isStatic(innerClass.getModifiers())).isTrue();
                        break;
                    }
                }
                assertThat(found).isTrue();
            }
        }

        @Test
        @DisplayName("Should verify CustomPostInsertEventListener implements correct interface")
        void shouldVerifyCustomPostInsertEventListenerImplementsCorrectInterface() {
            // Given
            Class<?> listenerClass = getInnerClass("CustomPostInsertEventListener");
            
            // Then
            assertThat(PostInsertEventListener.class.isAssignableFrom(listenerClass)).isTrue();
        }

        @Test
        @DisplayName("Should verify CustomPostUpdateEventListener implements correct interface")
        void shouldVerifyCustomPostUpdateEventListenerImplementsCorrectInterface() {
            // Given
            Class<?> listenerClass = getInnerClass("CustomPostUpdateEventListener");
            
            // Then
            assertThat(PostUpdateEventListener.class.isAssignableFrom(listenerClass)).isTrue();
        }

        @Test
        @DisplayName("Should verify CustomPostDeleteEventListener implements correct interface")
        void shouldVerifyCustomPostDeleteEventListenerImplementsCorrectInterface() {
            // Given
            Class<?> listenerClass = getInnerClass("CustomPostDeleteEventListener");
            
            // Then
            assertThat(PostDeleteEventListener.class.isAssignableFrom(listenerClass)).isTrue();
        }

        private Class<?> getInnerClass(String className) {
            Class<?>[] innerClasses = DatabaseLoggingConfig.class.getDeclaredClasses();
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals(className)) {
                    return innerClass;
                }
            }
            throw new IllegalArgumentException("Inner class not found: " + className);
        }
    }

    @Nested
    @DisplayName("Dependency Injection Tests")
    class DependencyInjectionTests {

        @Test
        @DisplayName("Should inject EntityManagerFactory dependency correctly")
        void shouldInjectEntityManagerFactoryDependencyCorrectly() {
            // Then
            assertThat(databaseLoggingConfig).isNotNull();
            // The dependency injection is verified through successful mock injection
            // and the fact that the configuration can be instantiated
        }

        @Test
        @DisplayName("Should handle EntityManagerFactory operations correctly")
        void shouldHandleEntityManagerFactoryOperationsCorrectly() {
            // When
            databaseLoggingConfig.registerEventListeners();

            // Then - should complete without exceptions and verify the chain of calls
            verify(entityManagerFactory).unwrap(SessionFactoryImplementor.class);
            verify(sessionFactory).getServiceRegistry();
            verify(serviceRegistry).getService(EventListenerRegistry.class);
            
            // Successful completion without exceptions demonstrates proper dependency handling
            assertThat(databaseLoggingConfig).isNotNull();
        }
    }
}
