package com.example.ExportClients;

import java.io.IOException;
import java.io.Writer;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
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
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.example.ExportClients.helpers.RutaArchivo;
import com.example.ExportClients.model.Cliente;
import com.example.ExportClients.model.ClienteRowMapper;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	
    private static final Logger logger = LoggerFactory.getLogger(ClienteItemProcessor.class);

	@Autowired
	public JobRepository jobRepository;

	@Autowired
	public PlatformTransactionManager transactionManager;
	
	@Autowired
	public DataSource dataSource;

	@Autowired
	public Environment environment;

	@Bean
	public JdbcCursorItemReader<Cliente> reader() throws Exception {
		
		JdbcCursorItemReader<Cliente> itemReader = new JdbcCursorItemReader<Cliente>();
		itemReader.setDataSource(dataSource);
		itemReader.setSql( "SELECT id, nombre, apellido, email, telefono FROM clientes");
		itemReader.setRowMapper(new ClienteRowMapper());
		int counter = 0;
		ExecutionContext executionContext = new ExecutionContext();
		itemReader.open(executionContext);
		Object cliente = new Object();
		
		while((cliente = itemReader.read()) != null){
		    System.out.println("Procesando cliente: " + cliente);
		    counter++;
		}
		
	    System.out.println("item reader" + itemReader.getDataSource());
	    System.out.println("clientes " + itemReader.getCurrentItemCount());
	    System.out.println("item reader" + itemReader);
		
		return itemReader;
	}
	
//	@Bean
//	public JdbcCursorItemReader<Cliente> reader() {
//	    JdbcCursorItemReader itemReader = new JdbcCursorItemReader();
//	    itemReader.setDataSource(dataSource);
//	    itemReader.setSql("SELECT id, nombre, apellido, email, telefono FROM clientes");
//	    itemReader.setRowMapper(new ClienteRowMapper());
//	    
//	    System.out.println(itemReader.getSql());
//	    System.out.println(itemReader.getDataSource());
//	    System.out.println(itemReader.getCurrentItemCount());
//	    System.out.println("item reader" + itemReader);
//	    
//	    return itemReader;
//	}

	@Bean
	public ClienteItemProcessor processor() {
		return new ClienteItemProcessor();
	}
	
	@Bean
	public FlatFileItemWriter<Cliente> Writer() {
		System.out.println("test");
		return  new FlatFileItemWriterBuilder<Cliente>()
	           			.name("Writer")
	           			.resource(new FileSystemResource("output.txt"))
	           			.lineAggregator(new PassThroughLineAggregator<>())
	           			.shouldDeleteIfExists(true)
	           			.build();
	}
	
//	@Bean
//	public FlatFileItemWriter<Cliente> itemWriter() throws Exception {
//		BeanWrapperFieldExtractor<Cliente> fieldExtractor = new BeanWrapperFieldExtractor<>();
//		fieldExtractor.setNames(new String[] {"id", "nombre", "apellido", "email", "telefono"});
//		fieldExtractor.afterPropertiesSet();
//		
//		
//		DelimitedLineAggregator<Cliente> lineAggregator = new DelimitedLineAggregator<>();
//		lineAggregator.setDelimiter(",");
//		lineAggregator.setFieldExtractor(fieldExtractor);
//		
//		String decodedpath = environment.getProperty("file.output.path");
//		String archivo = "clientes.csv";
//		Path rutaArchivo;
//		try {
//		    rutaArchivo = RutaArchivo.crearRuta(decodedpath, archivo);
//		} catch (IOException e) {
//		    throw new RuntimeException("No se pudo crear la ruta del archivo", e);
//		}
//		
//		FileSystemResource outputResource = new FileSystemResource(rutaArchivo.toFile());
//		
//		 logger.info("Escribiendo archivo CSV en: {}", outputResource.getPath());
//		
//		return new FlatFileItemWriterBuilder<Cliente>()
//					.name("itemWriter")
//					.resource(outputResource)
//					.lineAggregator(lineAggregator)
//					.append(true) 
//					.build();
//		}
	

	@Bean
	public Step step1() throws Exception {
	    StepBuilder stepBuilder = new StepBuilder("step1", jobRepository);
	    return stepBuilder.<Cliente, Cliente>chunk(10, transactionManager)
	            .reader(reader())
	            .processor(processor())
	            .writer(Writer())
	            .build();
	}

	@Bean
	public Job exportClienteJob() throws Exception {
		return new JobBuilder("exportClienteJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(step1())
				.build();
	}
}




//String directorio = environment.getProperty("file.outputh.path");
//String nombreArchivo = "clientes.csv";
//
//Path rutaArchivo;
//try {
//  rutaArchivo = RutaArchivo.crearRuta(directorio, nombreArchivo);
//} catch (IOException e) {
//  throw new RuntimeException("no se creo", e);
//}
//
//FileSystemResource outputResource = new FileSystemResource(rutaArchivo.toFile());
//
//BeanWrapperFieldExtractor<Cliente> fieldExtractor = new BeanWrapperFieldExtractor<>();
//fieldExtractor.setNames(new String[] {"id", "nombre", "apellido","email", "telefono"});
//fieldExtractor.afterPropertiesSet();
//
//DelimitedLineAggregator<Cliente> lineAggregator = new DelimitedLineAggregator<>();
//lineAggregator.setDelimiter(",");
//lineAggregator.setFieldExtractor(fieldExtractor);
//
//return new FlatFileItemWriterBuilder<Cliente>()
//        .name("clienteWriter")
//        .resource(outputResource)
//        .lineAggregator(lineAggregator)
//        .build();

// --------------------------------------------------

//	BeanWrapperFieldExtractor<CustomerCredit> fieldExtractor = new BeanWrapperFieldExtractor<>();
//	fieldExtractor.setNames(new String[] {"name", "credit"});
//	fieldExtractor.afterPropertiesSet();
//
//	DelimitedLineAggregator<CustomerCredit> lineAggregator = new DelimitedLineAggregator<>();
//	lineAggregator.setDelimiter(",");
//	lineAggregator.setFieldExtractor(fieldExtractor);
//
//	return new FlatFileItemWriterBuilder<CustomerCredit>()
//				.name("customerCreditWriter")
//				.resource(outputResource)
//				.lineAggregator(lineAggregator)
//				.build();
//}

//--------------------------------------------------

//@Bean
//public FlatFileItemWriter<Cliente> writer() {
//
//	FlatFileItemWriter<Cliente> writer = new FlatFileItemWriter<>();
//
//	String outputPath = environment.getProperty("file.output.path");
//	writer.setResource(new FileSystemResource(outputPath + "clientes.csv"));
//
//	writer.setHeaderCallback(new FlatFileHeaderCallback() {
//		public void writeHeader(Writer writer) throws IOException {
//
//			writer.write("id,nombre,apellido,email,telefono");
//		}
//	});
//
//	writer.setLineAggregator(new DelimitedLineAggregator<Cliente>() {
//		{
//			setDelimiter(",");
//			setFieldExtractor(new BeanWrapperFieldExtractor<Cliente>() {
//				{
//					setNames(new String[] { "id", "nombre", "apellido", "email", "telefono" });
//				}
//			});
//		}
//	});
//	return writer;
//}

//--------------------------------------------------

//BeanWrapperFieldExtractor<Cliente> fieldExtractor = new BeanWrapperFieldExtractor<>();
//fieldExtractor.setNames(new String[] {"id", "nombre", "apellido", "email", "telefono"});
//fieldExtractor.afterPropertiesSet();
//
//
//DelimitedLineAggregator<Cliente> lineAggregator = new DelimitedLineAggregator<>();
//lineAggregator.setDelimiter(",");
//lineAggregator.setFieldExtractor(fieldExtractor);
//
//String decodedpath = environment.getProperty("file.output.path");
//String archivo = "clientes.csv";
//Path rutaArchivo;
//try {
//    rutaArchivo = RutaArchivo.crearRuta(decodedpath, archivo);
//} catch (IOException e) {
//    throw new RuntimeException("No se pudo crear la ruta del archivo", e);
//}
//
//FileSystemResource outputResource = new FileSystemResource(rutaArchivo.toFile());
//
// logger.info("Escribiendo archivo CSV en: {}", outputResource.getPath());
//
//return new FlatFileItemWriterBuilder<Cliente>()
//			.name("itemReader")
//			.resource(outputResource)
//			.lineAggregator(lineAggregator)
//			.append(true)
//			.build();

//--------------------------------------------------

//FlatFileItemWriter<Cliente> writer = new FlatFileItemWriter<>();
//
//	String outputPath = environment.getProperty("file.output.path");
//	writer.setResource(new FileSystemResource(outputPath + "clientes.csv"));
//
//	writer.setHeaderCallback(new FlatFileHeaderCallback() {
//		public void writeHeader(Writer writer) throws IOException {
//
//			writer.write("id,nombre,apellido,email,telefono");
//		}
//	});
//
//	writer.setLineAggregator(new DelimitedLineAggregator<Cliente>() {
//		{
//			setDelimiter(",");
//			setFieldExtractor(new BeanWrapperFieldExtractor<Cliente>() {
//				{
//					setNames(new String[] { "id", "nombre", "apellido", "email", "telefono" });
//				}
//			});
//		}
//	});
//	writer.setAppendAllowed(true);
//	return writer;
