import kotlinx.atomicfu.*
import java.lang.IllegalArgumentException

class DynamicArrayImpl<E> : DynamicArray<E> {
    private val core = atomic(Core<E>(INITIAL_CAPACITY))
    private val _size = atomic(0)

    override fun get(index: Int): E {
        if (index >= _size.value) {
            throw IllegalArgumentException()
        }
        while (true) {
            val curCore = core.value;
            if (index >= curCore.capacity) {
                continue
            }
            val curContainer = curCore.array[index].value ?: continue
            return curContainer.elem.value ?: continue
        }
    }

    override fun put(index: Int, element: E) {
        if (index >= _size.value) {
            throw IllegalArgumentException()
        }
        while (true) {
            val curCore = core.value;
            if (index >= curCore.capacity) {
                continue
            }
            val curContainer = curCore.array[index].value ?: continue
            val oldValue = curContainer.elem.value ?: continue
            if (curContainer.elem.compareAndSet(oldValue, element)) {
                if (curCore.array[index].value == curContainer) {
                    return
                }
            }
        }
    }

    override fun pushBack(element: E) {
        val pos = _size.getAndIncrement()
        while (true) {
            val curCore = core.value
            if (pos >= curCore.capacity) {
                extend(curCore)
            } else {
                if (curCore.array[pos].compareAndSet(null, Container(atomic(element)))) {
                    break
                }
            }
        }
    }

    private fun extend(curCore: Core<E>) {
        val newCore = Core<E>(2 * curCore.capacity)
        if (curCore.next.compareAndSet(null, newCore)) {
            for (i in 0 until curCore.capacity) {
                while (true) {
                    val valToMove = curCore.array[i].getAndSet(null)
                    if (valToMove != null) {
                        newCore.array[i].compareAndSet(null, valToMove)
                        break
                    }
                }
            }
            core.compareAndSet(curCore, newCore)
        }
    }

    override val size: Int
        get() {
            return _size.value
        }
}

private class Core<E>(val capacity: Int) {
    val array = atomicArrayOfNulls<Container<E>>(capacity)
    val next: AtomicRef<Core<E>?> = atomic(null);
}

private class Container<T>(
    val elem: AtomicRef<T?>,
)

private const val INITIAL_CAPACITY = 1 // DO NOT CHANGE ME