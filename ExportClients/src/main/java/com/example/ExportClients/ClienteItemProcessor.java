package com.example.ExportClients;

import org.springframework.batch.item.ItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.ExportClients.model.Cliente;

public class ClienteItemProcessor implements ItemProcessor<Cliente, Cliente>  {
	
    private static final Logger logger = LoggerFactory.getLogger(ClienteItemProcessor.class);

	@Override
	public Cliente process(Cliente item) throws Exception {
		// TODO Auto-generated method stub
		logger.info("Procesando cliente: {}", item);
		return item;
	}

}
