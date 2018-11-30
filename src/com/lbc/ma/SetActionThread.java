package com.lbc.ma;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.math3.util.Pair;

import com.lbc.ma.dataStructure.Flow;
import com.lbc.ma.dataStructure.Workflow;

public class SetActionThread extends Thread {
	
	private CountDownLatch countDownLatch; 

	List<Pair<Timer, Double>> pmf;
	List<Integer> listOfSatisfiedWF;
	ArrayList<String> var_y_wpab;
	ArrayList<String> var_x_wtk;

	public SetActionThread(CountDownLatch countDownLatch, List<Pair<Timer, Double>> pmf,
			List<Integer> listOfSatisfiedWF, ArrayList<String> var_y_wpab, ArrayList<String> var_x_wtk) {
		super();
		this.countDownLatch = countDownLatch;
		this.pmf = pmf;
		this.listOfSatisfiedWF = listOfSatisfiedWF;
		this.var_y_wpab = var_y_wpab;
		this.var_x_wtk = var_x_wtk;
	}

	public void run() {
		for (Integer WF_ID : listOfSatisfiedWF) {
			// List<Flow> lstTaskFlows = WFInfo.get(WF_ID);
			ArrayList<Flow> lstTaskFlows = null;
			for (Workflow wf : Markov.WFInfo) {
				if (wf.getWF_ID() == WF_ID)
					lstTaskFlows = wf.getFlows();
			}
			if (null != lstTaskFlows)
				for (Flow aFlow : lstTaskFlows) {
					int taskAID = aFlow.getCurrTask().getTaskId();
					int taskBID = aFlow.getSuccTask().getTaskId();
					if (taskBID != 0) {
						Pair<Timer, Double> action = Markov.setActionForATaskFlow(WF_ID, taskAID, taskBID, (ArrayList)var_y_wpab.clone(),
								(ArrayList)var_x_wtk.clone());
						if (action != null) {
							synchronized (pmf) {
								pmf.add(action);
							}
						}
					}
				}
		}
		// 倒数器减1  
        countDownLatch.countDown();  
	}

}
