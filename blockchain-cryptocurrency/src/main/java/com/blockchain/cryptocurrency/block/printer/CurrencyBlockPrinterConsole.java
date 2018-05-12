package com.blockchain.cryptocurrency.block.printer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blockchain.cryptocurrency.block.CurrencyBlock;
import com.blockchain.cryptocurrency.block.printer.CurrencyBlockPrinterType.OutputType;
import com.blockchain.cryptocurrency.block.printer.format.CurrencyBlockPrinterTemplate;

@Component
@CurrencyBlockPrinterType(OutputType.Console)
public class CurrencyBlockPrinterConsole implements CurrencyBlockPrinter {
	
	@Autowired
	CurrencyBlockPrinterTemplate template;
	
	@Override
	public void print(List<CurrencyBlock> listBlocks, CurrencyBlockPrinterTemplate template) {
		System.out.println( template.applyLayout(listBlocks) );
	}

	@Override
	public void print(List<CurrencyBlock> listBlocks) {
		this.print(listBlocks, template);
	}
	
}
