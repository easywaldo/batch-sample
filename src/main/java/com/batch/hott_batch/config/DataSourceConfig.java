package com.batch.hott_batch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Profile(value = "local")
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = "hottEntityMangerFactory",
    transactionManagerRef = "hottTransactionManager",
    basePackages = {"com.batch.hott_batch.domain"}
)
public class DataSourceConfig {
    @Value("${spring.datasource.driver-class-name}")
    private String hottDriverClassName;

    @Value("${spring.datasource.url}")
    private String hottConnUrl;

    @Value("${spring.datasource.username}")
    private String hottUserName;

    @Value("${spring.datasource.password}")
    private String hottUserPwd;

    @Bean(name = "hottDatasource")
    @ConfigurationProperties(prefix = "spring.test.datasource")
    public DataSource hottDataSource() {
        return DataSourceBuilder.create()
            .driverClassName(hottDriverClassName)
            .url(hottConnUrl)
            .username(hottUserName)
            .password(hottUserPwd)
            .build();
    }

    @Primary
    @Bean(name = "hottEntityMangerFactory")
    public LocalContainerEntityManagerFactoryBean hottEntityMangerFactory(EntityManagerFactoryBuilder builder) {
        return builder.dataSource(hottDataSource())
            .packages("com.batch.hott_batch.domain")
            .persistenceUnit("hott-master")
            .build();
    }

    /*@Bean(name="hottEntityManger")
    public LocalContainerEntityManagerFactoryBean hottTestEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(hottDataSource());
        entityManagerFactoryBean.setPackagesToScan("com.batch.hott_batch.domain");
        entityManagerFactoryBean.setPersistenceUnitName("hott-master");
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setJpaPropertyMap(hibernateProperties());
        entityManagerFactoryBean.afterPropertiesSet();
        return entityManagerFactoryBean;
    }*/

    @Bean(name = "hottTransactionManager")
    public PlatformTransactionManager hottMasterTransactionManager(
        EntityManagerFactory entityManagerFactory, EntityManagerFactoryBuilder builder) {

        /*JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        transactionManager.setDataSource(hottDataSource());
        return transactionManager;*/

        return new JpaTransactionManager(hottEntityMangerFactory(builder).getObject());
    }

    private Map<String, Object> hibernateProperties(){
        Resource objResource = new ClassPathResource("hibernate.properties");
        try{
            Properties objProperties = PropertiesLoaderUtils.loadProperties(objResource);
            return objProperties.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
        }catch(IOException objEx){
            return new HashMap<>();
        }
    }

}
