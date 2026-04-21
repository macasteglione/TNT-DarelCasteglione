package com.example.practicafragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.practicafragmentos.databinding.FragmentListaBinding

class ListaFragment : Fragment() {

    private var _binding: FragmentListaBinding? = null
    private val binding get() = _binding!!

    @Deprecated("Deprecated in Java")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val datos = listOf(
            ItemModelo(1, "Tarea 1", "Descripción de la primera tarea"),
            ItemModelo(2, "Tarea 2", "Descripción de la segunda tarea"),
            ItemModelo(3, "Tarea 3", "Algo importante que hacer"),
            ItemModelo(4, "Tarea 4", "Revisar el código de Android")
        )

        val adaptador = ItemAdapter(datos)
        binding.rvLista.adapter = adaptador
        binding.rvLista.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}