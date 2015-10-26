package x.commons.util.ooxml.xlsx;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import x.commons.util.ooxml.xlsx.impl.DefaultDocumentReader;

public class ReadBigFile {

	public static void main(String[] args) throws Exception {
		String xlsxFile = ReadBigFile.class.getResource("/test-oom.xlsx").getPath();
		DocumentReader docReader = new DefaultDocumentReader(new File(xlsxFile));
		
		SheetReader sheetReader = docReader.getReaderForSheet(0);
		sheetReader.startRead();
		
		String[] lineValues = null;
		int i = 0;
		while ((lineValues = sheetReader.readLine()) != null) {
			i++;
			System.out.println(i + "# " + StringUtils.join(lineValues, ", "));
		}
		
		sheetReader.close();
		docReader.close();
		
		System.out.println("done.");
	}
}
