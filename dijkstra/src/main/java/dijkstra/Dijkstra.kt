package dijkstra

import java.util.*
import java.util.concurrent.Phaser
import java.util.concurrent.atomic.AtomicInteger
import kotlin.Comparator
import kotlin.concurrent.thread
import kotlin.random.Random

private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> Integer.compare(o1!!.distance, o2!!.distance) }

// Returns `Integer.MAX_VALUE` if a path has not been found.
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    // The distance to the start node is `0`
    start.distance = 0
    // Create a priority (by distance) queue and add the start node into it
    val mq = mutableListOf<PriorityQueue<Node>>()
    for (i in 0..workers * 2) {
        mq.add(PriorityQueue(NODE_DISTANCE_COMPARATOR))
    }
    mq[0].add(start)
    val activeNodes = AtomicInteger(1)
    // Run worker threads and wait until the total work is done
    val onFinish = Phaser(workers + 1) // `arrive()` should be invoked at the end by each worker
    repeat(workers) {
        thread {
            while (activeNodes.get() > 0) {
                // TODO Be careful, "empty queue" != "all nodes are processed".

                // TODO Write the required algorithm here,
                // TODO break from this loop when there is no more node to process.
                // TODO Be careful, "empty queue" != "all nodes are processed".
                val left = Random.nextInt(0, workers * 2 - 1)
                val right = Random.nextInt(1, workers * 2)
                if (right <= left) {
                    continue
                }

                var cur: Node
                var curNodeDistance: Int
                val leftNode: Node? = synchronized(mq[left]) { mq[left].poll() }
                val rightNode: Node? = synchronized(mq[right]) { mq[right].poll() }
                if (leftNode == null) {
                    if (rightNode == null) {
                        if (activeNodes.get() > 0) continue else break
                    }
                    curNodeDistance = rightNode.distance
                    cur = rightNode
                } else {
                    if (rightNode == null) {
                        cur = leftNode
                        curNodeDistance = leftNode.distance
                    } else {
                        val leftNodeDistance = leftNode.distance
                        val rightNodeDistance = rightNode.distance
                        if (leftNodeDistance < rightNodeDistance) {
                            cur = leftNode
                            curNodeDistance = leftNodeDistance
                            synchronized(mq[right]) {
                                mq[right].add(rightNode)
                            }
                        } else {
                            cur = rightNode
                            curNodeDistance = rightNodeDistance
                            synchronized(mq[left]) {
                                mq[left].add(leftNode)
                            }
                        }
                    }
                }
                for (e in cur.outgoingEdges) {
                    while (true) {
                        val curDistance = e.to.distance
                        if (curDistance > curNodeDistance + e.weight) {
                            if (e.to.casDistance(curDistance, curNodeDistance + e.weight)) {
                                val t = Random.nextInt(workers * 2)
                                synchronized(mq[t]) {
                                    mq[t].add(e.to)
                                }
                                activeNodes.incrementAndGet()
                                break
                            } else {
                                continue
                            }
                        } else {
                            break
                        }
                    }
                }
                activeNodes.decrementAndGet()
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}