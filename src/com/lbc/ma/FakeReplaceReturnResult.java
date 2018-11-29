package com.lbc.ma;

import java.util.List;

public class FakeReplaceReturnResult {
	Double estimatedSysObj;
	
	List<TasksFlowToReplaceInfo> taskFlows;
	
	public FakeReplaceReturnResult(double obj, List<TasksFlowToReplaceInfo> taskFlows) {
		this.estimatedSysObj = obj;
		this.taskFlows = taskFlows;
	}

	public Double getEstimatedSysObj() {
		return estimatedSysObj;
	}

	public void setEstimatedSysObj(Double estimatedSysObj) {
		this.estimatedSysObj = estimatedSysObj;
	}

	public List<TasksFlowToReplaceInfo> getTaskFlows() {
		return taskFlows;
	}

	public void setTaskFlows(List<TasksFlowToReplaceInfo> taskFlows) {
		this.taskFlows = taskFlows;
	}

	
}
