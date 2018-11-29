package com.lbc.ma.dataStructure;

public class Task {
	private Integer WF_ID;
	
	private Integer taskId;
	
	private Double neededResource;

	public Task(Integer WF_ID, Integer taskId, Double neededResource) {
		super();
		this.WF_ID = WF_ID;
		this.taskId = taskId;
		this.neededResource = neededResource;
	}

	
	
	public Integer getWF_ID() {
		return WF_ID;
	}

	public void setWF_ID(Integer wF_ID) {
		WF_ID = wF_ID;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer id) {
		this.taskId = id;
	}

	public Double getNeededResource() {
		return neededResource;
	}

	public void setNeededResource(Double neededResource) {
		this.neededResource = neededResource;
	}

}
