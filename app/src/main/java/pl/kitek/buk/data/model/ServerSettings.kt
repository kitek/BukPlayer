package pl.kitek.buk.data.model

data class ServerSettings(
    val url: String,
    val isBasicAuthEnabled: Boolean,
    val basicAuthUsername: String,
    val basicAuthPassword: String
)
