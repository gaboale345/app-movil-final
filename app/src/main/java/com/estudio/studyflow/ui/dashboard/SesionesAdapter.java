package com.estudio.studyflow.ui.dashboard;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.estudio.studyflow.data.local.entity.SesionConMateria;
import com.estudio.studyflow.databinding.ItemSesionBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SesionesAdapter extends ListAdapter<SesionConMateria, SesionesAdapter.SesionViewHolder> {

    public SesionesAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SesionConMateria> DIFF_CALLBACK = new DiffUtil.ItemCallback<SesionConMateria>() {
        @Override
        public boolean areItemsTheSame(@NonNull SesionConMateria oldItem, @NonNull SesionConMateria newItem) {
            return oldItem.idSesion == newItem.idSesion;
        }

        @Override
        public boolean areContentsTheSame(@NonNull SesionConMateria oldItem, @NonNull SesionConMateria newItem) {
            return oldItem.fechaInicio == newItem.fechaInicio &&
                    oldItem.tiempoEfectivoSeg == newItem.tiempoEfectivoSeg &&
                    oldItem.titulo.equals(newItem.titulo);
        }
    };

    @NonNull
    @Override
    public SesionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSesionBinding binding = ItemSesionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new SesionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SesionViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static class SesionViewHolder extends RecyclerView.ViewHolder {
        private final ItemSesionBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

        public SesionViewHolder(ItemSesionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SesionConMateria sesion) {
            binding.tvMateriaNombre.setText(sesion.nombreMateria != null ? sesion.nombreMateria : "General");

            if (sesion.titulo != null && !sesion.titulo.trim().isEmpty()) {
                binding.tvTema.setText(sesion.titulo);
                binding.tvTema.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvTema.setVisibility(android.view.View.GONE);
            }

            try {
                int color = Color.parseColor(sesion.colorHex != null ? sesion.colorHex : "#2196F3");
                binding.viewMateriaColor.setBackgroundTintList(ColorStateList.valueOf(color));
            } catch (Exception e) {
                binding.viewMateriaColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
            }

            String fechaStr = dateFormat.format(new Date(sesion.fechaInicio));
            String modalidadStr = "POMODORO".equals(sesion.modalidad) ? "Modo Pomodoro" : "Modo Libre";
            binding.tvFechaModalidad.setText(fechaStr + " • " + modalidadStr);

            long minutos = sesion.tiempoEfectivoSeg / 60;
            if (minutos < 60) {
                binding.tvDuracionEfectiva.setText(minutos + " min");
            } else {
                long horas = minutos / 60;
                long minsRest = minutos % 60;
                binding.tvDuracionEfectiva.setText(horas + "h " + minsRest + "m");
            }

            int satisfaccion = sesion.satisfaccionPuntos > 0 ? sesion.satisfaccionPuntos : 3;
            binding.tvSatisfaccion.setText("★ " + satisfaccion + "/5");
        }
    }
}
