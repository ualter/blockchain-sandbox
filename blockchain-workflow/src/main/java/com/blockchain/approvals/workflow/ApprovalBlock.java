package com.blockchain.approvals.workflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.blockchain.AbstractBlock;
import com.blockchain.approvals.workflow.model.Approval;

public class ApprovalBlock extends AbstractBlock {
	
	private List<Approval> approvals = new ArrayList<Approval>();
	
	public ApprovalBlock() {
		this.setTimeStamp(Instant.now().toEpochMilli());
	}
	
	public ApprovalBlock addApproval(Approval approval) {
		this.approvals.add(approval);
		return this;
	}

}
