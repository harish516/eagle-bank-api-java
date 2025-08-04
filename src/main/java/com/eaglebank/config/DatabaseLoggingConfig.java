package com.eaglebank.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

/**
 * Configuration for database operation logging.
 * Tracks all entity CRUD operations for audit and performance monitoring.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseLoggingConfig {

    private final EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void registerEventListeners() {
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        
        // Register custom event listeners
        registry.appendListeners(EventType.POST_INSERT, new CustomPostInsertEventListener());
        registry.appendListeners(EventType.POST_UPDATE, new CustomPostUpdateEventListener());
        registry.appendListeners(EventType.POST_DELETE, new CustomPostDeleteEventListener());
    }

    private static class CustomPostInsertEventListener implements PostInsertEventListener {
        @Override
        public void onPostInsert(PostInsertEvent event) {
            log.info("DATABASE_INSERT: entity={}, id={}", 
                    event.getEntity().getClass().getSimpleName(), 
                    event.getId());
        }

        @Override
        public boolean requiresPostCommitHandling(EntityPersister persister) {
            return false;
        }
    }

    private static class CustomPostUpdateEventListener implements PostUpdateEventListener {
        @Override
        public void onPostUpdate(PostUpdateEvent event) {
            log.info("DATABASE_UPDATE: entity={}, id={}", 
                    event.getEntity().getClass().getSimpleName(), 
                    event.getId());
        }

        @Override
        public boolean requiresPostCommitHandling(EntityPersister persister) {
            return false;
        }
    }

    private static class CustomPostDeleteEventListener implements PostDeleteEventListener {
        @Override
        public void onPostDelete(PostDeleteEvent event) {
            log.info("DATABASE_DELETE: entity={}, id={}", 
                    event.getEntity().getClass().getSimpleName(), 
                    event.getId());
        }

        @Override
        public boolean requiresPostCommitHandling(EntityPersister persister) {
            return false;
        }
    }
}
