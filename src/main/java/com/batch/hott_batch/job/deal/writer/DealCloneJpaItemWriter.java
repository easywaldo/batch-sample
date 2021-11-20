package com.batch.hott_batch.job.deal.writer;

import com.batch.hott_batch.domain.entity.Deal;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Configuration
public class DealCloneJpaItemWriter {

    @Bean
    @StepScope
    public JpaItemWriter<Deal> dealCloneJpaWriter(EntityManagerFactory entityManagerFactory) throws Exception {

        JpaItemWriter<Deal> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        jpaItemWriter.afterPropertiesSet();
        return jpaItemWriter;
    }
}
