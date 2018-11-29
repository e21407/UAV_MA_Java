package com.lbc.ma;

public class Timer {
	// {<WF_ID, taskA_ID, taskB_ID>:[ts_begin, timer_len, pathID_old, pathID_new,
	// UAVID_old, UAVID_new]}
	Integer WF_ID;

	Integer taskAID;

	Integer taskBID;

	Double ts_begin;

	Double timer_len;

	Integer oldPathID;

	Integer newPathID;

	Integer oldUAVID;

	Integer newUAVID;
	
	FakeReplaceReturnResult fakeReplaceReturnResult;

	public Timer(Integer WF_ID, Integer taskAID, Integer taskBID, Double ts_begin, Double timer_len, Integer oldPathID,
			Integer newPathID, Integer oldUAVID, Integer newUAVID, FakeReplaceReturnResult frrr) {
		this.WF_ID = WF_ID;
		this.taskAID = taskAID;
		this.taskBID = taskBID;
		this.ts_begin = ts_begin;
		this.timer_len = timer_len;
		this.oldPathID = oldPathID;
		this.newPathID = newPathID;
		this.oldUAVID = oldUAVID;
		this.newUAVID = newUAVID;
		this.fakeReplaceReturnResult = frrr;
	}

}
