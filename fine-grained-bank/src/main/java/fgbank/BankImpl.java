package fgbank;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 * <p>
 * <p>:TODO: This implementation has to be made thread-safe.
 */
public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;

    /**
     * Creates new bank instance.
     *
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(int n) {
	accounts = new Account[n];
	for (int i = 0; i < n; i++) {
	    accounts[i] = new Account();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAccounts() {
	return accounts.length;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getAmount(int index) {
	final Account account = accounts[index];
	long ret = 0;
	account.lock.lock();
	try {
	    ret = account.amount;
	} finally {
	    account.lock.unlock();
	}
	return ret;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getTotalAmount() {
	long sum = 0;

	try {
	    for (int i = 0; i < accounts.length; i++) {
		accounts[i].lock.lock();
	    }
	    for (Account account : accounts) {
		sum += account.amount;
	    }
	} finally {
	    for (Account account : accounts) {
		account.lock.unlock();
	    }
	}
	return sum;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long deposit(int index, long amount) {
	if (amount <= 0) {
	    throw new IllegalArgumentException("Invalid amount: " + amount);
	}
	long ret = 0;
	Account account = accounts[index];
	account.lock.lock();
	try{
	    if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT) {
		throw new IllegalStateException("Overflow");
	    }
	    account.amount += amount;
	    ret = account.amount;
	}   finally {
	    account.lock.unlock();
	}
	return ret;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long withdraw(int index, long amount) {
	if (amount <= 0) {
	    throw new IllegalArgumentException("Invalid amount: " + amount);
	}
	Account account = accounts[index];
	long ret = 0;
	account.lock.lock();
	try{
	    if (account.amount - amount < 0) {
		throw new IllegalStateException("Underflow");
	    }
	    account.amount -= amount;
	    ret = account.amount;
	}   finally {
	    account.lock.unlock();
	}
	return ret;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
	if (amount <= 0) {
	    throw new IllegalArgumentException("Invalid amount: " + amount);
	}
	if (fromIndex == toIndex) {
	    throw new IllegalArgumentException("fromIndex == toIndex");
	}
	Account from = accounts[fromIndex];
	Account to = accounts[toIndex];

	Account first = toIndex < fromIndex ? to : from;
	Account second = fromIndex > toIndex ? from : to;

	first.lock.lock();
	try {
	    second.lock.lock();
	    try {
		if (amount > from.amount) {
		    throw new IllegalStateException("Underflow");
		} else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT) {
		    throw new IllegalStateException("Overflow");
		}
		from.amount -= amount;
		to.amount += amount;
	    } finally {
		second.lock.unlock();
	    }
	} finally {
	    first.lock.unlock();
	}
    }

    /**
     * Private account data structure.
     */
    private static class Account {
	/**
	 * Amount of funds in this account.
	 */
	Lock lock = new ReentrantLock();
	long amount;
    }
}
