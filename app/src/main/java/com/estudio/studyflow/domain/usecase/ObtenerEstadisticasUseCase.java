package com.estudio.studyflow.domain.usecase;

import com.estudio.studyflow.data.local.entity.SesionConMateria;
import com.estudio.studyflow.domain.repository.EstudioRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ObtenerEstadisticasUseCase {

    private final EstudioRepository repository;

    public ObtenerEstadisticasUseCase(EstudioRepository repository) {
        this.repository = repository;
    }

    public static class ResumenEstadistico {
        public int totalMinutos;
        public int promedioDiarioMinutos;
        public String materiaTopNombre;
        public int materiaTopMinutos;
        public Map<String, Integer> minutosPorDia;       // ej: "Lunes": 120
        public Map<String, Integer> minutosPorMateria;   // ej: "Matemáticas": 300
        public Map<String, String> coloresPorMateria;    // ej: "Matemáticas": "#2196F3"
    }

    public ResumenEstadistico calcularResumenSemanal() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicioSemana = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long finSemana = cal.getTimeInMillis();

        List<SesionConMateria> sesiones = repository.getSesionesParaReporte(inicioSemana, finSemana);

        ResumenEstadistico resumen = new ResumenEstadistico();
        resumen.minutosPorDia = new HashMap<>();
        resumen.minutosPorMateria = new HashMap<>();
        resumen.coloresPorMateria = new HashMap<>();

        // Inicializar días de la semana
        String[] diasNombres = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (String dia : diasNombres) {
            resumen.minutosPorDia.put(dia, 0);
        }

        int totalMinutos = 0;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", new Locale("es", "ES"));

        if (sesiones != null) {
            for (SesionConMateria s : sesiones) {
                int mins = (int) (s.tiempoEfectivoSeg / 60);
                totalMinutos += mins;

                // Día
                String nombreDia = capitalize(dayFormat.format(new Date(s.fechaInicio)).replace(".", ""));
                resumen.minutosPorDia.put(nombreDia, resumen.minutosPorDia.getOrDefault(nombreDia, 0) + mins);

                // Materia
                String matNombre = s.nombreMateria != null ? s.nombreMateria : "General";
                resumen.minutosPorMateria.put(matNombre, resumen.minutosPorMateria.getOrDefault(matNombre, 0) + mins);
                if (s.colorHex != null) {
                    resumen.coloresPorMateria.put(matNombre, s.colorHex);
                }
            }
        }

        resumen.totalMinutos = totalMinutos;
        resumen.promedioDiarioMinutos = totalMinutos / 7;

        // Materia Top
        String topMat = "Ninguna";
        int topMins = 0;
        for (Map.Entry<String, Integer> entry : resumen.minutosPorMateria.entrySet()) {
            if (entry.getValue() > topMins) {
                topMins = entry.getValue();
                topMat = entry.getKey();
            }
        }
        resumen.materiaTopNombre = topMat;
        resumen.materiaTopMinutos = topMins;

        return resumen;
    }

    public String generarReporteCSV(long inicio, long fin) {
        List<SesionConMateria> sesiones = repository.getSesionesParaReporte(inicio, fin);
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Materia,Tema,Fecha,Modalidad,DuracionBrutaMin,TiempoEfectivoMin,Satisfaccion,Notas\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        if (sesiones != null) {
            for (SesionConMateria s : sesiones) {
                sb.append(s.idSesion).append(",");
                sb.append(escapeCsv(s.nombreMateria)).append(",");
                sb.append(escapeCsv(s.titulo)).append(",");
                sb.append(sdf.format(new Date(s.fechaInicio))).append(",");
                sb.append(s.modalidad).append(",");
                sb.append(s.duracionSegundos / 60).append(",");
                sb.append(s.tiempoEfectivoSeg / 60).append(",");
                sb.append(s.satisfaccionPuntos).append(",");
                sb.append(escapeCsv(s.notasReflexion)).append("\n");
            }
        }
        return sb.toString();
    }

    public String generarReporteTXT(long inicio, long fin) {
        List<SesionConMateria> sesiones = repository.getSesionesParaReporte(inicio, fin);
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("          REPORTE DE ESTUDIO STUDYFLOW    \n");
        sb.append("=========================================\n\n");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        long totalEfectivo = 0;

        if (sesiones != null) {
            for (SesionConMateria s : sesiones) {
                totalEfectivo += s.tiempoEfectivoSeg;
                sb.append("• Materia: ").append(s.nombreMateria).append("\n");
                sb.append("  Tema: ").append(s.titulo != null && !s.titulo.isEmpty() ? s.titulo : "Sin tema").append("\n");
                sb.append("  Fecha: ").append(sdf.format(new Date(s.fechaInicio))).append("\n");
                sb.append("  Modalidad: ").append(s.modalidad).append(" | Concentración: ").append(s.satisfaccionPuntos).append("/5\n");
                sb.append("  Tiempo Efectivo: ").append(s.tiempoEfectivoSeg / 60).append(" min\n");
                if (s.notasReflexion != null && !s.notasReflexion.isEmpty()) {
                    sb.append("  Reflexión: ").append(s.notasReflexion).append("\n");
                }
                sb.append("-----------------------------------------\n");
            }
        }
        sb.append("\nTOTAL TIEMPO EFECTIVO: ").append(totalEfectivo / 60).append(" minutos (")
                .append(String.format(Locale.getDefault(), "%.1f", (float) totalEfectivo / 3600f)).append(" horas)\n");
        sb.append("=========================================\n");
        return sb.toString();
    }

    private String escapeCsv(String str) {
        if (str == null) return "\"\"";
        return "\"" + str.replace("\"", "\"\"") + "\"";
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }
}
