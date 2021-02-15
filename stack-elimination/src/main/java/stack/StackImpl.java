package stack;


import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class StackImpl implements Stack {
    private static class EliminationInteger {
        int value;

        EliminationInteger(int value) {
            this.value = value;
        }
    }

    private static class Node {
        final AtomicReference<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicReference<>(next);
            this.x = x;
        }

    }

    // head pointer
    private AtomicReference<Node> head = new AtomicReference<>(null);

    final AtomicReferenceArray<EliminationInteger> eliminationArray = new AtomicReferenceArray<>(32);

    final EliminationInteger done = new EliminationInteger(228);

    @Override
    public void push(int x) {
        int cell = new Random().nextInt(32);
        EliminationInteger eliminationX = new EliminationInteger(x);
        for (int i = 0; i < 4; i++, cell = (cell + 1) % 32) {
            if (eliminationArray.compareAndSet(cell, null, eliminationX)) {
                for (int pass = 0; pass < 50; pass++) {
                }
                eliminationArray.compareAndSet(cell, eliminationX, null);
                if (eliminationArray.compareAndSet(cell, done, null)) {
                    return;
                }
                break;
            }
        }
        while (true) {
            Node curHead = head.get();
            Node newHead = new Node(x, curHead);
            if (head.compareAndSet(curHead, newHead)) {
                return;
            }
        }
    }

    @Override
    public int pop() {
        int cell = new Random().nextInt(32);
        for (int i = 0; i < 4; i++, cell = (cell + 1) % 32) {
            EliminationInteger smt = eliminationArray.get(cell);
            if (smt != null && smt != done && eliminationArray.compareAndSet(cell, smt, done)) {
                return smt.value;
            }

        }
        while (true) {
            Node curHead = head.get();
            if (curHead == null) return Integer.MIN_VALUE;
            if (head.compareAndSet(curHead, curHead.next.get())) {
                return curHead.x;
            }
        }
    }

    public static void main(String[] args) {
        Stack stack = new StackImpl();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println(stack.pop());
        System.out.println(stack.pop());
        System.out.println(stack.pop());
    }
}
