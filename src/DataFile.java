import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Scanner;

public class DataFile {
	
	private File file;
	private static final String TEMP_FILE_NAME = "tempData";
	private static final String TEMP_FILE_EXTEN = ".txt";
	
	// cached list of the lines from the end of the file to display
	private LinkedList<String> lastLines;
	private int numLinesToStore;
	private int numLinesAdded;
	
	// the database being used to check against for determining the current reference line
	private DatabaseFile database;
	
	// the index and reference line. This is the line after which new lines are added
	private String refLine;
	private int refLineInd;
	
	/**
	 * Constructor for DataFile. Creates a new DataFile
	 * <br>pre: dataFile != null && databaseFile != null && linesFromEnd >= 0
	 * <br>post: a new DataFile object is created
	 * @param dataFile: the file this DataFile will encapsulate
	 * @param linesFromEnd: the number of lines from the end of the file to cache
	 * @param databaseFile: the DatabaseFile object currently in use. Used to check
	 * against for determining which lines of data are new
	 */
	public DataFile(File dataFile, int linesFromEnd, DatabaseFile databaseFile) {
		file = dataFile;
		database = databaseFile;
		numLinesToStore = linesFromEnd;
		resetLastLinesAndRefLine(linesFromEnd);
	}
	
	/**
	 * Helper method to record the last few lines from the file, as determined by linesFromEnd
	 * and set the reference line to the last line in the data file that is not already in the
	 * database file
	 * @param linesFromEnd: the number of lines from the end of the data file to record
	 */
	private void resetLastLinesAndRefLine(int linesFromEnd) {
		try {
			// set up a scanner for the data file
			Scanner input = new Scanner(Paths.get(file.getCanonicalPath()));
			input.useDelimiter("[\n|\\n|\\r\\n]");
			
			lastLines = new LinkedList<> ();

			String currentLine = "";
			
			// find the last few line of text in the file
			while (input.hasNext()) {
				// read in a line
				currentLine = input.next();
				
				// if that line is not blank, add it to the list
				if (currentLine.length() != 0) {
					lastLines.add(currentLine);
				}
				
				// because lastLines is a LinkedList, it implements the
				// queue interface, which is helpful since when there are
				// more than the maximum number of desired lines in the
				// list, we want to remove the first one added
				if (lastLines.size() > linesFromEnd) {
					lastLines.poll();
				}
			}

			int offset = 1;
			// determine the reference line by starting at the last line
			// in the file and going backwards until a line that is in the
			// database file is found
			do {
				refLineInd = lastLines.size() - offset;
				refLine = lastLines.get(refLineInd);
				offset++;
			} while (!database.contains(refLine));

			// any lines that are not in the database file are new lines,
			// so record that
			numLinesAdded = offset - 2;
			
			input.close();
		} catch(FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the reference line (the first line from the end of the file that
	 * is in the database file)
	 */
	public String getRefLine() {
		return refLine;
	}
	
	/**
	 * Returns a single string containing all the data to add after the reference line
	 * formatted correctly (with a '|' between each piece of data)
	 * <br>pre: numLinesAdded > 0
	 * @return: a string containing each new piece of data in this file with a '|'
	 * between each
	 */
	public String getFormattedNewLines() {
		StringBuilder strBldr = new StringBuilder();
		
		// starting at the index of the reference line, append each line of data
		// to the StringBuilder
		for (int i = refLineInd + 1; i < lastLines.size(); i++) {
			strBldr.append(" | ");
			strBldr.append(lastLines.get(i));
		}
		
		return strBldr.toString();
	}
	
	/**
	 * Gets a list of the last few data lines in this file. The exact number is
	 * determined when this DataFile object is created. <br><b>WARNING:</b> Do 
	 * not modify the returned list! It will cause logic errors!
	 * @return: a LinkedList of Strings with the last few data lines in this file
	 */
	public LinkedList<String> getLastLines() {
		return lastLines;
	}
	
	/**
	 * Gets the number of new lines added to this data file
	 * @return: the number of new lines added to this data file
	 */
	public int getNumLinesAdded() {
		return numLinesAdded;
	}
	
	/**
	 * Adds a new data line to the end of this file
	 * <br>pre: line != null
	 * <br>post: the list returned by getLastLines() has line at the end and getLinesAdded()
	 * returns one more than before
	 * @param line: the data line to add
	 */
	public void addLine(String line) {
		numLinesAdded++;
		lastLines.add(line);
	}
	
	/**
	 * Searches for and removes the specified line
	 * <br>pre: line != null and write() has not been called since the line was added
	 * <br>post: the list returned by getLastLines() does not contain line and getLinesAdded()
	 * returns one less than before
	 * @param line: the data line to search for and remove
	 * @return true if line was removed, false otherwise
	 */
	public boolean removeLine(String line) {
		
		// search for the line to remove starting at the line after the reference line
		// we only want to allow the client to remove lines that are not in the database
		// file to prevent misalignments
		for (int i = refLineInd + 1; i < lastLines.size(); i++) {
			// if we have found the right line, then remove it
			if (lastLines.get(i).equals(line)) {
				lastLines.remove(i);
				numLinesAdded--;
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @return: the File object this DataFile is using
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Writes all new lines to the data file on disc
	 * <br>post: getLinesAdded() returns 0
	 */
	public void write() {
		try {
			// make new file, ensuring its name is unique so we don't overwrite another file
			int fileNumber = 0;
			File tempFile = new File(TEMP_FILE_NAME + fileNumber + TEMP_FILE_EXTEN);
			
			while (!tempFile.createNewFile()) {
				fileNumber++;
				tempFile = new File(TEMP_FILE_NAME + fileNumber + TEMP_FILE_EXTEN);
			}
			
			// set up output streams to new file and input streams from the data file
			FileWriter fw = new FileWriter(tempFile);
			BufferedWriter out = new BufferedWriter(fw);
			
			BufferedReader in = new BufferedReader(new FileReader(file));

			String line = "";
			
			// write all of the lines in the data file to the temp file, except
			// the new lines
			while (line != null && !line.equals(refLine)) {				
				line = in.readLine();

				out.write(line);
				out.newLine();
				
			}
			in.close();

			// write the new lines to the file
			boolean pastRef = false;
			for (String l : lastLines) {
				if (pastRef) {
					out.write(l);
					out.newLine();
				} else {
					if (l.equals(refLine)) {
						pastRef = true;
					}
				}
			}
			
			out.close();
			fw.close();
			
			// replace the original data file with the temp file
			file.delete();
			tempFile.renameTo(file);
			file = tempFile;
			
			// reset this DataFile object
			numLinesAdded = 0;
			resetLastLinesAndRefLine(numLinesToStore);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
