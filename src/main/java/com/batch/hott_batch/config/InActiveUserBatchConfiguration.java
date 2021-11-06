package com.batch.hott_batch.config;

import com.batch.hott_batch.domain.entity.Member;
import com.batch.hott_batch.domain.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class InActiveUserBatchConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Qualifier(value = "hottEntityMangerFactory")
    @Autowired
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    public EntityManager entityManager;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public TransactionManager transactionManager;

    private final static int CHUNK_SIZE = 15;

    @Bean
    @Primary
    public JpaTransactionManager jpaTransactionManager() {
        final JpaTransactionManager tm = new JpaTransactionManager();
        tm.setDataSource(entityManagerFactoryBean.getDataSource());
        return tm;
    }

    @Bean
    public Job inActiveUserJob(JobBuilderFactory jobBuilderFactory, Step inActiveJobStep) {
        entityManager = entityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager();
        return jobBuilderFactory.get("inActiveUserJob")
            .preventRestart()
            .start(inActiveJobStep)
            .build();
    }

    @Bean
    @JobScope
    @Transactional(transactionManager = "hottTransactionManager", readOnly = false)
    public Step inActiveJobStep(StepBuilderFactory stepBuilderFactory, JpaPagingItemReader<Member> inActiveUserJpaReader) {
        return stepBuilderFactory.get("InActiveMemberTargetingJob")
            .<Member, Member> chunk(CHUNK_SIZE)
            .reader(inActiveUserJpaReader())
            .processor(inActiveUserProcessor())
            .writer(inActiveUserWriter())
            .transactionManager(jpaTransactionManager())
            .build();
    }

    /*@Bean
    @StepScope
    public ListItemReader<Member> inActiveUserReader(UserRepository userRepository) {
        Member test = entityManager.find(Member.class, 1L);
        List<Member> memberList = entityManager.createQuery("select m from Member m").getResultList();
        return new ListItemReader<>(memberList);
    }*/

    @Bean
    @StepScope
    public JpaPagingItemReader<Member> inActiveUserJpaReader() {
        //entityManager.getTransaction().begin();
        var jpaItemReader = new JpaPagingItemReader<Member>();
        //jpaItemReader.setEntityManagerFactory(entityManagerFactoryBean.getNativeEntityManagerFactory());
        jpaItemReader.setEntityManagerFactory(entityManagerFactoryBean.getNativeEntityManagerFactory());
        jpaItemReader.setQueryString("select m from Member m");
        //jpaItemReader.setTransacted(true);
        //List<Member> memberList = userRepository.findAll();
        return jpaItemReader;
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, Member> inActiveUserProcessor() {
        return Member::setInActive;
    }

    @Bean
    @StepScope
    public JpaItemWriter<Member> inActiveUserWriter() {
        //return new JpaItemWriterBuilder<Member>().entityManagerFactory(entityManagerFactoryBean.getNativeEntityManagerFactory()).build();
        JpaItemWriter<Member> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactoryBean.getNativeEntityManagerFactory());
        //jpaItemWriter.setUsePersist(true);
        //List<Member> refreshedList = entityManager.createQuery("select m from Member m").getResultList();
        //entityManager.flush();
        //entityManager.getTransaction().commit();
        return jpaItemWriter;
    }
}
