package com.lbc.ma;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.lbc.ma.dataStructure.Workflow;

public class WorkflowMaintainer implements Runnable {
	protected Logger logger = Logger.getLogger(WorkflowMaintainer.class);

	// Vector<Workflow> WFInfo = Markov.WFInfo;

	int curr_times = 0;

	// 生产速率，控制工作流的生成速度，越大成产的越快
	double productionRate = 0.01;

	WorkflowGenerator wfGenerator = WorkflowGenerator.getWorkflowGenerator();

	@Override
	public void run() {
		logger.info("WorkflowMaintainer线程启动");
		Random ran = new Random();
		while (true) {

			// 模拟时间流逝
			synchronized (Markov.step_times) {
				if (curr_times == Markov.step_times) {
					continue;
				}
				// 当时间发生流逝的时候采取以下操作
				curr_times = Markov.step_times;
			}

			synchronized (Markov.WFInfo) {
				// 添加工作流
				double ranSeed = Double.valueOf(ran.nextInt(10000)) / 10000;
				if (ranSeed < productionRate) {
					logger.info("添加工作流...");
					synchronized (Markov.var_x_wtk) {
						Markov.old_x_wtk = (ArrayList<String>) Markov.var_x_wtk.clone();
					}
					int numOfWFExampleModel = wfGenerator.exampleWorkflows.size() + 1;
					Workflow aWorkflow = wfGenerator.generateAWorkflow_V2(ran.nextInt(numOfWFExampleModel));
					Markov.WFInfo.add(aWorkflow);
				}

				// 移除已经执行完毕的工作流

				for (Iterator<Workflow> WFIter = Markov.WFInfo.iterator(); WFIter.hasNext();) {
					Workflow wf = WFIter.next();
					if (wf.getDuration() <= 0) {
						Integer WF_ID = wf.getWF_ID();
						logger.info("移除ID为" + WF_ID + "的工作流");
						synchronized (Markov.var_x_wtk) {
							Markov.old_x_wtk = (ArrayList<String>) Markov.var_x_wtk.clone();
						}

						// 从WFInfo中移除
						WFIter.remove();
						// 删除var_x_wtk中的过期WF的信息&删除var_y_wpab中的过期WF的信息
						remove_X_Y(WF_ID);
					}
				}
			}
		}
	}

	/**
	 * 删除var_x_wtk中的过期WF的信息&删除var_y_wpab中的过期WF的信息
	 * 
	 * @param WF_ID
	 */
	private void remove_X_Y(Integer WF_ID) {
		// 删除var_x_wtk中的过期WF的信息
		synchronized (Markov.var_x_wtk) {
			for (Iterator<String> xIter = Markov.var_x_wtk.iterator(); xIter.hasNext();) {
				String x_wtk = xIter.next();
				String[] wtk = x_wtk.split(",");
				Integer wfId = Integer.valueOf(wtk[0]);
				if (wfId == WF_ID) {
					xIter.remove();
				}
			}
		}
		// 删除var_y_wpab中的过期WF的信息
		synchronized (Markov.var_y_wpab) {
			for (Iterator<String> yIter = Markov.var_y_wpab.iterator(); yIter.hasNext();) {
				String y_wpab = yIter.next();
				String[] wpab = y_wpab.split(",");
				Integer wfId = Integer.valueOf(wpab[0]);
				if (wfId == WF_ID) {
					yIter.remove();
				}
			}
		}
	}

	@Test
	public void testRan() {
		int sum = 0;
		Random ran = new Random();
		for (int i = 0; i < 10000; i++) {
			int num = ran.nextInt(1000);
			sum += num;
			System.out.println(num);
		}
		double result = sum / 10000;
		System.out.println(result);
		System.out.println(Double.valueOf(ran.nextInt(1000)) / 1000);
	}

}
