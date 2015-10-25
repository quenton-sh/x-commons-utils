package x.commons.util.ooxml.xlsx;

import java.io.Closeable;
import java.util.List;

public interface DocumentReader extends Closeable {

	public List<String> getSheetNames();

	public int getSheetNum();

	public SheetReader getReaderForSheet(int sheetIndex);

	public SheetReader getReaderForSheet(String sheetName);

	public void setDataFormatter(DataFormatter dataFormatter);

	public DataFormatter getDataFormatter();

}
