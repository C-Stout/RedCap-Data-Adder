import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

public class DatabaseFile {

	private File file;
	private static final char[] DATA_END_CHARS = new char[] {'|', ','};
	private static final int DATA_END_CHAR_OFFSET = 1;
	private static final String TEMP_FILE_NAME = "tempDatabase";
	private static final String TEMP_FILE_EXTEN = ".csv";
	
	public DatabaseFile(File database) {
		file = database;
	}
	
	/**
	 * Helper method to check if an array of chars contains a specified char
	 * @param arr: the array to search
	 * @param c: the char to check for
	 * @return: true if the specified char is in the array, false otherwise
	 */
	private boolean arrayContains(char[] arr, char c) {
		// linear search through the array for the target char
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == c) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks the database file to see if it contains the specified data with the correct format
	 * @param data: the data to check for
	 * @return: true if the data is in the database, false otherwise
	 */
	public boolean contains(String data) {
		BufferedReader in;
		try {
			// set up an input stream for the file
			in = new BufferedReader(new FileReader(file));

			String line = in.readLine();

			// go over each line of the file
			while (line != null) {			
				int dataLen = data.length();
				
				// if the line is long enough to possibly contain the data
				if (line.length() > dataLen) {
					// go through the line character by character
					for (int i = 0; i < line.length() - dataLen; i++) {
						// check if the specified data string is at this location in the line, and if
						// after this substring a valid data end char occurs within the right offset
						if (line.substring(i, i + dataLen).equals(data) && (i + dataLen + DATA_END_CHAR_OFFSET >= line.length() || arrayContains(DATA_END_CHARS, line.charAt(i + dataLen + DATA_END_CHAR_OFFSET)))) {
							in.close();
							return true;
						}
					}
				}
			
				line = in.readLine();
			}
			in.close();
			return false;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * @return: the name of this database file
	 */
	public String getName() {
		return file.getName();
	}
	
	/**
	 * Searches for each line in refLine, then writes the corresponding data into the file just after
	 * that reference line
	 * <br>pre: refLines != null && data != null && refLines.length == data.length
	 * <br>post: after each occurrence of any item in refLines, the corresponding string from data
	 * is inserted into the file
	 * @param refLines: the array of reference lines to search for. Should be parallel to
	 * data so that the data for each refLine occurs at the same index
	 * @param data: the array of data to insert. Should be parallel to refLines
	 */
	public void writeNewData(String[] refLines, String[] data) {
		if (refLines == null || data == null || refLines.length != data.length) {
			throw new IllegalArgumentException("refLines and data may not be null and must have"
					+ " the same length");
		}
		
		// assemble a TreeMap out of the two arrays and pass that to the other version of this method
		TreeMap<String, String> refLinesToData = new TreeMap<> ();
		for (int i = 0; i < refLines.length; i++) {
			refLinesToData.put(refLines[i], data[i]);
		}
		
		writeNewData(refLinesToData);
	}
	
	/**
	 * Searches for each key in the map, then writes the corresponding data into the file just after
	 * that key (reference line)
	 * <br>pre: refLinesToData != null
	 * <br>post: after each occurrence of any key in refLinesToData, the corresponding string from the
	 * map is inserted into the file
	 * @param refLinesToData: a TreeMap mapping each reference line to its corresponding data
	 */
	public void writeNewData(TreeMap<String, String> refLinesToData) {
		if (refLinesToData == null) {
			throw new IllegalArgumentException("refLinesToData may not be null");
		}
		
		try {
			// make new file, ensuring its name is unique					 
			int fileNumber = 0;
			File tempFile = new File(TEMP_FILE_NAME + fileNumber + TEMP_FILE_EXTEN);
			
			while (!tempFile.createNewFile()) {
				fileNumber++;
				tempFile = new File(TEMP_FILE_NAME + fileNumber + TEMP_FILE_EXTEN);
			}
			
			// set up streams
			FileWriter fw = new FileWriter(tempFile);
			BufferedWriter out = new BufferedWriter(fw);
			
			BufferedReader in = new BufferedReader(new FileReader(file));

			String line = in.readLine();
			
			// go over each line in the database file
			while (line != null) {				
				String newLine = line;
				
				boolean foundMatch = false;
				
				// for each reference line
				for (String l : refLinesToData.keySet()) {
					int dataLen = l.length();
					// if the current line is long enough to contain the current reference
					// line, and we haven't already found a match on this line
					if (line.length() > dataLen && !foundMatch) {
						// go over the line character by character
						for (int i = 0; i < line.length() - dataLen; i++) {
							// if a substring at the current position equals the reference line,
							// and the character at the specified end offset is one of the valid
							// data end chars
							if (line.substring(i, i + dataLen).equals(l) && (i + dataLen + DATA_END_CHAR_OFFSET >= line.length() || arrayContains(DATA_END_CHARS, line.charAt(i + dataLen + DATA_END_CHAR_OFFSET)))) {
								String newData = refLinesToData.get(l);
								// we have found a reference line, so insert the correct data into
								// this line
								newLine = newLine.substring(0, i + dataLen) + newData + newLine.substring(i + dataLen);
								foundMatch = true;
								break; // some pretty horrible gack, but a little more efficient
							}
						}
					}
				}
				
				// write out the lines to the temp file
				out.write(newLine);
				out.newLine();
				
				line = in.readLine();
			}
			out.close();
			fw.close();
			in.close();
			
			// delete the old file and replace it with the new one
			file.delete();
			tempFile.renameTo(file);
			file = tempFile;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
