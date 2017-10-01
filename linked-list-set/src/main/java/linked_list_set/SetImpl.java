package linked_list_set;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SetImpl implements Set {
    private class Node {
	Lock lock = new ReentrantLock();
	AtomicMarkableReference<Node> N;
	int x;

	Node(int x, Node next) {
	    // флаг означает удаленность ТЕКУЩЕГО элемента, не следующего!!!
	    this.N = new AtomicMarkableReference<>(next, false);
	    this.x = x;
	}
    }

    private class Window {
	public Window() {
	}

	public Window(Node cur, Node next) {
	    this.cur = cur;
	    this.next = next;
	}

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
     * Returns the {@link Window}, where cur.x < x <= N.x
     */
    private Window findWindow(int x) {
	boolean[] removed = new boolean[1];
	retry:
	while (true) {
	    Node cur = head;
	    Node next = cur.N.get(removed);
	    Node nextnext;

	    while (next.x < x) {
		nextnext = next.N.get(removed);
		if (removed[0]) {
		    // первый флаг - что cur не удален
		    if (!cur.N.compareAndSet(next, nextnext, false, false)) {
			continue retry;
		    }
		    next = nextnext;
		} else {
		    cur = next;
		    next = nextnext;
/*
		    nextnext = next.N.get(removed);
		    if (removed[0]) {
			if (!cur.N.compareAndSet(next, nextnext, false, false)) {
			    continue retry;
			}
			next = nextnext;
		    }
*/
		}
	    }
	    nextnext = next.N.get(removed);
	    if (removed[0]) {
		if (!cur.N.compareAndSet(next, nextnext, false, false)) {
		    continue;
		}
		next = nextnext;
	    }
	    return new Window(cur, next);
	}
    }

    @Override
    public boolean add(int x) {
	while (true) {
	    Window w = findWindow(x);
//	    if (!validate(w, x)) {
//		continue;
//	    }

	    boolean res;
	    if (w.next.x == x) {
		res = false;
	    } else {
		final Node node = new Node(x, w.next);
		if (!w.cur.N.compareAndSet(w.next, node, false, false)) {
		    continue;
		}
		res = true;
	    }
	    return res;
	}
    }

//    private boolean validate(Window w, int x) {
//	return !w.cur.removed && !w.next.removed && w.cur.N == w.next;
//    }

    @Override
    public boolean remove(int x) {
	while (true) {
	    Window w = findWindow(x);
	    boolean res;
	    if (w.next.x != x) {
		res = false;
	    } else {
		boolean[] removed = new boolean[1];
		final Node nextnext = w.next.N.get(removed);
		if (removed[0]) {
		    return false;
		}
		// помечаем next как удаленный
		if (!w.next.N.compareAndSet(nextnext, nextnext, false, true)) {
		    continue;
		}

		// одна попытка переставить
		w.cur.N.compareAndSet(w.next, nextnext, false, false);
		res = true;
	    }
	    return res;
	}
    }

    @Override
    public boolean contains(int x) {
	Window w = findWindow(x);
	return w.next.x == x;
    }
}
