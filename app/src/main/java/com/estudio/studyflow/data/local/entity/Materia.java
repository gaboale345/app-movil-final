package com.estudio.studyflow.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "materias")
public class Materia {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_materia")
    private long idMateria;

    @ColumnInfo(name = "nombre")
    private String nombre;

    @ColumnInfo(name = "codigo")
    private String codigo;

    @ColumnInfo(name = "color_hex")
    private String colorHex;

    @ColumnInfo(name = "creditos")
    private int creditos;

    @ColumnInfo(name = "docente")
    private String docente;

    @ColumnInfo(name = "fecha_creacion")
    private long fechaCreacion;

    @ColumnInfo(name = "esta_activa")
    private boolean estaActiva;

    // Constructor por defecto requerido por Room
    public Materia() {
        this.codigo = "";
        this.colorHex = "#2196F3";
        this.creditos = 0;
        this.docente = "";
        this.fechaCreacion = System.currentTimeMillis();
        this.estaActiva = true;
    }

    public Materia(String nombre, String codigo, String colorHex, int creditos, String docente) {
        this.nombre = nombre;
        this.codigo = codigo != null ? codigo : "";
        this.colorHex = colorHex != null ? colorHex : "#2196F3";
        this.creditos = creditos;
        this.docente = docente != null ? docente : "";
        this.fechaCreacion = System.currentTimeMillis();
        this.estaActiva = true;
    }

    public long getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(long idMateria) {
        this.idMateria = idMateria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public int getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }

    public String getDocente() {
        return docente;
    }

    public void setDocente(String docente) {
        this.docente = docente;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isEstaActiva() {
        return estaActiva;
    }

    public void setEstaActiva(boolean estaActiva) {
        this.estaActiva = estaActiva;
    }
}
