package com.example.gpcalculator.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.util.Arrays;

@Entity(primaryKeys = {"level", "session", "semester"}, tableName = "UserGrades")
public class GradeEntry {

    private String[] courses;
    private String[] units;
    private String[] grades;
    private double GP;
    @NonNull
    private String level;
    @NonNull
    private String session;
    @NonNull
    private String semester;
    private int totalUnits;

    public GradeEntry(String[] courses, String[] units, String[] grades, double GP,
                      String level, String session, String semester, int totalUnits) {
        this.courses = courses;
        this.units = units;
        this.grades = grades;
        this.level = level;
        this.session = session;
        this.semester = semester;
        this.totalUnits = totalUnits;
        this.GP = GP;
    }

    public String[] getCourses() {
        return courses;
    }

    public void setCourses(String[] courses) {
        this.courses = courses;
    }

    public String[] getUnits() {
        return units;
    }

    public void setUnits(String[] units) {
        this.units = units;
    }

    public String[] getGrades() {
        return grades;
    }

    public void setGrades(String[] grades) {
        this.grades = grades;
    }

    public double getGP() {
        return GP;
    }

    public void setGP(double GP) {
        this.GP = GP;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(int totalUnits) {
        this.totalUnits = totalUnits;
    }

    @Override
    public String toString() {
        return "GradeEntry{" +
                ", courses=" + Arrays.toString(courses) +
                ", units=" + Arrays.toString(units) +
                ", grades=" + Arrays.toString(grades) +
                ", level='" + level + '\'' +
                ", session='" + session + '\'' +
                ", semester='" + semester + '\'' +
                ", totalUnits=" + totalUnits +
                ", GP=" + GP +
                '}';
    }
}
