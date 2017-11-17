package com.webapp.media.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.webapp.media.utils.ConverVideoUtils;

public class TestDemo {

	@Test
	public void test() {
		System.out.println("111111111111");
		
		String filePath = "D:\\devtool\\video\\1.wmv";
		
		ConverVideoUtils cv = new ConverVideoUtils(filePath);
		String targetExtension = ".mp4";
		
		boolean isDelSourceFile = true;
		
		boolean beginConver = cv.beginConver(targetExtension, isDelSourceFile);
		
		System.out.println("===========" + beginConver);
		
	}

}
