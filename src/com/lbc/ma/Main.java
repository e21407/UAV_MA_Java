package com.lbc.ma;

import java.math.BigDecimal;

public class Main {

	public static void main(String[] args) {
		System.out.println("程序开始...");
		long startTime = System.currentTimeMillis();

		Thread controlWindow = new Thread(new ControlWindow());
		controlWindow.start();

		Markov.generateWorkflows(9);

		Markov.markoveFunction();
		long endTime = System.currentTimeMillis();
		BigDecimal durTime = new BigDecimal(endTime - startTime).divide(new BigDecimal(1000));
		System.out.println("程序结束，耗时：" + durTime.setScale(4) + "s");

	}

}
