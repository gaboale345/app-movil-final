package com.estudio.studyflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.domain.repository.EstudioRepository;
import com.estudio.studyflow.domain.usecase.GestionarMateriasUseCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Casos de prueba CP03, CP04, CP05: Gestión y validaciones de materias.
 */
public class GestionarMateriasUseCaseTest {

    private EstudioRepository repository;
    private GestionarMateriasUseCase useCase;

    @Before
    public void setUp() {
        repository = mock(EstudioRepository.class);
        useCase = new GestionarMateriasUseCase(repository);
    }

    @Test
    public void testRegistrarMateria_nombreVacioLanzaExcepcion() {
        // CP03: Validación de campos obligatorios
        try {
            useCase.registrarMateria("", "MAT101", "#2196F3", 4, "Dr. Pérez", null);
            fail("Debe lanzar IllegalArgumentException cuando el nombre está vacío");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            useCase.registrarMateria(null, "MAT101", "#2196F3", 4, "Dr. Pérez", null);
            fail("Debe lanzar IllegalArgumentException cuando el nombre es null");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testRegistrarMateria_exitosa() {
        // CP04: Registro exitoso con valores correctos
        ArgumentCaptor<Materia> captor = ArgumentCaptor.forClass(Materia.class);

        useCase.registrarMateria("Cálculo Integral", "MAT-201", "#E91E63", 4, "Ing. Gómez", null);

        verify(repository).insertMateria(captor.capture(), any());
        Materia capturada = captor.getValue();
        assertEquals("Cálculo Integral", capturada.getNombre());
        assertEquals("MAT-201", capturada.getCodigo());
        assertEquals("#E91E63", capturada.getColorHex());
        assertEquals(4, capturada.getCreditos());
        assertEquals("Ing. Gómez", capturada.getDocente());
    }

    @Test
    public void testArchivarMateria() {
        // CP05: Desactivar / archivar materia
        useCase.archivarMateria(10L, null);
        verify(repository).setEstadoActivaMateria(eq(10L), eq(false), any());
    }

    @Test
    public void testReactivarMateria() {
        useCase.reactivarMateria(10L, null);
        verify(repository).setEstadoActivaMateria(eq(10L), eq(true), any());
    }
}
