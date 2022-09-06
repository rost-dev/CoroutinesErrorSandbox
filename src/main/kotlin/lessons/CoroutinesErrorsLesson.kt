@file:Suppress("FunctionName")

package lessons

import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    slide10_1()
    while (true) {
    }
}

private val handler = CoroutineExceptionHandler { _, throwable ->
    println("We caught ${throwable.message}")
}
private val scope = CoroutineScope(Job() + Dispatchers.IO)
private val differentScope = CoroutineScope(Job())
private val superScope = CoroutineScope(SupervisorJob())

/**
 * Ошибка внутри одной корутины. Try-catch.
 * Работа launch корутин-билдера:
 * 1) Формирование CoroutineContext(Continuation + Job).
 * 2) Отправка Continuation Dispatcher-у.
 */
fun slide4() {
    scope.launch {
        error()
    }
}

/**
 * Слайд 4 без корутины
 */
fun slide4ThreadAnalogue() {
    thread {
        error()
    }
}

/**
 * Coroutine exception handler.
 */
fun slide5_1() {
    scope.launch {
        error()
    }
}

/**
 * Отмена родительской корутиной дочерних в случае ошибки.
 */
fun slide5_2() {
    scope.launch {
        delay(2.seconds)
        error()
    }
    scope.launch {
        repeat(5) {
            TimeUnit.MILLISECONDS.sleep(999)
            //delay(0.99.seconds)
            println("Message for mister $it, isActive = $isActive")
        }
    }
}

/**
 * Разные скоупы никак не связаны между собой
 */
fun slide5_3() {
    scope.launch {
        error()
    }
    differentScope.launch {
        repeat(5) {
            delay(0.99.seconds)
            println("Message for mister $it, isActive = $isActive")
        }
    }
}

/**
 * SupervisorJob решает все вышестоящие проблемы.
 */
fun slide5_4() {
    superScope.launch {
        delay(2.seconds)
        error()
    }
    superScope.launch {
        repeat(5) {
            //TimeUnit.MILLISECONDS.sleep(999)
            delay(0.99.seconds)
            println("Message for mister $it, isActive = $isActive")
        }
    }
}

/**
 * Ошибки во вложенных корутинах.
 */
fun slide6_1() {
    scope.launch(CoroutineName("coroutine1")) {
        launch(CoroutineName("coroutine1_1")) {
            delay(1.seconds)
            println("Error is here")
            error()
        }
        launch(CoroutineName("coroutine1_2")) {
            repeatWhenActive()
        }
        repeatWhenActive()
    }
    scope.launch(CoroutineName("coroutine2")) {

        launch(CoroutineName("coroutine2_1")) { repeatWhenActive() }

        launch(CoroutineName("coroutine2_2")) { repeatWhenActive() }

        repeatWhenActive()
    }
}

/**
 * Целесообразность применения хэндлера в цепочке корутин.
 */
fun slide6_2() {
    scope.launch(CoroutineName("coroutine1")) {
        launch(CoroutineName("coroutine1_1")) {
            launch(CoroutineName("coroutine1_1_1")) {
                launch(CoroutineName("coroutine1_1_1_1")) {
                    error()
                }
            }
        }
    }
}

/**
 * SupervisorJob и множество вложенных корутины.
 */
fun slide6_3() {
    superScope.launch(CoroutineName("coroutine1")) {
        launch(CoroutineName("coroutine1_1")) {
            delay(1.seconds)
            println("Error is here")
            error()
        }
        launch(CoroutineName("coroutine1_2")) {
            repeatWhenActive()
        }
        repeatWhenActive()
    }
    superScope.launch(CoroutineName("coroutine2")) {

        launch(CoroutineName("coroutine2_1")) { repeatWhenActive() }

        launch(CoroutineName("coroutine2_2")) { repeatWhenActive() }

        repeatWhenActive()
    }
}

fun CoroutineScope.repeatWhenActive() {
    repeat(5) {
        TimeUnit.MILLISECONDS.sleep(500)
        println("${coroutineContext[CoroutineName]?.name} isActive = $isActive")
    }
}

fun slide6_3Pseudo() {
    scope.launch {/* parent coroutine */
        launch(SupervisorJob()) { /* child coroutine */ }
    }

    // Power Treads
    scope.launch { /* parent coroutine */
        launch(SupervisorJob(coroutineContext[Job])) { /* child coroutine */ }
    }
}

/**
 * Ограничить распространение ошибки с помощью coroutineScope.
 * coroutine1 > ScopeCoroutine > coroutine1_2
 */
fun slide9_1() {
    scope.launch(CoroutineName("coroutine1")) {
        coroutineScope {
            launch(CoroutineName("coroutine1_1")) {}
            launch(CoroutineName("coroutine1_2")) { error() }
        }
        launch(CoroutineName("coroutine1_3")) { println(coroutineContext[CoroutineName]?.name + " do some work") }
        launch(CoroutineName("coroutine1_4")) { println(coroutineContext[CoroutineName]?.name + " do some work") }
    }
}

suspend fun coroutineScopeDemo() {
    var result = 0
    val n = 100 // number of coroutines
    val k = 1000 // number of repeats for each coroutine
    val firstPart = coroutineScope {
        repeat(n) {
            launch {
                repeat(k) { result++ }
            }
        }
        "Completed ${n * k} actions"
    }
    println("$firstPart but result is $result")
}

/**
 * coroutineScope with result
 */
fun slide9_2() {
    scope.launch {
        coroutineScopeDemo()
    }
}

/**
 * supervisorScope
 * coroutine1 > SupervisorCoroutine > coroutine1_2
 */
fun slide9_3() {
    scope.launch(CoroutineName("coroutine1")) {
        supervisorScope {
            launch(CoroutineName("coroutine1_1")) {}
            launch(CoroutineName("coroutine1_2")) { error() }
        }
        launch(CoroutineName("coroutine1_3")) { println(coroutineContext[CoroutineName]?.name + " do some work") }
        launch(CoroutineName("coroutine1_4")) { println(coroutineContext[CoroutineName]?.name + " do some work") }
    }
}

/**
 * async builder
 * executing code after await
 */
fun slide10() {
    scope.launch {
        val deffered = async {
            error()
            "some important result"
        }
        deffered.await()
        println("We're closed resources")
    }
}

/**
 * async builder
 * supervisorJob
 * обязательно оборачивать await в try-catch
 * launchJob -> supervisorJob -> asyncJob
 */
fun slide10_1() {
    scope.launch {
        val deffered = async(SupervisorJob(coroutineContext[Job])) {
            error()
            "some important result"
        }
        deffered.await()
        println("We're closed resources")
    }
}

fun error(): Nothing = throw IllegalStateException("Big bad error")