package msqueue;

import java.util.concurrent.atomic.AtomicReference;

public class MSQueue implements Queue {
    private AtomicReference<Node> head;
    private AtomicReference<Node> tail;

    public MSQueue() {
        Node dummy = new Node(0);
        this.head = new AtomicReference<>(dummy);
        this.tail = new AtomicReference<>(dummy);
    }

    @Override
    public void enqueue(int x) {
        Node newTail = new Node(x);
        while (true) {
            Node curTail = tail.get();
            if (curTail.next.compareAndSet(null, newTail)) {
                tail.compareAndSet(curTail, newTail);
                return;
            } else {
                tail.compareAndSet(curTail, curTail.next.get());
            }
        }
    }

    @Override
    public int dequeue() {
        while (true) {
            Node curHead = head.get();
            Node nextHead = curHead.next.get();
            if (nextHead != null) {
                tail.compareAndSet(curHead, nextHead);
                if (head.compareAndSet(curHead, nextHead)) {
                    return nextHead.x;
                }
            } else {
                return Integer.MIN_VALUE;
            }
        }
    }

    @Override
    public int peek() {
        Node curHead = head.get();
        Node nextHead = curHead.next.get();
        if (nextHead != null) {
            tail.compareAndSet(curHead, nextHead);
            return nextHead.x;
        } else {
            return Integer.MIN_VALUE;
        }
    }

    private class Node {
        final int x;
        AtomicReference<Node> next = new AtomicReference<>(null);

        Node(int x) {
            this.x = x;
        }
    }
}