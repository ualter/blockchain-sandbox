package com.blockchain.cryptocurrency.block.printer.format;

import java.util.List;

import com.blockchain.cryptocurrency.block.CurrencyBlock;

public interface CurrencyBlockPrinterTemplate {

	public String applyLayout(List<CurrencyBlock> listBlocks);

}