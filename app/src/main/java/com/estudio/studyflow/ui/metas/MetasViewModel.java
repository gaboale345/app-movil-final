package com.estudio.studyflow.ui.metas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.data.local.entity.MetaSemanal;
import com.estudio.studyflow.data.repository.EstudioRepositoryImpl;
import com.estudio.studyflow.domain.usecase.CalcularMetasUseCase;

import java.util.Calendar;
import java.util.List;

public class MetasViewModel extends AndroidViewModel {

    private final CalcularMetasUseCase calcularMetasUseCase;
    private final EstudioRepositoryImpl repository;
    private final MutableLiveData<String> feedbackMensaje = new MutableLiveData<>();

    public MetasViewModel(@NonNull Application application) {
        super(application);
        this.repository = EstudioRepositoryImpl.getInstance(application);
        this.calcularMetasUseCase = new CalcularMetasUseCase(repository);
    }

    public LiveData<List<MetaSemanal>> getMetasSemanaActual() {
        return calcularMetasUseCase.getMetasSemanaActual();
    }

    public LiveData<MetaSemanal> getMetaGlobalSemanaActual() {
        return calcularMetasUseCase.getMetaGlobalSemanaActual();
    }

    public LiveData<List<Materia>> getMateriasActivas() {
        return repository.getMateriasActivas();
    }

    public LiveData<String> getFeedbackMensaje() {
        return feedbackMensaje;
    }

    public void fijarMetaGlobal(int horasObjetivo) {
        if (horasObjetivo <= 0) {
            feedbackMensaje.setValue("El objetivo debe ser mayor a 0 horas.");
            return;
        }
        calcularMetasUseCase.fijarMetaGlobal(horasObjetivo, () -> {
            feedbackMensaje.setValue("Meta global semanal establecida en " + horasObjetivo + " horas.");
        });
    }

    public void fijarMetaPorMateria(long materiaId, int horasObjetivo) {
        if (horasObjetivo <= 0) {
            feedbackMensaje.setValue("El objetivo debe ser mayor a 0 horas.");
            return;
        }
        Calendar cal = Calendar.getInstance();
        int anio = cal.get(Calendar.YEAR);
        int semana = cal.get(Calendar.WEEK_OF_YEAR);
        int minutosObjetivo = horasObjetivo * 60;

        MetaSemanal meta = new MetaSemanal(materiaId, anio, semana, minutosObjetivo, 0, "EN_PROGRESO");
        repository.upsertMeta(meta, () -> {
            feedbackMensaje.setValue("Meta de materia establecida en " + horasObjetivo + " horas.");
        });
    }

    public void forzarRecalculo() {
        calcularMetasUseCase.recalcularProgreso(() -> {
            feedbackMensaje.setValue("Metas sincronizadas.");
        });
    }
}
