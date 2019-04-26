package com.lbc.ma.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class LogProcessTool {

	@Test
	public void test1() throws IOException {
		String[] inFilePaths = { "info_a0b1.log", "info_a1b0.log", "info_a1b1.log" };
		for (String filePath : inFilePaths) {
			String outputResPath = "pro_" + filePath;
			File file = new File(outputResPath);
			FileWriter fw = null;
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			String strFileContent = Tool.getStringFromFile(filePath);
			String[] lineOfContent = strFileContent.split("\n");

			if (!file.exists()) {
				file.createNewFile();
			}
			String oldRounds = "qt: 0";
			for (int i = 3; i < lineOfContent.length; i++) {
				String aLine = lineOfContent[i];
				String toWrite = "";
				if ("".equals(aLine.trim())) {
					continue;
				}
				String[] split = aLine.split("\\[ INFO \\]");
				// 2019-02-26 22:20:09 [ main:5378 ] - [ INFO ] t: 22 p: 4375.40...
				// 截取[ INFO ]的后面部分
				if (!split[1].trim().startsWith("t:")) {
					// 获取上一行信息
					String lastLine = lineOfContent[i - 1];
					split = lastLine.split("\\[ INFO \\]");
					toWrite = split[1].trim() + "\n";
					bw.write(toWrite);

					// String[] item = split[1].trim().split(" ");
					// String rounds = item[item.length - 1];
					// if (302 == i) {
					// toWrite = split[1].trim() + "\n";
					// bw.write(toWrite);
					// }
					// if (!oldRounds.equals(rounds)) {
					// toWrite = split[1].trim() + "\n";
					// bw.write(toWrite);
					// oldRounds = rounds;
					// }
				}
			}
			bw.close();
		}

		System.out.println("log process finish.");
	}

	@Test
	public void test2() throws IOException {
		String[] inFilePaths = { "pro_info_a0b1.log", "pro_info_a1b0.log", "pro_info_a1b1.log" };
		List<ArrayList<Double>> performences = new ArrayList<ArrayList<Double>>();
		List<ArrayList<Double>> routingCosts = new ArrayList<ArrayList<Double>>();
		List<ArrayList<Double>> computationCosts = new ArrayList<ArrayList<Double>>();
		List<ArrayList<Double>> queueBlocks = new ArrayList<ArrayList<Double>>();
		List<ArrayList<Double>> migrationCosts = new ArrayList<ArrayList<Double>>();
		List<ArrayList<Double>> averagemigrationCosts = new ArrayList<ArrayList<Double>>();
		for (int a = 0; a < inFilePaths.length; a++) {
			String strFileContent = Tool.getStringFromFile(inFilePaths[a]);
			String[] lineOfContent = strFileContent.split("\n");
			ArrayList<Double> performance = new ArrayList<>();
			ArrayList<Double> routingCost = new ArrayList<>();
			ArrayList<Double> computationCost = new ArrayList<>();
			ArrayList<Double> queueBlock = new ArrayList<>();
			ArrayList<Double> migrationCost = new ArrayList<>();
			ArrayList<Double> averagemigrationCost = new ArrayList<>();
			for (int i = 0; i < lineOfContent.length; i++) {
				String aLine = lineOfContent[i];
				if (!aLine.startsWith("t")) {
					continue;
				}
				aLine = aLine.replaceAll("  ", " ").replaceAll(" ", "	"); // 两个空格换成一个空格，一个空格换成tab
				String[] items = aLine.split("\t");
				for (int j = 0; j < items.length - 2; j += 2) {
					String itemName = items[j];
					String nextItem = items[j + 1];
					Double val = Double.valueOf(nextItem);
					if ("p:".equals(itemName)) {
						performance.add(val);
					} else if ("RCost:".equals(itemName)) {
						routingCost.add(val);
					} else if ("CCost:".equals(itemName)) {
						computationCost.add(val);
					} else if ("MCost:".equals(itemName)) {
						migrationCost.add(val);
					} else if ("Q(t):".equals(itemName)) {
						queueBlock.add(val);
					} else if ("Mavg:".equals(itemName)) {
						averagemigrationCost.add(val);
					}

				}

			}
			performences.add(a, performance);
			routingCosts.add(a, routingCost);
			computationCosts.add(a, computationCost);
			queueBlocks.add(a, queueBlock);
			migrationCosts.add(a, migrationCost);
			averagemigrationCosts.add(a, averagemigrationCost);
		}
		String outputPath1 = "_perfomance.txt";
		String outputPath2 = "_routingCost.txt";
		String outputPath3 = "_computationCost.txt";
		String outputPath4 = "_queueBlock.txt";
		String outputPath5 = "_migrationCost.txt";
		String outputPath6 = "_averagemigrationCost.txt";
		File file1 = new File(outputPath1);
		FileWriter fw1 = new FileWriter(file1.getAbsoluteFile());
		BufferedWriter bw1 = new BufferedWriter(fw1);

		File file2 = new File(outputPath2);
		FileWriter fw2 = new FileWriter(file2.getAbsoluteFile());
		BufferedWriter bw2 = new BufferedWriter(fw2);

		File file3 = new File(outputPath3);
		FileWriter fw3 = new FileWriter(file3.getAbsoluteFile());
		BufferedWriter bw3 = new BufferedWriter(fw3);

		File file4 = new File(outputPath4);
		FileWriter fw4 = new FileWriter(file4.getAbsoluteFile());
		BufferedWriter bw4 = new BufferedWriter(fw4);

		File file5 = new File(outputPath5);
		FileWriter fw5 = new FileWriter(file5.getAbsoluteFile());
		BufferedWriter bw5 = new BufferedWriter(fw5);

		File file6 = new File(outputPath6);
		FileWriter fw6 = new FileWriter(file6.getAbsoluteFile());
		BufferedWriter bw6 = new BufferedWriter(fw6);

		String info = "#";
		for (String str : inFilePaths) {
			info += str + "\t";
		}

		bw1.write(info + "\n");
		for (int i = 0; i < performences.get(0).size(); i++) {
			String writeLine = "";
			for (int j = 0; j < performences.size(); j++) {
				writeLine += performences.get(j).get(i) + "\t\t";
			}
			bw1.write(writeLine + "\n");
		}
		bw1.close();

		bw2.write(info + "\n");
		for (int i = 0; i < routingCosts.get(0).size(); i++) {
			String writeLine = "";
			for (int j = 0; j < routingCosts.size(); j++) {
				writeLine += routingCosts.get(j).get(i) + "\t\t";
			}
			bw2.write(writeLine + "\n");
		}
		bw2.close();

		bw3.write(info + "\n");
		for (int i = 0; i < computationCosts.get(0).size(); i++) {
			String writeLine = "";
			for (int j = 0; j < computationCosts.size(); j++) {
				writeLine += computationCosts.get(j).get(i) + "\t\t";
			}
			bw3.write(writeLine + "\n");
		}
		bw3.close();

		bw4.write(info + "\n");
		for (int i = 0; i < queueBlocks.get(0).size(); i++) {
			String writeLine = "";
			for (int j = 0; j < queueBlocks.size(); j++) {
				writeLine += queueBlocks.get(j).get(i) + "\t\t";
			}
			bw4.write(writeLine + "\n");
		}
		bw4.close();

		bw5.write(info + "\n");
		for (int i = 0; i < migrationCosts.get(0).size(); i++) {
			String writeLine = "";
			for (int j = 0; j < migrationCosts.size(); j++) {
				writeLine += migrationCosts.get(j).get(i) + "\t\t";
			}
			bw5.write(writeLine + "\n");
		}
		bw5.close();

		bw6.write(info + "\n");
		for (int i = 0; i < averagemigrationCosts.get(0).size(); i++) {
			String writeLine = "";
			for (int j = 0; j < averagemigrationCosts.size(); j++) {
				writeLine += averagemigrationCosts.get(j).get(i) + "\t\t";
			}
			bw6.write(writeLine + "\n");
		}
		bw6.close();

	}

}
