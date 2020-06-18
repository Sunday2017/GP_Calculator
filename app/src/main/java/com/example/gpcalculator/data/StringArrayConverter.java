package com.example.gpcalculator.data;

import android.text.TextUtils;

import androidx.room.TypeConverter;

public class StringArrayConverter {

    @TypeConverter
    public static String toString(String[] array){
        String s =array == null ? "" : TextUtils.join(Helper.STRING_SPLIT, array);
        return s;
    }

    @TypeConverter
    public static String[] toArray(String string){
        String[] s = string.equals("") ? new String[]{} : string.split(Helper.STRING_SPLIT);
        return s;
    }
}
