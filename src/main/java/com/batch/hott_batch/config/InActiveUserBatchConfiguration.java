package com.batch.hott_batch.config;

import com.batch.hott_batch.domain.entity.Member;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
public class InActiveUserBatchConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    private LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    public EntityManager entityManager;

    public EntityManagerFactory entityManagerFactory;

    private final static int CHUNK_SIZE = 15;

    @Autowired
    public InActiveUserBatchConfiguration(
        @Qualifier(value = "hottEntityMangerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        this.entityManagerFactoryBean = entityManagerFactoryBean;
        this.entityManagerFactory = entityManagerFactoryBean.getNativeEntityManagerFactory();
        this.entityManager = entityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager();
    }

    @Bean
    public Job inActiveUserJob(JobBuilderFactory jobBuilderFactory, Step inActiveJobStep) {
        return jobBuilderFactory.get("inActiveUserJob")
            .preventRestart()
            .start(inActiveJobStep)
            .build();
    }

    @Bean
    @JobScope
    public Step inActiveJobStep() throws Exception {
        return stepBuilderFactory.get("inActiveUserStep")
            .<Member, Member> chunk(CHUNK_SIZE)
            .reader(jpaItemReader())
            .processor(inActiveUserProcessor())
            .writer(jpaItemWriter())
            .transactionManager(jpaTransactionManager())
            .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Member> jpaItemReader() {
        return new JpaPagingItemReaderBuilder<Member>()
            .name("jpaItemReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(CHUNK_SIZE)
            .queryString("select m from Member m")
            .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, Member> inActiveUserProcessor() {
        return Member::setInActive;
    }

    @Bean
    @StepScope
    public JpaItemWriter<Member> jpaItemWriter() throws Exception {

        JpaItemWriter<Member> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        jpaItemWriter.afterPropertiesSet();
        return jpaItemWriter;
    }

    @Bean
    @Primary
    public JpaTransactionManager jpaTransactionManager() {
        final JpaTransactionManager tm = new JpaTransactionManager(entityManagerFactory);
        tm.setDataSource(entityManagerFactoryBean.getDataSource());
        return tm;
    }
}
