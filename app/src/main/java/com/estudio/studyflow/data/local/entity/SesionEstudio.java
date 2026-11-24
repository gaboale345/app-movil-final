package com.estudio.studyflow.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "sesiones_estudio",
        foreignKeys = @ForeignKey(
                entity = Materia.class,
                parentColumns = "id_materia",
                childColumns = "id_materia",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "id_materia"),
                @Index(value = "fecha_inicio")
        }
)
public class SesionEstudio {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_sesion")
    private long idSesion;

    @ColumnInfo(name = "id_materia")
    private long idMateria;

    @ColumnInfo(name = "titulo")
    private String titulo;

    @ColumnInfo(name = "fecha_inicio")
    private long fechaInicio;

    @ColumnInfo(name = "fecha_fin")
    private long fechaFin;

    @ColumnInfo(name = "duracion_segundos")
    private long duracionSegundos;

    @ColumnInfo(name = "tiempo_efectivo_seg")
    private long tiempoEfectivoSeg;

    @ColumnInfo(name = "modalidad")
    private String modalidad; // "LIBRE" o "POMODORO"

    @ColumnInfo(name = "estado")
    private String estado;    // "PLANIFICADA", "COMPLETADA", "DESCARTADA"

    @ColumnInfo(name = "satisfaccion_puntos")
    private int satisfaccionPuntos;

    @ColumnInfo(name = "notas_reflexion")
    private String notasReflexion;

    public SesionEstudio() {
        this.titulo = "";
        this.modalidad = "LIBRE";
        this.estado = "COMPLETADA";
        this.satisfaccionPuntos = 3;
        this.notasReflexion = "";
    }

    public SesionEstudio(long idMateria, String titulo, long fechaInicio, long fechaFin,
                         long duracionSegundos, long tiempoEfectivoSeg, String modalidad,
                         String estado, int satisfaccionPuntos, String notasReflexion) {
        this.idMateria = idMateria;
        this.titulo = titulo != null ? titulo : "";
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.duracionSegundos = duracionSegundos;
        this.tiempoEfectivoSeg = tiempoEfectivoSeg;
        this.modalidad = modalidad != null ? modalidad : "LIBRE";
        this.estado = estado != null ? estado : "COMPLETADA";
        this.satisfaccionPuntos = satisfaccionPuntos;
        this.notasReflexion = notasReflexion != null ? notasReflexion : "";
    }

    public long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(long idSesion) {
        this.idSesion = idSesion;
    }

    public long getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(long idMateria) {
        this.idMateria = idMateria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public long getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(long fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public long getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(long fechaFin) {
        this.fechaFin = fechaFin;
    }

    public long getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(long duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public long getTiempoEfectivoSeg() {
        return tiempoEfectivoSeg;
    }

    public void setTiempoEfectivoSeg(long tiempoEfectivoSeg) {
        this.tiempoEfectivoSeg = tiempoEfectivoSeg;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getSatisfaccionPuntos() {
        return satisfaccionPuntos;
    }

    public void setSatisfaccionPuntos(int satisfaccionPuntos) {
        this.satisfaccionPuntos = satisfaccionPuntos;
    }

    public String getNotasReflexion() {
        return notasReflexion;
    }

    public void setNotasReflexion(String notasReflexion) {
        this.notasReflexion = notasReflexion;
    }
}
