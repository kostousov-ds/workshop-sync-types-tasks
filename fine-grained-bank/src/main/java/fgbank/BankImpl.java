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
//	account.lock.tryLock(10, TimeUnit.SECONDS)
	synchronized (account) {
	    ret = account.amount;
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
	for (Account account : accounts) {
	    synchronized (account) {
		sum += account.amount;
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
	synchronized (account) {
	    if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT) {
		throw new IllegalStateException("Overflow");
	    }
	    account.amount += amount;
	    ret = account.amount;
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
	synchronized (account) {
	    if (account.amount - amount < 0) {
		throw new IllegalStateException("Underflow");
	    }
	    account.amount -= amount;
	    ret = account.amount;
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

	Account first = fromIndex > toIndex ? to : from;
	Account second = fromIndex >= toIndex ? from : to;

	synchronized (first){
	    synchronized (second) {
		if (amount > from.amount) {
		    throw new IllegalStateException("Underflow");
		} else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT) {
		    throw new IllegalStateException("Overflow");
		}
		from.amount -= amount;
		to.amount += amount;
	    }
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
