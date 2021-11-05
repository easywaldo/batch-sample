package com.batch.hott_batch.job;

import com.batch.hott_batch.domain.entity.Member;
import com.batch.hott_batch.domain.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.List;

/*@Configuration
@EnableBatchProcessing
public class InActiveUserJob {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Qualifier(value = "hottEntityMangerFactory")
    @Autowired
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    private final static int CHUNK_SIZE = 15;

    @Bean
    public Job inActiveUserJob(JobBuilderFactory jobBuilderFactory, Step inActiveJobStep) {
        return jobBuilderFactory.get("inActiveUserJob")
            .preventRestart()
            .start(inActiveJobStep)
            .build();
    }

    @Bean
    @JobScope
    public Step inActiveJobStep(StepBuilderFactory stepBuilderFactory, ListItemReader<Member> inactiveUserReader) {
        return stepBuilderFactory.get("inActiveUserStep")
            .<Member, Member> chunk(CHUNK_SIZE)
            .reader(inactiveUserReader)
            .processor(inActiveUserProcessor())
            .writer(inActiveUserWriter())
            .build();
    }

    @Bean
    @StepScope
    public ListItemReader<Member> inactiveUserReader(UserRepository userRepository) {
        Member test = entityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager().find(Member.class, 1L);
        List<Member> memberList = entityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager().createQuery("select m from Member m").getResultList();
        return new ListItemReader<>(memberList);
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, Member> inActiveUserProcessor() {
        return Member::setInActive;
    }

    @Bean
    @StepScope
    public JpaItemWriter<Member> inActiveUserWriter() {
        JpaItemWriter<Member> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactoryBean.getNativeEntityManagerFactory());
        return jpaItemWriter;
    }
}*/
