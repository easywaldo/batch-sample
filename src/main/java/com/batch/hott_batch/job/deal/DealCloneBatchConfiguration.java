package com.batch.hott_batch.job.deal;

import com.batch.hott_batch.domain.entity.Deal;
import com.batch.hott_batch.job.common.JpaJobConfiguration;
import com.batch.hott_batch.job.deal.writer.DealCloneJdbcPagingWriter;
import com.batch.hott_batch.job.deal.writer.DealCloneJpaItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class DealCloneBatchConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final EntityManager entityManager;
    private final StepBuilderFactory stepBuilderFactory;
    private final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;
    private final EntityManagerFactory entityManagerFactory;
    private final DealCloneJdbcPagingWriter dealCloneJdbcPagingWriter;
    private final DealCloneJpaItemWriter dealCloneJpaItemWriter;
    private final JpaJobConfiguration jpaJobConfiguration;

    private final static int CHUNK_SIZE = 15;

    @Autowired
    public DealCloneBatchConfiguration(
        @Qualifier(value = "hottEntityMangerFactory")
            LocalContainerEntityManagerFactoryBean entityManagerFactoryBean,
        JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory,
        DealCloneJdbcPagingWriter dealCloneJdbcPagingWriter,
        DealCloneJpaItemWriter dealCloneJpaItemWriter,
        JpaJobConfiguration jpaJobConfiguration) {

        this.entityManagerFactoryBean = entityManagerFactoryBean;
        this.entityManagerFactory = entityManagerFactoryBean.getNativeEntityManagerFactory();
        this.entityManager = entityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager();
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dealCloneJdbcPagingWriter = dealCloneJdbcPagingWriter;
        this.dealCloneJpaItemWriter = dealCloneJpaItemWriter;
        this.jpaJobConfiguration = jpaJobConfiguration;

    }

    @Bean(name = "cloneDealJob")
    public Job cloneDealJob(JobBuilderFactory jobBuilderFactory, Step cloneDealJobStep) throws Exception {
        return jobBuilderFactory.get("cloneDealJob")
            //.preventRestart()
            .start(cloneDealJobStep)
            .build();
    }

    @Bean
    @JobScope
    public Step cloneDealJobStep() throws Exception {
        jdbcTransactionManager().setEnforceReadOnly(false);
        return stepBuilderFactory.get("cloneDealJobStep")
            .<Deal, Deal> chunk(CHUNK_SIZE)
            .reader(dealJdbcPagingItemReader())
            .processor(dealItemProcessor())
            .writer(dealCloneJdbcPagingWriter.dealJdbcBatchItemWriter(entityManagerFactoryBean))
            //.writer(dealCloneJpaItemWriter.dealCloneJpaWriter(entityManagerFactory))
            .listener(new DealCloneBatchWriterListener())
            //.transactionManager(jdbcTransactionManager())
            .transactionManager(jpaJobConfiguration.jpaTransactionManager(entityManagerFactory, entityManagerFactoryBean))
            .allowStartIfComplete(true)
            .build();
    }

    @Bean
    @StepScope
    public ItemReader<Deal> dealJdbcPagingItemReader() throws Exception {
        Map<String, Object> parameterValues = new HashMap<>();
        //parameterValues.put("hottId", "aaaaa");
        //parameterValues.put("dealNum", "xxx123");

        var dealPagingItemReader = new JdbcPagingItemReaderBuilder<Deal>()
            .dataSource(entityManagerFactoryBean.getDataSource())
            .pageSize(CHUNK_SIZE)
            .fetchSize(CHUNK_SIZE)
            .name("dealJdbcPagingItemReader")
            .queryProvider(createQueryProvider(""))
            .parameterValues(parameterValues)
            .rowMapper((rs, rowNum) -> Deal.builder()
                .dealNum(rs.getString("num"))
                .dealName(rs.getString("name"))
                .hottId(rs.getString("hott_id"))
                .build())
            .build();
        dealPagingItemReader.afterPropertiesSet();
        return dealPagingItemReader;
    }

    @Bean
    @JobScope
    public PagingQueryProvider createQueryProvider(@Value("#{jobParameters[dealNumList]}") String dealNumList) throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(entityManagerFactoryBean.getDataSource());
        queryProvider.setSelectClause("select hott_id, num, name");
        queryProvider.setFromClause("from deal");
        queryProvider.setWhereClause("where hott_id = 'tester123' and num = 'test-deal000'");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("num", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }

    @Bean
    @StepScope
    public ItemProcessor<Deal, Deal> dealItemProcessor() {
        return Deal::clone;
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
