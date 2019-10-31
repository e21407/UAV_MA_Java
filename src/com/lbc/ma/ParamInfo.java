package com.lbc.ma;

public class ParamInfo {
    //输入文件配置
    final static String CandPaths_file = "op_data/pathInfo.txt";
    final static String CapLinks_file = "op_data/info_cap_links.txt";
    final static String Info_of_UAVs_file = "op_data/info_of_nodes.txt";
    final static String Info_of_WF_Change = "input_data/WF_change_infoE.txt";
    final static String Workflow_Model_file = "op_data/workflow_model.txt";
//	final static String CandPaths_file = "input_data/_input_PathSet3.txt";
//	final static String CapLinks_file = "input_data/_input_Cap_links10000.txt";
//	final static String Info_of_UAVs_file = "input_data/_input_Info_of_nodes.txt";
//	final static String Info_of_WF_Change = "input_data/WF_change_info4.txt";

    static final int originalWFNum = 2;    //一开始生产工作流的数量

    static final double T = 15300; // 运行总时长

    static final double M_avg = 5000000.0;

    static final double V = 1;

    static final double Beta = 10;

    static final int numOfThread = 1;    //处理task segment的线程数量

    static final int taskBaseCap = 300; // 任务所需资源基础值
    static final int taskRdmCap = 1; // 任务所需资源随机增加范围

    static final double UAVLinkCoefficient = 1;
    static final double EdgeServerLinkCoefficient = 5;
    static final double CloudServerLinkCoefficient = 25;

    static final int bandwidthBase = 75; // 两个任务之间通讯带宽基础值
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
