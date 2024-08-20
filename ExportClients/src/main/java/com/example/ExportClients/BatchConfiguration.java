package com.example.ExportClients;

import java.io.IOException;
import java.io.Writer;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.batch.item.ExecutionContext;


import com.example.ExportClients.model.Cliente;
import com.example.ExportClients.model.ClienteRowMapper;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	public JobRepository jobRepository;

	@Autowired
	public PlatformTransactionManager transactionManager;
	
	@Autowired
	public DataSource dataSource;

	@Autowired
	public Environment environment;

//	@Bean
//	public JdbcCursorItemReader<Cliente> reader() throws Exception {
//
//		JdbcCursorItemReader itemReader = new JdbcCursorItemReader();
//		itemReader.setDataSource(dataSource);
//		itemReader.setSql( "SELECT id, nombre, apellido, email, telefono FROM clientes");
//		itemReader.setRowMapper(new ClienteRowMapper());
//		int counter = 0;
//		ExecutionContext executionContext = new ExecutionContext();
//		itemReader.open(executionContext);
//		Object cliente = new Object();
//		while(cliente != null){
//		    cliente = itemReader.read();
//		    counter++;
//		}
//		
//		return itemReader;
//	}
	
	@Bean
	public JdbcCursorItemReader<Cliente> reader() {
	    JdbcCursorItemReader itemReader = new JdbcCursorItemReader();
	    itemReader.setDataSource(dataSource);
	    itemReader.setSql("SELECT id, nombre, apellido, email, telefono FROM clientes");
	    itemReader.setRowMapper(new ClienteRowMapper());
	    
	    System.out.println("item reader" + itemReader);
	    
	    return itemReader;
	}

//	@Bean
//	public ClienteItemProcessor processor() {
//		return new ClienteItemProcessor();
//	}
	
	@Bean
	public FlatFileItemWriter<Cliente> itemWriter() throws Exception {
		BeanWrapperFieldExtractor<Cliente> fieldExtractor = new BeanWrapperFieldExtractor<>();
		String decodedpath = environment.getProperty("file.output.path");
		fieldExtractor.setNames(new String[] {"id", "nombre", "apellido","email", "telefono"});
		fieldExtractor.afterPropertiesSet();

		DelimitedLineAggregator<Cliente> lineAggregator = new DelimitedLineAggregator<>();
		lineAggregator.setDelimiter(",");
		lineAggregator.setFieldExtractor(fieldExtractor);
		FileSystemResource outputResource = new FileSystemResource(decodedpath + "clientes.csv");

		return new FlatFileItemWriterBuilder<Cliente>()
					.name("clienteWriter")
					.resource(outputResource)
					.lineAggregator(lineAggregator)
					.build();
	}


	@Bean
	public Step step1() throws Exception {
		StepBuilder stepBuilder = new StepBuilder("step1", jobRepository);
		return stepBuilder.<Cliente, Cliente>chunk(10, transactionManager)
				.reader(reader())
				.writer(itemWriter()).build();
	}

	@Bean
	public Job exportClienteJob() throws Exception {
		return new JobBuilder("exportClienteJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(step1())
				.build();
	}
}
