package x.commons.util.ooxml.xlsx;

import java.io.Closeable;

public interface SheetReader extends Closeable {
	
	public String getSheetName();

	public int getSheetIndex();

	/**
	 * 启动读取线程，开始读取文件
	 * @return true：启动成功；false：之前已被启动过
	 */
	public boolean startRead();

	/**
	 * 中断读取线程，停止读取文件
	 * @return true：中断成功；false：尚未启动
	 */
	public boolean stopRead();

	public String[] readLine() throws Exception;
	
	public void setDataFormatter(DataFormatter dataFormatter);
	
	public DataFormatter getDataFormatter();
}
