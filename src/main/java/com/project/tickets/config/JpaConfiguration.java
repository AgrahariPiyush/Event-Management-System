package com.project.tickets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


//To track entity creation and udation (feilds in entity)
//Steps
// 1.make config file with enable jpa auditing
// 2.orm.xml file in META_INF under resources
//3.could be done using entity listenrs annotation
@Configuration
@EnableJpaAuditing
public class JpaConfiguration {
}
