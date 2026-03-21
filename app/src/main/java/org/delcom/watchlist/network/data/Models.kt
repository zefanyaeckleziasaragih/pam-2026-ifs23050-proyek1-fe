package org.delcom.watchlist.network.data

// ── Generic Response ──────────────────────────────────────────────────────────

data class ResponseMessage<T>(
    val status: String,
    val message: String,
    val data: T? = null
)

// ── Auth ──────────────────────────────────────────────────────────────────────

data class RequestAuthRegister(
    val name: String,
    val username: String,
    val password: String
)

data class RequestAuthLogin(
    val username: String,
    val password: String
)

data class RequestAuthLogout(
    val authToken: String
)

data class RequestAuthRefreshToken(
    val authToken: String,
    val refreshToken: String
)

data class ResponseAuthRegister(
    val userId: String
)

data class ResponseAuthLogin(
    val authToken: String,
    val refreshToken: String
)

// ── User ──────────────────────────────────────────────────────────────────────

data class ResponseUser(
    val user: ResponseUserData
)

data class ResponseUserData(
    val id: String,
    val name: String,
    val username: String,
    val about: String? = null,
    val createdAt: String,
    val updatedAt: String
)

data class RequestUserChange(
    val name: String,
    val username: String
)

data class RequestUserChangePassword(
    val newPassword: String,
    val password: String
)

data class RequestUserAbout(
    val about: String
)

// ── Movies (Watchlists) ───────────────────────────────────────────────────────

data class RequestMovie(
    val title: String,
    val description: String,
    val isDone: Boolean = false,
    val urgency: String = "medium"
)

data class ResponseMovies(
    val watchlists: List<ResponseMovieData>
)

data class ResponseMoviesPaginated(
    val watchlists: List<ResponseMovieData>,
    val pagination: ResponsePagination? = null
)

data class ResponsePagination(
    val currentPage: Int,
    val perPage: Int,
    val total: Long,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean
)

data class ResponseMovie(
    val watchlist: ResponseMovieData
)

data class ResponseMovieData(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val isDone: Boolean = false,
    val urgency: String? = null,
    val cover: String? = null,
    val createdAt: String = "",
    var updatedAt: String = ""
) {
    val watchStatus: WatchStatus get() = when (urgency?.lowercase()) {
        "low"  -> WatchStatus.WATCHING
        "high" -> WatchStatus.COMPLETED
        else   -> WatchStatus.PLANNED
    }

    val releaseYear: String? get() {
        return if (description.startsWith("[") && description.contains("]")) {
            val year = description.substringAfter("[").substringBefore("]")
            if (year.length == 4 && year.all { it.isDigit() }) year else null
        } else null
    }

    val cleanDescription: String get() {
        return if (releaseYear != null) {
            description.substringAfter("]").trim()
        } else description
    }
}

enum class WatchStatus(
    val label: String,
    val apiValue: String,
    val colorHex: Long,
    val bgColorHex: Long,
    val dotColorHex: Long
) {
    WATCHING(
        label       = "Sedang Ditonton",
        apiValue    = "low",
        colorHex    = 0xFF1565C0,
        bgColorHex  = 0xFFE3F2FD,
        dotColorHex = 0xFF1976D2
    ),
    PLANNED(
        label       = "Belum Ditonton",
        apiValue    = "medium",
        colorHex    = 0xFF6A1B9A,
        bgColorHex  = 0xFFF3E5F5,
        dotColorHex = 0xFF7B1FA2
    ),
    COMPLETED(
        label       = "Sudah Ditonton",
        apiValue    = "high",
        colorHex    = 0xFF1B5E20,
        bgColorHex  = 0xFFE8F5E9,
        dotColorHex = 0xFF2E7D32
    );

    companion object {
        fun fromApiValue(value: String?) = when (value?.lowercase()) {
            "low"  -> WATCHING
            "high" -> COMPLETED
            else   -> PLANNED
        }
    }
}

data class ResponseMovieAdd(
    val watchlistId: String
)

data class ResponseStats(
    val stats: ResponseStatsData
)

data class ResponseStatsData(
    val total: Long = 0,
    val done: Long = 0,
    val pending: Long = 0
)