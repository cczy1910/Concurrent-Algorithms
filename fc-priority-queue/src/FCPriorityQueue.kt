import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls
import java.util.*
import kotlin.random.Random

class FCPriorityQueue<E : Comparable<E>> {
    private val q = PriorityQueue<E>()
    private val lock: AtomicBoolean = atomic(false)
    private val buf = atomicArrayOfNulls<Wrap<E>>(16)

    class Wrap<E>(val add: Boolean, val arg: E?, val poll: Boolean, val peek: Boolean) {
        val done = atomic(false)
        val res: AtomicRef<E?> = atomic(null);
    }

    fun combine() {
        for (i in 0..15) {
//            continue
            val wrap =   buf[i].value ?: continue
            if (!wrap.done.value) {
                if (wrap.add) {
                    q.add(wrap.arg!!)
                }
                if (wrap.peek) {
                    wrap.res.value = q.peek()
                }
                if (wrap.poll) {
                    wrap.res.value = q.poll()
                }
                wrap.done.value = true
            }
        }
    }

    /**
     * Retrieves the element with the highest priority
     * and returns it as the result of this function;
     * returns `null` if the queue is empty.
     */
    fun poll(): E? {
        val myWrap: Wrap<E> = Wrap(false, null, true, false)
        while (true) {
            val pos = Random.nextInt(16)
            if (buf[pos].compareAndSet(null, myWrap)) {
                while (true) {
                    if (lock.compareAndSet(false, true)) {
                        combine()
                        lock.value = false
                    }
                    if (myWrap.done.value) {
                        val res = myWrap.res.value
                        buf[pos].value = null
                        return res
                    }
                }
            }
        }
    }

    /**
     * Returns the element with the highest priority
     * or `null` if the queue is empty.
     */
    fun peek(): E? {
        val myWrap: Wrap<E> = Wrap(false, null, false, true)
        while (true) {
            val pos = Random.nextInt(16)
            if (buf[pos].compareAndSet(null, myWrap)) {
                while (true) {
                    if (lock.compareAndSet(false, true)) {
                        combine()
                        lock.value = false
                    }
                    if (myWrap.done.value) {
                        val res = myWrap.res.value
                        buf[pos].value = null
                        return res
                    }
                }
            }
        }
    }

    /**
     * Adds the specified element to the queue.
     */
    fun add(element: E) {
        val myWrap: Wrap<E> = Wrap(true, element, false, false)
        while (true) {
            val pos = Random.nextInt(16)
            if (buf[pos].compareAndSet(null, myWrap)) {
                while (true) {
                    if (lock.compareAndSet(false, true)) {
                        combine()
                        lock.value = false
                    }
                    if (myWrap.done.value) {
                        buf[pos].value = null
                        return
                    }
                }
            }
        }
    }
}