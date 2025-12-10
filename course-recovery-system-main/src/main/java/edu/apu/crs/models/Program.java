package edu.apu.crs.models;

public class Program {

    private String programId;
    private String programName;

    public Program(String programId, String programName) {
        this.programId = programId;
        this.programName = programName;
    }

    // Getters
    public String getProgramId() {
        return programId;
    }

    public String getProgramName() {
        return programName;
    }

    // Setters
    public void setProgramName(String programName) {
        this.programName = programName;
    }

}
