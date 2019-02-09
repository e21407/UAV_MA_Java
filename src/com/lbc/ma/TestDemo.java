package com.lbc.ma;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import com.lbc.ma.tool.Tool;

public class TestDemo {
	@Test
	public void test0() {
		double expItem = Math.exp(50) + Double.MIN_VALUE/* + 0.00000001 */;
		// System.out.println("Xf_prime - Xf : " + (Xf_prime - Xf));
		// System.out.println("expItem: " + expItem);
		double meanTimerExp = 1.0 * expItem / (25 - 1);
		double lambdaExpRandomNumberSeed = 1.0 / meanTimerExp;
		// double timerVarExp = Tool.randExp(lambdaExpRandomNumberSeed);
		System.out.println(lambdaExpRandomNumberSeed);
		Double timerVarExp = Tool.randExp(lambdaExpRandomNumberSeed) * 100000.0;
		System.out.println(timerVarExp);
	}

	@Test
	public void test1() {
		List<Pair<Integer, Double>> pmf = new ArrayList<>();
		pmf.add(new Pair<Integer, Double>(1, 0.1));
		pmf.add(new Pair<Integer, Double>(2, 0.3));
		pmf.add(new Pair<Integer, Double>(3, 0.6));
		EnumeratedDistribution ed = new EnumeratedDistribution(pmf);
		int count1 = 0, count2 = 0, count3 = 0;
		for (int i = 10000; i > 0; i--) {
			Integer sample = (Integer) ed.sample();
			if (1 == sample)
				count1++;
			else if (2 == sample)
				count2++;
			else if (3 == sample)
				count3++;
		}
		System.out.println(1 + ": " + count1);
		System.out.println(2 + ": " + count2);
		System.out.println(3 + ": " + count3);
	}
	
	@Test
	public void test2() {
		Random rdm = new Random();
		int numIn = 0;
		int numOut = 0;
		for (int i = 0; i < 50; i++) {
			int nextInt = rdm.nextInt(10000);
			if(nextInt <5500) {
				System.out.println("-t	" + (i+1) * 300 + "	-action	in");
				numIn++;
			}
			else {
				System.out.println("-t	" + (i+1) * 300 + "	-action	out");
				numOut++;
			}
		}
		System.out.println("numIn: " + numIn + "  numOut:" + numOut);
	}
	
}
