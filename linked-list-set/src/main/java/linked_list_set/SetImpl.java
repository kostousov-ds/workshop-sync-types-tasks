package linked_list_set;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SetImpl implements Set {
    private class Node {
        Lock lock=new ReentrantLock();
	Node next;
	int x;

	Node(int x, Node next) {
	    this.next = next;
	    this.x = x;
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
	w.cur = head;
	w.cur.lock.lock();
	w.next = w.cur.next;
	w.next.lock.lock();

	while (w.next.x < x) {
	    w.cur.lock.unlock();
	    w.cur = w.next;
	    w.next = w.cur.next;
	    w.next.lock.lock();
	}
	return w;
    }

    @Override
    public boolean add(int x) {
	Window w = findWindow(x);
	try {
	    boolean res;
	    if (w.next.x == x) {
		res = false;
	    } else {
		w.cur.next = new Node(x, w.next);
		res = true;
	    }
	    return res;
	}finally {
	    w.cur.lock.unlock();
	    w.next.lock.unlock();
	}
    }

    @Override
    public boolean remove(int x) {
	Window w = findWindow(x);
	boolean res;
	try {
	    if (w.next.x != x) {
		res = false;
	    } else {
		w.cur.next = w.next.next;
		res = true;
	    }
	}finally {
	    w.cur.lock.unlock();
	    w.next.lock.unlock();
	}
	return res;
    }

    @Override
    public boolean contains(int x) {
	Window w = findWindow(x);
	try {
	    boolean res = w.next.x == x;
	    return res;
	}finally {
	    w.cur.lock.unlock();
	    w.next.lock.unlock();
	}
    }
}
