package x.commons.util.ooxml.xlsx.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import x.commons.util.ooxml.xlsx.DataFormatter;
import x.commons.util.ooxml.xlsx.SheetReader;
import x.commons.util.ooxml.xlsx.impl.DefaultSheetXMLHandler.SheetContentsHandler;

public class DefaultSheetReader implements SheetReader {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final int index;
	private final String name;
	private final InputStream inputStream;
	private final StylesTable stylesTable;
	private final ReadOnlySharedStringsTable sharedStringsTable;
	private final ExecutorService exec;
	private final boolean selfBuiltExec; // true：exec为自建；false：exec为外部传入

	private final InputSource inputSource;
	private final SynchronousQueue<String[]> lineValuesHolder;

	private boolean started = false;
	private boolean streamClosed = false;
	private DataFormatter dataFormatter;

	private AtomicBoolean runFlag = new AtomicBoolean(true); // 是否允许运行

	private CountDownLatch latch;
	private Future<Exception> result;

	DefaultSheetReader(int index, String name, InputStream sheetInputStream,
			ReadOnlySharedStringsTable sharedStringsTable,
			StylesTable stylesTable, ExecutorService exec) {
		this.index = index;
		this.name = name;
		this.inputStream = sheetInputStream;
		this.sharedStringsTable = sharedStringsTable;
		this.stylesTable = stylesTable;

		this.inputSource = new InputSource(inputStream);
		this.lineValuesHolder = new SynchronousQueue<String[]>(true);
		
		if (exec == null) {
			this.exec = Executors.newFixedThreadPool(1);
			this.selfBuiltExec = true;
		} else {
			this.exec = exec;
			this.selfBuiltExec = false;
		}
	}

	@Override
	public String getSheetName() {
		return this.name;
	}

	@Override
	public int getSheetIndex() {
		return this.index;
	}

	@Override
	public boolean startRead() {
		if (this.started) {
			logger.debug("already started, ignore the start request.");
			return false;
		}
		if (this.streamClosed) {
			throw new IllegalStateException("can't start: already closed.");
		}
		this.latch = new CountDownLatch(1);
		final DataFormatter formatterRef = this.dataFormatter == null ?
				new DefaultDataFormatter() : this.dataFormatter;
		final CountDownLatch latchRef = this.latch;
		this.result = this.exec.submit(new Callable<Exception>() {
			@Override
			public Exception call() {
				try {
					SheetContentsHandler sheetHandler = new SheetContentsHandlerImpl(
							lineValuesHolder, runFlag);
					try {
						ContentHandler handler = new DefaultSheetXMLHandler(
								stylesTable, null, sharedStringsTable,
								sheetHandler, formatterRef, false);
						XMLReader sheetParser = SAXHelper.newXMLReader();
						sheetParser.setContentHandler(handler);
						sheetParser.parse(inputSource);
					} catch (ParserConfigurationException e) {
						throw new RuntimeException(
								"SAX parser appears to be broken - " + e.getMessage());
					}
					logger.debug("job done.");
					return null;
				} catch (Exception e) {
					if (e instanceof StopSignal) {
						logger.debug("stop signal received, break.");
						return null;
					}
					return e;
				} finally {
					latchRef.countDown();
				}
			}
		});
		this.started = true;
		return true;
	}

	@Override
	public boolean stopRead() {
		if (this.started) {
			this.runFlag.set(false);
			try {
				this.latch.await();
			} catch (InterruptedException e) {
				logger.debug(e.toString(), e);
			}
			if (this.selfBuiltExec) {
				// 自建的exec需要关闭，否则后台线程会阻塞主线程退出
				this.exec.shutdown();
				try {
					this.exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.debug(e.toString(), e);
				}
			}
			return true;
		} else {
			logger.debug("not started yet, ignore the stop request.");
			return false;
		}
	}

	@Override
	public String[] readLine() throws Exception {
		if (!this.started) {
			throw new IllegalStateException("not started yet.");
		}
		String[] lineValues = null;
		while (true) {
			if (!this.runFlag.get()) {
				logger.debug("already stopped, return null.");
				return null;
			}
			if (this.result.isDone()) {
				Exception e = this.result.get();
				if (e != null) {
					throw e;
				} else {
					logger.debug("already finished, return null.");
					// 正常退出
					return null;
				}
			} else {
				try {
					lineValues = this.lineValuesHolder.poll(1, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.debug(e.toString(), e);
				}
				if (lineValues != null) {
					return lineValues;
				}
			}
		}
	}
	
	@Override
	public void setDataFormatter(DataFormatter dataFormatter) {
		this.dataFormatter = dataFormatter;
	}

	@Override
	public DataFormatter getDataFormatter() {
		return this.dataFormatter;
	}

	@Override
	public void close() {
		this.stopRead();
		IOUtils.closeQuietly(this.inputStream);
		this.streamClosed = true;
	}

	private static class SheetContentsHandlerImpl implements
			SheetContentsHandler {

		private final Logger logger = LoggerFactory.getLogger(getClass());

		private final SynchronousQueue<String[]> lineValuesHolder;
		private final AtomicBoolean runFlag;

		private int previousCollIndex = -1;
		private List<String> values;

		public SheetContentsHandlerImpl(
				SynchronousQueue<String[]> lineValuesHolder,
				AtomicBoolean runFlag) {
			this.lineValuesHolder = lineValuesHolder;
			this.runFlag = runFlag;
		}

		@Override
		public void startRow(int rowNum) {
			if (!this.runFlag.get()) {
				// 收到停止运行的信号
				throw new StopSignal();
			}
			this.values = new ArrayList<String>(100);
			this.previousCollIndex = -1;
		}

		@Override
		public void endRow(int rowNum) {
			if (!this.runFlag.get()) {
				// 收到停止运行的信号
				throw new StopSignal();
			}
			String[] values = this.values.toArray(new String[this.values.size()]);
			boolean offered = false; // offere的value是否被取走
			while (true) {
				if (!this.runFlag.get()) {
					throw new StopSignal();
				}
				try {
					offered = this.lineValuesHolder.offer(values, 1, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.debug(e.toString(), e);
				}
				if (offered) {
					break;
				}
			}
		}

		@Override
		public void cell(String cellReferenceStr, String formattedValue,
				XSSFComment comment) {
			if (!this.runFlag.get()) {
				// 收到停止运行的信号
				throw new StopSignal();
			}
			CellReference cellRef = new CellReference(cellReferenceStr);
			int currentCollIndex = cellRef.getCol(); // 当前列索引
			if (this.previousCollIndex >= 0) {
				int missedColls = currentCollIndex - this.previousCollIndex - 1;
				for (int i = 0; i < missedColls; i++) {
					// 缺失的Cell用null填充（空Cell不会记录在ooxml中，所以解析xml时读不到，导致缺失）
					this.values.add(null);
				}
			}
			this.previousCollIndex = currentCollIndex;
			this.values.add(formattedValue);
		}

		@Override
		public void headerFooter(String text, boolean isHeader, String tagName) {
			// Skip
		}
	}

	private static class StopSignal extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public StopSignal() {
			super();
		}
	}
}
