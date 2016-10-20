package hello;

public class Rendezvous<E> {
	private E data;
	private boolean dataReady = false;

	public void put(E e) throws InterruptedException {
		synchronized (this) {
			while (dataReady) {
				this.wait();
			}
			
			data = e;
			dataReady = true;
			this.notify();
		}
	}

	public E get() throws InterruptedException {
		E rv;
		synchronized(this) {
			while(!dataReady) {
				this.wait();
			}
			rv = data;
			dataReady = false;
			this.notify();
		}
		return rv;
	}
	
	public static void main(String [] args) {
		Rendezvous<Integer> rend = new Rendezvous<>();
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
