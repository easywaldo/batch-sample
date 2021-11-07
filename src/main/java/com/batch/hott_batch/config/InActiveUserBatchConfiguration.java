package com.batch.hott_batch.config;

import com.batch.hott_batch.domain.entity.Member;
import com.batch.hott_batch.domain.entity.MemberRowMapper;
import com.batch.hott_batch.domain.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.TransactionManager;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

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


    /***
     *  JOB 을 등록한다
     * @param jobBuilderFactory
     * @param inActiveJobStep
     * @return
     */
    @Bean
    public Job inActiveUserJob(JobBuilderFactory jobBuilderFactory, Step inActiveJobStep) {
        entityManager = entityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager();
        return jobBuilderFactory.get("inActiveUserJob")
            .preventRestart()
            .start(inActiveJobStep)
            .build();
    }

    /***
     * JOB 을 정의한다
     * @param stepBuilderFactory
     * @param inActiveUserJpaReader
     * @return
     * @throws Exception
     */
    @Bean
    @JobScope
    public Step inActiveJobStep(StepBuilderFactory stepBuilderFactory, JdbcCursorItemReader<Member> jdbcCursorItemReader) throws Exception {
        jdbcTransactionManager().setEnforceReadOnly(false);

        return stepBuilderFactory.get("InActiveMemberTargetingJob")
            .<Member, Member> chunk(CHUNK_SIZE)
            .reader(memberCursorReader())
            .processor(inActiveUserProcessor())
            .writer(jdbcBatchItemWriter())
            .transactionManager(jdbcTransactionManager())
            .build();
    }


    @Bean
    public JdbcCursorItemReader<Member> memberCursorReader() throws Exception {
        var memberCursorReader = new JdbcCursorItemReaderBuilder<Member>()
            .dataSource(entityManagerFactoryBean.getDataSource())
            .name("memberCursorReader")
            .sql("select member_seq, name, login_dt, create_dt, is_active from member")
            .rowMapper(new MemberRowMapper())
            .build();
        memberCursorReader.afterPropertiesSet();
        return memberCursorReader;
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, Member> inActiveUserProcessor() {
        return Member::setInActive;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Member> jdbcBatchItemWriter() {

        /*var updateQuery = "update member set is_active = :is_active where member_seq = :member_seq";
        var namedParametersJdbcTemplate = new NamedParameterJdbcTemplate(entityManagerFactoryBean.getDataSource());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("is_active", 1);
        parameters.put("member_seq", 1);
        namedParametersJdbcTemplate.execute(updateQuery, parameters, PreparedStatement::execute);*/

        var writer = new JdbcBatchItemWriterBuilder<Member>()
            .dataSource(entityManagerFactoryBean.getDataSource())
            .sql("update member set is_active = :isActive where member_seq = :seqNo")
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .columnMapped()
            .build();
        writer.afterPropertiesSet();
        return writer;
    }

    @Bean
    @Primary
    public JdbcTransactionManager jdbcTransactionManager() {
        final JdbcTransactionManager tm = new JdbcTransactionManager();
        tm.setDataSource(entityManagerFactoryBean.getDataSource());
        tm.setEnforceReadOnly(false);
        return tm;
    }

}
