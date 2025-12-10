package edu.apu.crs.dataIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class baseDataReader {

    // File path
    protected static final String DATA_DIR = "src/main/resources/data/";
    
    
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
