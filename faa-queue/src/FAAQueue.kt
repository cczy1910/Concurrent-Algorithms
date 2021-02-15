import kotlinx.atomicfu.*

class FAAQueue<T> {
    private val head: AtomicRef<Segment> // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private val tail: AtomicRef<Segment> // Tail pointer, similarly to the Michael-Scott queue

    init {
        val firstNode = Segment()
        head = atomic(firstNode)
        tail = atomic(firstNode)
    }

    /**
     * Adds the specified element [x] to the queue.
     */
    fun enqueue(x: T) {
        while (true) {
            val tail = this.tail.value
            val enqIdx = tail.enqIdx.getAndIncrement()
            if (enqIdx >= SEGMENT_SIZE) {
                if (tail.next.value != null){
                    this.tail.compareAndSet(tail, tail.next.value!!)
                    continue
                }
                val newTail = Segment(x)
                if (tail.next.compareAndSet(null, newTail)) {
                    this.tail.compareAndSet(tail, newTail)
                } else {
                    continue
                }
                return
            } else if (tail.elements[enqIdx].compareAndSet(null, x)) {
                return
            }
        }
    }

    /**
     * Retrieves the first element from the queue
     * and returns it; returns `null` if the queue
     * is empty.
     */
    fun dequeue(): T? {
        while (true) {
            val head = this.head.value
            val deqIdx = head.deqIdx.getAndIncrement()
            if (deqIdx >= SEGMENT_SIZE) {
                val headNext = head.next.value ?: return null
                this.head.compareAndSet(head, headNext)
                continue
            }
            val res = head.elements[deqIdx].getAndSet(DONE) ?: continue
            return res as T?
        }
    }

    /**
     * Returns `true` if this queue is empty;
     * `false` otherwise.
     */
    val isEmpty: Boolean
        get() {
            while (true) {
                val head = this.head.value
                val deqIdx = head.deqIdx.value
                val enqIdx = head.enqIdx.value
                if (deqIdx >= SEGMENT_SIZE) {
                    val headNext = head.next.value ?: return true
                    this.head.compareAndSet(head, headNext)
                    continue
                } else {
                    return deqIdx >= enqIdx
                }
            }
        }
}

private class Segment {
    val next: AtomicRef<Segment?> = atomic(null)
    val deqIdx = atomic(0) // index for the next dequeue operation
    val elements = atomicArrayOfNulls<Any>(SEGMENT_SIZE)
    val enqIdx: AtomicInt

    constructor() {
        enqIdx = atomic(0) // index for the next enqueue operation
    } // for the first segment creation

    constructor(x: Any?) { // each next new segment should be constructed with an element
        enqIdx = atomic(1)
        elements[0].value = x
    }

//    val isEmpty: Boolean
//        get() {
//            val deqIdx = deqIdx.value
//            val enqIdx = enqIdx.value
//            return deqIdx >= enqIdx || deqIdx >= SEGMENT_SIZE
//        }

}

private val DONE = Any() // Marker for the "DONE" slot state; to avoid memory leaks
const val SEGMENT_SIZE = 2 // DO NOT CHANGE, IMPORTANT FOR TESTS

