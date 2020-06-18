package com.example.gpcalculator.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Arrays;

@Entity(tableName = "UserGrades")
public class GradeEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String[] courses;
    private String[] units;
    private String[] grades;
    private String level;
    private String session;
    private String semester;
    private int totalUnits;
    private double GP;


    public GradeEntry(int id, String[] courses, String[] units, String[] grades,
                      String level, String session, String semester, int totalUnits, double GP) {
        this.id = id;
        this.courses = courses;
        this.units = units;
        this.grades = grades;
        this.level = level;
        this.session = session;
        this.semester = semester;
        this.totalUnits = totalUnits;
        this.GP = GP;
    }

    @Ignore
    public GradeEntry(String[] courses, String[] units, String[] grades,
                      String level, String session, String semester, int totalUnits, double GP) {
        this.courses = courses;
        this.units = units;
        this.grades = grades;
        this.level = level;
        this.session = session;
        this.semester = semester;
        this.totalUnits = totalUnits;
        this.GP = GP;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
                "id=" + id +
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
