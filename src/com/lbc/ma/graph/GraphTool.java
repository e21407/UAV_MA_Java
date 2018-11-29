package com.lbc.ma.graph;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.junit.Test;

import com.lbc.ma.Tool;

public class GraphTool {
	final static String OneHocLink_file = "input_data/one_hoc_link.txt";
	final static String Info_of_UAVs_file = "input_data/_input_Info_of_nodes.txt";

	final static double EdgeServerLinkCoefficient = 5;
	final static double CloudServerLinkCoefficient = 6;

	public static MyVertex findVertexById(Graph<MyVertex, MyEdge> graph, Integer id) {
		synchronized (graph) {
			Set<MyVertex> vertexSet = graph.vertexSet();
			for (MyVertex vertex : vertexSet) {
				if (vertex.getId().equals(id)) {
					return vertex;
				}
			}
		}
		return null;
	}

	public static DefaultDirectedWeightedGraph<MyVertex, MyEdge> getGraph() {
		DefaultDirectedWeightedGraph<MyVertex, MyEdge> graph = new DefaultDirectedWeightedGraph<MyVertex, MyEdge>(
				MyEdge.class);
		String strFileContent = Tool.getStringFromFile(Info_of_UAVs_file);
		String[] lineOfContent = strFileContent.split("\r\n");
		for (String line : lineOfContent) {
			String[] item = line.split("\t");
			String nodeType = item[0];
			Integer id = Integer.valueOf(item[1]);
			Integer capacity = Integer.valueOf(item[3]);
			MyVertex v1 = new MyVertex(id, null, capacity);
			if (nodeType.equals("U_ID")) {
				v1.setType(1);
			} else if (nodeType.equals("E_ID")) {
				v1.setType(2);
			} else if (nodeType.equals("C_ID")) {
				v1.setType(3);
			}
			graph.addVertex(v1);
		}

		strFileContent = Tool.getStringFromFile(OneHocLink_file);
		lineOfContent = strFileContent.split("\r\n");
		Integer edge_idx = 1;
		for (String line : lineOfContent) {
			String[] item = line.split(",");
			Integer v1_id = Integer.valueOf(item[0]);
			Integer v2_id = Integer.valueOf(item[1]);
			Set<MyVertex> vertexSet = graph.vertexSet();
			MyVertex v1 = null, v2 = null;
			for (MyVertex v : vertexSet) {
				if (v.getId().equals(v1_id)) {
					v1 = v;
				}
				if (v.getId().equals(v2_id)) {
					v2 = v;
				}
				if (v1 != null && v2 != null) {
					break;
				}
			}
			if (v1 != null && v2 != null) {
				Double linkCoefficient = 1.0;
				if (v1.getType() == 2 && v2.getType() != 2 || v1.getType() != 2 && v2.getType() == 2) {
					linkCoefficient = EdgeServerLinkCoefficient;
				} else if (v1.getType() == 3 && v2.getType() != 3 || v1.getType() != 3 && v2.getType() == 3) {
					linkCoefficient = CloudServerLinkCoefficient;
				}

				MyEdge edge = new MyEdge(edge_idx++, 10000.0, 0.0, linkCoefficient);
				graph.addEdge(v1, v2, edge);
			}
		}
		return graph;
	}

	public static List<GraphPath<MyVertex, MyEdge>> findAllPaths(AllDirectedPaths<MyVertex, MyEdge> pathAlgorithm,
			MyVertex v1, MyVertex v2) {
		long startTime = System.currentTimeMillis();
		List<GraphPath<MyVertex, MyEdge>> allPaths = pathAlgorithm.getAllPaths(v1, v2, true, Integer.MAX_VALUE);
		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		System.out.println("查找路径耗时：" + durTime.setScale(8) + "s");
		return allPaths;
	}

	public Map<String, List<GraphPath<MyVertex, MyEdge>>> findAllSimpleOfGraph(
			DefaultDirectedWeightedGraph<MyVertex, MyEdge> graph) {
		Map<String, List<GraphPath<MyVertex, MyEdge>>> result = new HashMap<>();
		AllDirectedPaths<MyVertex, MyEdge> pathAlgorithm = new AllDirectedPaths<>(graph);
		Set<MyVertex> vertexSet = graph.vertexSet();
		for (MyVertex startV : vertexSet) {
			for (MyVertex endV : vertexSet) {
				List<GraphPath<MyVertex, MyEdge>> allPaths = pathAlgorithm.getAllPaths(startV, endV, true,
						Integer.MAX_VALUE);
				String pathKay = startV.getId() + "," + endV.getId();
				result.put(pathKay, allPaths);
			}
		}
		return result;
	}

	@Test
	public void ty() {
		DefaultDirectedWeightedGraph<MyVertex, MyEdge> graph = getGraph();
		long startTime = System.currentTimeMillis();
		Map<String, List<GraphPath<MyVertex, MyEdge>>> findAllSimpleOfGraph = findAllSimpleOfGraph(graph);
		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		System.out.println("查找路径耗时：" + durTime.setScale(8) + "s");
	}

	@Test
	public void ty2() throws InterruptedException {
		DefaultDirectedWeightedGraph<MyVertex, MyEdge> graph = getGraph();
		long startTime = System.currentTimeMillis();
		Set<MyVertex> vertexSet = graph.vertexSet();
		List<MyVertex> list1 = new ArrayList<>(vertexSet);
		List<List<MyVertex>> subWithNum = subWithNum(list1, 5);
		for (List<MyVertex> list : subWithNum) {
			Callable<Object> oneCallable = new TraversePathTool<Object>();
			FutureTask<Object> oneTask = new FutureTask<Object>(oneCallable);
			Thread t = new Thread(oneTask);
			t.start();
			t.join();
		}

		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		System.out.println("查找路径耗时：" + durTime.setScale(8) + "s");
	}

	public <T> List<List<T>> subWithNum(List<T> source, int n) {
		List<List<T>> result = new ArrayList<List<T>>();
		int remaider = source.size() % n; // (先计算出余数)
		int number = source.size() / n; // 然后是商
		int offset = 0;// 偏移量
		for (int i = 0; i < n; i++) {
			List<T> value = null;
			if (remaider > 0) {
				value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
				remaider--;
				offset++;
			} else {
				value = source.subList(i * number + offset, (i + 1) * number + offset);
			}
			result.add(value);
		}
		return result;
	}

}

class TraversePathTool<Object> implements Callable<Object> {

	public Object call(AllDirectedPaths<MyVertex, MyEdge> pathAlgorithm, List<MyVertex> startVertexs,
			List<MyVertex> endVertexs) throws Exception {
		Map<String, List<GraphPath<MyVertex, MyEdge>>> result = new HashMap<>();
		for (MyVertex startV : startVertexs) {
			for (MyVertex endV : endVertexs) {
				List<GraphPath<MyVertex, MyEdge>> allPaths = pathAlgorithm.getAllPaths(startV, endV, true,
						Integer.MAX_VALUE);
				String pathKay = startV.getId() + "," + endV.getId();
				result.put(pathKay, allPaths);
			}
		}
		return (Object) result;
	}

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
