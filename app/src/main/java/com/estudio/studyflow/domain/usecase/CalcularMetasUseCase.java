package com.estudio.studyflow.domain.usecase;

import androidx.lifecycle.LiveData;

import com.estudio.studyflow.data.local.entity.MetaSemanal;
import com.estudio.studyflow.domain.repository.EstudioRepository;

import java.util.Calendar;
import java.util.List;

public class CalcularMetasUseCase {

    private final EstudioRepository repository;

    public CalcularMetasUseCase(EstudioRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<MetaSemanal>> getMetasSemanaActual() {
        Calendar cal = Calendar.getInstance();
        int anio = cal.get(Calendar.YEAR);
        int semana = cal.get(Calendar.WEEK_OF_YEAR);
        return repository.getMetasPorSemana(anio, semana);
    }

    public LiveData<MetaSemanal> getMetaGlobalSemanaActual() {
        Calendar cal = Calendar.getInstance();
        int anio = cal.get(Calendar.YEAR);
        int semana = cal.get(Calendar.WEEK_OF_YEAR);
        return repository.getMetaGlobalPorSemana(anio, semana);
    }

    public void fijarMetaGlobal(int horasObjetivo, Runnable onComplete) {
        Calendar cal = Calendar.getInstance();
        int anio = cal.get(Calendar.YEAR);
        int semana = cal.get(Calendar.WEEK_OF_YEAR);
        int minutosObjetivo = horasObjetivo * 60;

        MetaSemanal meta = new MetaSemanal(null, anio, semana, minutosObjetivo, 0, "EN_PROGRESO");
        repository.upsertMeta(meta, onComplete);
    }

    public void recalcularProgreso(Runnable onComplete) {
        Calendar cal = Calendar.getInstance();
        int anio = cal.get(Calendar.YEAR);
        int semana = cal.get(Calendar.WEEK_OF_YEAR);
        repository.recalcularProgresoMetas(anio, semana, onComplete);
    }
}
