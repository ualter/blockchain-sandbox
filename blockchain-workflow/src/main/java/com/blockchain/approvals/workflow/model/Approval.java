package com.blockchain.approvals.workflow.model;

import lombok.Data;

@Data
public class Approval {
	
	private String  hash;
	private String  approver;
	private boolean result;
	private Long    time;
	

}
