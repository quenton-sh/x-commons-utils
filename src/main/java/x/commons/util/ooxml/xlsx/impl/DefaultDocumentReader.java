package x.commons.util.ooxml.xlsx.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.SAXException;

import x.commons.util.ooxml.xlsx.DataFormatter;
import x.commons.util.ooxml.xlsx.DocumentReader;
import x.commons.util.ooxml.xlsx.SheetReader;

public class DefaultDocumentReader implements DocumentReader {

	private final OPCPackage opcPackage;
	private final Map<String, SheetReader> sheetReaderMap;
	private final List<SheetReader> sheetReaderList;
	private final ExecutorService exec;
	
	private DataFormatter dataFormatter;
	
	public DefaultDocumentReader(InputStream in) throws IOException, SAXException, OpenXML4JException {
		this(in, null);
	}

	public DefaultDocumentReader(File file) throws IOException, SAXException, OpenXML4JException {
		this(file, null);
	}
	
	public DefaultDocumentReader(InputStream in, ExecutorService exec) throws IOException, SAXException, OpenXML4JException {
		this.opcPackage = OPCPackage.open(in);
		this.sheetReaderMap = new LinkedHashMap<String, SheetReader>();
		this.sheetReaderList = new ArrayList<SheetReader>();
		this.exec = exec;
		this.init();
	}

	public DefaultDocumentReader(File file, ExecutorService exec) throws IOException, SAXException, OpenXML4JException {
		this.opcPackage = OPCPackage.open(file, PackageAccess.READ);
		this.sheetReaderMap = new LinkedHashMap<String, SheetReader>();
		this.sheetReaderList = new ArrayList<SheetReader>();
		this.exec = exec;
		this.init();
	}
	
	private void init() throws IOException, SAXException, OpenXML4JException {
		ReadOnlySharedStringsTable sharedStringsTable = new ReadOnlySharedStringsTable(
				this.opcPackage);
		XSSFReader xssfReader = new XSSFReader(this.opcPackage);
		StylesTable stylesTable = xssfReader.getStylesTable();

		// 遍历取得各sheet
		XSSFReader.SheetIterator sheetIter = (XSSFReader.SheetIterator) xssfReader
				.getSheetsData();
		int index = -1;
		while (sheetIter.hasNext()) {
			InputStream sheetInputStream = sheetIter.next();
			index++;
			String sheetName = sheetIter.getSheetName();

			DefaultSheetReader sheetReader = new DefaultSheetReader(index, sheetName,
					sheetInputStream, sharedStringsTable, stylesTable, this.exec);

			this.sheetReaderMap.put(sheetName, sheetReader);
			this.sheetReaderList.add(sheetReader);
		}
	}

	@Override
	public List<String> getSheetNames() {
		return new ArrayList<String>(this.sheetReaderMap.keySet());
	}

	@Override
	public int getSheetNum() {
		return this.sheetReaderMap.size();
	}

	@Override
	public SheetReader getReaderForSheet(int sheetIndex) {
		if (sheetIndex < 0 || sheetIndex > this.sheetReaderList.size() - 1) {
			return null;
		} else {
			SheetReader reader = this.sheetReaderList.get(sheetIndex);
			if (reader.getDataFormatter() == null) {
				reader.setDataFormatter(this.dataFormatter);
			}
			return reader;
		}
	}
	
	@Override
	public SheetReader getReaderForSheet(String sheetName) {
		SheetReader reader = this.sheetReaderMap.get(sheetName);
		if (reader.getDataFormatter() == null) {
			reader.setDataFormatter(this.dataFormatter);
		}
		return reader;
	}

	@Override
	public void close() {
		for (SheetReader reader : this.sheetReaderMap.values()) {
			IOUtils.closeQuietly(reader);
		}
		IOUtils.closeQuietly(this.opcPackage);
	}

	@Override
	public void setDataFormatter(DataFormatter dataFormatter) {
		this.dataFormatter = dataFormatter;
	}

	@Override
	public DataFormatter getDataFormatter() {
		return this.dataFormatter;
	}
}
