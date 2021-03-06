package com.blockchain.cryptocurrency.block.printer.format;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.blockchain.cryptocurrency.block.CurrencyBlock;

@Component
public class CurrencyBlockPrinterDefaultTemplate implements CurrencyBlockPrinterTemplate {
	
	protected DecimalFormat    numberFormatter4Digits = new DecimalFormat("0000");
	protected DecimalFormat    nonceFormatter         = new DecimalFormat("###,###,#00");
	protected NumberFormat     currencyFormatter      = NumberFormat.getCurrencyInstance();
	protected SimpleDateFormat dateFormatter          = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
	
	@Override
	public String applyLayout(List<CurrencyBlock> listBlocks) {
		StringBuffer blockToString = new StringBuffer();
		
		listBlocks.forEach(block -> {
			String genesis = block.getHeight() == 0 ? "  ► genesis ◄" : " of " + numberFormatter4Digits.format(listBlocks.size() - 1);
			
			final int longitude = 97;
			blockToString.append("╔").append(StringUtils.repeat("═",longitude+8)).append("╗").append("\n");
			int longitudeHeader = longitude - 15;
			blockToString.append("║ BLOCK #").append(printBlockChainLineBlock(numberFormatter4Digits.format(block.getHeight()) + genesis, longitude));
			blockToString.append("║       ► Hash........: ").append(printBlockChainLineBlock(block.getHash(), longitudeHeader));
			blockToString.append("║       ► Previous....: ").append(printBlockChainLineBlock(block.getPreviousBlock(), longitudeHeader));
			blockToString.append("║       ► Next........: ").append(printBlockChainLineBlock(block.getNextBlock(), longitudeHeader));
			blockToString.append("║       ► TimeStamp...: ").append(printBlockChainLineBlock(dateFormatter.format(block.getTimeStamp()), longitudeHeader));
			blockToString.append("║       ► Nonce.......: ").append(printBlockChainLineBlock(nonceFormatter.format(block.getNonce()), longitudeHeader));
			blockToString.append("║       ► Merkle Root.: ").append(printBlockChainLineBlock(block.getMerkleRoot(), longitudeHeader));
			blockToString.append("╟─────────┬").append(StringUtils.repeat("─",longitudeHeader+13)).append("╢");
			
			AtomicInteger counterTransaction = new AtomicInteger();
			block.getTransactions().forEach(transaction -> {
				
				int longitudeTransaction = (longitude + 10);
				StringBuilder transactionLine = new StringBuilder();
				transactionLine.append("\n║    #").append(numberFormatter4Digits.format(counterTransaction.get() + 1));
				if ( counterTransaction.incrementAndGet() == block.getTransactions().size() ) {
					transactionLine.append("├").append("─► ");
				} else { 
					transactionLine.append("├").append("─► ");
				}
				transactionLine.append("TRANSACTION: Hash.......: ").append(transaction.getHash());
				blockToString.append(transactionLine.toString());
				blockToString.append( StringUtils.repeat(" ", longitudeTransaction - transactionLine.length() )).append("║");
				
				transactionLine.delete(0, transactionLine.length());
				transactionLine.append("\n║         │                Value......: ");
				transactionLine.append(StringUtils.rightPad(currencyFormatter.format(transaction.getValue().floatValue()),11));
				blockToString.append(transactionLine.toString());
				blockToString.append(StringUtils.repeat(" ", longitudeTransaction - transactionLine.length())).append("║");
				
				transactionLine.delete(0, transactionLine.length());
				transactionLine.append("\n║         │                TimeStamp..: ");
				transactionLine.append(StringUtils.rightPad(dateFormatter.format(transaction.getTimeStamp()),11));
				blockToString.append(transactionLine.toString());
				blockToString.append(StringUtils.repeat(" ", longitudeTransaction - transactionLine.length())).append("║");
				
				transactionLine.delete(0, transactionLine.length());
				transactionLine.append("\n║         │                From → To..: ");
				transactionLine.append(transaction.getSender().getOwner());
				transactionLine.append(" ---→ " + StringUtils.rightPad(transaction.getRecipient().getOwner(),7));
				blockToString.append(transactionLine.toString());
				blockToString.append(StringUtils.repeat(" ", longitudeTransaction - transactionLine.length())).append("║");
				
				blockToString.append("\n║         │   INPUTS --→").append(StringUtils.repeat(" ",longitudeTransaction - 25)).append("║");
				if ( transaction.getInputs() != null ) {
					transaction.getInputs().forEach(inputs -> {
						StringBuilder line = new StringBuilder();
						line.append("\n║         │             + ");
						line.append(StringUtils.leftPad(currencyFormatter.format(inputs.getUTXO().getValue().floatValue()),10)).append(" FROM ");
						line.append(StringUtils.rightPad(inputs.getUTXO().getRecipient().getOwner(), 10));
						
						blockToString.append(line.toString()).append(StringUtils.repeat(" ", longitudeTransaction - line.length())).append("║");
					});
				}
				
				blockToString.append("\n║         │   ←-- OUTPUTS").append(StringUtils.repeat(" ",longitudeTransaction - 26)).append("║");
				transaction.getOutputs().forEach(outputs -> {
					StringBuilder line = new StringBuilder();
					line.append("\n║         │             - ");
					line.append(StringUtils.leftPad(currencyFormatter.format(outputs.getValue().floatValue()),10)).append(" TO   ");
					line.append(StringUtils.rightPad(outputs.getRecipient().getOwner(), 10));
					
					blockToString.append(line.toString()).append(StringUtils.repeat(" ", longitudeTransaction - line.length())).append("║");
				});
				
				if ( counterTransaction.get() != block.getTransactions().size() ) {
					blockToString.append("\n╟─────────┼──────────────").append(StringUtils.repeat("─", longitudeTransaction - 26)).append("╢");
				}
			});
			
			blockToString.append("\n╚═════════╧").append(StringUtils.repeat("═",longitude-2)).append("╝").append("\n");
		});
		return blockToString.toString();
	}

	private String printBlockChainLineBlock(String value, int longitude) {
		if ( value == null ) {
			value = " ";
		}
		int    sizeLeft   = longitude - value.length();
		String fillUp     = StringUtils.repeat(" ", sizeLeft);
		return value + fillUp + "║\n";
	}
}
