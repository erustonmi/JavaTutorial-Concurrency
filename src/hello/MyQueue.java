package hello;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyQueue<E> {
	@SuppressWarnings("unchecked")
	private E[] data = (E[])new Object[10];
	private int count = 0;
	private ReentrantLock lock = new ReentrantLock();
	private Condition notFull = lock.newCondition();
	private Condition notEmpty = lock.newCondition();

	public void put(E e) throws InterruptedException {
		lock.lock(); try {
			while (count >= data.length) {
				notFull.await();
			}
			
			data[count++] = e;
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	public E get() throws InterruptedException {
		E rv;
		lock.lock(); try {
			while(count <= 0) {
				notEmpty.await();
			}
			rv = data[0];
			System.arraycopy(data, 1, data, 0, --count);
			notFull.signal();
		} finally {
			lock.unlock();
		}
		return rv;
	}
	
	public static void main(String [] args) {
		MyQueue<Integer> rend = new MyQueue<>();
		new Thread(() -> {
			for (int i = 0; i < 1_000; i++) {
				try {
					Thread.sleep((int)(Math.random() * 2));
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
					Thread.sleep((int)(Math.random() * 2));
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
