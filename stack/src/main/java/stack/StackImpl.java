package stack;

import java.util.EmptyStackException;
import java.util.concurrent.atomic.AtomicReference;

public class StackImpl implements Stack {
    private class Node {
	Node next;
	int x;

	Node(int x, Node next) {
	    this.next = next;
	    this.x = x;
	}
    }

    private final Node NIL = new Node(0, null);
    private AtomicReference<Node> head = new AtomicReference<>(NIL);

    @Override
    public void push(int x) {
	while (true) {
	    Node curHead = head.get();
	    Node node = new Node(x, curHead);
	    if (head.compareAndSet(curHead, node)) {
		return;
	    }
	}
    }

    @Override
    public int pop() {
	while (true) {
	    Node curHead = head.get();
	    if (curHead == NIL) {
		throw new EmptyStackException();
	    }
	    if (head.compareAndSet(curHead, curHead.next)) {
		return curHead.x;
	    }
	}
    }
}
