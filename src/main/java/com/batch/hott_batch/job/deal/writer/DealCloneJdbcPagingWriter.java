package com.batch.hott_batch.job.deal.writer;

import com.batch.hott_batch.domain.entity.Deal;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
public class DealCloneJdbcPagingWriter {

    @Bean
    @StepScope
    public ItemWriter<Deal> dealJdbcBatchItemWriter(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        var writer = new JdbcBatchItemWriterBuilder<Deal>()
            .dataSource(entityManagerFactoryBean.getDataSource())
            .sql("INSERT INTO deal (hott_id, num, name) " +
                "VALUES (:hottId, :dealNum, :dealName);")
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .beanMapped()
            .build();
        writer.afterPropertiesSet();
        return writer;
    }
}
