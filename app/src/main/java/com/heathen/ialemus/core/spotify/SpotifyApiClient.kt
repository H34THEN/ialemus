package com.heathen.ialemus.core.spotify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SpotifyApiClient {
    suspend fun exchangeAuthorizationCode(
        code: String,
        clientId: String,
        redirectUri: String,
        codeVerifier: String,
    ): Result<SpotifyTokenResponse> = withContext(Dispatchers.IO) {
        postForm(
            url = SpotifyDefaults.TOKEN_URL,
            fields = mapOf(
                "grant_type" to "authorization_code",
                "code" to code,
                "redirect_uri" to redirectUri,
                "client_id" to clientId,
                "code_verifier" to codeVerifier,
            ),
        ).mapCatching { body -> parseTokenResponse(body) }
    }

    suspend fun refreshAccessToken(
        refreshToken: String,
        clientId: String,
    ): Result<SpotifyTokenResponse> = withContext(Dispatchers.IO) {
        postForm(
            url = SpotifyDefaults.TOKEN_URL,
            fields = mapOf(
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken,
                "client_id" to clientId,
            ),
        ).mapCatching { body -> parseTokenResponse(body) }
    }

    suspend fun fetchProfile(accessToken: String): Result<SpotifyProfile> = withContext(Dispatchers.IO) {
        getJson("${SpotifyDefaults.API_BASE}/me", accessToken).mapCatching { json ->
            val images = json.optJSONArray("images")
            SpotifyProfile(
                id = json.optString("id", ""),
                displayName = json.optString("display_name", "Spotify User"),
                email = json.optString("email").takeIf { it.isNotBlank() },
                product = json.optString("product").takeIf { it.isNotBlank() },
                country = json.optString("country").takeIf { it.isNotBlank() },
                imageUrl = images?.firstObjectUrl(),
            )
        }
    }

    suspend fun fetchCurrentlyPlaying(accessToken: String): Result<SpotifyPlaybackResult> =
        withContext(Dispatchers.IO) {
            val connection = openGet("${SpotifyDefaults.API_BASE}/me/player/currently-playing", accessToken)
            try {
                when (connection.responseCode) {
                    HttpURLConnection.HTTP_NO_CONTENT -> Result.success(SpotifyPlaybackResult.None)
                    HttpURLConnection.HTTP_OK -> {
                        val body = connection.inputStream.bufferedReader().readText()
                        if (body.isBlank()) {
                            Result.success(SpotifyPlaybackResult.None)
                        } else {
                            Result.success(parseCurrentlyPlaying(JSONObject(body)))
                        }
                    }
                    HttpURLConnection.HTTP_UNAUTHORIZED ->
                        Result.success(SpotifyPlaybackResult.Unauthorized)
                    HttpURLConnection.HTTP_FORBIDDEN ->
                        Result.success(SpotifyPlaybackResult.Forbidden)
                    else -> Result.failure(
                        SpotifyApiException(connection.responseCode, "Currently playing request failed"),
                    )
                }
            } finally {
                connection.disconnect()
            }
        }

    suspend fun fetchPlayer(accessToken: String): Result<SpotifyPlaybackResult> = withContext(Dispatchers.IO) {
        val connection = openGet("${SpotifyDefaults.API_BASE}/me/player", accessToken)
        try {
            when (connection.responseCode) {
                HttpURLConnection.HTTP_NO_CONTENT -> Result.success(SpotifyPlaybackResult.None)
                HttpURLConnection.HTTP_OK -> {
                    val body = connection.inputStream.bufferedReader().readText()
                    Result.success(parsePlayer(JSONObject(body)))
                }
                HttpURLConnection.HTTP_NOT_FOUND ->
                    Result.success(SpotifyPlaybackResult.NoDevice)
                HttpURLConnection.HTTP_UNAUTHORIZED ->
                    Result.success(SpotifyPlaybackResult.Unauthorized)
                HttpURLConnection.HTTP_FORBIDDEN ->
                    Result.success(SpotifyPlaybackResult.Forbidden)
                else -> Result.failure(
                    SpotifyApiException(connection.responseCode, "Player request failed"),
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun play(accessToken: String): Result<Unit> = playerCommand(accessToken, "PUT", "/me/player/play")
    suspend fun pause(accessToken: String): Result<Unit> = playerCommand(accessToken, "PUT", "/me/player/pause")
    suspend fun next(accessToken: String): Result<Unit> = playerCommand(accessToken, "POST", "/me/player/next")
    suspend fun previous(accessToken: String): Result<Unit> =
        playerCommand(accessToken, "POST", "/me/player/previous")

    suspend fun setShuffle(accessToken: String, enabled: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        putJson(
            "${SpotifyDefaults.API_BASE}/me/player/shuffle?state=$enabled",
            accessToken,
            "{}",
        )
    }

    private suspend fun playerCommand(
        accessToken: String,
        method: String,
        path: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val connection = openConnection("${SpotifyDefaults.API_BASE}$path", method, accessToken)
        try {
            when (connection.responseCode) {
                HttpURLConnection.HTTP_NO_CONTENT,
                HttpURLConnection.HTTP_OK,
                -> Result.success(Unit)
                HttpURLConnection.HTTP_NOT_FOUND ->
                    Result.failure(SpotifyApiException(404, "No active Spotify device"))
                HttpURLConnection.HTTP_FORBIDDEN ->
                    Result.failure(SpotifyApiException(403, "Spotify Premium or active device required"))
                HttpURLConnection.HTTP_UNAUTHORIZED ->
                    Result.failure(SpotifyApiException(401, "Spotify session expired"))
                else -> Result.failure(
                    SpotifyApiException(connection.responseCode, "Spotify control failed"),
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseTokenResponse(body: String): SpotifyTokenResponse {
        val json = JSONObject(body)
        if (json.has("error")) {
            throw SpotifyApiException(0, json.optString("error_description", json.optString("error")))
        }
        return SpotifyTokenResponse(
            accessToken = json.getString("access_token"),
            refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
            expiresInSeconds = json.getInt("expires_in"),
            tokenType = json.optString("token_type", "Bearer"),
            scope = json.optString("scope", ""),
        )
    }

    private fun parseCurrentlyPlaying(json: JSONObject): SpotifyPlaybackResult {
        val item = json.optJSONObject("item") ?: return SpotifyPlaybackResult.None
        return SpotifyPlaybackResult.Active(parseTrackPlayback(item, json.optBoolean("is_playing", false), null))
    }

    private fun parsePlayer(json: JSONObject): SpotifyPlaybackResult {
        val item = json.optJSONObject("item") ?: return SpotifyPlaybackResult.None
        val device = json.optJSONObject("device")
        return SpotifyPlaybackResult.Active(
            parseTrackPlayback(
                item,
                json.optBoolean("is_playing", false),
                device?.optString("name"),
            ),
        )
    }

    private fun parseTrackPlayback(
        item: JSONObject,
        isPlaying: Boolean,
        deviceName: String?,
    ): SpotifyPlaybackState {
        val album = item.optJSONObject("album")
        return SpotifyPlaybackState(
            isPlaying = isPlaying,
            trackName = item.optString("name", "Unknown track"),
            artistName = item.optJSONArray("artists")?.joinNames().orEmpty().ifBlank { "Unknown artist" },
            albumName = album?.optString("name").orEmpty().ifBlank { "Unknown album" },
            albumArtUrl = album?.optJSONArray("images")?.firstObjectUrl(),
            deviceName = deviceName,
            progressMs = null,
            durationMs = item.optLong("duration_ms").takeIf { it > 0 },
        )
    }

    private fun postForm(url: String, fields: Map<String, String>): Result<String> {
        val body = fields.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        return try {
            OutputStreamWriter(connection.outputStream).use { it.write(body) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.let { BufferedReader(InputStreamReader(it)).readText() }.orEmpty()
            if (code in 200..299) {
                Result.success(response)
            } else {
                Result.failure(SpotifyApiException(code, parseErrorMessage(response)))
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun getJson(url: String, accessToken: String): Result<JSONObject> {
        val connection = openGet(url, accessToken)
        return try {
            val code = connection.responseCode
            if (code == HttpURLConnection.HTTP_OK) {
                val body = connection.inputStream.bufferedReader().readText()
                Result.success(JSONObject(body))
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.readText().orEmpty()
                Result.failure(SpotifyApiException(code, parseErrorMessage(errorBody)))
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun putJson(url: String, accessToken: String, jsonBody: String): Result<Unit> {
        val connection = openConnection(url, "PUT", accessToken)
        connection.setRequestProperty("Content-Type", "application/json")
        return try {
            OutputStreamWriter(connection.outputStream).use { it.write(jsonBody) }
            val code = connection.responseCode
            if (code in 200..299 || code == HttpURLConnection.HTTP_NO_CONTENT) {
                Result.success(Unit)
            } else {
                Result.failure(SpotifyApiException(code, "Request failed"))
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun openGet(url: String, accessToken: String): HttpURLConnection =
        openConnection(url, "GET", accessToken)

    private fun openConnection(url: String, method: String, accessToken: String): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        return connection
    }

    private fun parseErrorMessage(body: String): String {
        if (body.isBlank()) return "Spotify request failed"
        return runCatching {
            JSONObject(body).optJSONObject("error")?.optString("message")
                ?: JSONObject(body).optString("error_description")
                ?: body
        }.getOrDefault(body)
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun JSONArray.joinNames(): String = buildString {
        for (i in 0 until length()) {
            if (i > 0) append(", ")
            append(getJSONObject(i).optString("name"))
        }
    }

    private fun JSONArray.firstObjectUrl(): String? {
        if (length() == 0) return null
        return getJSONObject(0).optString("url").takeIf { it.isNotBlank() }
    }

    companion object {
        private const val TIMEOUT_MS = 15_000
    }
}

sealed class SpotifyPlaybackResult {
    data object None : SpotifyPlaybackResult()
    data object NoDevice : SpotifyPlaybackResult()
    data object Unauthorized : SpotifyPlaybackResult()
    data object Forbidden : SpotifyPlaybackResult()
    data class Active(val state: SpotifyPlaybackState) : SpotifyPlaybackResult()
}

class SpotifyApiException(val code: Int, message: String) : Exception(message)
