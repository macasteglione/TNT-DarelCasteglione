package com.example.practicafragmentos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.practicafragmentos.databinding.ItemListaBinding

class ItemAdapter(private val lista: List<ItemModelo>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(val binding: ItemListaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemListaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = lista[position]
        holder.binding.tvTitulo.text = item.titulo
        holder.binding.tvDescripcion.text = item.descripcion
    }

    override fun getItemCount(): Int = lista.size
}