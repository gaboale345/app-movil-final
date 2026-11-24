package com.estudio.studyflow.domain.usecase;

import androidx.lifecycle.LiveData;

import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.domain.repository.EstudioRepository;

import java.util.List;

public class GestionarMateriasUseCase {

    private final EstudioRepository repository;

    public GestionarMateriasUseCase(EstudioRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Materia>> getMateriasActivas() {
        return repository.getMateriasActivas();
    }

    public LiveData<List<Materia>> getTodasLasMaterias() {
        return repository.getTodasLasMaterias();
    }

    public void registrarMateria(String nombre, String codigo, String colorHex, int creditos, String docente, Runnable onComplete) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la materia es obligatorio.");
        }
        if (colorHex == null || colorHex.trim().isEmpty()) {
            colorHex = "#2196F3";
        }
        Materia materia = new Materia(nombre.trim(), codigo != null ? codigo.trim() : "", colorHex, Math.max(0, creditos), docente != null ? docente.trim() : "");
        repository.insertMateria(materia, onComplete);
    }

    public void actualizarMateria(Materia materia, Runnable onComplete) {
        if (materia == null || materia.getNombre() == null || materia.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Datos de materia inválidos.");
        }
        repository.updateMateria(materia, onComplete);
    }

    public void archivarMateria(long idMateria, Runnable onComplete) {
        repository.setEstadoActivaMateria(idMateria, false, onComplete);
    }

    public void reactivarMateria(long idMateria, Runnable onComplete) {
        repository.setEstadoActivaMateria(idMateria, true, onComplete);
    }

    public void eliminarMateriaDefinitivamente(Materia materia, Runnable onComplete) {
        repository.deleteMateria(materia, onComplete);
    }
}
