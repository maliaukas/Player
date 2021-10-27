package maliauka.sasha.player.utils

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?) = Resource(Status.SUCCESS, data, null)
        fun <T> error(message: String, data: T?) = Resource(Status.ERROR, data, message)
        fun <T> loading(data: T?) = Resource(Status.LOADING, data, null)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

// https://medium.com/androiddevelopers/sealed-with-a-class-a906f28ab7b5
// todo: replace Resource with Result
// sealed class Result<out T : Any> {
//    data class Success<out T : Any>(val data: T) : Result<T>()
//    data class Error<out T : Any>(val message: String) : Result<Nothing>()
//    object InProgress : Result<Nothing>()
//}
