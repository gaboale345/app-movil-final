package com.estudio.studyflow.data.local.entity;

import androidx.room.ColumnInfo;

public class SesionConMateria {

    @ColumnInfo(name = "id_sesion")
    public long idSesion;

    @ColumnInfo(name = "id_materia")
    public long idMateria;

    @ColumnInfo(name = "titulo")
    public String titulo;

    @ColumnInfo(name = "fecha_inicio")
    public long fechaInicio;

    @ColumnInfo(name = "fecha_fin")
    public long fechaFin;

    @ColumnInfo(name = "duracion_segundos")
    public long duracionSegundos;

    @ColumnInfo(name = "tiempo_efectivo_seg")
    public long tiempoEfectivoSeg;

    @ColumnInfo(name = "modalidad")
    public String modalidad;

    @ColumnInfo(name = "estado")
    public String estado;

    @ColumnInfo(name = "satisfaccion_puntos")
    public int satisfaccionPuntos;

    @ColumnInfo(name = "notas_reflexion")
    public String notasReflexion;

    @ColumnInfo(name = "nombre_materia")
    public String nombreMateria;

    @ColumnInfo(name = "color_hex")
    public String colorHex;
}
