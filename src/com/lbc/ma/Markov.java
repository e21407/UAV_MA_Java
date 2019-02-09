package com.lbc.ma;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;
import com.lbc.ma.dataStructure.Flow;
import com.lbc.ma.dataStructure.Workflow;
import com.lbc.ma.tool.Tool;

public class Markov {

	static protected Logger logger = Logger.getLogger(Markov.class);

	// input file name
	final static String CandPaths_file = "input_data/_input_PathSet3.txt";
	final static String CapLinks_file = "input_data/_input_Cap_links10000.txt";
	final static String Info_of_UAVs_file = "input_data/_input_Info_of_nodes.txt";
	final static String Info_of_WF_Chaneg = "input_data/WF_change_infoE.txt";

	/**
	 * {"Src-UAV,Dst-UAV":[path1_id,path2_id,...]}, the candidate path-set for all
	 * pair of UAVs. !!!! SPECIAL-CASE-20161109: EVEN a SW to itself has an
	 * individual path_ID, 2016-1109!!!!!
	 */
	static Map<String, List<Integer>> candPathIDSetFor2UAVs = new HashMap<>();

	/**
	 * {Path_id: pathContent}, e.g., pathContent:["1, 2", "2, 5", "5, 3"]; or EMPTY
	 * list [].
	 */
	static Map<Integer, List<String>> pathDatabase = new HashMap<>();

	/** {"workflow_id, task_id":[Cap]} */
	// Map<String, Double> taskInfo = new HashMap<>();

	/**
	 * {worlflow_ID: list_of_a_flow}, list_of_a_flow=[(currTask_ID, succTask_ID,
	 * needed_bandwidth)...]
	 */
	static ArrayList<Workflow> WFInfo = new ArrayList<>();
	// static Map<Integer, List<Flow>> WFInfo = new HashMap<>();

	/** {"u,v":CapVal}, the Capacity set of all links */
	Map<String, Double> capLinks = new HashMap<>();

	/** {Node_ID:CapVal}, the compute capacity of all nodes */
	static Map<Integer, Double> nodeInfo = new HashMap<>();

	/** list that record the ID of UAVs */
	static List<Integer> lstUAV = new ArrayList<>();

	/** list that record the ID of edge server */
	static List<Integer> lstEdgeServer = new ArrayList<>();

	/** list of UAV's ID which can be assigned task */
	static List<Integer> lstCloudServer = new ArrayList<>();

	/** list that record the ID of cloud server */
	static List<Integer> lstAssignableNode_ID = new ArrayList<>();

	/**
	 * {"workflow_ID, task_ID, UAV_ID": int},x_(k)^(w,t) == 1,if task t of workflow
	 * w is assigned to UAV k
	 */
	static ArrayList<String> var_x_wtk = new ArrayList<>();

	/**
	 * {"workflow_ID, task_ID, UAV_ID": int},x_(k)^(w,t) == 1,if task t of workflow
	 * w is assigned to UAV k
	 */
	static ArrayList<String> old_x_wtk = new ArrayList<>();

	/**
	 * {"workflow_ID, path_ID, task_a_ID, task_b_ID"}, y_(a,b)^(w,p) == 1, if task a
	 * and task b in workflow w use path p to transfer data
	 */
	static ArrayList<String> var_y_wpab = new ArrayList<>();

	static EnumeratedDistribution<?> actions;

	final int Global_PATH_ID_START_COUNTING = 0;

	/**
	 * {(u,v): aggregated-TR-in-arc-uv }. This variable record the aggregated
	 * traffic rate on each UAV link
	 */
	static // Map<String, Double> Aggregated_TR_in_acrs = new HashMap<>();
	double global_system_throughput = 0.0;
	static double global_weighted_RoutingCost = 0.0;
	static double global_weighted_computeCost = 0.0;
	final double global_weighted_throughput = 0.0;
	final static double WEIGHT_OF_COMPUTE_COST = ParamInfo.WEIGHT_OF_COMPUTE_COST;
	final static double WEIGHT_OF_ROUTING_COST = ParamInfo.WEIGHT_OF_ROUTING_COST;
	final static double WEIGHT_OF_THROUGHPUT = ParamInfo.WEIGHT_OF_THROUGHPUT;
	final static double weight_a_com = ParamInfo.weight_a_com;
	final static double weight_b_rou = ParamInfo.weight_b_rou;

	final static double UAVLinkCoefficient = ParamInfo.UAVLinkCoefficient;
	final static double EdgeServerLinkCoefficient = ParamInfo.EdgeServerLinkCoefficient;
	final static double CloudServerLinkCoefficient = ParamInfo.CloudServerLinkCoefficient;

	/** The total running period of system. */
	static double T = ParamInfo.T;
	/**
	 * The length of time-slot, e.g., 0.001 second is the BEST step after testing.
	 */
	static double STEP_TO_RUN = ParamInfo.STEP_TO_RUN;
	/**
	 * The step (length of interval) of check timer-expiration, e.g., 0.1 second.
	 */
	static double STEP_TO_CHECK_TIMER = STEP_TO_RUN;
	/** The parameter in the theoretical derivation. */
	static double Beta = ParamInfo.Beta;
	/** The alpha regarding the Markov_Chain. */
	double Tau = ParamInfo.Tau;
	static Integer step_times = 1;
	static Integer iterationNum = 1;

	/** 生产速率，控制工作流的生成速度，越大成产的越快 */
	static double productionRate = ParamInfo.productionRate;

	/** virtual queue Q(t) */
	static Double Qt = 0.0;

	// static Double Qt_old = 0.0;

	/** long-term time-averaged migrate cost budget */
	static Double M_avg = ParamInfo.M_avg;

	static final Double V = ParamInfo.V;

	static Double migrationCost = 0.0;

	static Double allMigrationCost = 0.0;

	static WorkflowGenerator wfGenerator = WorkflowGenerator.getWorkflowGenerator();

	/** 表示系统中运行的工作流的信息是否发生了变化 */
	static boolean WFInfoChangeFlag = false;

	/** 迁移代价队列的变更次数 */
	static Integer queueTime = -1;

	static Queue<Pair<Integer, String>> WFChangeInfo;

	static Random randomTool = new Random();

	static Map<String, Double> migCostListOf2Node = new HashMap<>();

	static final int numOfThread = 3;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void initializeReadData(String inCandPathsFile, String inCapLinksFile, String inInfoOfUAVsFile) {
		logger.info("开始初始化读取数据...");
		long startTime = System.currentTimeMillis(); // 计算读取准备数据的时间

		// _input_PathSet.txt///////////////////////////////////////
		int uniquePathID_idx = Global_PATH_ID_START_COUNTING;
		String strFileContent = Tool.getStringFromFile(inCandPathsFile);
		String[] lineOfContent = strFileContent.split("\r\n");
		for (String aLine : lineOfContent) {
			if (aLine.length() <= 0)
				continue;
			String[] lineContent = aLine.split("\t");
			int srcUAV_ID = Integer.valueOf(lineContent[1]);
			int dstUAV_ID = Integer.valueOf(lineContent[3]);
			String strPath = lineContent[5];

			if (!lstAssignableNode_ID.contains(srcUAV_ID))
				lstAssignableNode_ID.add(srcUAV_ID);
			if (!lstAssignableNode_ID.contains(dstUAV_ID))
				lstAssignableNode_ID.add(dstUAV_ID);

			String[] pathContent = strPath.split(">");

			List<String> onePathInSeg = new ArrayList<>();
			for (int i = 0; i < pathContent.length - 1; i++)
				onePathInSeg.add(pathContent[i] + "," + pathContent[i + 1]);

			// if (!pathDatabase.containsKey(uniquePathID_idx))
			pathDatabase.put(uniquePathID_idx, onePathInSeg);

			String _2uav = srcUAV_ID + "," + dstUAV_ID;
			if (!candPathIDSetFor2UAVs.containsKey(_2uav)) {
				List<Integer> paths = new ArrayList<>();
				paths.add(uniquePathID_idx);
				candPathIDSetFor2UAVs.put(_2uav, paths);
			} else {
				candPathIDSetFor2UAVs.get(_2uav).add(uniquePathID_idx);
			}
			uniquePathID_idx++;

			// reverse path
			List<String> reversePath = new ArrayList<>();
			for (int i = pathContent.length - 1; i > 0; i--)
				reversePath.add(pathContent[i] + "," + pathContent[i - 1]);

			pathDatabase.put(uniquePathID_idx, reversePath);

			_2uav = dstUAV_ID + "," + srcUAV_ID;
			if (!candPathIDSetFor2UAVs.containsKey(_2uav)) {
				List<Integer> paths = new ArrayList<>();
				paths.add(uniquePathID_idx);
				candPathIDSetFor2UAVs.put(_2uav, paths);
			} else {
				candPathIDSetFor2UAVs.get(_2uav).add(uniquePathID_idx);
			}
			uniquePathID_idx++;
		}

		// _input_Cap_links.txt///////////////////////////////////////////
		strFileContent = Tool.getStringFromFile(inCapLinksFile);
		lineOfContent = strFileContent.split("\r\n");
		for (String line : lineOfContent) {
			String[] lineContent = line.split("\t");
			String uID = lineContent[1];
			String vID = lineContent[3];
			double capVal = Double.valueOf(lineContent[5]);
			String key = uID + "," + vID;
			if (!capLinks.containsKey(key))
				capLinks.put(key, capVal);
			// if (!Aggregated_TR_in_acrs.containsKey(key))
			// Aggregated_TR_in_acrs.put(key, 0.0);
		}

		// _input_Info_of_nodes.txt//////////////////////////////////////
		strFileContent = Tool.getStringFromFile(inInfoOfUAVsFile);
		lineOfContent = strFileContent.split("\r\n");
		for (String line : lineOfContent) {
			String[] lineContent = line.split("\t");
			String nodeType = lineContent[0];
			int nodeID = Integer.valueOf(lineContent[1]);
			double capVal = Double.valueOf(lineContent[3]);
			// double capVal = Double.valueOf(100);

			if ("U_ID".equals(nodeType) && !lstUAV.contains(nodeID))
				lstUAV.add(nodeID);
			if ("E_ID".equals(nodeType) && !lstEdgeServer.contains(nodeID))
				lstEdgeServer.add(nodeID);
			if ("C_ID".equals(nodeType) && !lstCloudServer.contains(nodeID))
				lstCloudServer.add(nodeID);
			if (!nodeInfo.containsKey(nodeID))
				nodeInfo.put(nodeID, capVal);
		}

		// 计算读取准备数据文件的时间
		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime);
		durTime = durTime.divide(new BigDecimal(1000));
		logger.info("读取处理文件耗时：" + durTime.setScale(4) + "s");
	}

	/**
	 * randomly assign task to UAV and find a path for the tasks have communication
	 * 
	 * @param WFs
	 */
	private void assignTaskToUAVRandomly(ArrayList<Workflow> WFs) {
		// logger.debug("初始化随机分配工作流到系统...");
		long startTime = System.currentTimeMillis();

		int numOfUAVs = lstUAV.size();
		for (Workflow wf : WFs) {
			int WF_ID = wf.getWF_ID();
			for (Flow flow : wf.getFlows()) {
				int currTaskID = flow.getCurrTask().getTaskId();
				int succTaskID = flow.getSuccTask().getTaskId();

				int tryNo = 10;
				while (tryNo-- > 0) {
					Integer uID1 = checkWetherATaskHasAssignment(WF_ID, currTaskID);
					Integer uID2 = checkWetherATaskHasAssignment(WF_ID, succTaskID);
					Integer idxUAV1 = null, idxUAV2 = null;
					if (null == uID1) {
						idxUAV1 = randomTool.nextInt(numOfUAVs);
						uID1 = lstUAV.get(idxUAV1);
					}
					if (null == uID2) {
						if (succTaskID == 0) {
							idxUAV2 = numOfUAVs - 1; // 特殊id的任务，固定布置到云端执行
						}
						idxUAV2 = randomTool.nextInt(numOfUAVs);
						if (idxUAV1 == idxUAV2) {
							if (idxUAV1 == numOfUAVs - 1) { // 避免同一个节点执行一个flow中的2个任务
								idxUAV1--;
							} else {
								idxUAV1++;
							}
						}
						uID2 = lstUAV.get(idxUAV2);
					}

					List<Integer> pathIDList = findPathIDListForAPairOfUAVs(uID1, uID2);
					if (pathIDList != null) {
						int idxPath = randomTool.nextInt(pathIDList.size());
						int pathID = pathIDList.get(idxPath);
						// if (!checkWhetherAPathIsFeasibleToTheTaskSegment(pathID, WF_ID, currTaskID,
						// succTaskID)) {
						// tryNo--;
						// continue;
						// }
						// 执行分配
						if (null == checkWetherATaskHasAssignment(WF_ID, currTaskID))
							var_x_wtk.add(WF_ID + "," + currTaskID + "," + uID1);
						if (null == checkWetherATaskHasAssignment(WF_ID, succTaskID))
							var_x_wtk.add(WF_ID + "," + succTaskID + "," + uID2);
						OneAPath(pathID, WF_ID, currTaskID, succTaskID, var_y_wpab);
						break;
					}
				}
			}
		}
		// System.out.println("分配完毕");
		// printVarX();
		// printVarY();

		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		logger.debug("分配工作流耗时：" + durTime.setScale(8) + "s");
	}

	private Queue<Pair<Integer, String>> initWFChangeInfo(String filePath) {
		String strFileContent = Tool.getStringFromFile(filePath);
		String[] lineOfContent = strFileContent.split("\r\n");
		Queue<Pair<Integer, String>> WFChangeInfo = new LinkedList<>();
		for (String line : lineOfContent) {
			if (!"".equals(line.trim())) {
				String[] items = line.split("\t");
				Integer timeSlot = Integer.parseInt(items[1]);
				String action = items[3];
				Pair<Integer, String> toAdd = new Pair<Integer, String>(timeSlot, action);
				WFChangeInfo.add(toAdd);
			}
		}
		return WFChangeInfo;
	}

	private static List<Integer> findPathIDListForAPairOfUAVs(int uID1, int uID2) {
		String key = uID1 + "," + uID2;
		return candPathIDSetFor2UAVs.get(key);
	}

	// private boolean checkWhetherAPathIsFeasibleToTheTaskSegment(int pathID, int
	// WF_ID, int taskAID, int taskBID) {
	// List<String> pathContent = pathDatabase.get(pathID);
	// if (pathContent != null) {
	// double neededBandwidth = getTheNeedBandwidthOfATaskFlow(WF_ID, taskAID,
	// taskBID);
	// for (String item : pathContent) {
	// Double aggregatedTR = Aggregated_TR_in_acrs.get(item);
	// Double linkCap = capLinks.get(item);
	// if (linkCap - aggregatedTR < neededBandwidth)
	// return false;
	// }
	// }
	// return true;
	// }

	private static double getTheNeedBandwidthOfATaskFlow(int WF_ID, int taskAID, int taskBID) {
		double neededBandwidth = -1;
		// List<Flow> flows = WFInfo.get(WF_ID);

		ArrayList<Flow> flows = null;
		for (Workflow wf : WFInfo) {
			if (wf.getWF_ID() == WF_ID)
				flows = wf.getFlows();
		}
		for (Flow flow : flows) {
			if (flow.getCurrTask().getTaskId().intValue() == taskAID
					&& flow.getSuccTask().getTaskId().intValue() == taskBID) {
				neededBandwidth = flow.getNeededBandwidth().doubleValue();
				break;
			}
		}

		return neededBandwidth;
	}

	/**
	 * 查找并返回执行某个任务的节点id，如果没有则返回null。
	 * 
	 * @param WF_ID
	 * @param taskID
	 * @return
	 */
	private Integer checkWetherATaskHasAssignment(int WF_ID, int taskID) {
		for (String item : var_x_wtk) {
			String[] elem = item.split(",");
			int _WF_ID = Integer.valueOf(elem[0]);
			int _taskID = Integer.valueOf(elem[1]);
			if (WF_ID == _WF_ID && taskID == _taskID)
				return Integer.valueOf(elem[2]);
		}
		return null;
	}

	private static void OneAPath(int pathID, int WF_ID, int taskAID, int taskBID, List<String> var_y_wpab) {
		// List<String> pathContent = pathDatabase.get(pathID);
		// double neededBandwidth = getTheNeedBandwidthOfATaskFlow(WF_ID, taskAID,
		// taskBID);
		// for (String item : pathContent) {
		// Double TR = Aggregated_TR_in_acrs.get(item);
		// TR += neededBandwidth;
		// Aggregated_TR_in_acrs.replace(item, TR);
		// }
		var_y_wpab.add(WF_ID + "," + pathID + "," + taskAID + "," + taskBID);
	}

	private static void updateSystemMetrics() {
		// logger.debug("计算系统性能指标...");
		long startTime = System.currentTimeMillis();
		// moveUnsatisfiedWFFromUAVs();
		double system_throughput = 0.0;
		double routingCost = 0.0;
		double computeCost = 0.0;

		List<Integer> listOfSatisfiedWF_ID = getListOfSatisfiedWF();
		for (Integer WF_ID : listOfSatisfiedWF_ID) {
			// List<Flow> flows = WFInfo.get(WF_ID);
			ArrayList<Flow> flows = null;
			for (Workflow wf : WFInfo) {
				if (wf.getWF_ID() == WF_ID)
					flows = wf.getFlows();
			}
			if (flows != null) {
				for (Flow aFlow : flows) {
					int taskAID = aFlow.getCurrTask().getTaskId();
					int taskBID = aFlow.getSuccTask().getTaskId();
					double bandwidth = aFlow.getNeededBandwidth();
					system_throughput += bandwidth;
					int IUPathID = getTheIUPathIDBetweenTwoTask(WF_ID, taskAID, taskBID);
					if (IUPathID != -1) {
						List<String> paths = pathDatabase.get(IUPathID);
						for (String string : paths) {
							String[] sstr = string.split(",");
							int UAV1 = Integer.valueOf(sstr[0]);
							int UAV2 = Integer.valueOf(sstr[1]);
							if ((lstEdgeServer.contains(UAV2) && lstEdgeServer.contains(UAV1))
									|| (lstEdgeServer.contains(UAV1) && lstUAV.contains(UAV2))
									|| (lstEdgeServer.contains(UAV2) && lstUAV.contains(UAV1))) {
								routingCost += 1 * EdgeServerLinkCoefficient;
							} else if ((lstCloudServer.contains(UAV2) && lstCloudServer.contains(UAV1))
									|| (lstCloudServer.contains(UAV1) && lstEdgeServer.contains(UAV2))
									|| (lstCloudServer.contains(UAV2) && lstEdgeServer.contains(UAV1))) {
								routingCost += 1 * CloudServerLinkCoefficient;
							} else
								routingCost += 1 * UAVLinkCoefficient;
						}
					}
				}
			}

		}
		Set<Entry<Integer, Double>> nodes = nodeInfo.entrySet();
		for (Entry<Integer, Double> node : nodes) {
			// "WF_ID,task_id"
			List<String> taskListAssignedToAUAV = getTheTaskListAssignedToAUAV(node.getKey());
			double totalCapOfAUAV = 0;
			for (String string : taskListAssignedToAUAV) {
				String[] strs = string.split(",");
				Integer WF_ID = Integer.valueOf(strs[0]);
				Integer task_id = Integer.valueOf(strs[1]);
				FindTask: for (Workflow wf : WFInfo) {
					if (wf.getWF_ID() == WF_ID) {
						for (Flow f : wf.getFlows()) {
							if (task_id == f.getCurrTask().getTaskId()) {
								// totalCapOfAUAV += f.getCurrTask().getNeededResource() *
								// taskNumAssignedToANode;
								totalCapOfAUAV += f.getCurrTask().getNeededResource();
								break FindTask;
							}
							if (task_id == f.getSuccTask().getTaskId()) {
								// totalCapOfAUAV += f.getSuccTask().getNeededResource() *
								// taskNumAssignedToANode;
								totalCapOfAUAV += f.getSuccTask().getNeededResource();
								break FindTask;
							}
						}
					}
				}

			}
			// 计算代价=(节点需要处理的计算资源需求 / 节点可以提供的计算资源)^2
			computeCost += Math.pow(totalCapOfAUAV / node.getValue(), 2);
		}
		double weighted_routing_cost = WEIGHT_OF_ROUTING_COST * routingCost;
		double weighted_compute_cost = WEIGHT_OF_COMPUTE_COST * computeCost;
		double weighted_throughput = WEIGHT_OF_THROUGHPUT * system_throughput;
		global_system_throughput = weighted_throughput;
		global_weighted_RoutingCost = weighted_routing_cost;
		global_weighted_computeCost = weighted_compute_cost;

		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		logger.debug("updateSystemMetrics耗时：" + durTime.setScale(8) + "s");
	}

	private static double updateSystemMetricsAndReturnSystenObj(List<String> var_y_wpab, List<String> var_x_wtk) {
		// logger.debug("计算系统性能指标...");
		long startTime = System.currentTimeMillis();
		// moveUnsatisfiedWFFromUAVs();
		double system_throughput = 0.0;
		double routingCost = 0.0;
		double computeCost = 0.0;

		List<Integer> listOfSatisfiedWF_ID = getListOfSatisfiedWF();
		for (Integer WF_ID : listOfSatisfiedWF_ID) {
			// List<Flow> flows = WFInfo.get(WF_ID);
			ArrayList<Flow> flows = null;
			for (Workflow wf : WFInfo) {
				if (wf.getWF_ID() == WF_ID)
					flows = wf.getFlows();
			}
			if (flows != null) {
				for (Flow aFlow : flows) {
					int taskAID = aFlow.getCurrTask().getTaskId();
					int taskBID = aFlow.getSuccTask().getTaskId();
					double bandwidth = aFlow.getNeededBandwidth();
					system_throughput += bandwidth;
					int IUPathID = -1;
					// ----getTheIUPathIDBetweenTwoTask(WF_ID, taskAID, taskBID);---
					for (String str : var_y_wpab) {
						String[] sstr = str.split(",");
						int w = Integer.valueOf(sstr[0]).intValue();
						int a = Integer.valueOf(sstr[2]).intValue();
						int b = Integer.valueOf(sstr[3]).intValue();
						if (w == WF_ID && a == taskAID && b == taskBID) {
							int pathID = Integer.valueOf(sstr[1]).intValue();
							IUPathID = pathID;
							break;
						}
					}
					// -------------------------------------------------------------
					if (IUPathID != -1) {
						List<String> paths = pathDatabase.get(IUPathID);
						for (String string : paths) {
							String[] sstr = string.split(",");
							int UAV1 = Integer.valueOf(sstr[0]);
							int UAV2 = Integer.valueOf(sstr[1]);
							if ((lstEdgeServer.contains(UAV2) && lstEdgeServer.contains(UAV1))
									|| (lstEdgeServer.contains(UAV1) && lstUAV.contains(UAV2))
									|| (lstEdgeServer.contains(UAV2) && lstUAV.contains(UAV1))) {
								routingCost += 1 * EdgeServerLinkCoefficient;
							} else if ((lstCloudServer.contains(UAV2) && lstCloudServer.contains(UAV1))
									|| (lstCloudServer.contains(UAV1) && lstEdgeServer.contains(UAV2))
									|| (lstCloudServer.contains(UAV2) && lstEdgeServer.contains(UAV1))) {
								routingCost += 1 * CloudServerLinkCoefficient;
							} else
								routingCost += 1 * UAVLinkCoefficient;
						}
					}
				}
			}

		}
		Set<Entry<Integer, Double>> nodes = nodeInfo.entrySet();
		for (Entry<Integer, Double> node : nodes) {
			// "WF_ID,task_id"
			List<String> taskListAssignedToAUAV = new ArrayList<>();
			// ---getTheTaskListAssignedToAUAV(node.getKey());-----
			for (String str : var_x_wtk) {
				String[] sstr = str.split(",");
				int u = Integer.valueOf(sstr[2]);
				if (u == node.getKey()) {
					taskListAssignedToAUAV.add(sstr[0] + "," + sstr[1]);
				}
			}
			// ----------------------------------------------------
			double totalCapOfAUAV = 0;
			for (String string : taskListAssignedToAUAV) {
				String[] strs = string.split(",");
				Integer WF_ID = Integer.valueOf(strs[0]);
				Integer task_id = Integer.valueOf(strs[1]);
				FindTask: for (Workflow wf : WFInfo) {
					if (wf.getWF_ID() == WF_ID) {
						for (Flow f : wf.getFlows()) {
							if (task_id == f.getCurrTask().getTaskId()) {
								totalCapOfAUAV += f.getCurrTask().getNeededResource();
								break FindTask;
							}
							if (task_id == f.getSuccTask().getTaskId()) {
								totalCapOfAUAV += f.getSuccTask().getNeededResource();
								break FindTask;
							}
						}
					}
				}

			}
			// 计算代价=(节点需要处理的计算资源需求 / 节点可以提供的计算资源)^2
			computeCost += Math.pow(totalCapOfAUAV / node.getValue(), 2);
		}
		double weighted_routing_cost = WEIGHT_OF_ROUTING_COST * routingCost;
		double weighted_compute_cost = WEIGHT_OF_COMPUTE_COST * computeCost;
		double weighted_throughput = WEIGHT_OF_THROUGHPUT * system_throughput;

		double xf = weighted_throughput - weight_b_rou * weighted_routing_cost - weight_a_com * weighted_compute_cost;
		double queueBlocak = getQueueBlock(Qt, migrationCost, M_avg);
		double mcost = getMigrationCosts(old_x_wtk, var_x_wtk);

		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		logger.debug("updateSystemMetricsAndReturnSystenObj耗时：" + durTime.setScale(8) + "s");
		return V * xf - queueBlocak * (mcost - M_avg);
	}

	// private void moveUnsatisfiedWFFromUAVs() {
	// ArrayList<Workflow> listOfUnsatisfiedWF = getListOfUnsatisfiedWF();
	// for (Workflow workflow : listOfUnsatisfiedWF) {
	// int WF_ID = workflow.getWF_ID();
	// List<String> toRemove = new ArrayList<String>();
	// for (String astr : var_x_wtk) {
	// String[] sstr = astr.split(",");
	// if (WF_ID == Integer.valueOf(sstr[0]).intValue()) {
	// toRemove.add(astr);
	// }
	// }
	// for (String string : toRemove) {
	// var_x_wtk.remove(string);
	// }
	// }
	// }

	private ArrayList<Workflow> getListOfUnsatisfiedWF() {
		ArrayList<Workflow> resultList = new ArrayList<>();
		List<Integer> listOfSatisfiedWF = getListOfSatisfiedWF();
		for (Workflow wf : WFInfo) {
			if (!listOfSatisfiedWF.contains(wf.getWF_ID())) {
				resultList.add(wf);
			}
		}

		return resultList;
	}

	private static List<Integer> getListOfSatisfiedWF() {
		List<Integer> retSatisfiedWF_ID = new ArrayList<Integer>();
		boolean flagOfAWF = false;
		for (Workflow wf : WFInfo) {
			flagOfAWF = true;
			int WF_ID = wf.getWF_ID();
			for (Flow aFlow : wf.getFlows()) {
				boolean flagOfAFlow = false;
				for (String str : var_y_wpab) {
					String[] items = str.split(",");
					int w = Integer.valueOf(items[0]);
					int a = Integer.valueOf(items[2]);
					int b = Integer.valueOf(items[3]);
					if (w == WF_ID && a == aFlow.getCurrTask().getTaskId() && b == aFlow.getSuccTask().getTaskId()) {
						flagOfAFlow = true;
						break;
					}
				}
				if (!flagOfAFlow) {
					flagOfAWF = false;
					break;
				}
			}
			if (flagOfAWF) {
				retSatisfiedWF_ID.add(WF_ID);
			}
		}

		return retSatisfiedWF_ID;
	}

	private static int getTheIUPathIDBetweenTwoTask(int WF_ID, int taskA_ID, int taskB_ID) {
		for (String str : var_y_wpab) {
			String[] sstr = str.split(",");
			int w = Integer.valueOf(sstr[0]).intValue();
			int a = Integer.valueOf(sstr[2]).intValue();
			int b = Integer.valueOf(sstr[3]).intValue();
			if (w == WF_ID && a == taskA_ID && b == taskB_ID) {
				int pathID = Integer.valueOf(sstr[1]).intValue();
				return pathID;
			}
		}
		return -1;
	}

	private static List<String> getTheTaskListAssignedToAUAV(int UAV_ID) {
		List<String> retResult = new ArrayList<>();
		for (String str : var_x_wtk) {
			String[] sstr = str.split(",");
			int u = Integer.valueOf(sstr[2]);
			if (u == UAV_ID) {
				retResult.add(sstr[0] + "," + sstr[1]);
			}
		}
		return retResult;
	}

	private void printCurrentSysInfo() {
		double Sys_performance = global_system_throughput - global_weighted_RoutingCost - global_weighted_computeCost;
		// System.out.printf("-step:%d
		// -performance\t%.2f\t-thr\t%.2f\t-RoutingCost\t%.2f\t-computeCost\t%.2f\n",
		// step_times, Sys_performance, global_system_throughput,
		// global_weighted_RoutingCost,
		// global_weighted_computeCost);
		int qt = queueTime > 0 ? queueTime : 0;
		double m_avg = queueTime > 0 ? (allMigrationCost / queueTime) : 0;
		String infoStr = String.format(
				"t: %d  p: %.2f  thr: %.2f  RCost: %.2f  CCost: %.2f  MCost: %.2f  Q(t): %.2f  Mavg: %.2f  qt: %d",
				step_times, Sys_performance, global_system_throughput, global_weighted_RoutingCost,
				global_weighted_computeCost, migrationCost, Qt, m_avg, qt);
		logger.info(infoStr);

	}

	private EnumeratedDistribution<?> setActionForAllTaskFlows(List<String> var_y_wpab, List<String> var_x_wtk) {
		logger.debug("设置全部action...");
		long startTime = System.currentTimeMillis();

		List<Pair<Timer, Double>> pmf = new ArrayList<Pair<Timer, Double>>();
		List<Integer> listOfSatisfiedWF = getListOfSatisfiedWF();

		// ---------------------------------------------------
		CountDownLatch countDownLatch = new CountDownLatch(numOfThread);
		for (int i = 0; i < numOfThread; i++) {
			List<Integer> subList = listOfSatisfiedWF.subList(listOfSatisfiedWF.size() / numOfThread * i,
					listOfSatisfiedWF.size() / numOfThread * (i + 1));
			SetActionThread setActionThread = new SetActionThread(countDownLatch, pmf, subList, (ArrayList) var_y_wpab,
					(ArrayList) var_x_wtk);
			setActionThread.start();
		}

		// List<Integer> subList1 = listOfSatisfiedWF.subList(0,
		// listOfSatisfiedWF.size() / 3);
		// List<Integer> subList2 = listOfSatisfiedWF.subList(listOfSatisfiedWF.size() /
		// 3,
		// listOfSatisfiedWF.size() / 3 * 2);
		// List<Integer> subList3 = listOfSatisfiedWF.subList(listOfSatisfiedWF.size() /
		// 3 * 2, listOfSatisfiedWF.size());
		// CountDownLatch countDownLatch = new CountDownLatch(3);
		// SetActionThread t1 = new SetActionThread(countDownLatch, pmf, subList1,
		// (ArrayList) var_y_wpab,
		// (ArrayList) var_x_wtk);
		// SetActionThread t2 = new SetActionThread(countDownLatch, pmf, subList2,
		// (ArrayList) var_y_wpab,
		// (ArrayList) var_x_wtk);
		// SetActionThread t3 = new SetActionThread(countDownLatch, pmf, subList3,
		// (ArrayList) var_y_wpab,
		// (ArrayList) var_x_wtk);
		//
		// t1.start();
		// t2.start();
		// t3.start();
		try {
			// 阻塞当前线程，直到倒数计数器倒数到0
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// ****************************************************
		// for (Integer WF_ID : listOfSatisfiedWF) {
		// // List<Flow> lstTaskFlows = WFInfo.get(WF_ID);
		// ArrayList<Flow> lstTaskFlows = null;
		// for (Workflow wf : WFInfo) {
		// if (wf.getWF_ID() == WF_ID)
		// lstTaskFlows = wf.getFlows();
		// }
		// if (null != lstTaskFlows)
		// for (Flow aFlow : lstTaskFlows) {
		// int taskAID = aFlow.getCurrTask().getTaskId();
		// int taskBID = aFlow.getSuccTask().getTaskId();
		// if (taskBID != 0) {
		// Pair<Timer, Double> action = setActionForATaskFlow(WF_ID, taskAID, taskBID,
		// var_y_wpab,
		// var_x_wtk);
		// if (action != null) {
		// pmf.add(action);
		// }
		// }
		// }
		// }
		// ********************************************************
		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		logger.debug("设置全部action耗时：" + durTime.setScale(8) + "s");
		return new EnumeratedDistribution(pmf);
	}

	static Pair<Timer, Double> setActionForATaskFlow(int WF_ID, int taskAID, int taskBID, ArrayList<String> var_y_wpab,
			ArrayList<String> var_x_wtk) {
		long startTime = System.currentTimeMillis();

		int feasibleNewUAV_IDRdm = selectARdmNIU_UAVForTheTask(WF_ID, taskBID);
		if (-1 == feasibleNewUAV_IDRdm)
			return null;

		int UAV_IDNew = feasibleNewUAV_IDRdm;
		int UAV_IDOld = getTheIU_UAV_IDOfATask(WF_ID, taskBID);
		int pathIDOld = getTheIUPathIDBetweenTwoTask(WF_ID, taskAID, taskBID);
		int UAV_IDOfTaskA = getTheIU_UAV_IDOfATask(WF_ID, taskAID);
		int pathIDNew = selectARdmPathForAPairOfUAVs(UAV_IDOfTaskA, UAV_IDNew);
		// if (!checkWhetherAPathIsFeasibleToTheTaskSegment(pathIDNew, WF_ID, taskAID,
		// taskBID))
		// return null;
		updateSystemMetrics();
		double Xf = getObjValOfConfigurationsInWholeSystem();
		FakeReplaceReturnResult fakeReplaceResult = fakeReplaceUAVorPathForATaskToReturnEstimatedSysObj(WF_ID, taskAID,
				taskBID, UAV_IDOld, UAV_IDNew, pathIDOld, pathIDNew, var_y_wpab, var_x_wtk);
		double Xf_prime = fakeReplaceResult.estimatedSysObj;
		double expItem = Math.exp(0.5 * Beta * (Xf_prime - Xf)) + Double.MIN_VALUE/* + 0.00000001 */;
		// System.out.println("Xf_prime - Xf : " + (Xf_prime - Xf));
		// System.out.println("expItem: " + expItem);
		// 避免Infinity的情况
		expItem = expItem > Double.MAX_VALUE ? Double.MAX_VALUE : expItem;
		// 避免0.0的情况
		expItem = expItem < Double.MIN_VALUE ? Double.MIN_VALUE : expItem;
		Timer timer = new Timer(WF_ID, taskAID, taskBID, null, null, pathIDOld, pathIDNew, UAV_IDOld, UAV_IDNew,
				fakeReplaceResult);

		Pair<Timer, Double> pair = new Pair<Timer, Double>(timer, expItem);
		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		logger.debug("设置单个action耗时：" + durTime.setScale(8) + "s");
		return pair;
	}

	private static int selectARdmNIU_UAVForTheTask(int WF_ID, int taskID) {
		int retNIU_UAV_ID = -1;
		List<Integer> lstNIU_UAV = getListOfNIU_UAV_IDToTheTask(WF_ID, taskID);
		if (!lstNIU_UAV.isEmpty()) {
			int idxTargetUAV = randomTool.nextInt(lstNIU_UAV.size());
			retNIU_UAV_ID = lstNIU_UAV.get(idxTargetUAV);
		}
		return retNIU_UAV_ID;
	}

	private static List<Integer> getListOfNIU_UAV_IDToTheTask(int WF_ID, int taskID) {
		int IU_UAV_ID = getTheIU_UAV_IDOfATask(WF_ID, taskID);
		List<Integer> resultList = new ArrayList<>();
		for (Integer UAV_ID : lstAssignableNode_ID) {
			if (IU_UAV_ID != UAV_ID)
				resultList.add(UAV_ID);
		}
		return resultList;
	}

	private static int getTheIU_UAV_IDOfATask(int WF_ID, int taskID) {
		for (String str : var_x_wtk) {
			String[] sstr = str.split(",");
			int w = Integer.valueOf(sstr[0]);
			int t = Integer.valueOf(sstr[1]);
			if (w == WF_ID && t == taskID) {
				int u = Integer.valueOf(sstr[2]);
				return u;
			}
		}
		return -1;
	}

	private static int selectARdmPathForAPairOfUAVs(int UAV1_ID, int UAV2_ID) {
		List<Integer> candPath = findPathIDListForAPairOfUAVs(UAV1_ID, UAV2_ID);
		if (candPath == null || candPath.isEmpty())
			return -1;
		int idxTargetPath = randomTool.nextInt(candPath.size());
		return candPath.get(idxTargetPath);
	}

	private static Double getCostOfRdmShortestPathForAPairOfUAVs(int UAV1_ID, int UAV2_ID) {
		// long startTime = System.currentTimeMillis();
		String key = UAV1_ID + "," + UAV2_ID;
		Double cost = migCostListOf2Node.get(key);
		if (null != cost) {
			return cost;
		} else {
			key = UAV2_ID + "," + UAV1_ID;
			cost = migCostListOf2Node.get(key);
			if (null != cost) {
				return cost;
			}
		}

		List<Integer> candPath = findPathIDListForAPairOfUAVs(UAV1_ID, UAV2_ID);
		List<String> resultPath = null;
		int shortestLength = Integer.MAX_VALUE;
		for (Integer pathID : candPath) {
			List<String> path = pathDatabase.get(pathID);
			if (shortestLength > path.size()) {
				shortestLength = path.size();
				resultPath = path;
			}
		}
		migCostListOf2Node.put(UAV1_ID + "," + UAV2_ID, (double) resultPath.size());
		// long endTime = System.currentTimeMillis();
		// BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new
		// BigDecimal(1000));
		// logger.debug("getCostOfRdmShortestPathForAPairOfUAVs耗时：" +
		// durTime.setScale(8) + "s " + UAV1_ID + ", " + UAV2_ID);
		return (double) resultPath.size();
	}

	private static double getObjValOfConfigurationsInWholeSystem() {
		double xf = global_system_throughput - weight_b_rou * global_weighted_RoutingCost
				- weight_a_com * global_weighted_computeCost;
		double queueBlocak = getQueueBlock(Qt, migrationCost, M_avg);
		double mcost = getMigrationCosts(old_x_wtk, var_x_wtk);
		// return V * xf - queueBlocak * migrationCost;
		// return V * xf - queueBlocak * (mcost - M_avg);
		return V * xf - queueBlocak * (mcost - M_avg) - mcost;
	}

	private static FakeReplaceReturnResult fakeReplaceUAVorPathForATaskToReturnEstimatedSysObj(int WF_ID, int taskA_ID,
			int taskB_ID, int UAVID_old, int UAVID_new, int pathID_old, int pathID_new, List<String> var_y_wpab,
			List<String> var_x_wtk) {
		// logger.debug("模拟采用下一个方案...");
		long startTime = System.currentTimeMillis();
		// 对原方案信息进行拷贝备份
		// ArrayList<String> x_wtk_clone = (ArrayList<String>) ((ArrayList<String>)
		// var_x_wtk).clone();
		// ArrayList<String> y_wpab_clone = (ArrayList<String>) ((ArrayList<String>)
		// var_y_wpab).clone();

		replaceTheSelectedNewUAVorPathForAFlow(WF_ID, taskA_ID, taskB_ID, UAVID_old, UAVID_new, pathID_old, pathID_new,
				var_y_wpab, var_x_wtk);
		List<Flow> lstAffectedSuccessorTaskFlow = findTheFlowListOfSuccessorTask(WF_ID, taskB_ID);
		List<TasksFlowToReplaceInfo> successorTaskFlowInfo = prepareFlows(WF_ID, lstAffectedSuccessorTaskFlow);
		for (TasksFlowToReplaceInfo tfi : successorTaskFlowInfo) {
			replaceTheSelectedNewUAVorPathForAFlow(WF_ID, tfi.curTaskID, tfi.sucTaskID, tfi.taskB_UAV_ID,
					tfi.taskB_UAV_ID, tfi.oldPathID, tfi.newPathID, var_y_wpab, var_x_wtk);
		}

		List<Flow> lstAffectedPredecessorTaskFlow = findTheFlowListOfPredecessorTask(WF_ID, taskB_ID);
		for (Flow flow : lstAffectedPredecessorTaskFlow) {
			if (flow.getCurrTask().getTaskId() == taskA_ID) {
				lstAffectedPredecessorTaskFlow.remove(flow);
				break;
			}
		}
		List<TasksFlowToReplaceInfo> predecessorTaskFlowInfo = prepareFlows(WF_ID, lstAffectedPredecessorTaskFlow);
		for (TasksFlowToReplaceInfo tfi : predecessorTaskFlowInfo) {
			replaceTheSelectedNewUAVorPathForAFlow(WF_ID, tfi.curTaskID, tfi.sucTaskID, tfi.taskB_UAV_ID,
					tfi.taskB_UAV_ID, tfi.oldPathID, tfi.newPathID, var_y_wpab, var_x_wtk);
			// System.out.println(
			// "swap pred-- (" + tfi.curTaskID + "," + tfi.sucTaskID + ") uOld:" +
			// tfi.taskB_UAV_ID
			// + " uNew:" + tfi.taskB_UAV_ID + " pOld:" + tfi.oldPathID + " pNew:" +
			// tfi.newPathID);
			// printVarX();
			// printVarY();
		}

		double estimatedSysObj = updateSystemMetricsAndReturnSystenObj(var_y_wpab, var_x_wtk);
		// swap back
		// var_y_wpab = y_wpab_clone;
		// var_x_wtk = x_wtk_clone;
		// 需要吗？
		// updateSystemMetrics();

		List<TasksFlowToReplaceInfo> resultTaskFlows = new ArrayList<>();
		resultTaskFlows.addAll(successorTaskFlowInfo);
		resultTaskFlows.addAll(predecessorTaskFlowInfo);

		FakeReplaceReturnResult result = new FakeReplaceReturnResult(estimatedSysObj, resultTaskFlows);

		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		logger.debug("fakeReplaceUAVorPathForATaskToReturnEstimatedSysObj耗时：" + durTime.setScale(8) + "s");
		return result;
	}

	private static List<TasksFlowToReplaceInfo> prepareFlows(int WF_ID, List<Flow> flows) {
		List<TasksFlowToReplaceInfo> result = new ArrayList<>();
		for (Flow flow : flows) {
			int taskA_UAV_ID = getTheIU_UAV_IDOfATask(WF_ID, flow.getCurrTask().getTaskId());
			int taskB_UAV_ID = getTheIU_UAV_IDOfATask(WF_ID, flow.getSuccTask().getTaskId());
			int oldPathID = getTheIUPathIDBetweenTwoTask(WF_ID, flow.getCurrTask().getTaskId(),
					flow.getSuccTask().getTaskId());
			int newPathID = selectARdmPathForAPairOfUAVs(taskA_UAV_ID, taskB_UAV_ID);
			TasksFlowToReplaceInfo tfi = new TasksFlowToReplaceInfo(WF_ID, flow.getCurrTask().getTaskId(),
					flow.getSuccTask().getTaskId(), taskB_UAV_ID, oldPathID, newPathID);
			result.add(tfi);
		}
		return result;
	}

	private static void replaceTheSelectedNewUAVorPathForAFlow(int WF_ID, int taskA_ID, int taskB_ID, int UAVID_old,
			int UAVID_new, int pathID_old, int pathID_new, List<String> var_y_wpab, List<String> var_x_wtk) {
		if (UAVID_old != UAVID_new) {
			String key = WF_ID + "," + taskB_ID + "," + UAVID_old;
			if (var_x_wtk.contains(key))
				var_x_wtk.remove(key);
			String newKey = WF_ID + "," + taskB_ID + "," + UAVID_new;
			var_x_wtk.add(newKey);
		}
		zeroAPath(pathID_old, WF_ID, taskA_ID, taskB_ID, var_y_wpab);
		OneAPath(pathID_new, WF_ID, taskA_ID, taskB_ID, var_y_wpab);

	}

	private static void zeroAPath(int pathID, int WF_ID, int taskA_ID, int taskB_ID, List<String> var_y_wpab) {
		// List<String> pathContent = pathDatabase.get(pathID);
		// double neededBandwidth = getTheNeedBandwidthOfATaskFlow(WF_ID, taskA_ID,
		// taskB_ID);
		// for (String sedment : pathContent) {
		// Double TR = Aggregated_TR_in_acrs.get(sedment);
		// TR -= neededBandwidth;
		// Aggregated_TR_in_acrs.put(sedment, TR);
		// }
		String k = WF_ID + "," + pathID + "," + taskA_ID + "," + taskB_ID;
		if (var_y_wpab.contains(k))
			var_y_wpab.remove(k);
	}

	private static List<Flow> findTheFlowListOfSuccessorTask(int WF_ID, int taskID) {
		List<Flow> resultList = new ArrayList<>();
		for (Workflow wf : WFInfo) {
			if (wf.getWF_ID() == WF_ID) {
				ArrayList<Flow> flows = wf.getFlows();
				for (Flow flow : flows) {
					if (flow.getCurrTask().getTaskId() == taskID)
						resultList.add(flow);
				}
			}
		}

		return resultList;
	}

	private static List<Flow> findTheFlowListOfPredecessorTask(int WF_ID, int taskID) {
		List<Flow> resultList = new ArrayList<>();
		for (Workflow wf : WFInfo) {
			if (wf.getWF_ID() == WF_ID) {
				ArrayList<Flow> flows = wf.getFlows();
				for (Flow flow : flows) {
					if (flow.getSuccTask().getTaskId() == taskID)
						resultList.add(flow);
				}
			}
		}
		return resultList;
	}

	/*
	 * private void updateWorkflowsDuration() { synchronized (WFInfo) { for
	 * (Workflow wf : WFInfo) { wf.setDuration(wf.getDuration() - 1); } } }
	 */

	public static void generateWorkflows(int numOfWF) {
		// logger.debug("生成工作流，数量：" + numOfWF);
		long startTime = System.currentTimeMillis();
		for (int i = numOfWF; i > 0; i--) {
			WorkflowGenerator workflowGenerator = WorkflowGenerator.getWorkflowGenerator();
			Workflow wf = workflowGenerator.generateAWorkflow_V2(0);
			WFInfo.add(wf);

		}
		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		logger.debug("生成工作流耗时：" + durTime.setScale(4) + "s");
	}

	private static Double getMigrationCosts(List<String> old_x_wtk, List<String> var_x_wtk) {
		long startTime = System.currentTimeMillis();

		if (null == old_x_wtk || old_x_wtk.isEmpty()) {
			return 0.0;
		}
		Double migrationCost = 0.0;
		for (String oldContent : old_x_wtk) {
			String[] oldItem = oldContent.split(",");
			Integer WF_ID_old = Integer.valueOf(oldItem[0]);
			for (Iterator<String> iterator = var_x_wtk.iterator(); iterator.hasNext();) {
				String newContent = iterator.next();
				String[] newItem = newContent.split(",");
				Integer WF_ID_new = Integer.valueOf(newItem[0]);
				if (WF_ID_old == WF_ID_new) {
					Integer task_id_old = Integer.valueOf(oldItem[1]);
					Integer task_id_new = Integer.valueOf(newItem[1]);
					if (task_id_old == task_id_new) {
						Integer UAV_id_old = Integer.valueOf(oldItem[2]);
						Integer UAV_id_new = Integer.valueOf(newItem[2]);
						Double migrateCost = getCostOfRdmShortestPathForAPairOfUAVs(UAV_id_old, UAV_id_new);
						migrationCost += migrateCost;
						break;
					}
				}
			}
		}
		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		logger.debug("getMigrationCosts耗时：" + durTime.setScale(8) + "s");
		return migrationCost * 10;
	}

	/**
	 * 获取t+1时刻的队列积存
	 * 
	 * @param Q_t
	 *            t时刻的进入队列的迁移代价
	 * @param migrationCost
	 *            t时刻的进入队列的迁移代价
	 * @param E_avg
	 *            长期平均迁移代价
	 * @return t+1时刻的队列代价积存
	 */
	private static Double getQueueBlock(Double _Qt, Double migrationCost, Double E_avg) {
		double nextBlock = _Qt + migrationCost - E_avg;
		return nextBlock > 0 ? nextBlock : 0;
	}

	public static void addWorkflow() {
		double ranSeed = Double.valueOf(randomTool.nextInt(100000000)) / 100000000;
		// if (ranSeed < productionRate) {
		if (ranSeed < 2) {
			old_x_wtk = (ArrayList<String>) var_x_wtk.clone();
			int numOfWFExampleModel = wfGenerator.exampleWorkflows.size() + 1;
			// Workflow aWorkflow =
			// wfGenerator.generateAWorkflow_V2(randomTool.nextInt(numOfWFExampleModel));
			Workflow aWorkflow = wfGenerator.generateAWorkflow_V2(0);
			WFInfo.add(aWorkflow);
			// 修改工作流集合变化标志，表示系统中运行的工作流已发生变化
			WFInfoChangeFlag = true;
			logger.info("添加工作流...");
		}
	}

	public void removeOverdueWorkflow() {
		for (Iterator<Workflow> WFIter = Markov.WFInfo.iterator(); WFIter.hasNext();) {
			Workflow wf = WFIter.next();
			if (wf.getDuration() <= 0) {
				Integer WF_ID = wf.getWF_ID();
				old_x_wtk = (ArrayList<String>) var_x_wtk.clone();
				// 从WFInfo中移除
				WFIter.remove();
				// 删除var_x_wtk中的过期WF的信息&删除var_y_wpab中的过期WF的信息
				remove_X_Y(WF_ID);

				// 修改工作流集合变化标志，表示系统中运行的工作流已发生变化
				WFInfoChangeFlag = true;
				logger.info("移除ID为" + WF_ID + "的工作流");
			}
		}
	}

	/**
	 * 删除var_x_wtk中的过期WF的信息&删除var_y_wpab中的过期WF的信息
	 * 
	 * @param WF_ID
	 */
	private static void remove_X_Y(Integer WF_ID) {
		// 删除var_x_wtk中的过期WF的信息
		for (Iterator<String> xIter = var_x_wtk.iterator(); xIter.hasNext();) {
			String x_wtk = xIter.next();
			String[] wtk = x_wtk.split(",");
			Integer wfId = Integer.valueOf(wtk[0]);
			if (wfId == WF_ID) {
				xIter.remove();
			}
		}
		// 删除var_y_wpab中的过期WF的信息
		for (Iterator<String> yIter = var_y_wpab.iterator(); yIter.hasNext();) {
			String y_wpab = yIter.next();
			String[] wpab = y_wpab.split(",");
			Integer wfId = Integer.valueOf(wpab[0]);
			if (wfId == WF_ID) {
				yIter.remove();
			}
		}
	}

	public static void randomlyRemoveAWorkflow() {
		int numOfWF = WFInfo.size();
		int WF_ID_idx = randomTool.nextInt(numOfWF);

		if (null != WFInfo) {
			old_x_wtk = (ArrayList<String>) var_x_wtk.clone();
			Workflow WF = WFInfo.get(WF_ID_idx);
			WFInfo.remove(WF);
			Integer WF_ID = WF.getWF_ID();
			remove_X_Y(WF_ID);
			// 修改工作流集合变化标志，表示系统中运行的工作流已发生变化
			WFInfoChangeFlag = true;
			logger.info("移除ID为" + WF_ID + "的工作流");
		}
	}

	static void logParamInfo() {
		ParamInfo paramInfo = new ParamInfo();
		Class clazz = paramInfo.getClass();
		Field[] declaredFields = clazz.getDeclaredFields();
		String params = "";
		for (Field field : declaredFields) {
			// 获取属性
			String name = field.getName();
			// 获取属性值
			Object value = null;
			try {
				value = field.get(paramInfo);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			params += name + ":" + value.toString() + ";\t";
		}
		logger.info(params);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	public static void markoveFunction() {
		// 记录参数
		Markov.logParamInfo();
		// DecimalFormat df = new DecimalFormat("#.00");
		// String logStr = "";
		Markov markov = new Markov();
		markov.initializeReadData(CandPaths_file, CapLinks_file, Info_of_UAVs_file);
		WFChangeInfo = markov.initWFChangeInfo(Info_of_WF_Chaneg);
		markov.assignTaskToUAVRandomly(WFInfo);

		/*
		 * 启动工作流维护线程 在单线程版本中弃用 Thread WFMaintainerThread = new Thread(new
		 * WorkflowMaintainer()); WFMaintainerThread.start();
		 */

		Markov.updateSystemMetrics();
		// markov.printCurrentSysInfo();
		// markov.setTimerForAllTaskFlows(0.0);
		actions = markov.setActionForAllTaskFlows((List<String>) var_y_wpab.clone(), (List<String>) var_x_wtk.clone());

		double current_ts = 0.0;

		while (current_ts < T) {
			// 根据文件内容执行添加或者移除工作流操作
			if (!WFChangeInfo.isEmpty()) {
				Pair<Integer, String> workflowAction = WFChangeInfo.peek();
				if (workflowAction.getKey().intValue() == (int) current_ts) {
					if ("in".equals(workflowAction.getValue())) {
						Markov.addWorkflow();
					} else if ("out".equals(workflowAction.getValue())) {
						Markov.randomlyRemoveAWorkflow();
					}
					WFChangeInfo.poll();
				}
			}

			if (WFInfoChangeFlag) {
				WFInfoChangeFlag = false;
				// markov.printCurrentSysInfo();
				// markov.updateSystemMetrics();
				// migrationCost = markov.getMigrationCosts(old_x_wtk, var_x_wtk);
				allMigrationCost += migrationCost;
				// 更新迁移代价队列
				Qt = Markov.getQueueBlock(Qt, migrationCost, M_avg);
				queueTime++;
				// markov.updateSystemMetrics();
				// markov.printCurrentSysInfo();
				actions = markov.setActionForAllTaskFlows((List<String>) var_y_wpab.clone(),
						(List<String>) var_x_wtk.clone());
				ArrayList<Workflow> listOfUnsatisfiedWF = markov.getListOfUnsatisfiedWF();
				markov.assignTaskToUAVRandomly(listOfUnsatisfiedWF);
				continue;
			}

			Timer timer = (Timer) actions.sample();
			int WF_ID = timer.WF_ID;
			int taskA_ID = timer.taskAID;
			int taskB_ID = timer.taskBID;
			int pathID_Old = timer.oldPathID;
			int pathID_New = timer.newPathID;
			int UAV_ID_Old = timer.oldUAVID;
			int UAV_ID_New = timer.newUAVID;
			List<TasksFlowToReplaceInfo> taskFlows = timer.fakeReplaceReturnResult.taskFlows;
			Markov.replaceTheSelectedNewUAVorPathForAFlow(WF_ID, taskA_ID, taskB_ID, UAV_ID_Old, UAV_ID_New, pathID_Old,
					pathID_New, var_y_wpab, var_x_wtk);
			for (TasksFlowToReplaceInfo tf : taskFlows) {
				Markov.replaceTheSelectedNewUAVorPathForAFlow(tf.WF_ID, tf.curTaskID, tf.sucTaskID, tf.taskB_UAV_ID,
						tf.taskB_UAV_ID, tf.oldPathID, tf.newPathID, var_y_wpab, var_x_wtk);
			}
			// 打印输出性能信息
			Markov.updateSystemMetrics();
			migrationCost = Markov.getMigrationCosts(old_x_wtk, var_x_wtk);
			markov.printCurrentSysInfo();
			iterationNum++;
			// markov.printVarX();
			// markov.printVarY();
			actions = markov.setActionForAllTaskFlows((List<String>) var_y_wpab.clone(),
					(List<String>) var_x_wtk.clone());

			current_ts += STEP_TO_RUN;
			step_times++;
			// 暂时不用
			// markov.updateWorkflowsDuration();

			// performance = markov.global_system_throughput -
			// markov.global_weighted_RoutingCost;
			// if (step_times % 1 == 0) {
			// logStr += "-step:\t" + step_times + "\t-perf:\t" + df.format(performance) +
			// "\t-thr:\t"
			// + df.format(markov.global_system_throughput) + "\t-RoutingCost\t"
			// + df.format(markov.global_weighted_RoutingCost) + "\t-computeCost:\t"
			// + df.format(markov.global_weighted_computeCost);
			// }
			// if (step_times % 1 == 0)
			// markov.printCurrentSysInfo();
			ArrayList<Workflow> listOfUnsatisfiedWF = markov.getListOfUnsatisfiedWF();
			markov.assignTaskToUAVRandomly(listOfUnsatisfiedWF);

		}
		markov.printVarX();
	}

	// private void printVarY() {
	// for (String str : var_y_wpab) {
	// String[] item = str.split(",");
	// int path_ID = Integer.valueOf(item[1]);
	// int taskA_id = Integer.valueOf(item[2]);
	// int taskB_id = Integer.valueOf(item[3]);
	// List<String> path = pathDatabase.get(path_ID);
	// try {
	// String startNode = path.firstElement().split(",")[0];
	// String endNode = path.lastElement().split(",")[1];
	// String toPrint = "(" + taskA_id + "," + taskB_id + ")\t[" + startNode + "-->"
	// + endNode + "]";
	// System.out.println(toPrint);
	// } catch (Exception e) {
	// System.out.println("(" + taskA_id + "," + taskB_id + ")\t" + path);
	//// System.out.println(e);
	// }
	// }
	// }

	private void printVarX() {
		String[] nodeVan = new String[nodeInfo.size()];
		for (int i = 0; i < nodeVan.length; i++) {
			nodeVan[i] = "";
		}
		for (String item : var_x_wtk) {
			String[] seg = item.split(",");
			nodeVan[Integer.valueOf(seg[2]) - 1] += seg[0] + "," + seg[1] + "\t";
		}
		for (int i = 0; i < nodeVan.length; i++) {
			System.out.print("[" + (i + 1) + "]: ");
			System.out.println(nodeVan[i]);
		}
	}
}
