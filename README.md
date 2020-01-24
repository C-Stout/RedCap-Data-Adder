# RedCap-Data-Adder
Programmed designed to improve the efficiency of adding data to RedCap for Project SEED. Essentially a glorified find-and-replace.

How to use:
Add a database file to search through and replace matches.
Next, add data files. The program will parse the data files and find the line closest to the end that has not yet been added to the database file.
Selecting a data file will display the last few (can be changed in settings) lines in the data file along with the number of lines that are not yet in the database file.
Use the text box to add any new lines of data to the selected data file. Note that only lines that are not yet in the database file can be removed.
Once all of the data files have been updated, click the Write to Files button to update the database file and the data files. For each of the data files, the program will find the line closest to the end that is in the database file, search for each occurrance of that line and insert all the new lines into the database file after each occurrance of that line in the correct format.
After the new lines have been written to the database file, the program will write all of the new lines in each data file to the data files. Note that those lines CANNOT be removed through the program once this is done.
