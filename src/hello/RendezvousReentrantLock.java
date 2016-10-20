package hello;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RendezvousReentrantLock<E> {
	private E data;
	private boolean dataReady = false;
	private ReentrantLock lock = new ReentrantLock();
	private Condition notFull = lock.newCondition();
	private Condition notEmpty = lock.newCondition();

	public void put(E e) throws InterruptedException {
		lock.lock(); try {
			while (dataReady) {
				notFull.await();
			}
			
			data = e;
			dataReady = true;
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	public E get() throws InterruptedException {
		E rv;
		lock.lock(); try {
			while(!dataReady) {
				notEmpty.await();
			}
			rv = data;
			dataReady = false;
			notFull.signal();
		} finally {
			lock.unlock();
		}
		return rv;
	}
	
	public static void main(String [] args) {
		RendezvousReentrantLock<Integer> rend = new RendezvousReentrantLock<>();
		new Thread(() -> {
			for (int i = 0; i < 1_000; i++) {
				try {
					rend.put(i);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			System.out.println("producer finished");
		}).start();
		new Thread(() -> {
			for (int i = 0; i < 1_000; i++) {
				try {
					if (i != rend.get()) System.err.println("BROKEN!!!!");
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			System.out.println("consumer finished");
		}).start();
		System.out.println("Main finished");
	}
}
