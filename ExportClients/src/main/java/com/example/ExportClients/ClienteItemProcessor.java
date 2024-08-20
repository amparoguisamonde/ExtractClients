package com.example.ExportClients;

import org.springframework.batch.item.ItemProcessor;

import com.example.ExportClients.model.Cliente;

public class ClienteItemProcessor implements ItemProcessor<Cliente, Cliente>  {

	@Override
	public Cliente process(Cliente item) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("sysout en la clase clienteitemprocessor, item: " + item);
		return item;
	}

}
