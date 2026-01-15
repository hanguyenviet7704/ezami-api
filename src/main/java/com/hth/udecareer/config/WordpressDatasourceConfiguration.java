package com.hth.udecareer.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "com.hth.udecareer.entities",
        "com.hth.udecareer.repository",
        "com.hth.udecareer.eil.repository" }, entityManagerFactoryRef = "wordpressEntityManagerFactory", transactionManagerRef = "wordpressTransactionManager")
public class WordpressDatasourceConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.wordpress")
    public DataSourceProperties wordpressDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource wordpressDataSource() {
        DataSource realDataSource = wordpressDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();

        // return ProxyDataSourceBuilder
        // .create(realDataSource)
        // .name("WordpressDS")
        // .logQueryBySlf4j() // log qua logger
        // .countQuery()
        // .multiline() // dễ đọc
        // .build();

        return wordpressDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    @Primary
    public JdbcTemplate wordpressJdbcTemplate(@Qualifier("wordpressDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean wordpressEntityManagerFactory(
            @Qualifier("wordpressDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder,
            JpaProperties jpaProperties,
            HibernateProperties hibernateProperties) {
        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());
        properties.putAll(hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings()));

        return builder
                .dataSource(dataSource)
                .packages("com.hth.udecareer.entities", "com.hth.udecareer.eil.entities")
                .properties(properties)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager wordpressTransactionManager(
            @Qualifier("wordpressEntityManagerFactory") LocalContainerEntityManagerFactoryBean wordpressEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(wordpressEntityManagerFactory.getObject()));
    }
}
