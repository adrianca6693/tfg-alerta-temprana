package com.example.alertatemprana

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alertatemprana.models.Usuario
import com.example.alertatemprana.network.Retrofit
import com.example.alertatemprana.ui.theme.AlertaTempranaTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.alertatemprana.models.LoginRequest
import com.example.alertatemprana.models.BaseResponse

import androidx.compose.material3.OutlinedTextField

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.example.alertatemprana.models.RegisterRequest
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import com.example.alertatemprana.models.Contacto
import com.example.alertatemprana.models.addContactRequest
import com.example.alertatemprana.models.updateContactRequest
import com.example.alertatemprana.models.tripResponse
import com.example.alertatemprana.models.endTripRequest
import com.example.alertatemprana.models.checkResponse
import com.example.alertatemprana.models.checkPosResponse

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.core.content.ContextCompat
import com.example.alertatemprana.models.checkDistanceRequest
import com.example.alertatemprana.models.checkPositionRequest
import com.example.alertatemprana.models.tripRequest
import com.example.alertatemprana.network.PersistanceManager
import com.mapbox.common.location.LocationError
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.geojson.LineString
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.turf.TurfMeasurement
import com.mapbox.turf.TurfMisc
import kotlinx.coroutines.isActive
import kotlin.collections.isNotEmpty

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlertaTempranaTheme {






                AppNavigation()

                }


        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Estado de la API:")
        Text(text = name)
    }
}
@Composable
fun AppNavigation() {

    var pantallaActual by remember { mutableStateOf(if (PersistanceManager.isLoggedIn()) "mapa" else "login"
    ) }
    var userId by remember { mutableIntStateOf(PersistanceManager.userId) }
    var contactEdit by remember { mutableStateOf<Contacto?>(null) }

    var onTrip by remember { mutableStateOf(PersistanceManager.onTrip) }
    var tripId by remember { mutableIntStateOf(PersistanceManager.tripId) }
    var routePoints by remember { mutableStateOf(
        if (PersistanceManager.routePolyline.isNotEmpty())
            getPointsFromPolyline(PersistanceManager.routePolyline)
        else emptyList()
    ) }
    var destSelected by remember { mutableStateOf(
        if (PersistanceManager.destLat != 0.0 && PersistanceManager.destLon != 0.0)
            Point.fromLngLat(PersistanceManager.destLon, PersistanceManager.destLat)
        else null
    ) }
    Box(modifier = Modifier.fillMaxSize()) {
        when (pantallaActual) {
            "login" -> {
                LoginForm(
                    changeRegister = { pantallaActual = "registro" },
                    onLoginSuccess = { idRecibido ->
                        userId = idRecibido ?: -1
                        pantallaActual = "mapa"

                    })
            }

            "registro" -> {
                RegisterForm(changeRegister = { pantallaActual = "login" })
            }

            "contactos" -> {

                if (userId != -1) {
                    ContactsScreen(changeRegister = { pantallaActual = "mapa" },
                        onAddContact = { pantallaActual = "addContact"},
                        onUpdateContact = {contacto ->
                        contactEdit = contacto
                        pantallaActual = "updateContact"},
                        userId = userId)
                } else {

                    pantallaActual = "login"
                }
            }
            "addContact" -> {

                ContactForm(changeRegister = {pantallaActual = "contactos"},userId = userId)
            }
            "updateContact" -> {
                UpdateContactForm(
                    contacto = contactEdit,
                    onBack = { pantallaActual = "contactos" }

                )
            }
            "mapa" -> {
                MapboxScreen(changeRegister = {pantallaActual = "contactos"},userid = userId,onTrip = onTrip,
                    onTripChange = {
                        onTrip = it
                        PersistanceManager.onTrip = it },



                    tripId = tripId,
                    onTripIdChange = { tripId = it
                        PersistanceManager.tripId = it},
                    routePoints = routePoints,
                    onRoutePointsChange = { routePoints = it

                        if (it.isEmpty()) {
                            PersistanceManager.routePolyline = ""
                        }},
                    destSelected = destSelected,
                    onDestSelectedChange = { destSelected = it
                        if (it != null) {
                            PersistanceManager.destLat = it.latitude()
                            PersistanceManager.destLon = it.longitude()
                        } else {
                            PersistanceManager.destLat = 0.0
                            PersistanceManager.destLon = 0.0
                        } })
            }

        }
    }
}

@Composable
fun LoginForm(changeRegister: () -> Unit, onLoginSuccess: (Int?) -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Alerta Temprana",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    isError = false
                },
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                isError = isError,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    isError = false
                },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                },
                visualTransformation = PasswordVisualTransformation(),
                isError = isError,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )


            if (isError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = estado,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    cargando = true
                    estado = ""
                    isError = false
                    login(email, password) { user, token ->
                        cargando = false
                        if (user != null && token != null) {
                            PersistanceManager.token = token
                            PersistanceManager.userId = user.userid ?: -1
                            onLoginSuccess(user.userid)
                        } else {
                            estado = "Credenciales incorrectas"
                            isError = true
                        }
                    }
                },
                enabled = !cargando && email.isNotBlank() && password.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Iniciar Sesión", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { changeRegister() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Crear cuenta", fontWeight = FontWeight.Bold)
            }
        }
    }
}





fun login(email: String, password: String,result:(Usuario?,String?) -> Unit){
    val loginDatos = LoginRequest(email, password)
    Retrofit.instance.login(loginDatos).enqueue(object : Callback<BaseResponse<Usuario>> {
        override fun onResponse(call: Call<BaseResponse<Usuario>>, response: Response<BaseResponse<Usuario>>) {
            if (response.isSuccessful) {

                val body = response.body()
                result(body?.user, body?.token)
            } else {

                result(null,null)
            }
        }

        override fun onFailure(call: Call<BaseResponse<Usuario>>, t: Throwable) {
            Log.e("API_FAILURE", "Error de red: ${t.message}")

            result(null,null)
        }
    })
}

@Composable
fun RegisterForm(changeRegister: () -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(48.dp))

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Rellena los datos para registrarte",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it},
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it;},
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it;},
                label = { Text("Número de teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { pin = it; } },
                label = { Text("PIN (4 dígitos)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )


            if (estado.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = estado,
                        color = if (isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    cargando = true

                    isSuccess = false
                    estado = ""
                    register(name, email, password, phoneNumber, pin) { resultado ->
                        cargando = false
                        estado = resultado
                        if (resultado == "Te has registrado correctamente") {
                            isSuccess = true
                            scope.launch {
                                delay(3000)
                                changeRegister()
                            }
                        }
                    }
                },
                enabled = !cargando && email.isNotBlank() && password.isNotBlank() && name.isNotBlank() && phoneNumber.isNotBlank() && pin.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Crear cuenta", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { changeRegister() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Ya tengo cuenta", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun register(name: String,email: String,password: String,phoneNumber:String,pin:String,estado: (String) -> Unit){
    val registerData = RegisterRequest(name, email, password, phoneNumber, pin)
    Retrofit.instance.register(registerData).enqueue(object : Callback<BaseResponse<Usuario>> {
        override fun onResponse(call: Call<BaseResponse<Usuario>>, response: Response<BaseResponse<Usuario>>) {
            if (response.isSuccessful) {


                estado("Te has registrado correctamente")

            } else {

                estado("Ha ocurrido un error")
            }
        }

        override fun onFailure(call: Call<BaseResponse<Usuario>>, t: Throwable) {
            Log.e("API_FAILURE", "Error de red: ${t.message}")

            estado("Error de conexión")
        }
    })
}

@Composable
fun ContactsScreen(
    changeRegister: () -> Unit,
    onAddContact: () -> Unit,
    onUpdateContact: (Contacto) -> Unit,
    userId: Int
) {
    var contactList by remember { mutableStateOf<List<Contacto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var deleteContact by remember { mutableStateOf<Contacto?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        getContacts(userId) { lista ->
            contactList = lista
            loading = false
        }
    }

    if (showDialog && deleteContact != null) {
        val contactd = deleteContact
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar contacto", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que quieres eliminar a ${deleteContact?.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        deleteContact(contactd?.contactid ?: -1) {
                            loading = true
                            getContacts(userId) { lista ->
                                contactList = lista
                                loading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column {
                    Text(
                        text = "Contactos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "${contactList.size} contacto${if (contactList.size != 1) "s" else ""} guardado${if (contactList.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }


            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                contactList.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No tienes contactos guardados",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pulsa + para añadir uno",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(contactList) { contacto ->
                            ContactoItem(
                                contacto = contacto,
                                onEditClick = { onUpdateContact(contacto) },
                                onDeleteClick = {
                                    deleteContact = contacto
                                    showDialog = true
                                }
                            )
                        }
                    }
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { changeRegister() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("Volver", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onAddContact() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Añadir", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}







@Composable
fun ContactoItem(contacto: Contacto, onEditClick: () -> Unit, onDeleteClick:() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = contacto.name, fontWeight = FontWeight.Bold)
                    Text(text = contacto.phonenumber, style = MaterialTheme.typography.bodyMedium)

                }


                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modificar contacto",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar",
                        tint = Color(0xFFEF5350)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            CompartirEnlace(contacto.contactid,contacto.name)
        }
    }
}
fun getContacts(userId: Int, onResult: (List<Contacto>) -> Unit) {

    Retrofit.instance.getContacts(userId).enqueue(object : Callback<List<Contacto>> {
        override fun onResponse(call: Call<List<Contacto>>, response: Response<List<Contacto>>) {
            if (response.isSuccessful) {
                Log.e("body","body: ${response.body()}")
                onResult(response.body() ?: emptyList())
            } else {

                Log.e("API_ERROR", "Error en la respuesta: ${response.code()}")
                onResult(emptyList())
            }
        }

        override fun onFailure(call: Call<List<Contacto>>, t: Throwable) {

            Log.e("API_FAILURE", "Fallo total: ${t.message}")
            onResult(emptyList())
        }
    })
}
@Composable
fun ContactForm(changeRegister: () -> Unit, userId: Int) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nuevo contacto",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número de teléfono") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = changeRegister,
                modifier = Modifier.weight(1f)
            ) {
                Text("Volver")
            }

            Button(
                onClick = {
                    isLoading = true
                    addContact(userId, name, phoneNumber) { resultado ->
                        isLoading = false
                        if (resultado == "El contacto se ha añadido correctamente") {
                            scope.launch {
                                delay(1500)
                                changeRegister()
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() && phoneNumber.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}

fun addContact(userid: Int,name: String,phoneNumber:String,estado: (String) -> Unit){
    val contactData = addContactRequest(userid,name, phoneNumber)

    Retrofit.instance.addContact(contactData).enqueue(object : Callback<BaseResponse<Contacto>> {
        override fun onResponse(call: Call<BaseResponse<Contacto>>, response: Response<BaseResponse<Contacto>>) {
            if (response.isSuccessful) {


                estado("El contacto se ha añadido correctamente")

            } else {

                estado("Ha ocurrido un error")
            }
        }

        override fun onFailure(call: Call<BaseResponse<Contacto>>, t: Throwable) {
            Log.e("API_FAILURE", "Error de red: ${t.message}")

            estado("Error de conexión")
        }
    })
}
@Composable
fun UpdateContactForm(contacto: Contacto?, onBack: () -> Unit) {


    var name by remember { mutableStateOf(contacto?.name.toString()) }
    var phonenumber by remember { mutableStateOf(contacto?.phonenumber) }

    var estado by remember { mutableStateOf("Modifica los datos necesarios") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("MODIFICAR CONTACTO", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phonenumber.toString(),
            onValueChange = { phonenumber = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))





        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = {

                    estado = "Guardando cambios..."
                    updateContact(contacto?.contactid ?: -1,name,phonenumber?: ""){resultado ->

                        estado = resultado

                    }
                    scope.launch {
                        delay(1000)
                        onBack()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Guardar")
            }
        }

        Text(text = estado, modifier = Modifier.padding(top = 16.dp))
    }
}
fun updateContact(contactid: Int,name: String,phonenumber:String,estado: (String) -> Unit){
    val contactData = updateContactRequest(contactid,name, phonenumber)

    Retrofit.instance.updateContact(contactid,contactData).enqueue(object : Callback<BaseResponse<Contacto>> {
        override fun onResponse(call: Call<BaseResponse<Contacto>>, response: Response<BaseResponse<Contacto>>) {
            if (response.isSuccessful) {


                estado("El contacto se ha modificado correctamente")

            } else {

                estado("Ha ocurrido un error")
                val errorText = response.errorBody()?.string() ?: "Sin mensaje de error"

                Log.e("API_FAILURE", "Código de error: ${response.code()}")
                Log.e("API_FAILURE", "Cuerpo del error: $errorText")
            }
        }

        override fun onFailure(call: Call<BaseResponse<Contacto>>, t: Throwable) {
            Log.e("API_FAILURE", "Error de red: ${t.message}")

            estado("Error de conexión")
        }
    })
}
fun deleteContact(contactid: Int, onResult: (String) -> Unit) {


    Retrofit.instance.deleteContact(contactid).enqueue(object : Callback<BaseResponse<Unit>> {
        override fun onResponse(call: Call<BaseResponse<Unit>>, response: Response<BaseResponse<Unit>>) {
            if (response.isSuccessful) {
                onResult("Contacto eliminado correctamente")
            } else {

                onResult("Error al eliminar: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<BaseResponse<Unit>>, t: Throwable) {

            onResult("Error de conexión")
        }
    })
}


@Composable
fun MapboxScreen(changeRegister: () -> Unit,userid: Int,onTrip: Boolean, onTripChange: (Boolean) -> Unit, tripId: Int, onTripIdChange: (Int) -> Unit,routePoints: List<Point>,onRoutePointsChange: (List<Point>) -> Unit, destSelected: Point?, onDestSelectedChange: (Point?) -> Unit) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Point?>(null) }

    var isSelecting by remember { mutableStateOf(false) }
    var pinRequest by remember { mutableStateOf(false) }
    var pin by remember {mutableIntStateOf(-1)}



    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }


    val mapView = remember { MapView(context) }
    val circleAnnotationManager = remember {
        mapView.annotations.createCircleAnnotationManager()
    }
    val polylineAnnotationManager = remember {
        mapView.annotations.createPolylineAnnotationManager()
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
        }
    }


    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    DisposableEffect(onTrip, tripId, isSelecting) {
        val longClickListener = com.mapbox.maps.plugin.gestures.OnMapLongClickListener { point ->
            if (onTrip) {




                Retrofit.instance.checkDistance(
                    checkDistanceRequest(tripId, point.latitude(), point.longitude())
                ).enqueue(object : Callback<checkResponse> {
                    override fun onResponse(call: Call<checkResponse>, response: Response<checkResponse>) {
                        if (response.isSuccessful) {
                            val isValid = response.body()?.isValid
                            val isFinished = response.body()?.isFinished
                            Log.d("finalizado", isFinished.toString())
                            if (isValid == false) {
                                Log.w("Distance", "¡ALERTA! El usuario se ha salido de la ruta o se aleja")
                            }else if(isFinished == true){
                                Log.d("API", "Trayecto finalizado")

                                onTripChange(false)
                                onDestSelectedChange(null)
                                onRoutePointsChange(emptyList())
                                polylineAnnotationManager.deleteAll()
                                circleAnnotationManager.deleteAll()
                                onRoutePointsChange(emptyList())
                            }
                            else {
                                Log.w("Distance", "Posición correcta. Todo en orden.")
                            }
                        } else {
                            Log.e("API_ERROR", "Error en la respuesta del servidor: ${response.code()}")
                        }
                    }
                    override fun onFailure(call: Call<checkResponse>, t: Throwable) {
                        Log.e("API", "Error de conexión con el backend: ${t.message}")
                    }

                })
            } else if (isSelecting) {

                onDestSelectedChange(point)
                circleAnnotationManager.deleteAll()
                val circleOptions = CircleAnnotationOptions()
                    .withPoint(point)
                    .withCircleRadius(10.0)
                    .withCircleColor("#EE4E4E")
                    .withCircleStrokeWidth(2.0)
                    .withCircleStrokeColor("#ffffff")
                circleAnnotationManager.create(circleOptions)
                Log.d("MAPA", "Destino elegido: ${point.latitude()}, ${point.longitude()}")
            }
            true
        }


        mapView.gestures.addOnMapLongClickListener(longClickListener)


        onDispose {
            mapView.gestures.removeOnMapLongClickListener(longClickListener)
        }
    }



    LaunchedEffect(onTrip,tripId) {
        while (isActive) {
            if(onTrip) {


                userLocation?.let { location ->

                    val request = checkPositionRequest(
                        tripid = tripId,
                        currentLat = location.latitude(),
                        currentLon = location.longitude()
                    )
                    Retrofit.instance.checkPosition(request)
                        .enqueue(object : Callback<checkPosResponse> {
                            override fun onResponse(
                                call: Call<checkPosResponse>,
                                response: Response<checkPosResponse>
                            ) {
                                Log.w("Pos", "Posicion enviada")
                                val isStopped = response.body()?.isStopped
                                if (isStopped == true) {
                                    Log.w("Quieto", "El usuario está quieto")
                                }
                                val isSharpTurn = response.body()?.isSharpTurn
                                if(isSharpTurn == true){
                                    Log.w("Giro brusco", "El usuario ha dado varios giros bruscos [PELIGRO]")
                                }
                            }

                            override fun onFailure(call: Call<checkPosResponse>, t: Throwable) {
                                Log.e("API_ERROR", "Error de red en checkPosition: ${t.message}")
                            }

                        })


                }

            }
            delay(5000)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            if (hasLocationPermission) {

                mapView.location.updateSettings {
                    enabled = true
                    pulsingEnabled = true
                }


                val locationProvider = mapView.location.getLocationProvider()
                locationProvider?.registerLocationConsumer(object : LocationConsumer {
                    override fun onLocationUpdated(vararg location: Point, options: (ValueAnimator.() -> Unit)?) {
                        val lastLocation = location.lastOrNull()
                        if (lastLocation != null) {
                            userLocation = lastLocation
                            mapView.getMapboxMap().setCamera(
                                CameraOptions.Builder()
                                    .center(lastLocation)
                                    .zoom(15.0)
                                    .build()
                            )

                            locationProvider.unRegisterLocationConsumer(this)
                        }
                    }
                    override fun onBearingUpdated(vararg bearing: Double, options: (ValueAnimator.() -> Unit)?) {}
                    override fun onHorizontalAccuracyRadiusUpdated(vararg radius: Double, options: (ValueAnimator.() -> Unit)?) {}
                    override fun onError(error: LocationError) {}
                    override fun onPuckAccuracyRadiusAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {}
                    override fun onPuckBearingAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {}
                    override fun onPuckLocationAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {}
                })
                if (routePoints.isNotEmpty()) {
                    drawRoute(polylineAnnotationManager, routePoints)
                }
                if (destSelected != null) {
                    val circleOptions = CircleAnnotationOptions()
                        .withPoint(destSelected)
                        .withCircleRadius(10.0)
                        .withCircleColor("#EE4E4E")
                        .withCircleStrokeWidth(2.0)
                        .withCircleStrokeColor("#ffffff")
                    circleAnnotationManager.create(circleOptions)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(factory = { mapView },modifier = Modifier.fillMaxSize())

        if (isSelecting && !onTrip) {
            SearchBar(
                mapboxToken = "pk.eyJ1IjoiYWRyaWFuY3VhYXJjIiwiYSI6ImNtb3Y4MXJqcTA0bjEycnNjNWI4OTBkcGQifQ.eSxpAFz2fX7mSMwyn3SDQw",
                proximity = userLocation,
                onLocationSelected = { punto, nombre ->
                    onDestSelectedChange(punto)

                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(punto)
                            .zoom(15.0)
                            .build()
                    )

                    circleAnnotationManager.deleteAll()
                    val circleOptions = CircleAnnotationOptions()
                        .withPoint(punto)
                        .withCircleRadius(10.0)
                        .withCircleColor("#EE4E4E")
                        .withCircleStrokeWidth(2.0)
                        .withCircleStrokeColor("#ffffff")
                    circleAnnotationManager.create(circleOptions)
                    Log.d("MAPA", "Destino por búsqueda: $nombre")
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            )
        }


        if (isSelecting == false) {
            Button(
                onClick = { changeRegister() },
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    "Contactos",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if(pinRequest) {
            PinDialog(
                onDismiss = { pinRequest = false },
                onConfirm = { pinIntroduced ->
                    pin = pinIntroduced

                    Retrofit.instance.finishtrip(tripId, pin, endTripRequest(tripId, pin))
                        .enqueue(object : Callback<tripResponse> {
                            override fun onResponse(
                                call: Call<tripResponse>,
                                response: Response<tripResponse>
                            ) {
                                if (response.isSuccessful) {
                                    pinRequest = false
                                    Log.d("API", "Trayecto finalizado")

                                    onTripChange(false)
                                    onDestSelectedChange(null)
                                    onRoutePointsChange(emptyList())
                                    polylineAnnotationManager.deleteAll()
                                    circleAnnotationManager.deleteAll()
                                    onRoutePointsChange(emptyList())
                                }
                            }

                            override fun onFailure(call: Call<tripResponse>, t: Throwable) {
                                Log.e("API", "Error al conectar: ${t.message}")
                            }
                        })
                })




        }
        else {
            Button(
                onClick = {
                    if (onTrip) {
                        pinRequest = true
                    } else if (!isSelecting) {
                        isSelecting = true
                    } else if (destSelected != null) {
                        val nuevoTrayecto = tripRequest(
                            userid = userid,
                            name = "Nuevo Trayecto ${System.currentTimeMillis()}",
                            inilat = userLocation!!.latitude(),
                            inilon = userLocation!!.longitude(),
                            destlat = destSelected!!.latitude(),
                            destlon = destSelected!!.longitude(),
                            status = "ACTIVO"
                        )
                        Retrofit.instance.newtrip(nuevoTrayecto)
                            .enqueue(object : Callback<tripResponse> {
                                override fun onResponse(
                                    call: Call<tripResponse>,
                                    response: Response<tripResponse>
                                ) {
                                    if (response.isSuccessful) {

                                        Log.d("API", "Trayecto creado")
                                        onTripIdChange(response.body()!!.tripid)
                                        val route = response.body()?.route

                                        if (route != null) {
                                            PersistanceManager.routePolyline = route
                                            onRoutePointsChange(getPointsFromPolyline(route))
                                            onTripIdChange(response.body()!!.tripid)
                                            drawRoute(polylineAnnotationManager, getPointsFromPolyline(route))
                                            isSelecting = false
                                            onTripChange(true)

                                        }


                                    }
                                }

                                override fun onFailure(call: Call<tripResponse>, t: Throwable) {
                                    Log.e("API", "Error al conectar: ${t.message}")
                                }
                            })

                    }
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
                    .shadow(6.dp, RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (onTrip) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = when {
                        onTrip -> Icons.Default.Clear
                        !isSelecting -> Icons.Default.Add
                        destSelected == null -> Icons.Default.Search
                        else -> Icons.Default.Done
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = when {
                        onTrip -> "Finalizar trayecto"
                        !isSelecting -> "Nuevo viaje"
                        destSelected == null -> "Selecciona un destino..."
                        else -> "Confirmar destino"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

fun drawRoute(manager: PolylineAnnotationManager, puntos: List<Point>){


    manager.deleteAll()

    val polylineOptions = PolylineAnnotationOptions().withPoints(puntos).withLineColor("#4A90E2").withLineWidth(5.0).withLineJoin(LineJoin.ROUND)

    manager.create(polylineOptions)
}
fun getPointsFromPolyline(polyline: String): List<Point> {

    val lineString = LineString.fromPolyline(polyline, 6)
    return lineString.coordinates()
}
@Composable
fun PinDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {

    var pinText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Seguridad", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Introduce tu PIN para confirmar:")
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(

                    value = pinText,
                    onValueChange = { newValue ->

                        if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                            pinText = newValue
                        }
                    },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(

                onClick = {
                    val pinAsInt = pinText.toIntOrNull() ?: 0
                    onConfirm(pinAsInt)
                },

                enabled = pinText.length == 4
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
fun SearchBar(
    mapboxToken: String,
    proximity: Point? = null,
    onLocationSelected: (Point, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<Pair<String, Point>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                searchJob?.cancel()
                if (newQuery.length >= 3) {
                    searchJob = scope.launch {
                        delay(400)
                        isLoading = true
                        searchLocations(newQuery, mapboxToken,proximity) { results ->
                            suggestions = results
                            isLoading = false
                        }
                    }
                } else {
                    suggestions = emptyList()
                }
            },
            placeholder = { Text("Buscar destino...") },
            leadingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = {
                        query = ""
                        suggestions = emptyList()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp))
        )


        if (suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    suggestions.forEachIndexed { index, (nombre, punto) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    query = nombre
                                    suggestions = emptyList()
                                    onLocationSelected(punto, nombre)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Column {
                                val partes = nombre.split(",")
                                Text(
                                    text = partes.firstOrNull()?.trim() ?: nombre,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (partes.size > 1) {
                                    Text(
                                        text = partes.drop(1).joinToString(",").trim(),
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        if (index < suggestions.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}
fun searchLocations(
    query: String,
    token: String,
    proximity: Point? = null,
    onResult: (List<Pair<String, Point>>) -> Unit
) {
    val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
    val proximity2 = proximity?.let { "&proximity=${it.longitude()},${it.latitude()}" } ?: ""
    val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$token&language=es&limit=5" + proximity2

    Thread {
        try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val response = connection.inputStream.bufferedReader().readText()
            val json = org.json.JSONObject(response)
            val features = json.getJSONArray("features")
            val results = mutableListOf<Pair<String, Point>>()

            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val nombre = feature.getString("place_name")
                val coords = feature.getJSONObject("geometry").getJSONArray("coordinates")
                val punto = Point.fromLngLat(coords.getDouble(0), coords.getDouble(1))
                results.add(Pair(nombre, punto))
            }

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onResult(results)
            }
        } catch (e: Exception) {
            Log.e("Geocoding", "Error: ${e.message}")
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onResult(emptyList())
            }
        }
    }.start()
}
@Composable
fun CompartirEnlace(contactId: Int?,nombre:String){
    val context = LocalContext.current


    Button(
        onClick = {
            val url = "https://t.me/alertatemprana1bot?start=$contactId"
            val message = "$nombre, Necesito que te vincules como mi contacto de emergencia por mi Seguridad. Entra en este enlace y pulsa el botón de 'Iniciar': $url"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent,"Compartir enlace")
            context.startActivity(shareIntent)
        }


    ){
        Text(text= "Compartir enlace de telegram")
    }

}