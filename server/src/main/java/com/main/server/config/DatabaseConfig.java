package com.main.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;



import javax.sql.DataSource;


/**
 * Configuration class for setting up the database connection and JDBC template.
 * 
 * This config pulls PostgreSQL connection credentials from environment variables
 * or a Spring-supported properties source and registers `DataSource` and `JdbcTemplate`
 * beans for use throughout the application.
 */
@Configuration
public class DatabaseConfig {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Value("${spring.datasource.username}")
  private String dbUser;

  @Value("${spring.datasource.password}")
  private String dbPassword;


  /**
   * Initializes the {@link DataSource} bean using PostgreSQL connection properties.
   *
   * @return configured {@link DataSource} for JDBC use
   */
  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl(dbUrl);
    dataSource.setUsername(dbUser);
    dataSource.setPassword(dbPassword);
    return dataSource;
  }

  /**
   * Provides a configured {@link JdbcTemplate} bean backed by the application's {@link DataSource}.
   *
   * @param dataSource the injected data source
   * @return an instance of {@link JdbcTemplate} to simplify DB queries
   */
  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }
}
