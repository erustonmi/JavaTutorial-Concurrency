package hello;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

public class MyCallable {
	static class MyJob implements Callable<String> {
		private static int nextId = 0;
		private int myId = ++nextId;

		@Override
		public String call() throws Exception {
			System.out.println("Callable " + myId + " starting...");
			try {
				Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
			} catch (InterruptedException ie) {
				System.out.println("Callable " + myId + " shut down on request...");
				return "";
			}
			System.out.println("Callable " + myId + " finishing...");
			return "Callable " + myId + " finished";
		}

	}

	public static void main(String[] args) {
		ExecutorService es = Executors.newFixedThreadPool(2);
		Deque<Future<String>> handles = new LinkedList<>();
		for (int i = 0; i < 6; i++) {
			handles.add(es.submit(new MyJob()));
		}
		System.out.println("All jobs submitted...");
		try {
			Thread.sleep(400);
			handles.getFirst().cancel(true);
			System.out.println("attempted to clean-shutdown job 1");
		} catch (InterruptedException ie) {

		}
		while (handles.size() > 0) {
			Future<String> handle = handles.removeFirst();
			try {
				String result = handle.get();
				System.out.println("Job returned: " + result);
			} catch (CancellationException e) {
				System.out.println("Job was canceled...");
			} catch (InterruptedException e) {
				System.out.println("get was interrupted");
			} catch (ExecutionException e) {
				System.out.println("execution exeption--cause was: " 
						+ e.getCause().getMessage());
			}
		}
		es.shutdown();
		System.out.println("shutdown!");
	}
}
