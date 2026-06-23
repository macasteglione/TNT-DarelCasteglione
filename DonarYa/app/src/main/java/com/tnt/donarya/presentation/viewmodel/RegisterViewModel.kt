package com.tnt.donarya.presentation.viewmodel

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.tnt.donarya.data.remote.dto.UpdateProfileRequestDto
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.AddressSuggestion
import com.tnt.donarya.domain.model.User
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.domain.usecase.RegisterUseCase
import com.tnt.donarya.presentation.state.RegisterUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val registerUseCase = RegisterUseCase(UserRepositoryImpl)
    private val placesClient = Places.createClient(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val geocoder = Geocoder(application, Locale.getDefault())

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    private val _addressSuggestions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val addressSuggestions: StateFlow<List<AutocompletePrediction>> = _addressSuggestions

    private val _selectedLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val selectedLocation: StateFlow<Pair<Double, Double>?> = _selectedLocation

    // State for form fields to ensure persistence and easy updates from VM
    var nombre = MutableStateFlow("")
    var email = MutableStateFlow("")
    var nombreComedor = MutableStateFlow("")
    var whatsapp = MutableStateFlow("")
    var direccion = MutableStateFlow("")
    var selectedRole = MutableStateFlow<UserRole?>(null)

    private var selectedLat = 0.0
    private var selectedLng = 0.0
    private var addressSearchJob: Job? = null

    fun initWithUser(user: User) {
        nombre.value = user.nombre
        email.value = user.email
        nombreComedor.value = user.nombreComedor ?: ""
        whatsapp.value = user.whatsapp ?: ""
        direccion.value = user.direccion ?: ""
        selectedRole.value = user.rol
        
        // No tenemos lat/lng en User directamente pero si en el repo/perfil
        // Si el merendero tiene coordenadas, deberíamos traerlas.
        viewModelScope.launch {
            user.merenderoId?.let { id ->
                val m = com.tnt.donarya.data.repository.MerenderoRepositoryImpl.getById(id)
                m?.let {
                    selectedLat = it.latitude
                    selectedLng = it.longitude
                    _selectedLocation.value = Pair(it.latitude, it.longitude)
                }
            }
        }
    }

    fun searchAddress(query: String) {
        Log.d("PlacesAPI", "Searching for: $query")
        if (query.length < 3) {
            _addressSuggestions.value = emptyList()
            return
        }
        addressSearchJob?.cancel()
        addressSearchJob = viewModelScope.launch {
            delay(500)
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    Log.d("PlacesAPI", "Results found: ${response.autocompletePredictions.size}")
                    _addressSuggestions.value = response.autocompletePredictions
                }
                .addOnFailureListener { exception ->
                    Log.e("PlacesAPI", "Error searching address", exception)
                    _addressSuggestions.value = emptyList()
                }
        }
    }

    fun selectAddress(prediction: AutocompletePrediction, onAddressSet: (String) -> Unit) {
        val placeId = prediction.placeId
        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        viewModelScope.launch {
            try {
                val response = placesClient.fetchPlace(request).await()
                val place = response.place
                place.latLng?.let {
                    selectedLat = it.latitude
                    selectedLng = it.longitude
                    _selectedLocation.value = Pair(selectedLat, selectedLng)
                }
                place.address?.let {
                    direccion.value = it
                    onAddressSet(it)
                }
                _addressSuggestions.value = emptyList()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            try {
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    selectedLat = it.latitude
                    selectedLng = it.longitude
                    _selectedLocation.value = Pair(selectedLat, selectedLng)
                    
                    // Obtener dirección a partir de coordenadas
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    addresses?.firstOrNull()?.let { address ->
                        val addressLine = address.getAddressLine(0) ?: ""
                        direccion.value = addressLine
                    }
                }
            } catch (e: SecurityException) {
                // Permisos no otorgados
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setLocation(lat: Double, lng: Double, onAddressSet: ((String) -> Unit)? = null) {
        selectedLat = lat
        selectedLng = lng
        _selectedLocation.value = Pair(lat, lng)
        
        onAddressSet?.let { callback ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    addresses?.firstOrNull()?.let { address ->
                        val addressLine = address.getAddressLine(0) ?: ""
                        direccion.value = addressLine
                        callback(addressLine)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun clearAddressSuggestions() {
        _addressSuggestions.value = emptyList()
    }

    fun registrar(
        nombre: String,
        email: String,
        password: String,
        rol: UserRole,
        nombreComedor: String? = null,
        whatsapp: String? = null,
        direccion: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = RegisterUiState.Loading

            val result = registerUseCase(
                nombre, email, password, rol,
                nombreComedor, whatsapp, direccion,
                selectedLat, selectedLng
            )

            _uiState.value = if (result.isSuccess)
                RegisterUiState.Success(result.getOrThrow())
            else
                RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
        }
    }



    fun actualizar(
        nombre: String,
        email: String,
        contrasenia: String,
        rol: UserRole,
        nombreComedor: String? = null,
        whatsapp: String? = null,
        direccion: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = RegisterUiState.Loading

            val result = UserRepositoryImpl.updateProfile(
                UpdateProfileRequestDto(
                    nombre = nombre,
                    email = email,
                    nombreComedor = nombreComedor?.ifBlank { null },
                    whatsapp = whatsapp?.ifBlank { null },
                    direccion = direccion?.ifBlank { null },
                    latitude = selectedLat,
                    longitude = selectedLng
                )
            )

            _uiState.value = if (result.isSuccess)
                RegisterUiState.Success(result.getOrThrow())
            else
                RegisterUiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al actualizar"
                )
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
