package com.estudio.studyflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.estudio.studyflow.data.local.entity.MetaSemanal;
import com.estudio.studyflow.data.repository.EstudioRepositoryImpl;

import org.junit.Test;

/**
 * Casos de prueba CP01, CP02: Cálculo de metas y semaforización.
 */
public class CalcularMetasUseCaseTest {

    @Test
    public void testPorcentajeAvance_calculoExacto() {
        // CP01: Meta de 10 horas (600 minutos), alcanzados 5 horas (300 minutos)
        MetaSemanal meta = new MetaSemanal(1L, 2026, 48, 600, 300, "EN_PROGRESO");
        assertEquals(50.0f, meta.getPorcentajeAvance(), 0.01f);

        // Caso meta cumplida al 100%
        meta.setMinutosAlcanzados(600);
        assertEquals(100.0f, meta.getPorcentajeAvance(), 0.01f);

        // Caso meta superada (120%)
        meta.setMinutosAlcanzados(720);
        assertEquals(120.0f, meta.getPorcentajeAvance(), 0.01f);

        // Caso inicial (0%)
        meta.setMinutosAlcanzados(0);
        assertEquals(0.0f, meta.getPorcentajeAvance(), 0.01f);
    }

    @Test
    public void testPorcentajeAvance_conObjetivoCero() {
        MetaSemanal meta = new MetaSemanal(1L, 2026, 48, 0, 100, "EN_PROGRESO");
        assertEquals(0.0f, meta.getPorcentajeAvance(), 0.01f);
    }

    @Test
    public void testSemaforo_cumplidaCuandoAlcanzadoSuperaObjetivo() {
        // CP02: Estado CUMPLIDA cuando alcanzado >= objetivo
        String estado = EstudioRepositoryImpl.evaluarEstadoSemaforo(600, 600);
        assertEquals("CUMPLIDA", estado);

        String estadoSuperado = EstudioRepositoryImpl.evaluarEstadoSemaforo(800, 600);
        assertEquals("CUMPLIDA", estadoSuperado);
    }

    @Test
    public void testSemaforo_enProgresoOEnRiesgo() {
        String estado = EstudioRepositoryImpl.evaluarEstadoSemaforo(300, 600);
        // Debe ser EN_PROGRESO o EN_RIESGO dependiendo del día de ejecución
        assertTrue("EN_PROGRESO".equals(estado) || "EN_RIESGO".equals(estado));
    }
}
