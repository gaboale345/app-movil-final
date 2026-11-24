package com.estudio.studyflow.domain.repository;

import androidx.lifecycle.LiveData;

import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.data.local.entity.MetaSemanal;
import com.estudio.studyflow.data.local.entity.SesionConMateria;
import com.estudio.studyflow.data.local.entity.SesionEstudio;

import java.util.List;

public interface EstudioRepository {

    // Materias
    LiveData<List<Materia>> getMateriasActivas();
    LiveData<List<Materia>> getTodasLasMaterias();
    LiveData<Materia> getMateriaPorId(long id);
    void insertMateria(Materia materia, Runnable onComplete);
    void updateMateria(Materia materia, Runnable onComplete);
    void deleteMateria(Materia materia, Runnable onComplete);
    void setEstadoActivaMateria(long idMateria, boolean activa, Runnable onComplete);
    LiveData<Integer> getCantidadMateriasActivas();

    // Sesiones
    void insertSesion(SesionEstudio sesion, Runnable onComplete);
    void updateSesion(SesionEstudio sesion, Runnable onComplete);
    void deleteSesion(SesionEstudio sesion, Runnable onComplete);
    LiveData<List<SesionConMateria>> getSesionesConMateria();
    LiveData<SesionConMateria> getProximaSesionPlanificada();
    LiveData<List<SesionConMateria>> getSesionesPlanificadas();
    LiveData<List<SesionConMateria>> getSesionesRecientes(int limite);
    LiveData<Integer> getMinutosEstudiadosHoy();
    LiveData<Integer> getMinutosEstudiadosSemana();
    List<SesionConMateria> getSesionesParaReporte(long inicio, long fin);
    int calcularRachaDiasSync();

    // Metas
    LiveData<List<MetaSemanal>> getMetasPorSemana(int anio, int semana);
    LiveData<MetaSemanal> getMetaGlobalPorSemana(int anio, int semana);
    void upsertMeta(MetaSemanal meta, Runnable onComplete);
    void recalcularProgresoMetas(int anio, int semana, Runnable onComplete);
}
