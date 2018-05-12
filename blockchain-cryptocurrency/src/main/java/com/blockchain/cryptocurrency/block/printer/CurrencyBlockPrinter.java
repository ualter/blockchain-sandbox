package com.blockchain.cryptocurrency.block.printer;

import java.util.List;

import com.blockchain.cryptocurrency.block.CurrencyBlock;
import com.blockchain.cryptocurrency.block.printer.format.CurrencyBlockPrinterTemplate;

public interface CurrencyBlockPrinter {
	
	public void print(List<CurrencyBlock> listBlocks);
	
	public void print(List<CurrencyBlock> listBlocks, CurrencyBlockPrinterTemplate template);

}