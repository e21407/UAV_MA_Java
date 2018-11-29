package com.lbc.ma;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import cern.jet.random.Exponential;

public class Tool {
//	public static double randExp(double const_lambda) {
//		Exponential expo = new Exponential(const_lambda, new DRand(new Date()));
//		return expo.nextDouble();
//	}
	
	public static double randExp(double const_lambda) {
		return Exponential.staticNextDouble(const_lambda);
	}


	public static String getStringFromFile(String filePath) {
		File file = new File(filePath);
		Long filelength = file.length();
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
		return strFileContent;
	}
	
	@Test
	public void test1() {
		double max = -1;
		double min = 10000;
		double sum = 0;
		for (int i = 1000; i > 0; i--) {
			double res = randExp(4);
			
			sum += res;
			if(max <= res)
				
				max = res;
			if(min >= res)
				min = res;
		}
		System.out.println("max: " + max);
		System.out.println("min: " + min);
		System.out.println("mean: " + sum/1000);
	}
	
	@Test
	public void test2() {
		double d = Exponential.staticNextDouble(Math.exp(-10000000));
		System.out.println(d < Double.MIN_VALUE);
		System.out.println(d);
	}

}
