package com.estudio.studyflow.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.estudio.studyflow.data.local.dao.EstudioDao;
import com.estudio.studyflow.data.local.entity.EtiquetaTema;
import com.estudio.studyflow.data.local.entity.IntervaloTiempo;
import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.data.local.entity.MetaSemanal;
import com.estudio.studyflow.data.local.entity.SesionEstudio;

@Database(
        entities = {
                Materia.class,
                SesionEstudio.class,
                MetaSemanal.class,
                IntervaloTiempo.class,
                EtiquetaTema.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "studyflow_database";
    private static volatile AppDatabase sInstance;

    public abstract EstudioDao estudioDao();

    public static AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return sInstance;
    }
}
