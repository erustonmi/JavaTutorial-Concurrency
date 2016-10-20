package hello;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyQueueMulti<E> {
	@SuppressWarnings("unchecked")
	private E[] data = (E[]) new Object[10];
	private int count = 0;
	private ReentrantLock lock = new ReentrantLock();
	private Condition notFull = lock.newCondition();
	private Condition notEmpty = lock.newCondition();

	public void put(E e) throws InterruptedException {
		lock.lock();
		try {
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
		lock.lock();
		try {
			while (count <= 0) {
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

	public boolean isEmpty() {
		lock.lock(); try {
			return count == 0;
		} finally {
			lock.unlock(); // create synchronization edge for happens before relationship
		}
	}

	public static void main(String[] args) throws Throwable {
		final int NUMBERS_TO_SEND = 10_000;
		final int PRODUCER_COUNT = 5;
		final int CONSUMER_COUNT = 5;
		MyQueueMulti<Integer> rend = new MyQueueMulti<>();
		Runnable producer = () -> {
//			try {
//				rend.put(99);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} // break it to verify the test!
			for (int i = 0; i < NUMBERS_TO_SEND; i++) {
				try {
					rend.put(i);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
//			System.out.println("producer finished");
		};

		Thread[] producers = new Thread[PRODUCER_COUNT];
		for (int i = 0; i < producers.length; i++) {
			producers[i] = new Thread(producer);
			producers[i].start();
		}

		class MyConsumer implements Runnable {
			private int[] numbersSeen = new int[NUMBERS_TO_SEND];

			public void run() {
				boolean shutdown = false;
				while (!shutdown || !rend.isEmpty()) {
					try {
						numbersSeen[rend.get()]++;
					} catch (InterruptedException ie) {
						shutdown = true;
					}
//					if (Thread.currentThread().isInterrupted())
//						shutdown = true;
				}
//				System.out.println("consumer shutdown");
			}
		}
		MyConsumer[] consumers = new MyConsumer[CONSUMER_COUNT];
		Thread[] consumerThreads = new Thread[CONSUMER_COUNT];
		
		long start = System.nanoTime();
		for (int i = 0; i < CONSUMER_COUNT; i++) {
			consumers[i] = new MyConsumer();
			consumerThreads[i] = new Thread(consumers[i]);
			consumerThreads[i].start();
		}

		for (int i = 0; i < producers.length; i++) {
			producers[i].join();
		}
		long end = System.nanoTime();
		
		for (int i = 0; i < consumerThreads.length; i++) {
			consumerThreads[i].interrupt();
		}

		for (int i = 0; i < consumerThreads.length; i++) {
			consumerThreads[i].join();
		}

		for (int i = 1; i < consumers.length; i++) {
			for (int j = 0; j < NUMBERS_TO_SEND; j++) {
				consumers[0].numbersSeen[j] += consumers[i].numbersSeen[j];
			}
		}
		
		for (int i = 0; i < NUMBERS_TO_SEND; i++) {
			if (consumers[0].numbersSeen[i] != PRODUCER_COUNT) {
				System.out.println("!!! ERROR, number " + i
						+ " seen " + consumers[0].numbersSeen[i] + " times"
						+ " should be " + PRODUCER_COUNT);
			}
		}

		System.out.printf("Main finished. Approximate execution "
				+ "time was %12.6f ms\n", (end - start) / 1_000_000.0);
	}
}
