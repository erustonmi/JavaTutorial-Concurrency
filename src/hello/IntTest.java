package hello;

public class IntTest {
	public static void main(String[] args) throws Throwable {
		Thread t1 = new Thread(() -> {
			double d = 0;
			for (int i = 0; i < 100_000_000; i++) {
				d += Math.random();
			}
			System.out.println("about to sleep");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				System.out.println("interrupted");
			}
		});
		t1.start();
		Thread.sleep(10);
		t1.interrupt();
	}
}
