package com.example.gpcalculator.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public final class Helper {

    private static final String TAG = Helper.class.getSimpleName();

    public static final String NONE = "None";
    public static final String STRING_SPLIT = "###";
    public static final String KEY_MODE = "mode";
    public static final String MODE_ADD_NEW = "add new";
    public static final String MODE_VIEW = "view";
    public static final String KEY_COURSES = "courses";
    public static final String KEY_UNITS = "units";
    public static final String KEY_GRADES = "grades";
    public static final String KEY_PROPERTIES = "props";

    public static int getGradePoint(String grade) {
        switch (grade){
            case "A":
                return 5;
            case "B":
                return 4;
            case "C":
                return 3;
            case "D":
                return 2;
            case "E":
                return 1;
            default:
                return 0;
        }
    }

    public static String getGradesStat(String[] sArr) {

        int total = sArr.length;
        String s = Arrays.toString(sArr);

        char[] c = {'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder ret = new StringBuilder();

        for (char a : c) {

            int count = 0;
            int indexStart = -1;

            while (s.indexOf(a, indexStart + 1) > 0) {
                indexStart = s.indexOf(a, indexStart + 1);
                count++;
            }

            if(count > 0) {

                if (count == total) {
                    String msg = count == 1 ? "All "+a : "All "+a+"'s";
                    ret.append(msg);
                    break;
                }

                String msg = count == 1 ? count+""+a+" " : count+""+a+"'s " ;

                ret.append(msg);
            }
        }

        return ret.toString();
    }

    public static String getGradeClass(double gp) {
        if (4.5 <= gp && gp <= 5.0){
            return "First Class";
        } else if (3.5 <= gp && gp < 4.5) {
            return "Second Class Upper";
        } else if (2.4 <= gp && gp < 3.5) {
            return "Second Class Lower";
        } else if (1.5 <= gp && gp < 2.4) {
            return "Third Class";
        } else if (1 <= gp && gp < 1.5) {
            return "Pass";
        } else {
            return "Fail";
        }
    }

    public static int getPositionInSpinner(String value, String[] items) {
        ArrayList<String> itemsList = new ArrayList<>();

        Collections.addAll(itemsList, items);

        return itemsList.indexOf(value);
    }
}

