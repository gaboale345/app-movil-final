package com.estudio.studyflow.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "intervalos_tiempo",
        foreignKeys = @ForeignKey(
                entity = SesionEstudio.class,
                parentColumns = "id_sesion",
                childColumns = "id_sesion",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "id_sesion")
        }
)
public class IntervaloTiempo {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_intervalo")
    private long idIntervalo;

    @ColumnInfo(name = "id_sesion")
    private long idSesion;

    @ColumnInfo(name = "tipo")
    private String tipo; // "TRABAJO" o "DESCANSO"

    @ColumnInfo(name = "marca_inicio")
    private long marcaInicio;

    @ColumnInfo(name = "marca_fin")
    private long marcaFin;

    @ColumnInfo(name = "segundos_transcurridos")
    private long segundosTranscurridos;

    public IntervaloTiempo() {
        this.tipo = "TRABAJO";
    }

    public IntervaloTiempo(long idSesion, String tipo, long marcaInicio, long marcaFin, long segundosTranscurridos) {
        this.idSesion = idSesion;
        this.tipo = tipo != null ? tipo : "TRABAJO";
        this.marcaInicio = marcaInicio;
        this.marcaFin = marcaFin;
        this.segundosTranscurridos = segundosTranscurridos;
    }

    public long getIdIntervalo() {
        return idIntervalo;
    }

    public void setIdIntervalo(long idIntervalo) {
        this.idIntervalo = idIntervalo;
    }

    public long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(long idSesion) {
        this.idSesion = idSesion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getMarcaInicio() {
        return marcaInicio;
    }

    public void setMarcaInicio(long marcaInicio) {
        this.marcaInicio = marcaInicio;
    }

    public long getMarcaFin() {
        return marcaFin;
    }

    public void setMarcaFin(long marcaFin) {
        this.marcaFin = marcaFin;
    }

    public long getSegundosTranscurridos() {
        return segundosTranscurridos;
    }

    public void setSegundosTranscurridos(long segundosTranscurridos) {
        this.segundosTranscurridos = segundosTranscurridos;
    }
}
