@file:Suppress("FunctionName")

package lessons

import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

private val scope = CoroutineScope(EmptyCoroutineContext)

fun main(args: Array<String>) {
    slide3_2()
    while (true) {
    } // main thread works surrogate
}

/**
 * Любой корутине нужен scope.
 * Билдер корутины launch является extension функцией scope.
 */
fun slide2_1() {
    scope.launch {
        println("text")
    }
}

/**
 * Корутины внутри scope запускаются паралельно.
 */
fun slide2_2() {
    println("function started")
    scope.launch {
        println("Coroutine 1 started")
        delay(1.seconds)
        println("Coroutine 1 ended")
    }
    println("function middle")
    scope.launch {
        println("Coroutine 2 started")
        delay(1.5.seconds)
        println("Coroutine 2 ended")
    }
    println("function ended")
}

/**
 * Отслеживание статуса корутины / Использование isActive
 */
fun slide3_1() {
    println("function started")
    val job = scope.launch {
        println("coroutine started")
        var i = 0
        while (i < 5) {
            TimeUnit.MILLISECONDS.sleep(1000)
            println("coroutine in ${i++} step")
        }
        println("coroutine ended")
    }
    //scope.launch { slide3CancelJob(job) }
    println("function ended")
}

suspend fun slide3CancelJob(job: Job) {
    delay(2.5.seconds)
    println("on cancel")
    job.cancel()
}

/**
 * Тоже самое с suspend-функцией.
 * delay-cancellable suspend function.
 */
fun slide3_2() {
    println("function started")
    val job = scope.launch {
        println("coroutine started")
        var i = 0
        while (i < 5) {
            delay(1.seconds)
            println("coroutine in ${i++} step isActive $isActive")
        }
        println("coroutine ended")
    }
    scope.launch { slide3CancelJob(job) }
    println("function ended")
}