package hello;

class MyJob implements Runnable {
	private static int nextId = 0;
	private int myId = nextId++;
	private int i = 0;
	
	public void run() {
		for (; i < 10_000; i++) {
			System.out.println(Thread.currentThread().getName()
					+ ": i is " + i);
		}
	}
}

public class SimpleThread {

	public static void main(String[] args) {
		Runnable r = new MyJob();
		Thread t1 = new Thread(r);
		t1.start();
		Thread t2 = new Thread(r);
		t2.start();
		
		System.out.println("Main exits...");
	}

}
