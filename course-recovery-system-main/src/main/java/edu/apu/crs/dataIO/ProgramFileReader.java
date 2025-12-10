package edu.apu.crs.dataIO;

import edu.apu.crs.models.Program;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProgramFileReader extends baseDataReader {

    private static final String FILE_NAME = "program.txt";

    public static List<Program> readPrograms() {
        List<Program> programs = new ArrayList<>();
        // Use an instance of the class to access the protected getReader()
        try (BufferedReader br = new ProgramFileReader().getReader(FILE_NAME)) {
            if (br == null) return programs;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    programs.add(new Program(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return programs;
    }




}
