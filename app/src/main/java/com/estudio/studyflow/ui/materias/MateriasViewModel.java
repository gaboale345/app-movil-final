package com.estudio.studyflow.ui.materias;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.data.repository.EstudioRepositoryImpl;
import com.estudio.studyflow.domain.usecase.GestionarMateriasUseCase;

import java.util.List;

public class MateriasViewModel extends AndroidViewModel {

    private final GestionarMateriasUseCase gestionarMateriasUseCase;
    private final MutableLiveData<String> mensajeFeedback = new MutableLiveData<>();

    public MateriasViewModel(@NonNull Application application) {
        super(application);
        EstudioRepositoryImpl repository = EstudioRepositoryImpl.getInstance(application);
        this.gestionarMateriasUseCase = new GestionarMateriasUseCase(repository);
    }

    public LiveData<List<Materia>> getMateriasActivas() {
        return gestionarMateriasUseCase.getMateriasActivas();
    }

    public LiveData<List<Materia>> getTodasLasMaterias() {
        return gestionarMateriasUseCase.getTodasLasMaterias();
    }

    public LiveData<String> getMensajeFeedback() {
        return mensajeFeedback;
    }

    public void registrarMateria(String nombre, String codigo, String colorHex, int creditos, String docente) {
        try {
            gestionarMateriasUseCase.registrarMateria(nombre, codigo, colorHex, creditos, docente, () -> {
                mensajeFeedback.setValue("Materia registrada exitosamente.");
            });
        } catch (IllegalArgumentException e) {
            mensajeFeedback.setValue(e.getMessage());
        }
    }

    public void actualizarMateria(Materia materia) {
        try {
            gestionarMateriasUseCase.actualizarMateria(materia, () -> {
                mensajeFeedback.setValue("Materia actualizada exitosamente.");
            });
        } catch (IllegalArgumentException e) {
            mensajeFeedback.setValue(e.getMessage());
        }
    }

    public void archivarMateria(long idMateria) {
        gestionarMateriasUseCase.archivarMateria(idMateria, () -> {
            mensajeFeedback.setValue("Materia archivada.");
        });
    }

    public void reactivarMateria(long idMateria) {
        gestionarMateriasUseCase.reactivarMateria(idMateria, () -> {
            mensajeFeedback.setValue("Materia reactivada.");
        });
    }

    public void eliminarMateria(Materia materia) {
        gestionarMateriasUseCase.eliminarMateriaDefinitivamente(materia, () -> {
            mensajeFeedback.setValue("Materia eliminada.");
        });
    }
}
