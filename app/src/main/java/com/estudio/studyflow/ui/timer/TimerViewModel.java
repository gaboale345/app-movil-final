package com.estudio.studyflow.ui.timer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.data.local.entity.SesionEstudio;
import com.estudio.studyflow.data.repository.EstudioRepositoryImpl;
import com.estudio.studyflow.domain.usecase.RegistrarSesionUseCase;
import com.estudio.studyflow.service.StudyTimerService;

import java.util.List;

public class TimerViewModel extends AndroidViewModel {

    private final RegistrarSesionUseCase registrarSesionUseCase;
    private final EstudioRepositoryImpl repository;
    private final MutableLiveData<Boolean> sesionGuardadaExito = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMensaje = new MutableLiveData<>();

    public TimerViewModel(@NonNull Application application) {
        super(application);
        this.repository = EstudioRepositoryImpl.getInstance(application);
        this.registrarSesionUseCase = new RegistrarSesionUseCase(repository);
    }

    public LiveData<List<Materia>> getMateriasActivas() {
        return repository.getMateriasActivas();
    }

    public LiveData<StudyTimerService.TimerStatus> getTimerStatus() {
        return StudyTimerService.getTimerStatus();
    }

    public LiveData<StudyTimerService.TimerMode> getTimerMode() {
        return StudyTimerService.getTimerMode();
    }

    public LiveData<StudyTimerService.PomodoroPhase> getPomodoroPhase() {
        return StudyTimerService.getPomodoroPhase();
    }

    public LiveData<Long> getSecondsElapsed() {
        return StudyTimerService.getSecondsElapsed();
    }

    public LiveData<Long> getEffectiveSeconds() {
        return StudyTimerService.getEffectiveSeconds();
    }

    public LiveData<Long> getPomodoroRemainingSeconds() {
        return StudyTimerService.getPomodoroRemainingSeconds();
    }

    public LiveData<Integer> getPomodoroCycle() {
        return StudyTimerService.getPomodoroCycle();
    }

    public LiveData<Long> getCurrentMateriaId() {
        return StudyTimerService.getCurrentMateriaId();
    }

    public LiveData<String> getCurrentMateriaNombre() {
        return StudyTimerService.getCurrentMateriaNombre();
    }

    public LiveData<String> getCurrentTema() {
        return StudyTimerService.getCurrentTema();
    }

    public LiveData<Boolean> getSesionGuardadaExito() {
        return sesionGuardadaExito;
    }

    public LiveData<String> getErrorMensaje() {
        return errorMensaje;
    }

    public void iniciarModoLibre(Context context, long materiaId, String materiaNombre, String tema) {
        Intent intent = new Intent(context, StudyTimerService.class);
        intent.setAction(StudyTimerService.ACTION_START_LIBRE);
        intent.putExtra(StudyTimerService.EXTRA_MATERIA_ID, materiaId);
        intent.putExtra(StudyTimerService.EXTRA_MATERIA_NOMBRE, materiaNombre);
        intent.putExtra(StudyTimerService.EXTRA_TEMA, tema);
        context.startService(intent);
    }

    public void iniciarModoPomodoro(Context context, long materiaId, String materiaNombre, String tema) {
        Intent intent = new Intent(context, StudyTimerService.class);
        intent.setAction(StudyTimerService.ACTION_START_POMODORO);
        intent.putExtra(StudyTimerService.EXTRA_MATERIA_ID, materiaId);
        intent.putExtra(StudyTimerService.EXTRA_MATERIA_NOMBRE, materiaNombre);
        intent.putExtra(StudyTimerService.EXTRA_TEMA, tema);
        context.startService(intent);
    }

    public void pausarTimer(Context context) {
        Intent intent = new Intent(context, StudyTimerService.class);
        intent.setAction(StudyTimerService.ACTION_PAUSE);
        context.startService(intent);
    }

    public void reanudarTimer(Context context) {
        Intent intent = new Intent(context, StudyTimerService.class);
        intent.setAction(StudyTimerService.ACTION_RESUME);
        context.startService(intent);
    }

    public void detenerServicio(Context context) {
        Intent intent = new Intent(context, StudyTimerService.class);
        intent.setAction(StudyTimerService.ACTION_STOP);
        context.startService(intent);
    }

    public void guardarSesionFinalizada(long materiaId, String titulo, long fechaInicio,
                                        long duracionSegundos, long tiempoEfectivoSeg,
                                        String modalidad, int satisfaccion, String notas) {
        try {
            long fin = System.currentTimeMillis();
            SesionEstudio sesion = new SesionEstudio(
                    materiaId,
                    titulo != null ? titulo : "",
                    fechaInicio > 0 ? fechaInicio : fin - (duracionSegundos * 1000L),
                    fin,
                    duracionSegundos,
                    tiempoEfectivoSeg,
                    modalidad != null ? modalidad : "LIBRE",
                    "COMPLETADA",
                    satisfaccion,
                    notas != null ? notas : ""
            );

            registrarSesionUseCase.execute(sesion, () -> {
                sesionGuardadaExito.setValue(true);
            });
        } catch (Exception e) {
            errorMensaje.setValue("Error al guardar sesión: " + e.getMessage());
        }
    }

    public void registrarSesionManual(long materiaId, String titulo, long fechaInicio,
                                      long duracionMinutos, int satisfaccion, String notas) {
        try {
            long duracionSeg = duracionMinutos * 60L;
            long fin = fechaInicio + (duracionSeg * 1000L);
            SesionEstudio sesion = new SesionEstudio(
                    materiaId,
                    titulo != null ? titulo : "Estudio Manual",
                    fechaInicio,
                    fin,
                    duracionSeg,
                    duracionSeg,
                    "LIBRE",
                    "COMPLETADA",
                    satisfaccion,
                    notas != null ? notas : ""
            );

            registrarSesionUseCase.execute(sesion, () -> {
                sesionGuardadaExito.setValue(true);
            });
        } catch (Exception e) {
            errorMensaje.setValue("Error al registrar sesión manual: " + e.getMessage());
        }
    }
}
