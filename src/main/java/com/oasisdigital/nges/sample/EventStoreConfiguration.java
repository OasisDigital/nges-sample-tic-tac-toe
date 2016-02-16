package com.oasisdigital.nges.sample;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.oasisdigital.nges.cluster.MessageGroup;
import com.oasisdigital.nges.event.EventStore;
import com.oasisdigital.nges.event.config.EventStoreContext;

/**
 * Bridge exposing objects created by {@link EventStoreContext} as Spring beans.
 *
 */
@Configuration
public class EventStoreConfiguration {
    @Autowired
    private DataSource dataSource;

    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    @DependsOn(value = "flywayInitializer")
    public EventStoreContext eventStoreContext() {
        return new EventStoreContext(dataSource);
    }

    @Bean
    public EventStore eventStore() {
        return eventStoreContext().getEventStore();
    }

    @Bean
    public MessageGroup messageGroup() {
        return eventStoreContext().getMessageGroup();
    }
}
