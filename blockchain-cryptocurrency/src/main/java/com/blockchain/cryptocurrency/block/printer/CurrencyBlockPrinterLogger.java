package com.blockchain.cryptocurrency.block.printer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blockchain.cryptocurrency.block.CurrencyBlock;
import com.blockchain.cryptocurrency.block.printer.CurrencyBlockPrinterType.OutputType;
import com.blockchain.cryptocurrency.block.printer.format.CurrencyBlockPrinterTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@CurrencyBlockPrinterType(OutputType.Logger)
@Slf4j
public class CurrencyBlockPrinterLogger implements CurrencyBlockPrinter {
	
	@Autowired
	CurrencyBlockPrinterTemplate template;
	
	@Override
	public void print(List<CurrencyBlock> listBlocks, CurrencyBlockPrinterTemplate template) {
		log.info( template.applyLayout(listBlocks) );
	}

	@Override
	public void print(List<CurrencyBlock> listBlocks) {
		this.print(listBlocks, template);
	}
	
}
