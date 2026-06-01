package com.example.alertatemprana.network

import com.example.alertatemprana.models.Usuario
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body
import com.example.alertatemprana.models.LoginRequest
import com.example.alertatemprana.models.BaseResponse
import com.example.alertatemprana.models.RegisterRequest
import com.example.alertatemprana.models.Contacto
import com.example.alertatemprana.models.addContactRequest
import com.example.alertatemprana.models.checkResponse
import com.example.alertatemprana.models.endTripRequest
import com.example.alertatemprana.models.tripRequest
import com.example.alertatemprana.models.tripResponse
import com.example.alertatemprana.models.updateContactRequest
import com.example.alertatemprana.models.checkDistanceRequest
import com.example.alertatemprana.models.checkPositionRequest
import com.example.alertatemprana.models.checkPosResponse
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {

    @GET("usuarios/{id}")
    fun obtenerUsuarioPorId(@Path("id") id: Int): Call<Usuario>

    @POST("login")
    fun login(@Body request: LoginRequest): Call<BaseResponse<Usuario>>

    @POST(value="register")
    fun register(@Body request: RegisterRequest): Call <BaseResponse<Usuario>>

    @GET("contacts/{userid}")
    fun getContacts(@Path("userid") userId: Int): Call<List<Contacto>>

    @POST("contacts/add")
    fun addContact(@Body request: addContactRequest): Call <BaseResponse<Contacto>>

    @PUT("contacts/update/{contactid}")
    fun updateContact(
        @Path("contactid") contactid: Int,
        @Body request: updateContactRequest
    ): Call<BaseResponse<Contacto>>

    @DELETE("contacts/delete/{contactid}")
    fun deleteContact(
        @Path("contactid") contactid: Int
    ): Call<BaseResponse<Unit>>

    @POST("trips/newtrip")
    fun newtrip(@Body request: tripRequest): Call<tripResponse>

    @PATCH("trips/finish/{id}/{pin}")
    fun finishtrip(@Path("id") id: Int,
                   @Path("pin") pin: Int,
                   @Body request: endTripRequest): Call<tripResponse>

    @POST("trips/checkDistance")
    fun checkDistance(@Body request: checkDistanceRequest): Call <checkResponse>

    @POST("trips/checkPosition")
    fun checkPosition(@Body request: checkPositionRequest): Call <checkPosResponse>

}