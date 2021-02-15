/**
 * @author :TODO: Zhukov Zakhar
 */
public class Solution implements AtomicCounter {
    final Node root;
    final ThreadLocal<Node> last;

    Solution() {
        root = new Node(0);
        last = new ThreadLocal<>();
    }

    public int getAndAdd(int x) {
        if (last.get() == null) {
            last.set(root);
        }
        while (true) {
            int old = last.get().value;
            Node node = new Node(old + x);
            last.set(last.get().next.decide(node));
            if (last.get() == node) {
                return old;
            }
        }
    }

    // вам наверняка потребуется дополнительный класс
    private static class Node {
        Node(int value) {
            this.value = value;
        }

        final int value;
        final Consensus<Node> next = new Consensus<>();
    }
}
