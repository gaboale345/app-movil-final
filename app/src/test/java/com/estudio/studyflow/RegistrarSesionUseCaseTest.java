package com.estudio.studyflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.estudio.studyflow.data.local.entity.SesionEstudio;
import com.estudio.studyflow.domain.repository.EstudioRepository;
import com.estudio.studyflow.domain.usecase.RegistrarSesionUseCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Casos de prueba CP06, CP07: Registro y validaciones de sesión de estudio.
 */
public class RegistrarSesionUseCaseTest {

    private EstudioRepository repository;
    private RegistrarSesionUseCase useCase;

    @Before
    public void setUp() {
        repository = mock(EstudioRepository.class);
        useCase = new RegistrarSesionUseCase(repository);
    }

    @Test
    public void testRegistrarSesion_idMateriaInvalidoLanzaExcepcion() {
        // CP06: Validación de materia obligatoria
        SesionEstudio sesionInvalida = new SesionEstudio(0L, "Tema X", 1000L, 2000L, 1000L, 800L, "LIBRE", "COMPLETADA", 4, "");
        try {
            useCase.execute(sesionInvalida, null);
            fail("Debe lanzar IllegalArgumentException si idMateria <= 0");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testRegistrarSesion_exitosa() {
        // CP07: Registro exitoso y ajuste de tiempos
        long ahora = System.currentTimeMillis();
        SesionEstudio sesionValida = new SesionEstudio(1L, "Derivadas Parciales", ahora - 1800000L, ahora, 1800L, 1500L, "POMODORO", "COMPLETADA", 5, "Buen enfoque");

        ArgumentCaptor<SesionEstudio> captor = ArgumentCaptor.forClass(SesionEstudio.class);

        useCase.execute(sesionValida, null);

        verify(repository).insertSesion(captor.capture(), any());
        SesionEstudio capturada = captor.getValue();
        assertEquals(1L, capturada.getIdMateria());
        assertEquals("Derivadas Parciales", capturada.getTitulo());
        assertEquals(1800L, capturada.getDuracionSegundos());
        assertEquals(1500L, capturada.getTiempoEfectivoSeg());
        assertEquals("POMODORO", capturada.getModalidad());
        assertEquals(5, capturada.getSatisfaccionPuntos());
    }

    @Test
    public void testRegistrarSesion_tiemposNegativosAjustadosACero() {
        SesionEstudio sesionNegativa = new SesionEstudio(2L, "Física", 0L, 0L, -50L, -20L, "LIBRE", "COMPLETADA", 3, "");
        ArgumentCaptor<SesionEstudio> captor = ArgumentCaptor.forClass(SesionEstudio.class);

        useCase.execute(sesionNegativa, null);

        verify(repository).insertSesion(captor.capture(), any());
        SesionEstudio capturada = captor.getValue();
        assertEquals(0L, capturada.getDuracionSegundos());
        assertEquals(0L, capturada.getTiempoEfectivoSeg());
        assertTrue(capturada.getFechaInicio() > 0);
        assertTrue(capturada.getFechaFin() > 0);
    }
}
