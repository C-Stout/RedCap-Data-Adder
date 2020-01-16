import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GUI extends JFrame {
	
	private static final long serialVersionUID = 2912176970512102802L;

	private static JFileChooser fileChooser = 
			new JFileChooser(System.getProperties().getProperty("user.dir"));

	// displays
	private JLabel databaseFileDisplay;
	private JLabel newLineDisplay;
	private static final String NEW_LINES_DISPLAY_TEXT = "New lines: ";

	// lists
	private DefaultListModel<String> dataFilesDisplayList;
	private DefaultListModel<String> dataFileLineDisplayList;
	private JList<String> dataFilesList;
	private JList<String> dataFileLineList;

	// buttons
	private JButton lineAdd;
	private JButton lineRemove;
	private JButton write;
	private JButton fileRemove;

	// text fields
	private JTextField lineInput;
	private JTextField message;

	// utility
	private ArrayList<DataFile> dataFiles;
	private DatabaseFile databaseFile;
	private JFrame frame;
	private int numDataLinesToDisplay = 10;
	private DataFile selectedFile;
	
	public GUI(String title) {
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// create the main panel
		JPanel panel = (JPanel) getContentPane();
		panel.setLayout(new BorderLayout());
		setTitle(title);

		// add an output panel for messages
		JPanel p = new JPanel(new BorderLayout());
		message = new JTextField(30);
		p.setBorder(BorderFactory.createTitledBorder("output"));
		p.add(message, BorderLayout.CENTER);

		panel.add(p, BorderLayout.SOUTH);

		// create data files list display
		panel.add(makeDataFilesDisplay(), BorderLayout.BEFORE_LINE_BEGINS);


		// create display for database file
		panel.add(makeDatabaseDisplay(), BorderLayout.NORTH);

		// create display for data lines from selected data file
		panel.add(makeDataLinesDisplay(), BorderLayout.CENTER);

		// make top bar menus
		makeMenus();

		dataFiles = new ArrayList<> ();

		pack();
		setSize(650, 400);
		setLocation(200, 100);
		setVisible(true);

		frame = this;
	}

	/**
	 * Helper method to make a basic JList of Strings
	 * @param listModel: the list model to use so that objects can be added and removed
	 * @param name: the name of the list
	 * @param listener: a listener so things can happen when the list selection changes
	 * @return: the created JList of Strings
	 */
	private JList<String> makeList(DefaultListModel<String> listModel, String name, ListSelectionListener listener) {
		JList<String> newList = new JList<> (listModel);
		newList.setBorder(BorderFactory.createTitledBorder(name));
		newList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		newList.setLayoutOrientation(JList.VERTICAL);
		newList.addListSelectionListener(listener);
		newList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		newList.setVisibleRowCount(-1);

		return newList;
	}
	
	/**
	 * Helper method to make the display and button for managing data files 
	 */
	private JPanel makeDataFilesDisplay() {
		dataFilesDisplayList = new DefaultListModel<> ();

		dataFilesList = makeList(dataFilesDisplayList, "Data Files", new DataFileListener());
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(dataFilesList), BorderLayout.CENTER);
		
		// make button to remove data files
		fileRemove = new JButton("Remove Data File");
		fileRemove.addActionListener(new DataFileRemoveListener());
		panel.add(fileRemove, BorderLayout.SOUTH);
		
		return panel;
	}
	
	/**
	 * Helper method to make the display and button for the database file
	 */
	private JPanel makeDatabaseDisplay() {
		JPanel panel = new JPanel(new BorderLayout());
		databaseFileDisplay = new JLabel("none");
		databaseFileDisplay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		panel.setBorder(BorderFactory.createTitledBorder("Database File"));
		panel.add(databaseFileDisplay, BorderLayout.WEST);
		
		// make button to write data
		write = new JButton("Write data to files");
		write.addActionListener(new WriteListener());
		panel.add(write, BorderLayout.EAST);
		
		return panel;
	}
	
	/**
	 * Helper method to make the display, buttons and text field for managing data lines
	 */
	private JPanel makeDataLinesDisplay() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Data File Lines"));
		dataFileLineDisplayList = new DefaultListModel<> ();

		dataFileLineList = makeList(dataFileLineDisplayList, "", new DataFileLinesListener());
		panel.add(new JScrollPane(dataFileLineList), BorderLayout.CENTER);
		newLineDisplay = new JLabel(NEW_LINES_DISPLAY_TEXT + "0");
		panel.add(newLineDisplay, BorderLayout.NORTH);

		JPanel inputPanel = new JPanel(new BorderLayout());

		// make input text field and buttons
		lineInput = new JTextField(20);
		lineAdd = new JButton("Add");
		lineRemove = new JButton("Remove");
		lineInput.setEnabled(false);
		lineAdd.setEnabled(false);
		lineRemove.setEnabled(false);

		lineAdd.addActionListener(new AddListener());
		lineRemove.addActionListener(new RemoveListener());
		lineInput.addActionListener(new AddListener());

		inputPanel.add(lineInput, BorderLayout.WEST);
		inputPanel.add(lineAdd, BorderLayout.CENTER);
		inputPanel.add(lineRemove, BorderLayout.AFTER_LINE_ENDS);
		panel.add(inputPanel, BorderLayout.SOUTH);
		
		return panel;
	}

	/**
	 * Helper method to make the options menu
	 */
	private JMenu makeOptionsMenu() {
		JMenu menu = new JMenu("Options");

		menu.add(new AbstractAction("Change number of data lines displayed") {
			private static final long serialVersionUID = 4256400466561723725L;

			public void actionPerformed(ActionEvent ev) {
				// show a dialog to allow the client to enter in how many lines they want
				numDataLinesToDisplay = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter the number of data lines to display", numDataLinesToDisplay));
				
				// recreate the DataFile list
				ArrayList<DataFile> temp = new ArrayList<> ();
				for (DataFile df : dataFiles) {
					temp.add(new DataFile(df.getFile(), numDataLinesToDisplay, databaseFile));
				}
				
				dataFiles = temp;
				
				// reset the data line and data file displays
				dataFileLineDisplayList.clear();
				newLineDisplay.setText(NEW_LINES_DISPLAY_TEXT + "0");
				dataFilesList.clearSelection();
			}
		});

		menu.add(new AbstractAction("About") {
			private static final long serialVersionUID = 5366599284901583082L;

			public void actionPerformed(ActionEvent ev) {
				JOptionPane.showMessageDialog(frame, "Program by Colin Stout\nPlease send any questions, comments or feedback to: cstout2718@gmail.com", "About",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

		return menu;

	}

	/**
	 * Helper method to make the file menu
	 */
	private JMenu makeFileMenu() {
		JMenu fileMenu = new JMenu("File");

		fileMenu.add(new AbstractAction("Add Data File") {
			private static final long serialVersionUID = 4256400466561723725L;

			public void actionPerformed(ActionEvent ev) {
				if (databaseFile == null) {
					message.setText("Please select a database file before adding data files");
					return;
				}
				message.setText("Add data file");
				File newFile = openFile();
				
				// if a valid file is selected
				if (newFile != null) {
					// make a new DataFile
					DataFile temp = new DataFile(newFile, numDataLinesToDisplay, databaseFile);
					// and add it to the internal list and the display list
					dataFiles.add(temp);
					dataFilesDisplayList.addElement(newFile.getName());
				}
			}
		});

		fileMenu.add(new AbstractAction("Open Database File") {
			private static final long serialVersionUID = -5717038867905691461L;

			public void actionPerformed(ActionEvent ev) {
				message.setText("Select database file");
				
				File newDBFile = openFile();
				
				// if a valid file is selected
				if (newDBFile != null) {
					// set the new database file and the display
					databaseFile = new DatabaseFile(newDBFile);
					databaseFileDisplay.setText(databaseFile.getName());

					// if there are any data files, reset them by creating new DataFile objects
					// this is to ensure that any new lines already in the data files are correctly
					// accounted for
					if (dataFiles.size() > 0) {
						ArrayList<DataFile> temp = new ArrayList<> ();
						for (DataFile df : dataFiles) {
							temp.add(new DataFile(df.getFile(), numDataLinesToDisplay, databaseFile));
						}
						
						dataFiles = temp;
						
						// reset the data line and data file displays
						dataFileLineDisplayList.clear();
						newLineDisplay.setText(NEW_LINES_DISPLAY_TEXT + "0");
						dataFilesList.clearSelection();
					}
				}
			}
		});

		fileMenu.add(new AbstractAction("Quit") {
			private static final long serialVersionUID = -8149120618938520076L;

			public void actionPerformed(ActionEvent ev) {
				System.exit(0);
			}
		});
		return fileMenu;
	}

	/**
	 * Opens a file viewer to allow the client to select a file to open
	 * @return: the file the client selected
	 */
	private File openFile() {
		File file = null;
		int retval = fileChooser.showOpenDialog(null);
		
		if (retval != JFileChooser.APPROVE_OPTION) {
			return file;
		}
		
		file = fileChooser.getSelectedFile();
		
		String name = file.getName();
		
		message.setText("Opening file: " + name);
		return file;
	}

	/**
	 * Show a modal-dialog indicating an error; the user must dismiss the
	 * displayed dialog.
	 * @param s is the error-message displayed
	 */
	public void showError(String s) {
		JOptionPane.showMessageDialog(this, s, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Creates the top bar menus
	 */
	private void makeMenus() {
		JMenuBar bar = new JMenuBar();
		bar.add(makeFileMenu());
		bar.add(makeOptionsMenu());
		setJMenuBar(bar);
	}

	/**
	 * Listener to detect when the data file selection changes and act accordingly
	 */
	class DataFileListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				// if the selection is valid
				if (dataFilesList.getSelectedIndex() != -1) {
					// reset the data line display
					dataFileLineDisplayList.clear();
					
					// activate the data line buttons and text box
					lineInput.setEnabled(true);
					lineAdd.setEnabled(true);
					lineRemove.setEnabled(true);

					selectedFile = dataFiles.get(dataFilesList.getSelectedIndex());

					// add the last lines from the selected data file to the data line
					// display
					for (String line : selectedFile.getLastLines()) {
						dataFileLineDisplayList.addElement(line);
					}
					// update the new lines display for any new lines in the selected
					// data file
					newLineDisplay.setText(NEW_LINES_DISPLAY_TEXT + selectedFile.getNumLinesAdded());

				} else {
					lineInput.setEnabled(false);
					lineAdd.setEnabled(false);
					lineRemove.setEnabled(false);
				}
			}
		}

	}

	/**
	 * Listener to detect when the data line selection changes. Primarily here just to allow
	 * for ease of use in combination with the makeList method
	 */
	class DataFileLinesListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				// activate or deactivate the remove button depending on whether
				// a line is selected
				if (dataFilesList.getSelectedIndex() != -1) {
					fileRemove.setEnabled(true);
				} else {
					fileRemove.setEnabled(false);
				}
			}
		}

	}

	/**
	 * Listener to detect and update the data lines when the data line add button is pressed,
	 * or enter is pressed while the data line entry text box is selected
	 */
	class AddListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// make sure a valid file is selected
			if (selectedFile == null) {
				message.setText("No file selected to add to!");
				return;
			}

			String newLine = lineInput.getText();
			// if some text was entered
			if (newLine.length() > 0) {
				// add it to the selected data file and the data line display
				selectedFile.addLine(lineInput.getText());
				dataFileLineDisplayList.addElement(lineInput.getText());
				// reset the text box
				lineInput.setText("");
				
				// increment the new lines display
				newLineDisplay.setText(NEW_LINES_DISPLAY_TEXT + selectedFile.getNumLinesAdded());
			}
		}
	}

	/**
	 * Listener for the data line remove button
	 */
	class RemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// if a data line is selected
			if (dataFileLineList.getSelectedIndex() != -1) {
				// see if the data file will remove it
				if (selectedFile.removeLine(dataFileLineList.getSelectedValue())) {
					// if the line is removed, then also remove it from the display
					// and decrement the new line count display
					dataFileLineDisplayList.removeElementAt(dataFileLineList.getSelectedIndex());
					newLineDisplay.setText(NEW_LINES_DISPLAY_TEXT + selectedFile.getNumLinesAdded());
				} else {
					message.setText("Cannot remove that line!");
				}
			} else {
				message.setText("Must select a line before removing");
			}
		}
	}

	/**
	 * Listener for the data file remove button
	 */
	class DataFileRemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// if a data file is selected
			if (dataFilesList.getSelectedIndex() != -1) {
				// if there are new data lines in this file, then show a dialog to ask the 
				// client if they really want to remove this file
				if (selectedFile.getNumLinesAdded() > 0) {
					if (JOptionPane.showConfirmDialog(frame, "This file has new lines of data, they will be lost if the file is removed.\nAre you sure you"
							+ " want to remove this file?", "Remove Confirm", JOptionPane.YES_NO_OPTION) == 1) {
						return;
					}
				}

				// remove the data file from the list and the display
				dataFiles.remove(dataFilesList.getSelectedIndex());
				dataFilesDisplayList.removeElementAt(dataFilesList.getSelectedIndex());
				
				// and clear the data line display
				dataFileLineDisplayList.clear();
			} else {
				message.setText("Must select a file to remove");
			}
		}
	}

	/**
	 * Listener for the write to files button
	 */
	class WriteListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// ensure that there are data files
			if (dataFiles.isEmpty()) {
				message.setText("Cannot write without any data files");
				return;
			}
			
			// ensure that there are lines to add
			boolean anyNewLines = false;
			for (DataFile df : dataFiles) {
				if (df.getNumLinesAdded() > 0) {
					anyNewLines = true;
				}
			}
			if (!anyNewLines) {
				message.setText("No new lines to write");
				return;
			}
			
			message.setText("Writing to files...");
			
			// create a TreeMap of the reference lines and data to insert from the
			// data files
			TreeMap<String, String> refLinesToData = new TreeMap<> (new DataComparator());
			for (DataFile df : dataFiles) {
				refLinesToData.put(df.getRefLine(), df.getFormattedNewLines());
			}
			
			// tell the database to insert the TreeMap of data
			databaseFile.writeNewData(refLinesToData);
			
			// tell each data file to write any new lines
			for (DataFile df : dataFiles) {
				df.write();
			}
			
			message.setText("Files updated!");
		}
	}

	private static class DataComparator implements Comparator<String> {
		/**
		 * comparator to compare the <b>lengths</b> of two strings
		 */
		public int compare(String str1, String str2) {
			return str2.length() - str1.length();
		}

	}

}
