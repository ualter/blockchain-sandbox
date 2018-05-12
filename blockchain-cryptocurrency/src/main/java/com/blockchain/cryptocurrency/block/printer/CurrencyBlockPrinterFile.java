package com.blockchain.cryptocurrency.block.printer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blockchain.cryptocurrency.block.CurrencyBlock;
import com.blockchain.cryptocurrency.block.printer.CurrencyBlockPrinterType.OutputType;
import com.blockchain.cryptocurrency.block.printer.format.CurrencyBlockPrinterTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@CurrencyBlockPrinterType(OutputType.File)
@Slf4j
public class CurrencyBlockPrinterFile implements CurrencyBlockPrinter {

	@Autowired
	private CurrencyBlockPrinterTemplate template;
	
	@Override
	public void print(List<CurrencyBlock> listBlocks) {
		this.print(listBlocks, template);
	}
	
	@Override
	public void print(List<CurrencyBlock> listBlocks, CurrencyBlockPrinterTemplate template) {
		DateTimeFormatter  formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd__HHmm");
		String             fileName  = "report_BlockChain_" + formatter.format( Instant.now().atZone(ZoneId.systemDefault() ) ); 
		Path               pathFile  = Paths.get("src/main/resources/reports/" + fileName + ".txt");
		//Path             pathFile  = Paths.get(fileName + ".txt");
		try {
			Files.deleteIfExists(pathFile);
			Files.write(pathFile, template.applyLayout(listBlocks).getBytes(), StandardOpenOption.CREATE_NEW );
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	

}
