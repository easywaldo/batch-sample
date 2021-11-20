package com.batch.hott_batch.job.member;

import com.batch.hott_batch.domain.entity.Member;
import com.batch.hott_batch.job.common.JpaJobConfiguration;
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
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;


@Configuration
@EnableBatchProcessing
public class InActiveUserBatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final EntityManager entityManager;
    private final StepBuilderFactory stepBuilderFactory;
    private final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;
    private final EntityManagerFactory entityManagerFactory;
    private final JpaJobConfiguration jpaJobConfiguration;

    private final static int CHUNK_SIZE = 15;

    @Autowired
    public InActiveUserBatchConfiguration(
        @Qualifier(value = "hottEntityMangerFactory")
            LocalContainerEntityManagerFactoryBean entityManagerFactoryBean,
        JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory,
        JpaJobConfiguration jpaJobConfiguration) {

        this.entityManagerFactoryBean = entityManagerFactoryBean;
        this.entityManagerFactory = entityManagerFactoryBean.getNativeEntityManagerFactory();
        this.entityManager = entityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager();
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jpaJobConfiguration = jpaJobConfiguration;

    }

    @Bean
    public Job inActiveUserJob(JobBuilderFactory jobBuilderFactory, Step inActiveJobStep) {
        return jobBuilderFactory.get("inActiveUserJob")
            //.preventRestart()
            .start(inActiveJobStep)
            .listener(new InActiveUserBatchWriterListener())
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
            .listener(new InActiveUserBatchWriterListener())
            .transactionManager(jpaJobConfiguration.jpaTransactionManager(entityManagerFactory, entityManagerFactoryBean))
            .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Member> jpaItemReader() {
        return new JpaPagingItemReaderBuilder<Member>()
            .name("jpaItemReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(CHUNK_SIZE)
            .queryString("select m from Member m where m.seqNo = 14754")
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
}