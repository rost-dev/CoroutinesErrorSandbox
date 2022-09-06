package lessons

import kotlinx.coroutines.*
import kotlin.concurrent.thread

val cache = ContinuationCache()
val contScope = CoroutineScope(Job())

fun main(args: Array<String>) {
    contScope.launch {
        slide11()
    }
    while (true) {
    }
}

/**
 * suspend cancelable part
 * few words
 */
suspend fun slide11() = suspendCancellableCoroutine { continuation ->
    val result = cache.file
    if (result != null) {
        continuation.resume(result) {}
    } else {
        getNetworkResult()
    }
}

fun getNetworkResult() {
    thread {
        cache.file = "network result"
    }.start()
}

class ContinuationCache {
    var file: String? = null
}