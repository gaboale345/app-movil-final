package com.estudio.studyflow.ui.reportes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.estudio.studyflow.data.AppExecutors;
import com.estudio.studyflow.data.local.entity.SesionConMateria;
import com.estudio.studyflow.data.repository.EstudioRepositoryImpl;
import com.estudio.studyflow.domain.usecase.ObtenerEstadisticasUseCase;

import java.util.Calendar;
import java.util.List;

public class ReportesViewModel extends AndroidViewModel {

    private final ObtenerEstadisticasUseCase estadisticasUseCase;
    private final EstudioRepositoryImpl repository;

    private final MutableLiveData<ObtenerEstadisticasUseCase.ResumenEstadistico> resumenLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargandoLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> reporteExportadoLiveData = new MutableLiveData<>();

    public ReportesViewModel(@NonNull Application application) {
        super(application);
        this.repository = EstudioRepositoryImpl.getInstance(application);
        this.estadisticasUseCase = new ObtenerEstadisticasUseCase(repository);
        cargarEstadisticasSemana();
    }

    public LiveData<ObtenerEstadisticasUseCase.ResumenEstadistico> getResumenLiveData() {
        return resumenLiveData;
    }

    public LiveData<Boolean> getCargandoLiveData() {
        return cargandoLiveData;
    }

    public LiveData<String> getReporteExportadoLiveData() {
        return reporteExportadoLiveData;
    }

    public LiveData<List<SesionConMateria>> getHistorialSesiones() {
        return repository.getSesionesConMateria();
    }

    public void cargarEstadisticasSemana() {
        cargandoLiveData.setValue(true);
        AppExecutors.getInstance().diskIO().execute(() -> {
            ObtenerEstadisticasUseCase.ResumenEstadistico resumen = estadisticasUseCase.calcularResumenSemanal();
            AppExecutors.getInstance().mainThread().execute(() -> {
                resumenLiveData.setValue(resumen);
                cargandoLiveData.setValue(false);
            });
        });
    }

    public void exportarCSV() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -30);
            long inicio = cal.getTimeInMillis();
            long fin = System.currentTimeMillis();

            String csvContent = estadisticasUseCase.generarReporteCSV(inicio, fin);
            reporteExportadoLiveData.postValue(csvContent);
        });
    }

    public void exportarTXT() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -30);
            long inicio = cal.getTimeInMillis();
            long fin = System.currentTimeMillis();

            String txtContent = estadisticasUseCase.generarReporteTXT(inicio, fin);
            reporteExportadoLiveData.postValue(txtContent);
        });
    }
}
