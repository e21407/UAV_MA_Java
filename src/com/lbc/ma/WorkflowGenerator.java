package com.lbc.ma;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;


import com.lbc.ma.dataStructure.Flow;
import com.lbc.ma.dataStructure.Task;
import com.lbc.ma.dataStructure.Workflow;

/**
 * 这个类采用某个模板生产workflow
 * 
 * @author liubaichuan
 * @since 2018-10-11
 *
 */
public class WorkflowGenerator {
	// 单例模式
	private static WorkflowGenerator workflowGenerator = new WorkflowGenerator();
	/** workflow模板文件路径 */
	String filePath = "input_data/_input_Info_of_workflow_model.txt";

	/** workflow工作时间服从指数分布的lambda参数 */
	Double lambda = (double) (1.0 / 1000);

	/** 全局workflow id计数 */
	static int workflow_idx = 0;

	/** 工作流样例 */
	Map<Integer, Vector<Flow>> exampleWorkflows = new HashMap<>();

	private WorkflowGenerator() {
		initializeWorkflowModel();
	}

	public static synchronized WorkflowGenerator getWorkflowGenerator() {
		return workflowGenerator;
	}

	private void initializeWorkflowModel() {
		File file = new File(filePath);
		Double filelength = (double) file.length();
		byte[] fileContent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(fileContent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String strFileContent = null;
		try {
			strFileContent = new String(fileContent, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String[] lineOfContent = strFileContent.split("\r\n");
		for (String line : lineOfContent) {
			String[] lineContent = line.split("\t");
			Integer model_ID = Integer.valueOf(lineContent[1]);
			Integer currTaskID = Integer.valueOf(lineContent[3]);
			Integer succTaskID = Integer.valueOf(lineContent[5]);
			Task currTask = new Task(null, currTaskID, null);
			Task succTask = new Task(null, succTaskID, null);
			Flow flow = new Flow(currTask, succTask, null);
			Vector<Flow> example = exampleWorkflows.get(model_ID);
			if (example != null) {
				example.add(flow);
			} else {
				Vector<Flow> exam = new Vector<>();
				exam.add(flow);
				exampleWorkflows.put(model_ID, exam);
			}
		}
	}

	/**
	 * 根据WorkflowGenerator里面的工作流模板获取一个工作流，其工作时长符合指数分布。
	 * 
	 * @param idx
	 *            WorkflowGenerator模板集合中第idx个模板，idx>=0，当idx大于模板集合的长度时候，对其进行取余操作
	 * @return
	 */
	public Workflow generateAWorkflow_V1(int idx) {
		if (idx < 0)
			return null;
		int exampleSetSize = exampleWorkflows.size();
		idx = idx % exampleSetSize;
		Collection<Vector<Flow>> exampleSet = exampleWorkflows.values();
		for (Vector<Flow> flows : exampleSet) {
			if (--idx < 0) {
//				Double duration = Tool.randExp(lambda);
				Double duration = ParamInfo.duration;
				Integer WF_ID = workflow_idx++;
				Workflow wf = new Workflow(WF_ID, duration, null);
				for (Flow f : flows) {
					Random random = new Random();
					double bandwidth = random.nextInt(ParamInfo.bandwidthRdm) + ParamInfo.bandwidthBase;
					double curTaskCap = random.nextInt(ParamInfo.taskRdmCap) + ParamInfo.taskBaseCap;
					double sucTaskCap = random.nextInt(ParamInfo.taskRdmCap) + ParamInfo.taskBaseCap;
//					double curTaskCap = 500.0;
//					double sucTaskCap = 500.0;
					Task currTask = new Task(WF_ID, f.getCurrTask().getTaskId(), curTaskCap);
					Task succTask = new Task(WF_ID, f.getSuccTask().getTaskId(), sucTaskCap);
					// Flow toAddFlow = new Flow(f.currTaskID, f.succTaskID, bandwidth);
					Flow toAddFlow = new Flow(currTask, succTask, bandwidth);
					wf.addFlow(toAddFlow);
				}
				return wf;
			}
		}
		return null;
	}
	
	/**
	 * 第二个版本
	 * 根据WorkflowGenerator里面的工作流模板获取一个工作流，其工作时长符合指数分布。
	 * 
	 * @param idx
	 *            WorkflowGenerator模板集合中第idx个模板，idx>=0，当idx大于模板集合的长度时候，对其进行取余操作
	 * @return
	 */
	public Workflow generateAWorkflow_V2(int idx) {
		if(idx < 0) {
			return null;
		}
		int exampleSetSize = exampleWorkflows.size();
		idx = idx % exampleSetSize;
		Double duration = ParamInfo.duration;
		Integer WF_ID = workflow_idx++;
		Workflow wf = new Workflow(WF_ID, duration, null);
		Vector<Flow> exampleWorkflow = exampleWorkflows.get(idx);
		for (Flow f : exampleWorkflow) {
			Random random = new Random();
			double bandwidth = random.nextInt(ParamInfo.bandwidthRdm) + ParamInfo.bandwidthBase;
			double curTaskCap = random.nextInt(ParamInfo.taskRdmCap) + ParamInfo.taskBaseCap;
			double sucTaskCap = random.nextInt(ParamInfo.taskRdmCap) + ParamInfo.taskBaseCap;
			Task currTask = new Task(WF_ID, f.getCurrTask().getTaskId(), curTaskCap);
			Task succTask = new Task(WF_ID, f.getSuccTask().getTaskId(), sucTaskCap);
			Flow toAddFlow = new Flow(currTask, succTask, bandwidth);
			wf.addFlow(toAddFlow);
		}
		return wf;
	}

}
