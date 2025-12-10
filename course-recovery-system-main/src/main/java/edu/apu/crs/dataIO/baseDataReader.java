package edu.apu.crs.dataIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class baseDataReader {

    // File path where all data files are located
    protected static final String DATA_DIR = "src/main/resources/data/";
    
    /**
     * Creates a BufferedReader for a given file name.
     * @param fileName The name of the file (e.g., "stuList.txt").
     * @return BufferedReader if successful, null otherwise.
     */
    protected BufferedReader getReader(String fileName) {
        File file = new File(DATA_DIR + fileName);
        if (!file.exists()) {
            System.err.println("FATAL ERROR: Data file not found: " + DATA_DIR + fileName);
            return null;
        }
        try {
            return new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            System.err.println("Error opening file: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

}
