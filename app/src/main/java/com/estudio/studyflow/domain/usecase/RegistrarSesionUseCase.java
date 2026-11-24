package com.estudio.studyflow.domain.usecase;

import com.estudio.studyflow.data.local.entity.SesionEstudio;
import com.estudio.studyflow.domain.repository.EstudioRepository;

public class RegistrarSesionUseCase {

    private final EstudioRepository repository;

    public RegistrarSesionUseCase(EstudioRepository repository) {
        this.repository = repository;
    }

    public void execute(SesionEstudio sesion, Runnable onComplete) {
        if (sesion == null) return;
        if (sesion.getIdMateria() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una materia válida.");
        }
        if (sesion.getFechaInicio() <= 0) {
            sesion.setFechaInicio(System.currentTimeMillis());
        }
        if (sesion.getFechaFin() <= 0) {
            sesion.setFechaFin(System.currentTimeMillis());
        }
        if (sesion.getDuracionSegundos() < 0) {
            sesion.setDuracionSegundos(0);
        }
        if (sesion.getTiempoEfectivoSeg() < 0) {
            sesion.setTiempoEfectivoSeg(0);
        }

        repository.insertSesion(sesion, onComplete);
    }
}
