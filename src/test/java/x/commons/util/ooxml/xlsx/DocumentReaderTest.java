package x.commons.util.ooxml.xlsx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import x.commons.util.ooxml.xlsx.impl.DefaultDataFormatter;
import x.commons.util.ooxml.xlsx.impl.DefaultDocumentReader;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DocumentReaderTest {

	@Test
	public void test1() throws Exception {
		// 文件方式打开sheet1，正常读取
		String xlsxFile = this.getClass().getResource("/test.xlsx").getPath();
		DataFormatter formatter = new DefaultDataFormatter() {
			@Override
			public String formatCellTextValue(String value) {
				return "@" + value;
			}
		};
		
		DefaultDocumentReader docReader = new DefaultDocumentReader(new File(xlsxFile));
		docReader.setDataFormatter(formatter);
		assertTrue(docReader.getDataFormatter() == formatter);
		
		assertTrue(docReader.getSheetNum() == 3);
		assertTrue(docReader.getSheetNames().size() == 3);
		assertEquals("工作表1", docReader.getSheetNames().get(0));
		assertEquals("test2", docReader.getSheetNames().get(1));
		assertEquals("test3", docReader.getSheetNames().get(2));
		
		SheetReader sheetReader = docReader.getReaderForSheet(0);
		assertTrue(sheetReader.getDataFormatter() == formatter);
		assertTrue(sheetReader.startRead());
		assertTrue(!sheetReader.startRead()); // 多次启动无效
		
		String[] lineValues = null;
		for (int i = 1; i <= 200; i++) {
			lineValues = sheetReader.readLine();
			
			assertTrue(lineValues != null);
			assertTrue(lineValues.length == 100);
			
			for (int j = 1; j <= 100; j++) {
				// "@"是被formatter加上的
				String expected = String.format("@cell-%d-%d", i, j);
				assertEquals(expected, lineValues[j - 1]);
			}
		}
		lineValues = sheetReader.readLine();
		assertTrue(lineValues == null);
		
		assertTrue(sheetReader.stopRead());
		assertTrue(sheetReader.stopRead());
		
		sheetReader.close();
		docReader.close();
	}
	
	@Test
	public void test2() throws Exception {
		// 流方式打开sheet2，正常读取
		String xlsxFile = this.getClass().getResource("/test.xlsx").getPath();
		ExecutorService exec = Executors.newCachedThreadPool();
		
		DefaultDocumentReader docReader = new DefaultDocumentReader(
				new FileInputStream(xlsxFile), exec);
		assertTrue(docReader.getDataFormatter() == null);
		
		SheetReader sheetReader = docReader.getReaderForSheet("test2");
		assertTrue(sheetReader.getDataFormatter() == null);
		sheetReader.startRead();
		
		String[] lineValues = null;
		while ((lineValues = sheetReader.readLine()) != null) {
			System.out.println(StringUtils.join(lineValues, ","));
		}
		// 正常读完后再度，永远为null
		assertTrue((lineValues = sheetReader.readLine()) == null);
		
		sheetReader.close();
		docReader.close();
	}

	@Test
	public void test3() throws Exception {
		// 打开sheet3，检测空单元格能否读出
		String xlsxFile = this.getClass().getResource("/test.xlsx").getPath();
		DefaultDocumentReader docReader = new DefaultDocumentReader(
				new FileInputStream(xlsxFile));

		SheetReader sheetReader = docReader.getReaderForSheet("test3");
		sheetReader.startRead();

		// D列是null
		String[] lineValues = sheetReader.readLine();
		assertTrue(lineValues.length == 6);
		assertTrue(lineValues[3] == null);
		
		// D列是空字符
		lineValues = sheetReader.readLine();
		assertTrue(lineValues.length == 6);
		assertTrue(lineValues[3] != null && lineValues[3].trim().length() == 0);

		sheetReader.close();
		docReader.close();
	}
	
	@Test
	public void test4() throws Exception {
		// 读到一半中止
		ExecutorService exec = Executors.newFixedThreadPool(1);
		String xlsxFile = this.getClass().getResource("/test.xlsx").getPath();
		
		DefaultDocumentReader docReader = new DefaultDocumentReader(new File(xlsxFile), exec);
		SheetReader sheetReader = docReader.getReaderForSheet(0);
		sheetReader.startRead();
		exec.shutdown();
		
		String[] lineValues = null;
		for (int i = 1; i <= 200; i++) {
			lineValues = sheetReader.readLine();
			assertTrue(lineValues != null);
			assertTrue(lineValues.length == 100);
			if (i == 10) {
				break;
			}
		}
		// 提前中止
		sheetReader.stopRead();
		
		// 此时再读为null
		lineValues = sheetReader.readLine();
		assertTrue(lineValues == null);
		
		// exec里线程执行停止
		Thread.sleep(100);
		assertTrue(exec.isTerminated());
		
		sheetReader.close();
		docReader.close();
	}
	
	@Test
	public void test5() throws Exception {
		// 各种非正常使用方式
		String xlsxFile = this.getClass().getResource("/test.xlsx").getPath();
		DefaultDocumentReader docReader = new DefaultDocumentReader(new File(xlsxFile));
		SheetReader sheetReader = docReader.getReaderForSheet(0);

		// 没start直接读取
		try {
			sheetReader.readLine();
			fail();
		} catch (IllegalStateException e) {
			System.out.println(e.getMessage());
		}
		
		// 没start直接stop
		assertTrue(!sheetReader.stopRead());
		
		// 多次start后能正常读取
		assertTrue(sheetReader.startRead());
		assertTrue(!sheetReader.startRead());
		assertTrue(!sheetReader.startRead());

		String[] lineValues = sheetReader.readLine();
		assertTrue(lineValues != null);
		
		// 多次stop后能正常读出null
		assertTrue(sheetReader.stopRead());
		assertTrue(sheetReader.stopRead());
		assertTrue(sheetReader.stopRead());
		
		lineValues = sheetReader.readLine();
		assertTrue(lineValues == null);
		
		sheetReader.close();
		
		// 先close后start
		sheetReader = docReader.getReaderForSheet(1);
		sheetReader.close();
		try {
			sheetReader.startRead();
   			fail();
		} catch (IllegalStateException e) {
			System.out.println(e.getMessage());
		}
		
		docReader.close();
	}
	
	@Test
	public void test6() throws Exception {
		// 模拟读取过程发生异常
		DataFormatter evilDataFormater = new DataFormatter() {
			@Override
			public String formatCellNumberValue(double value, int formatIndex,
					String formatString) {
				// 故意抛出异常，模拟读取中途发生异常
				throw new RuntimeException("hahaha!");
			}

			@Override
			public String formatCellTextValue(String value) {
				return value;
			}
		};
		String xlsxFile = this.getClass().getResource("/test.xlsx").getPath();
		
		DefaultDocumentReader docReader = new DefaultDocumentReader(new File(xlsxFile));
		docReader.setDataFormatter(evilDataFormater);
		
		SheetReader sheetReader = docReader.getReaderForSheet(1);
		sheetReader.startRead();
		try {
			@SuppressWarnings("unused")
			String[] lineValues = null;
			while ((lineValues = sheetReader.readLine()) != null) {
				// just ignore
			}
			fail();
		} catch (RuntimeException e) {
			assertEquals("hahaha!", e.getMessage());
		}
		
		sheetReader.close();
		docReader.close();
	}
}
