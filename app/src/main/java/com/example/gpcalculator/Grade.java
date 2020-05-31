package com.example.gpcalculator;

import android.os.Parcel;
import android.os.Parcelable;

public class Grade implements Parcelable {
    private static String session, semester;
    private static int level, totalUnits;
    private static double GP;
    private static String stat;

    private final String mCourse;
    private final int mUnit;
    private final String mGrade;

    public Grade(String course, int unit, String grade) {
        this.mCourse = course;
        this.mUnit = unit;
        this.mGrade = grade;
    }

    public static final Creator<Grade> CREATOR = new Creator<Grade>() {
        @Override
        public Grade createFromParcel(Parcel in) {
            return new Grade(in);
        }

        @Override
        public Grade[] newArray(int size) {
            return new Grade[size];
        }
    };

    public static int getTotalUnits() {
        return  totalUnits;
    }
    public String getCourse() { return mCourse; }
    public int getUnit() { return mUnit; }
    public String getGrade() { return mGrade; }
    public static int getLevel() {
        return level;
    }
    public static String getSession() {
        return session;
    }
    public static String getSemester() {
        return semester;
    }
    public static double getGP() {
        return GP;
    }
    public static String getStat() {
        return stat;
    }

    public static void setLevel(int level) {
        Grade.level = level;
    }
    public static void setSession(String session) {
        Grade.session = session;
    }
    public static void setSemester(String semester) {
        Grade.semester = semester;
    }
    public static void setGP(double GP) {
        Grade.GP = GP;
    }
    public static void setTotalUnits(int totalUnits) {
        Grade.totalUnits = totalUnits;
    }
    public static void setStat(String stat){
        Grade.stat = stat;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCourse);
        dest.writeInt(mUnit);
        dest.writeString(mGrade);
    }


    private Grade(Parcel in) {
        mCourse = in.readString();
        mUnit = in.readInt();
        mGrade = in.readString();
    }
}
