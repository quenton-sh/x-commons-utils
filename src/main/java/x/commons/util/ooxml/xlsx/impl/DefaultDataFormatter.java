package x.commons.util.ooxml.xlsx.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import x.commons.util.ooxml.xlsx.DataFormatter;


public class DefaultDataFormatter implements DataFormatter {
	
	// excel定义的时间日期formatId，参见:
	// https://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.numberingformat(v=office.14).aspx
	private static final Set<Integer> DATE_FORMATS = new HashSet<Integer>(
			Arrays.asList(14, 15, 16, 17, 27, 28, 29, 30, 31, 36, 50, 51, 52,
					53, 54, 57, 58));

	private static final Set<Integer> TIME_FORMATS = new HashSet<Integer>(
			Arrays.asList(18, 19, 20, 21, 45, 46, 32, 33, 34, 35, 55, 56));

	private static final Set<Integer> DATETIME_FORMATS = new HashSet<Integer>(
			Arrays.asList(22));
	
	private final org.apache.poi.ss.usermodel.DataFormatter formatter = 
			new org.apache.poi.ss.usermodel.DataFormatter(); 
	
	private String dateFmtPattern = "yyyy-MM-dd";
	private String timeFmtPattern = "HH:mm:ss";
	private String datetimeFmtPattern = "yyyy-MM-dd HH:mm:ss";
	
	protected boolean isDate(int formatIndex) {
		return DATE_FORMATS.contains(formatIndex);
	}
	
	protected boolean isDateTime(int formatIndex) {
		return DATETIME_FORMATS.contains(formatIndex);
	}
	
	protected boolean isTime(int formatIndex) {
		return TIME_FORMATS.contains(formatIndex);
	}

	private String formatDateTime(double value, int formatIndex) {
		SimpleDateFormat sdf = null;
		if (this.isDate(formatIndex)) {
			sdf = new SimpleDateFormat(this.getDateFmtPattern());
		} else if (this.isTime(formatIndex)) {
			sdf = new SimpleDateFormat(this.getTimeFmtPattern());
		} else if (this.isDateTime(formatIndex)) {
			sdf = new SimpleDateFormat(this.getDatetimeFmtPattern());
		}
		if (sdf != null) {
			Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
			return sdf.format(date);
		} else {
			return null;
		}
	}

	@Override
	public String formatCellNumberValue(double value, int formatIndex,
			String formatString) {
		// 拦截对时间日期的处理
		String ret = this.formatDateTime(value, formatIndex);
		if (ret != null) {
			return ret;
		}
		if (formatString == null) {
			return "" + value;
		}
		return this.formatter.formatRawCellContents(value, formatIndex, formatString);
	}

	@Override
	public String formatCellTextValue(String value) {
		return value;
	}

	public String getDateFmtPattern() {
		return dateFmtPattern;
	}

	public void setDateFmtPattern(String dateFmtPattern) {
		this.dateFmtPattern = dateFmtPattern;
	}

	public String getTimeFmtPattern() {
		return timeFmtPattern;
	}

	public void setTimeFmtPattern(String timeFmtPattern) {
		this.timeFmtPattern = timeFmtPattern;
	}

	public String getDatetimeFmtPattern() {
		return datetimeFmtPattern;
	}

	public void setDatetimeFmtPattern(String datetimeFmtPattern) {
		this.datetimeFmtPattern = datetimeFmtPattern;
	}
}
