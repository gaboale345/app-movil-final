package com.estudio.studyflow.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.estudio.studyflow.data.local.entity.EtiquetaTema;
import com.estudio.studyflow.data.local.entity.IntervaloTiempo;
import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.data.local.entity.MetaSemanal;
import com.estudio.studyflow.data.local.entity.SesionConMateria;
import com.estudio.studyflow.data.local.entity.SesionEstudio;

import java.util.List;

@Dao
public interface EstudioDao {

    // ==========================================
    // OPERACIONES DE MATERIAS (RF01)
    // ==========================================

    @Query("SELECT * FROM materias WHERE esta_activa = 1 ORDER BY nombre ASC")
    LiveData<List<Materia>> getMateriasActivas();

    @Query("SELECT * FROM materias ORDER BY esta_activa DESC, nombre ASC")
    LiveData<List<Materia>> getTodasLasMaterias();

    @Query("SELECT * FROM materias WHERE id_materia = :idMateria LIMIT 1")
    Materia getMateriaPorIdSync(long idMateria);

    @Query("SELECT * FROM materias WHERE id_materia = :idMateria LIMIT 1")
    LiveData<Materia> getMateriaPorId(long idMateria);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMateria(Materia materia);

    @Update
    void updateMateria(Materia materia);

    @Delete
    void deleteMateria(Materia materia);

    @Query("UPDATE materias SET esta_activa = :activa WHERE id_materia = :idMateria")
    void setEstadoActivaMateria(long idMateria, boolean activa);

    @Query("SELECT COUNT(*) FROM materias WHERE esta_activa = 1")
    LiveData<Integer> getCantidadMateriasActivas();

    // ==========================================
    // OPERACIONES DE SESIONES DE ESTUDIO (RF02, RF03, RF04, RF05, RF06)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSesion(SesionEstudio sesion);

    @Update
    void updateSesion(SesionEstudio sesion);

    @Delete
    void deleteSesion(SesionEstudio sesion);

    @Query("SELECT * FROM sesiones_estudio WHERE id_sesion = :idSesion LIMIT 1")
    SesionEstudio getSesionPorId(long idSesion);

    @Query("SELECT s.*, m.nombre AS nombre_materia, m.color_hex AS color_hex " +
            "FROM sesiones_estudio s " +
            "INNER JOIN materias m ON s.id_materia = m.id_materia " +
            "ORDER BY s.fecha_inicio DESC")
    LiveData<List<SesionConMateria>> getSesionesConMateria();

    @Query("SELECT s.*, m.nombre AS nombre_materia, m.color_hex AS color_hex " +
            "FROM sesiones_estudio s " +
            "INNER JOIN materias m ON s.id_materia = m.id_materia " +
            "WHERE s.estado = 'PLANIFICADA' AND s.fecha_inicio >= :ahora " +
            "ORDER BY s.fecha_inicio ASC LIMIT 1")
    LiveData<SesionConMateria> getProximaSesionPlanificada(long ahora);

    @Query("SELECT s.*, m.nombre AS nombre_materia, m.color_hex AS color_hex " +
            "FROM sesiones_estudio s " +
            "INNER JOIN materias m ON s.id_materia = m.id_materia " +
            "WHERE s.estado = 'PLANIFICADA' " +
            "ORDER BY s.fecha_inicio ASC")
    LiveData<List<SesionConMateria>> getSesionesPlanificadas();

    @Query("SELECT s.*, m.nombre AS nombre_materia, m.color_hex AS color_hex " +
            "FROM sesiones_estudio s " +
            "INNER JOIN materias m ON s.id_materia = m.id_materia " +
            "WHERE s.estado = 'COMPLETADA' " +
            "ORDER BY s.fecha_inicio DESC LIMIT :limite")
    LiveData<List<SesionConMateria>> getSesionesRecientes(int limite);

    @Query("SELECT COALESCE(SUM(tiempo_efectivo_seg), 0) / 60 FROM sesiones_estudio " +
            "WHERE id_materia = :idMateria AND fecha_inicio >= :timestampInicio " +
            "AND fecha_inicio <= :timestampFin AND estado = 'COMPLETADA'")
    int getMinutosEstudiadosMateria(long idMateria, long timestampInicio, long timestampFin);

    @Query("SELECT COALESCE(SUM(tiempo_efectivo_seg), 0) / 60 FROM sesiones_estudio " +
            "WHERE fecha_inicio >= :timestampInicio AND fecha_inicio <= :timestampFin " +
            "AND estado = 'COMPLETADA'")
    LiveData<Integer> getMinutosEstudiadosRangoLiveData(long timestampInicio, long timestampFin);

    @Query("SELECT COALESCE(SUM(tiempo_efectivo_seg), 0) / 60 FROM sesiones_estudio " +
            "WHERE fecha_inicio >= :timestampInicio AND fecha_inicio <= :timestampFin " +
            "AND estado = 'COMPLETADA'")
    int getMinutosEstudiadosRangoSync(long timestampInicio, long timestampFin);

    @Query("SELECT s.*, m.nombre AS nombre_materia, m.color_hex AS color_hex " +
            "FROM sesiones_estudio s " +
            "INNER JOIN materias m ON s.id_materia = m.id_materia " +
            "WHERE s.fecha_inicio >= :timestampInicio AND s.fecha_inicio <= :timestampFin " +
            "AND s.estado = 'COMPLETADA' ORDER BY s.fecha_inicio ASC")
    List<SesionConMateria> getSesionesParaReporte(long timestampInicio, long timestampFin);

    @Query("SELECT DISTINCT (fecha_inicio / 86400000) FROM sesiones_estudio " +
            "WHERE estado = 'COMPLETADA' ORDER BY (fecha_inicio / 86400000) DESC")
    List<Long> getDiasConEstudioSync();

    // ==========================================
    // OPERACIONES DE INTERVALOS Y ETIQUETAS
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertIntervalo(IntervaloTiempo intervalo);

    @Query("SELECT * FROM intervalos_tiempo WHERE id_sesion = :idSesion ORDER BY marca_inicio ASC")
    List<IntervaloTiempo> getIntervalosPorSesionSync(long idSesion);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertEtiqueta(EtiquetaTema etiqueta);

    @Query("SELECT * FROM etiquetas_temas WHERE id_sesion = :idSesion")
    List<EtiquetaTema> getEtiquetasPorSesionSync(long idSesion);

    // ==========================================
    // OPERACIONES DE METAS SEMANALES (RF07, RF08)
    // ==========================================

    @Query("SELECT * FROM metas_semanales WHERE anio = :anio AND numero_semana = :semana")
    LiveData<List<MetaSemanal>> getMetasPorSemana(int anio, int semana);

    @Query("SELECT * FROM metas_semanales WHERE anio = :anio AND numero_semana = :semana AND id_materia IS NULL LIMIT 1")
    LiveData<MetaSemanal> getMetaGlobalPorSemana(int anio, int semana);

    @Query("SELECT * FROM metas_semanales WHERE anio = :anio AND numero_semana = :semana AND id_materia = :idMateria LIMIT 1")
    MetaSemanal getMetaPorMateriaSync(long idMateria, int anio, int semana);

    @Query("SELECT * FROM metas_semanales WHERE anio = :anio AND numero_semana = :semana AND id_materia IS NULL LIMIT 1")
    MetaSemanal getMetaGlobalSync(int anio, int semana);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long upsertMeta(MetaSemanal meta);

    @Update
    void updateMeta(MetaSemanal meta);

    @Delete
    void deleteMeta(MetaSemanal meta);
}
