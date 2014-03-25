package x.commons.util;

import java.math.BigDecimal;

public class NumberUtils {

	/**
	 * 四舍五入到整数
	 * @param d
	 * @return
	 */
	public static int roundHalfUp(double d) {
		return new BigDecimal("" + d).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
	}
	
	/**
	 * 四舍五入，保留小数点后scale位
	 * @param d
	 * @param scale
	 * @return
	 */
	public static double roundHalfUp(double d, int scale) {
		return new BigDecimal("" + d).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
}
