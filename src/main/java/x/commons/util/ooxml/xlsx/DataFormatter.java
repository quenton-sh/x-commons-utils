package x.commons.util.ooxml.xlsx;

public interface DataFormatter {

	public String formatCellNumberValue(double value, int formatIndex,
			String formatString);
	
	public String formatCellTextValue(String value);
}
