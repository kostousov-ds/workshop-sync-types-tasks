package linked_list_set;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SetImpl implements Set {
    private class Node {
	Lock lock = new ReentrantLock();
	volatile Node next;
	int x;

	Node(int x, Node next) {
	    this.next = next;
	    this.x = x;
	}
    }

    private class Window {
	Node cur, next;

	void lock() {
	    cur.lock.lock();
	    next.lock.lock();
	}

	void unlock() {
	    cur.lock.unlock();
	    next.lock.unlock();
	}
    }

    private final Node head = new Node(Integer.MIN_VALUE, new Node(Integer.MAX_VALUE, null));

    /**
     * Returns the {@link Window}, where cur.x < x <= next.x
     */
    private Window findWindow(int x) {
	Window w = new Window();
	w.cur = head;
	w.next = w.cur.next;

	while (w.next.x < x) {
	    w.cur = w.next;
	    w.next = w.cur.next;
	}
	return w;
    }

    @Override
    public boolean add(int x) {
	while (true) {
	    boolean res = false;
	    try {
		return addImpl(x);
	    } catch (Exception ignored) {
	    }
	}
    }

    private boolean addImpl(int x) throws Exception {
	Window w = findWindow(x);
	w.lock();
	try {
	    if (validate(w, x)) {
		boolean res;
		if (w.next.x == x) {
		    res = false;
		} else {
		    w.cur.next = new Node(x, w.next);
		    res = true;
		}
		return res;
	    } else {
		throw new Exception("next try");
	    }
	} finally {
	    w.unlock();
	}
    }

    private boolean validate(Window w, int x) {
	Window ww = findWindow(x);
	return ww.cur == w.cur && ww.next == w.next && ww.cur.next == ww.next;
    }

    @Override
    public boolean remove(int x) {
	while (true) {
	    try {
		return removeImpl(x);
	    } catch (Exception ignored) {
	    }
	}
    }

    private boolean removeImpl(int x) throws Exception {
	Window w = findWindow(x);
	boolean res;
	try {
	    w.lock();
	    if (!validate(w, x)) {
		throw new Exception("retry");
	    }
	    if (w.next.x != x) {
		res = false;
	    } else {
		w.cur.next = w.next.next;
		res = true;
	    }
	} finally {
	    w.unlock();
	}
	return res;
    }

    @Override
    public boolean contains(int x) {
	while (true) {
	    try {
		return containsImpl(x);
	    } catch (Exception ignored) {
	    }
	}
    }

    private boolean containsImpl(int x) throws Exception {
	Window w = findWindow(x);
	try {
	    w.lock();
	    if (!validate(w, x)) {
		throw new Exception("retry");
	    }
	    boolean res = w.next.x == x;
	    return res;
	} finally {
	    w.unlock();
	}
    }
}
