package com.lbc.ma;

public class ParamInfo {

	static final double T = 10000; // 运行总时长

	static final int taskBaseCap = 250; // 任务所需资源基础值

	static final int taskRdmCap = 50; // 任务所需资源随机增加范围

	static final double UAVLinkCoefficient = 1;
	static final double EdgeServerLinkCoefficient = 5;
	static final double CloudServerLinkCoefficient = 13;

	static final int bandwidthBase = 65; // 两个任务之间通讯带宽基础值
	static final int bandwidthRdm = 25; // 两个任务之间通讯带宽的随机增加范围

	static final double duration = 2000; // 工作流执行时间

	static final double global_weighted_throughput = 0.0;

	static final double WEIGHT_OF_COMPUTE_COST = 1;
	static final double WEIGHT_OF_ROUTING_COST = 1;
	static final double WEIGHT_OF_THROUGHPUT = 1;

	static final double weight_a_com = 1;

	static final double weight_b_rou = 1;

	static final double STEP_TO_RUN = 1; // 运行步长

	static final double Beta = 0.5;

	static final double Tau = 0;

	static final double M_avg = 60.0;

	static final double V = 3.0;

	static final double productionRate = 0.002;

}
