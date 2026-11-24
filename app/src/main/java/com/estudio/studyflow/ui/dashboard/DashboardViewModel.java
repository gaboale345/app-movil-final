package com.estudio.studyflow.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.estudio.studyflow.data.AppExecutors;
import com.estudio.studyflow.data.local.entity.MetaSemanal;
import com.estudio.studyflow.data.local.entity.SesionConMateria;
import com.estudio.studyflow.data.repository.EstudioRepositoryImpl;

import java.util.Calendar;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final EstudioRepositoryImpl repository;
    private final MutableLiveData<Integer> rachaDiasLiveData = new MutableLiveData<>(0);

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.repository = EstudioRepositoryImpl.getInstance(application);
        cargarRacha();
    }

    public void cargarRacha() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            int racha = repository.calcularRachaDiasSync();
            rachaDiasLiveData.postValue(racha);
        });
    }

    public LiveData<Integer> getMinutosEstudiadosHoy() {
        return repository.getMinutosEstudiadosHoy();
    }

    public LiveData<Integer> getMinutosEstudiadosSemana() {
        return repository.getMinutosEstudiadosSemana();
    }

    public LiveData<Integer> getRachaDias() {
        return rachaDiasLiveData;
    }

    public LiveData<Integer> getCantidadMateriasActivas() {
        return repository.getCantidadMateriasActivas();
    }

    public LiveData<SesionConMateria> getProximaSesion() {
        return repository.getProximaSesionPlanificada();
    }

    public LiveData<MetaSemanal> getMetaGlobalSemanaActual() {
        Calendar cal = Calendar.getInstance();
        int anio = cal.get(Calendar.YEAR);
        int semana = cal.get(Calendar.WEEK_OF_YEAR);
        return repository.getMetaGlobalPorSemana(anio, semana);
    }

    public LiveData<List<SesionConMateria>> getSesionesRecientes() {
        return repository.getSesionesRecientes(5);
    }
}
