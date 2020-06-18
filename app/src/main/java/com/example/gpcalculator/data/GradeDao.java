package com.example.gpcalculator.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GradeDao {
    @Query("SELECT * FROM UserGrades")
    LiveData<List<GradeEntry>> loadAllGrades();

    @Insert
    void insertGrade(GradeEntry gradeEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateGrade(GradeEntry gradeEntry);

    @Delete
    void deleteGrade(GradeEntry taskEntry);

    @Query("SELECT * FROM UserGrades " +
            "WHERE level = :level AND session = :session AND semester = :semester" +
            " LIMIT 1")
    LiveData<GradeEntry> loadTaskByProperties(String level, String session, String semester);
}
