import java.util.concurrent.atomic.*


class Solution(val env: Environment) : Lock<Solution.Node> {
    val tail: AtomicReference<Node?> = AtomicReference(null)

    override fun lock(): Node {
        val my = Node() // сделали узел
        val pred = tail.getAndSet(my)
        if (pred != null) {
            pred.next.set(my)
            while (my.locked.get()) {
                env.park()
            }
        }
        return my // вернули узел
    }

    override fun unlock(node: Node) {
        if (node.next.get() == null) {
            if (tail.compareAndSet(node, null)) {
                return
            } else {
                while (node.next.get() == null) {
                    //pass
                }
            }
        }
        node.next.get()!!.locked.set(false)
        env.unpark(node.next.get()!!.thread)
    }

    class Node {
        val thread = Thread.currentThread() // запоминаем поток, которые создал узел
        val locked: AtomicReference<Boolean> = AtomicReference(true)
        val next: AtomicReference<Node?> = AtomicReference(null)
        // todo: необходимые поля (val, используем AtomicReference)
    }
}