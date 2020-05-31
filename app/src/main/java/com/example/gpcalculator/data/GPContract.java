package com.example.gpcalculator.data;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Collections;

public final class GPContract {

    // Empty Consructor
    public GPContract() {}


    static final String CONTENT_AUTHORITY = "com.example.gpcalculator";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" +CONTENT_AUTHORITY);
    static final String PATH_GP = "gp";

    public static final class GPEntry implements BaseColumns {
        /*the content_uri to access the gp data in the provider*/
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GP);

        static final String TABLE_NAME = "gp";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_COURSES = "courses";
        public static final String COLUMN_UNITS = "units";
        public static final String COLUMN_GRADES = "grades";
        public static final String COLUMN_GP = "gp";
        public static final String COLUMN_TU = "tu";
        public static final String COLUMN_SEMESTER = "semester";
        public static final String COLUMN_DETAILS = "details";

    }


    public static final class GPConstants {
        // Prevent instantiation of this class
        public GPConstants() {}

        public static final String NONE = "None";
        public static final String STRING_SPLIT = "###";
        public static final String STRING_VIEW_MODE = "View";
        public static final String KEY_INIT_DETAILS = "InitDetails";
        public static final String KEY_LEVEL = "Level";
        public static final String KEY_SESSION = "Session";
        public static final String KEY_SEMESTER = "Semester";
        public static final String KEY_TOTAL_UNITS = "Total Units";
        public static final String KEY_GRADES_STAT = "Stat";
        public static final String KEY_CALCULATED_GP = "GP";
        public static final String KEY_MODE = "Mode";
        public static final String KEY_INIT_SEMESTER = "InitSemester";
        public static final String KEY_GRADE = "Grade";

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

        public static String getGradesStat(String s) {
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

                    if (count == 1) {
                        ret.append(count).append(a).append(" ");
                        continue;
                    }
                    ret.append(count).append(a).append("'s ");
                }
            }

            return ret.toString();
        }

        public static String[] extractExtrasFromUri(Uri uri) {
            String uriString = uri.toString();

            String uriAppendedText = uriString.substring(uriString.indexOf(GPConstants.STRING_SPLIT));

            int lastIndex = uriAppendedText.lastIndexOf(GPConstants.STRING_SPLIT);

            String details = uriAppendedText.substring(0, lastIndex);

            String semester = uriAppendedText.substring(lastIndex + 3);

            return new String [] {details, semester};
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

}
