import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SynchronousQueueMS<E> : SynchronousQueue<E> {
    open class Node {
        val next: AtomicRef<Node?> = atomic(null)
    }

    class Reciever<E>(
        val run: Continuation<E>
    ) : Node()

    class Sender<E>(
        val run: Continuation<Unit>,
        val arg: E
    ) : Node()

    private val RETRY = "RETRY"
    private val dummy = Node()
    private val tail = atomic(dummy)
    private val head = atomic(dummy)

    override suspend fun send(element: E) {
        while (true) {
            val curTail = tail.value
            val curHead = head.value
            if (curTail != curHead && curTail is Reciever<*>) {
                val nextHead = curHead.next.value
                if (nextHead != null && nextHead is Reciever<*>) {
                    tail.compareAndSet(curHead, nextHead)
                    if (head.compareAndSet(curHead, nextHead)) {
                        (nextHead.run as Continuation<E>).resume(element)
                        return
                    }
                }
            } else {
                val res = suspendCoroutine<Any> sc@{ cont ->
                    val newTail = Sender(cont, element)
                    val curTail = tail.value
                    if (curTail != head.value && curTail is Reciever<*> ||
                        !curTail.next.compareAndSet(null, newTail)
                    ) {
                        if (curTail.next.value != null) {
                            tail.compareAndSet(curTail, curTail.next.value!!)
                        }
                        cont.resume(RETRY)
                        return@sc
                    }
                    tail.compareAndSet(curTail, newTail)
                }
                if (res != RETRY) return
            }
        }
    }

    override suspend fun receive(): E {
        while (true) {
            val curTail = tail.value
            val curHead = head.value
            if (curTail != curHead && curTail is Sender<*>) {
                val nextHead = curHead.next.value
                if (nextHead != null && nextHead is Sender<*>) {
                    tail.compareAndSet(curHead, nextHead)
                    if (head.compareAndSet(curHead, nextHead)) {
                        nextHead.run.resume(Unit)
                        return (nextHead.arg as E)
                    }
                }
            } else {
                val res = suspendCoroutine<E?> sc@{ cont ->
                    val newTail = Reciever(cont)
                    val curTail = tail.value
                    if (curTail != head.value && curTail is Sender<*> ||
                        !curTail.next.compareAndSet(null, newTail)
                    ) {
                        if (curTail.next.value != null) {
                            tail.compareAndSet(curTail, curTail.next.value!!)
                        }
                        cont.resume(null)
                        return@sc
                    } else {
                        tail.compareAndSet(curTail, newTail)
                    }
                }
                if (res != null) return res
            }
        }
    }
}
