package com.lbc.ma;

public class TasksFlowToReplaceInfo {
	
	Integer WF_ID;
	
	Integer curTaskID;
	
	Integer sucTaskID;
	
	Integer taskB_UAV_ID;

	Integer oldPathID;
	
	Integer newPathID;
	
	public TasksFlowToReplaceInfo(int WF_ID, int curTaskID, int sucTaskID, int taskB_UAV_ID, int oldPathID, int newPathID) {
		this.WF_ID = WF_ID;
		this.curTaskID = curTaskID;
		this.sucTaskID = sucTaskID;
		this.taskB_UAV_ID = taskB_UAV_ID;
		this.oldPathID = oldPathID;
		this.newPathID = newPathID;
	}

	public Integer getWF_ID() {
		return WF_ID;
	}

	public void setWF_ID(Integer wF_ID) {
		WF_ID = wF_ID;
	}

	public Integer getTaskB_UAV_ID() {
		return taskB_UAV_ID;
	}

	public void setTaskB_UAV_ID(Integer taskB_UAV_ID) {
		this.taskB_UAV_ID = taskB_UAV_ID;
	}

	public Integer getOldPathID() {
		return oldPathID;
	}

	public void setOldPathID(Integer oldPathID) {
		this.oldPathID = oldPathID;
	}

	public Integer getNewPathID() {
		return newPathID;
	}

	public void setNewPathID(Integer newPathID) {
		this.newPathID = newPathID;
	}

	public Integer getCurTaskID() {
		return curTaskID;
	}

	public void setCurTaskID(Integer curTaskID) {
		this.curTaskID = curTaskID;
	}

	public Integer getSucTaskID() {
		return sucTaskID;
	}

	public void setSucTaskID(Integer sucTaskID) {
		this.sucTaskID = sucTaskID;
	}
	
	

}
