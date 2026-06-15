package com.example.alertatemprana.network
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object PersistanceManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var token: String
        get() = prefs.getString(KEY_TOKEN, "") ?: ""
        set(value) { prefs.edit().putString(KEY_TOKEN, value).apply() }

    var userId: Int
        get() = prefs.getInt(KEY_USER_ID, -1)
        set(value) { prefs.edit().putInt(KEY_USER_ID, value).apply() }

    var onTrip: Boolean
        get() = prefs.getBoolean("on_trip", false)
        set(value) { prefs.edit().putBoolean("on_trip", value).apply() }

    var tripId: Int
        get() = prefs.getInt("trip_id", -1)
        set(value) { prefs.edit().putInt("trip_id", value).apply() }
    var routePolyline: String
        get() = prefs.getString("route_polyline", "") ?: ""
        set(value) { prefs.edit().putString("route_polyline", value).apply() }

    var destLat: Double
        get() = prefs.getString("dest_lat", null)?.toDoubleOrNull() ?: 0.0
        set(value) { prefs.edit().putString("dest_lat", value.toString()).apply() }

    var destLon: Double
        get() = prefs.getString("dest_lon", null)?.toDoubleOrNull() ?: 0.0
        set(value) { prefs.edit().putString("dest_lon", value.toString()).apply() }
    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER_ID).apply()
    }

    fun isLoggedIn(): Boolean = token.isNotEmpty()
}
object Retrofit {
    private const val BASE_URL = "https://51.170.45.131/"
    private val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
    })
    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, trustAllCerts, java.security.SecureRandom())
    }
    private val okHttpClient = OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${PersistanceManager.token}")
                .build()
            chain.proceed(request)
        }
        .build()
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }

}