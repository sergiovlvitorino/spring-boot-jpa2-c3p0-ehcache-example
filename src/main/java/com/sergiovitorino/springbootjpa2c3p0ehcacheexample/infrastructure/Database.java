package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.logging.Logger;

@Primary
@Configuration
public class Database {

	public static final Logger log = Logger.getLogger(Database.class.getCanonicalName());

	@Bean(name = "dataSource")
	public ComboPooledDataSource comboPooledDataSource() throws Exception {
		log.info("**************Creating instance of ComboPooledDataSource**************");
		
		ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource(true);
		
		comboPooledDataSource.setDriverClass("org.h2.Driver");
		comboPooledDataSource.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=- 1;DB_CLOSE_ON_EXIT=FALSE");
		comboPooledDataSource.setUser("sa");
		comboPooledDataSource.setPassword("");
		
		comboPooledDataSource.setInitialPoolSize(10);
		comboPooledDataSource.setMinPoolSize(10);
		comboPooledDataSource.setMaxPoolSize(20);
		comboPooledDataSource.setAcquireIncrement(1);
		comboPooledDataSource.setMaxStatements(50);
		comboPooledDataSource.setIdleConnectionTestPeriod(3000);
		comboPooledDataSource.setLoginTimeout(300);
		return comboPooledDataSource;
	}

	@Autowired
	@Bean
	public EntityManagerFactory entityManagerFactory(DataSource dataSource) {
		log.info("**************Creating instance of EntityManagerFactory**************");
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		localContainerEntityManagerFactoryBean.setDataSource(dataSource);
		localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		localContainerEntityManagerFactoryBean.afterPropertiesSet();
		return localContainerEntityManagerFactoryBean.getNativeEntityManagerFactory();
	}

	@Autowired
	@Bean
	public EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
		log.info("**************Creating instance of EntityManager**************");
		return entityManagerFactory.createEntityManager();
	}

}
