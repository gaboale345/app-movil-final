package com.estudio.studyflow.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "etiquetas_temas",
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
public class EtiquetaTema {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_etiqueta")
    private long idEtiqueta;

    @ColumnInfo(name = "id_sesion")
    private long idSesion;

    @ColumnInfo(name = "nombre_tema")
    private String nombreTema;

    @ColumnInfo(name = "dificultad_percibida")
    private int dificultadPercibida; // 1 a 5

    public EtiquetaTema() {
        this.nombreTema = "";
        this.dificultadPercibida = 3;
    }

    public EtiquetaTema(long idSesion, String nombreTema, int dificultadPercibida) {
        this.idSesion = idSesion;
        this.nombreTema = nombreTema != null ? nombreTema : "";
        this.dificultadPercibida = dificultadPercibida;
    }

    public long getIdEtiqueta() {
        return idEtiqueta;
    }

    public void setIdEtiqueta(long idEtiqueta) {
        this.idEtiqueta = idEtiqueta;
    }

    public long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(long idSesion) {
        this.idSesion = idSesion;
    }

    public String getNombreTema() {
        return nombreTema;
    }

    public void setNombreTema(String nombreTema) {
        this.nombreTema = nombreTema;
    }

    public int getDificultadPercibida() {
        return dificultadPercibida;
    }

    public void setDificultadPercibida(int dificultadPercibida) {
        this.dificultadPercibida = dificultadPercibida;
    }
}
