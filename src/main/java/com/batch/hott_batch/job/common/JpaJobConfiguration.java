package com.batch.hott_batch.job.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManagerFactory;

@Configuration
public class JpaJobConfiguration {
    @Bean
    @Primary
    public JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory,
                                                       LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        final JpaTransactionManager tm = new JpaTransactionManager(entityManagerFactory);
        tm.setDataSource(entityManagerFactoryBean.getDataSource());
        return tm;
    }
}
