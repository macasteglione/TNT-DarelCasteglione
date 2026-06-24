package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.NeedRepositoryImpl
import com.tnt.donarya.domain.model.MerenderoWithNeeds
import com.tnt.donarya.presentation.state.MerenderoListUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MerenderoListViewModel : ViewModel() {

    private val merenderoRepository = MerenderoRepositoryImpl
    private val needRepository = NeedRepositoryImpl


    // Nuevo — evento para la pantalla
    private val _notificacion = MutableSharedFlow<String>()
    val notificacion: SharedFlow<String> = _notificacion

    // Snapshot de IDs conocidos antes del refresh
    private val needsSnapshot = mutableSetOf<String>()
    private val _uiState =
        MutableStateFlow<MerenderoListUiState>(
            MerenderoListUiState.Loading
        )

    val uiState: StateFlow<MerenderoListUiState> =
        _uiState

    init {
        cargarMerenderos()
    }

    private fun cargarMerenderos() {
        viewModelScope.launch(Dispatchers.IO) {   // ← hilo de fondo
            try {
                val merenderos = merenderoRepository.getAll()

                val merenderosWithNeeds = merenderos.map { merendero ->
                    MerenderoWithNeeds(
                        merendero = merendero,
                        needs = needRepository.getByMerendero(merendero.id).filter { !it.isCovered }
                    )
                }
                // Guardar IDs iniciales sin notificar
                merenderosWithNeeds.flatMap { it.needs }.forEach { needsSnapshot.add(it.id) }

                _uiState.value = MerenderoListUiState.Success(merenderosWithNeeds)

            } catch (e: Exception) {
                _uiState.value = MerenderoListUiState.Error(
                    e.message ?: "Error al cargar merenderos"
                )
            }
        }
    }

    fun refresh() {

        val current = _uiState.value

        if (current is MerenderoListUiState.Success) {
            _uiState.value =
                current.copy(isRefreshing = true)
        }

        viewModelScope.launch(Dispatchers.IO) {

            try {

                MerenderoRepositoryImpl.invalidateCache()

                val merenderos =
                    merenderoRepository.getAll()

                val merenderosWithNeeds =
                    merenderos.map { merendero ->

                        MerenderoWithNeeds(
                            merendero = merendero,
                            needs = needRepository.getByMerendero(
                                merendero.id
                            ).filter { !it.isCovered }
                        )
                    }

                val nuevas = merenderosWithNeeds
                    .flatMap { it.needs }
                    .filter { !it.isCovered && !needsSnapshot.contains(it.id) }

                if (nuevas.isNotEmpty()) {
                    val msg = if (nuevas.size == 1)
                        "🆕 Nueva necesidad en ${nuevas.first().title}"
                    else
                        "🆕 ${nuevas.size} nuevas necesidades publicadas"
                    _notificacion.emit(msg)
                }

                // Actualizar snapshot
                merenderosWithNeeds.flatMap { it.needs }.forEach { needsSnapshot.add(it.id) }

                _uiState.value =
                    MerenderoListUiState.Success(
                        merenderosWithNeeds,
                        isRefreshing = false
                    )

            } catch (e: Exception) {

                _uiState.value =
                    MerenderoListUiState.Error(
                        e.message ?: "Error al refrescar"
                    )
            }
        }
    }
}