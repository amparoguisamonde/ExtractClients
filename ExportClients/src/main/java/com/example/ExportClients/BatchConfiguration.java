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
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

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

	@Bean
	public JdbcCursorItemReader<Cliente> reader() {

		JdbcCursorItemReader<Cliente> reader = new JdbcCursorItemReader<>();

		reader.setDataSource(dataSource);

		String sql = "SELECT id, nombre, apellido, email, telefono FROM clientes";

		reader.setSql(sql);

		reader.setRowMapper(new ClienteRowMapper());
		return reader;
	}

	@Bean
	public ClienteItemProcessor processor() {
		return new ClienteItemProcessor();
	}

	@Bean
	public FlatFileItemWriter<Cliente> writer() {

		FlatFileItemWriter<Cliente> writer = new FlatFileItemWriter<>();

		String outputPath = environment.getProperty("file.output.path");
		writer.setResource(new FileSystemResource(outputPath + "clientes.csv"));

		writer.setHeaderCallback(new FlatFileHeaderCallback() {
			public void writeHeader(Writer writer) throws IOException {

				writer.write("id,nombre,apellido,email,telefono");
			}
		});

		writer.setLineAggregator(new DelimitedLineAggregator<Cliente>() {
			{
				setDelimiter(",");
				setFieldExtractor(new BeanWrapperFieldExtractor<Cliente>() {
					{
						setNames(new String[] { "id", "nombre", "apellido", "email", "telefono" });
					}
				});
			}
		});
		return writer;
	}

	@Bean
	public Step step1() {
		StepBuilder stepBuilder = new StepBuilder("step1", jobRepository);
		return stepBuilder.<Cliente, Cliente>chunk(10, transactionManager).reader(reader()).processor(processor())
				.writer(writer()).build();
	}

	@Bean
	public Job exportClienteJob() {
		return new JobBuilder("exportClienteJob", jobRepository).incrementer(new RunIdIncrementer()).start(step1())
				.build();
	}
}
