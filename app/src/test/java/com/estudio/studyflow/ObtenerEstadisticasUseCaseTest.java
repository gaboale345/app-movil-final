package com.estudio.studyflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.estudio.studyflow.data.local.entity.SesionConMateria;
import com.estudio.studyflow.domain.repository.EstudioRepository;
import com.estudio.studyflow.domain.usecase.ObtenerEstadisticasUseCase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Caso de prueba CP08: Cálculo de métricas estadísticas y generación de reportes.
 */
public class ObtenerEstadisticasUseCaseTest {

    private EstudioRepository repository;
    private ObtenerEstadisticasUseCase useCase;

    @Before
    public void setUp() {
        repository = mock(EstudioRepository.class);
        useCase = new ObtenerEstadisticasUseCase(repository);
    }

    @Test
    public void testCalcularResumenSemanal_conSesiones() {
        List<SesionConMateria> sesiones = new ArrayList<>();

        SesionConMateria s1 = new SesionConMateria();
        s1.idSesion = 1;
        s1.idMateria = 1;
        s1.nombreMateria = "Cálculo";
        s1.colorHex = "#2196F3";
        s1.fechaInicio = System.currentTimeMillis();
        s1.tiempoEfectivoSeg = 3600; // 60 minutos
        sesiones.add(s1);

        SesionConMateria s2 = new SesionConMateria();
        s2.idSesion = 2;
        s2.idMateria = 2;
        s2.nombreMateria = "Física";
        s2.colorHex = "#4CAF50";
        s2.fechaInicio = System.currentTimeMillis();
        s2.tiempoEfectivoSeg = 1800; // 30 minutos
        sesiones.add(s2);

        when(repository.getSesionesParaReporte(anyLong(), anyLong())).thenReturn(sesiones);

        ObtenerEstadisticasUseCase.ResumenEstadistico resumen = useCase.calcularResumenSemanal();

        assertNotNull(resumen);
        assertEquals(90, resumen.totalMinutos);
        assertEquals(90 / 7, resumen.promedioDiarioMinutos);
        assertEquals("Cálculo", resumen.materiaTopNombre);
        assertEquals(60, resumen.materiaTopMinutos);
    }

    @Test
    public void testGenerarReporteCSV() {
        List<SesionConMateria> sesiones = new ArrayList<>();

        SesionConMateria s = new SesionConMateria();
        s.idSesion = 1;
        s.nombreMateria = "Programación";
        s.titulo = "Estructuras de Datos";
        s.fechaInicio = 1700000000000L;
        s.modalidad = "POMODORO";
        s.duracionSegundos = 3000;
        s.tiempoEfectivoSeg = 2400;
        s.satisfaccionPuntos = 5;
        s.notasReflexion = "Árboles binarios";
        sesiones.add(s);

        when(repository.getSesionesParaReporte(anyLong(), anyLong())).thenReturn(sesiones);

        String csv = useCase.generarReporteCSV(0L, System.currentTimeMillis());

        assertNotNull(csv);
        assertTrue(csv.contains("ID,Materia,Tema,Fecha,Modalidad,DuracionBrutaMin,TiempoEfectivoMin,Satisfaccion,Notas"));
        assertTrue(csv.contains("Programación"));
        assertTrue(csv.contains("Estructuras de Datos"));
        assertTrue(csv.contains("POMODORO"));
    }

    @Test
    public void testGenerarReporteTXT() {
        List<SesionConMateria> sesiones = new ArrayList<>();

        SesionConMateria s = new SesionConMateria();
        s.idSesion = 1;
        s.nombreMateria = "Álgebra Lineal";
        s.titulo = "Matrices y Determinantes";
        s.fechaInicio = 1700000000000L;
        s.modalidad = "LIBRE";
        s.tiempoEfectivoSeg = 1800;
        s.satisfaccionPuntos = 4;
        sesiones.add(s);

        when(repository.getSesionesParaReporte(anyLong(), anyLong())).thenReturn(sesiones);

        String txt = useCase.generarReporteTXT(0L, System.currentTimeMillis());

        assertNotNull(txt);
        assertTrue(txt.contains("REPORTE DE ESTUDIO STUDYFLOW"));
        assertTrue(txt.contains("Álgebra Lineal"));
        assertTrue(txt.contains("Matrices y Determinantes"));
    }
}
