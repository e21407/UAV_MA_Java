package com.lbc.ma;

public class ParamInfo {

	static final int originalWFNum = 8;	//一开始生产工作流的数量
	
	static final double T = 1000; // 运行总时长
	
	static final double M_avg = 300.0;

	static final double V = 5.0;
	
	static final double Beta = 5;
 
	static final int taskBaseCap = 275; // 任务所需资源基础值

	static final int taskRdmCap = 1; // 任务所需资源随机增加范围

	static final double UAVLinkCoefficient = 1;
	static final double EdgeServerLinkCoefficient = 5;
	static final double CloudServerLinkCoefficient = 34;

	static final int bandwidthBase = 90; // 两个任务之间通讯带宽基础值
	static final int bandwidthRdm = 1; // 两个任务之间通讯带宽的随机增加范围

	static final double duration = 2000; // 工作流执行时间

	static final double WEIGHT_OF_COMPUTE_COST = 1;
	static final double WEIGHT_OF_ROUTING_COST = 1;
	static final double WEIGHT_OF_THROUGHPUT = 1;

	static final double weight_a_com = 1;

	static final double weight_b_rou = 1;

	static final double STEP_TO_RUN = 1; // 运行步长

	static final double Tau = 0;

	static final double productionRate = 0.002;

}
