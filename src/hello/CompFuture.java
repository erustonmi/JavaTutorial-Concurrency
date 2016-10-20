package hello;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class CompFuture {

	public static void delay() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
		}
	}
	
	public static CompletableFuture<String> doBlockingStuff(String input) {
		CompletableFuture<String> cfs = new CompletableFuture<>();
		new Thread(()->{
			delay();
			if (ThreadLocalRandom.current().nextBoolean()) {
				cfs.completeExceptionally(
						new RuntimeException("Ouch disk system failure" + input));
			}
			cfs.complete("This came from a disk! " + input);
		}).start();
		
		return cfs;
	}

	public static void main(String[] args) throws Throwable {
//		CompletableFuture<String> cfs = CompletableFuture.supplyAsync(() -> {
//			delay();
//			return "Beginning";
//		});
//		System.out.println("Stage One configured");
//		cfs = cfs.thenApplyAsync(x -> {
//			delay();
//			return "Ran stage two on " + x;
//		});
//		System.out.println("Stage Two configured");
//		cfs = cfs.whenComplete((v, t) -> {
//			if (v != null) {
//				System.out.println("Completed with " + v);
//			} else {
//				System.out.println("Failed, exception is " + t.getMessage());
//			}
//		});
//		System.out.println("Stage Three configured");
//		String result = cfs.get();
//		System.out.println("Result produced is " + result);
		
//		System.out.println("Result produced is " 
//		+ CompletableFuture.supplyAsync(() -> {
//			delay();
//			return "Beginning";
//		}).thenApplyAsync(x -> {
//			delay();
//			return "Ran stage two on " + x;
//		}).whenComplete((v, t) -> {
//			if (v != null) {
//				System.out.println("Completed with " + v);
//			} else {
//				System.out.println("Failed, exception is " + t.getMessage());
//			}
//		}).get());

		System.out.println("Result produced is " 
		+ CompletableFuture.supplyAsync(() -> "Beginning")
		.thenApplyAsync(x -> {
			if (ThreadLocalRandom.current().nextBoolean()) {
				return x + " [success] ";
			} else {
				throw new RuntimeException("Broke processing " + x);
			}
		})
		.exceptionally(x -> "I recovered the problem: " + x.getMessage())
		.thenApplyAsync(x -> "Ran stage two on " + x)
		.thenCompose(CompFuture::doBlockingStuff)
		.whenComplete((v, t) -> {
			if (v != null) {
				System.out.println("Completed with " + v);
			} else {
				System.out.println("Failed, exception is " + t.getMessage());
			}
		}).get());

	}
}
