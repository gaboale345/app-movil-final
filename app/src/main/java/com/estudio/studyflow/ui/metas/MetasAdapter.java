package com.estudio.studyflow.ui.metas;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.estudio.studyflow.data.local.entity.MetaSemanal;
import com.estudio.studyflow.databinding.ItemMetaBinding;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MetasAdapter extends ListAdapter<MetaSemanal, MetasAdapter.MetaViewHolder> {

    private final Map<Long, String> nombresMaterias = new HashMap<>();

    public MetasAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setNombresMaterias(Map<Long, String> map) {
        nombresMaterias.clear();
        if (map != null) {
            nombresMaterias.putAll(map);
        }
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<MetaSemanal> DIFF_CALLBACK = new DiffUtil.ItemCallback<MetaSemanal>() {
        @Override
        public boolean areItemsTheSame(@NonNull MetaSemanal oldItem, @NonNull MetaSemanal newItem) {
            return oldItem.getIdMeta() == newItem.getIdMeta();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MetaSemanal oldItem, @NonNull MetaSemanal newItem) {
            return oldItem.getMinutosObjetivo() == newItem.getMinutosObjetivo() &&
                    oldItem.getMinutosAlcanzados() == newItem.getMinutosAlcanzados() &&
                    oldItem.getEstado().equals(newItem.getEstado());
        }
    };

    @NonNull
    @Override
    public MetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMetaBinding binding = ItemMetaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MetaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MetaViewHolder holder, int position) {
        MetaSemanal meta = getItem(position);
        String nombreMateria = null;
        if (meta.getIdMateria() != null) {
            nombreMateria = nombresMaterias.get(meta.getIdMateria());
        }
        holder.bind(meta, nombreMateria);
    }

    public static class MetaViewHolder extends RecyclerView.ViewHolder {
        private final ItemMetaBinding binding;

        public MetaViewHolder(ItemMetaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MetaSemanal meta, String nombreMateria) {
            String titulo;
            if (meta.getIdMateria() == null) {
                titulo = "Meta Global Semanal";
            } else if (nombreMateria != null && !nombreMateria.isEmpty()) {
                titulo = "Meta: " + nombreMateria;
            } else {
                titulo = "Meta de Materia #" + meta.getIdMateria();
            }
            binding.tvMetaTitulo.setText(titulo);

            float horasObj = meta.getMinutosObjetivo() / 60f;
            float horasAlc = meta.getMinutosAlcanzados() / 60f;
            binding.tvMetaTiempo.setText(String.format(Locale.getDefault(), "%.1fh alcanzadas de %.1fh objetivo", horasAlc, horasObj));

            int porcentaje = (int) meta.getPorcentajeAvance();
            binding.progressMeta.setProgress(Math.min(100, porcentaje));
            binding.tvMetaPorcentaje.setText(porcentaje + "%");

            int statusColor;
            int statusBgColor;
            String estadoTexto;

            switch (meta.getEstado()) {
                case "CUMPLIDA":
                    statusColor = Color.parseColor("#2E7D32");
                    statusBgColor = Color.parseColor("#E8F5E9");
                    estadoTexto = "CUMPLIDA";
                    break;
                case "EN_RIESGO":
                    statusColor = Color.parseColor("#C62828");
                    statusBgColor = Color.parseColor("#FFEBEE");
                    estadoTexto = "EN RIESGO";
                    break;
                case "EN_PROGRESO":
                default:
                    statusColor = Color.parseColor("#F57C00");
                    statusBgColor = Color.parseColor("#FFF3E0");
                    estadoTexto = "EN PROGRESO";
                    break;
            }

            binding.tvMetaEstado.setText(estadoTexto);
            binding.tvMetaEstado.setTextColor(statusColor);
            binding.tvMetaEstado.setBackgroundTintList(ColorStateList.valueOf(statusBgColor));
            binding.progressMeta.setIndicatorColor(statusColor);
        }
    }
}
