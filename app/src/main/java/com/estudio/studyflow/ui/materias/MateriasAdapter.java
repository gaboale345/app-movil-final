package com.estudio.studyflow.ui.materias;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.estudio.studyflow.R;
import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.databinding.ItemMateriaBinding;

public class MateriasAdapter extends ListAdapter<Materia, MateriasAdapter.MateriaViewHolder> {

    public interface OnMateriaActionListener {
        void onEditar(Materia materia);
        void onToggleActiva(Materia materia);
        void onEliminar(Materia materia);
    }

    private final OnMateriaActionListener listener;

    public MateriasAdapter(OnMateriaActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Materia> DIFF_CALLBACK = new DiffUtil.ItemCallback<Materia>() {
        @Override
        public boolean areItemsTheSame(@NonNull Materia oldItem, @NonNull Materia newItem) {
            return oldItem.getIdMateria() == newItem.getIdMateria();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Materia oldItem, @NonNull Materia newItem) {
            return oldItem.getNombre().equals(newItem.getNombre()) &&
                    oldItem.isEstaActiva() == newItem.isEstaActiva() &&
                    oldItem.getColorHex().equals(newItem.getColorHex());
        }
    };

    @NonNull
    @Override
    public MateriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMateriaBinding binding = ItemMateriaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MateriaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MateriaViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class MateriaViewHolder extends RecyclerView.ViewHolder {
        private final ItemMateriaBinding binding;

        public MateriaViewHolder(ItemMateriaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Materia materia) {
            binding.tvMateriaNombre.setText(materia.getNombre());

            String infoExtra = materia.getCodigo();
            if (materia.getCreditos() > 0) {
                infoExtra += (infoExtra.isEmpty() ? "" : " • ") + materia.getCreditos() + " Créditos";
            }
            binding.tvMateriaCodigo.setText(infoExtra.isEmpty() ? "Sin código" : infoExtra);

            if (materia.getDocente() != null && !materia.getDocente().isEmpty()) {
                binding.tvDocente.setText("Docente: " + materia.getDocente());
                binding.tvDocente.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvDocente.setVisibility(android.view.View.GONE);
            }

            try {
                int color = Color.parseColor(materia.getColorHex());
                binding.viewColorIndicator.setBackgroundColor(color);
            } catch (Exception e) {
                binding.viewColorIndicator.setBackgroundColor(Color.parseColor("#2196F3"));
            }

            if (materia.isEstaActiva()) {
                binding.tvEstado.setText("Activa");
                binding.tvEstado.setTextColor(Color.parseColor("#2E7D32"));
                binding.tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            } else {
                binding.tvEstado.setText("Archivada");
                binding.tvEstado.setTextColor(Color.parseColor("#757575"));
                binding.tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EEEEEE")));
            }

            binding.btnOpciones.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add(0, 1, 0, "Editar");
                popup.getMenu().add(0, 2, 1, materia.isEstaActiva() ? "Archivar" : "Reactivar");
                popup.getMenu().add(0, 3, 2, "Eliminar Definitivamente");
                popup.setOnMenuItemClickListener(item -> {
                    if (listener == null) return false;
                    switch (item.getItemId()) {
                        case 1:
                            listener.onEditar(materia);
                            return true;
                        case 2:
                            listener.onToggleActiva(materia);
                            return true;
                        case 3:
                            listener.onEliminar(materia);
                            return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}
