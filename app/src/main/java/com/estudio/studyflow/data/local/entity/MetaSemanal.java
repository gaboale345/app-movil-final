package com.estudio.studyflow.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "metas_semanales",
        foreignKeys = @ForeignKey(
                entity = Materia.class,
                parentColumns = "id_materia",
                childColumns = "id_materia",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "id_materia"),
                @Index(value = {"anio", "numero_semana"})
        }
)
public class MetaSemanal {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_meta")
    private long idMeta;

    @ColumnInfo(name = "id_materia")
    private Long idMateria; // null si es meta global

    @ColumnInfo(name = "anio")
    private int anio;

    @ColumnInfo(name = "numero_semana")
    private int numeroSemana;

    @ColumnInfo(name = "minutos_objetivo")
    private int minutosObjetivo;

    @ColumnInfo(name = "minutos_alcanzados")
    private int minutosAlcanzados;

    @ColumnInfo(name = "estado")
    private String estado; // "EN_PROGRESO", "CUMPLIDA", "EN_RIESGO"

    @ColumnInfo(name = "fecha_registro")
    private long fechaRegistro;

    public MetaSemanal() {
        this.estado = "EN_PROGRESO";
        this.fechaRegistro = System.currentTimeMillis();
    }

    public MetaSemanal(Long idMateria, int anio, int numeroSemana, int minutosObjetivo,
                       int minutosAlcanzados, String estado) {
        this.idMateria = idMateria;
        this.anio = anio;
        this.numeroSemana = numeroSemana;
        this.minutosObjetivo = minutosObjetivo;
        this.minutosAlcanzados = minutosAlcanzados;
        this.estado = estado != null ? estado : "EN_PROGRESO";
        this.fechaRegistro = System.currentTimeMillis();
    }

    public long getIdMeta() {
        return idMeta;
    }

    public void setIdMeta(long idMeta) {
        this.idMeta = idMeta;
    }

    public Long getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(Long idMateria) {
        this.idMateria = idMateria;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getNumeroSemana() {
        return numeroSemana;
    }

    public void setNumeroSemana(int numeroSemana) {
        this.numeroSemana = numeroSemana;
    }

    public int getMinutosObjetivo() {
        return minutosObjetivo;
    }

    public void setMinutosObjetivo(int minutosObjetivo) {
        this.minutosObjetivo = minutosObjetivo;
    }

    public int getMinutosAlcanzados() {
        return minutosAlcanzados;
    }

    public void setMinutosAlcanzados(int minutosAlcanzados) {
        this.minutosAlcanzados = minutosAlcanzados;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(long fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public float getPorcentajeAvance() {
        if (minutosObjetivo > 0) {
            return Math.max(0f, ((float) minutosAlcanzados / (float) minutosObjetivo) * 100f);
        }
        return 0f;
    }
}
