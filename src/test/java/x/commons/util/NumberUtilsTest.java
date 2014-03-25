package x.commons.util;

import static org.junit.Assert.*;

import org.junit.Test;

import x.commons.util.NumberUtils;

public class NumberUtilsTest {

	@Test
	public void roundHalfUp2Int() {
		double d = 4.4;
		int result = NumberUtils.roundHalfUp(d);
		assertEquals(4, result);
		
		d = 4.5;
		result = NumberUtils.roundHalfUp(d);
		assertEquals(5, result);
	}
	
	@Test
	public void roundHalfUp2Double() {
		double d = 4.544;
		double result = NumberUtils.roundHalfUp(d, 2);
		assertEquals(String.format("%.2f", 4.54), String.format("%.2f", result));
		
		d = 4.545;
		result = NumberUtils.roundHalfUp(d, 2);
		assertEquals(String.format("%.2f", 4.55), String.format("%.2f", result));
	}
}
