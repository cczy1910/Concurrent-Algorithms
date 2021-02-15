package linked_list_set;

import kotlinx.atomicfu.*;

public class SetImpl implements Set {
    private class Node {
        AtomicRef<Node> next;
        int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    private class Removed extends Node {
        Removed(Node other) {
            super(other.x, other.next.getValue());
        }
    }

    private class Window {
        Node cur, next;
    }

    private final Node head = new Node(Integer.MIN_VALUE, new Node(Integer.MAX_VALUE, null));

    /**
     * Returns the {@link Window}, where cur.x < x <= next.x
     */
    private Window findWindow(int x) {
        Window w = new Window();
        while (true) {
            w.cur = head;
            w.next = w.cur.next.getValue();
            if (w.next instanceof Removed) {
                w.next = w.next.next.getValue();
            }
            boolean fail = false;
            while (w.next.x < x) {
                Node node = w.next.next.getValue();
                if (node instanceof Removed) {
                    if (!w.cur.next.compareAndSet(w.next, node.next.getValue())) {
                        fail = true;
                        break;
                    } else {
                        w.next = node.next.getValue();
                        continue;
                    }
                }
                w.cur = w.next;
                w.next = w.cur.next.getValue();
                if (w.next instanceof Removed) {
                    w.next = w.next.next.getValue();
                }
            }
            if (fail) {
                continue;
            }
            Node node = w.next.next.getValue();
            if (node instanceof Removed) {
                w.cur.next.compareAndSet(w.next, node.next.getValue());
            } else {
                return w;
            }
        }
    }

    @Override
    public boolean add(int x) {
        while (true) {
            Window w = findWindow(x);
            boolean res;
            if (w.next.x == x) {
                res = false;
            } else {
                if ((w.next instanceof Removed) || !w.cur.next.compareAndSet(w.next, new Node(x, w.next))) {
                    continue;
                }
                res = true;
            }
            return res;
        }
    }

    @Override
    public boolean remove(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.x != x) {
                return false;
            } else {
                Node node = w.next.next.getValue();
                if (node instanceof Removed) {
                    node = node.next.getValue();
                }
                if (w.next.next.compareAndSet(node, new Removed(w.next))) {
                    w.cur.next.compareAndSet(w.next, node);
                    return true;
                }
            }
        }
    }

    @Override
    public boolean contains(int x) {
        Window w = findWindow(x);
        boolean res = w.next.x == x;
        return res;
    }
}