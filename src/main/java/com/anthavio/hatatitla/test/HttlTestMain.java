package com.anthavio.hatatitla.test;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import com.anthavio.hatatitla.HttpClient3Sender;
import com.anthavio.hatatitla.HttpSender;
import com.anthavio.hatatitla.PutRequest;
import com.anthavio.hatatitla.SenderRequest;
import com.anthavio.hatatitla.inout.ResponseBodyExtractor.ExtractedBodyResponse;
import com.anthavio.jetty.JettyWrapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class HttlTestMain {

	static TestObject bean;
	static {
		bean = new TestObject();
		bean.setDate(new Date());
		bean.setNumber(333);
		bean.setString("Hurray!");
	}

	public static void main(String[] args) {
		JettyWrapper jetty = new JettyWrapper("src/main/jetty8", 0);
		jetty.start();

		try {
			//System.setProperty("keep.alive", "true");
			int iterations = 10000;
			int threads = 10;
			int port = jetty.getPort();

			//long millis = httl(iterations, threads, port);
			long millis = jersey(iterations, threads, port);
			System.out.println(millis + " millis");
		} catch (Exception x) {
			x.printStackTrace();
		} finally {
			jetty.stop();
		}
	}

	private static long httl(int iterations, int threads, int port) throws InterruptedException {
		//HttpURLSender sender = new HttpURLSender("localhost:" + port);
		//HttpClient4Sender sender = new HttpClient4Sender("localhost:" + port);
		HttpClient3Sender sender = new HttpClient3Sender("localhost:" + port);
		AtomicInteger counter = doSender(sender, 10, 1); //little warmup
		synchronized (counter) {
			counter.wait();
		}
		long start = System.currentTimeMillis();
		counter = doSender(sender, iterations, threads);
		synchronized (counter) {
			counter.wait();
		}
		long millis = System.currentTimeMillis() - start;
		sender.close();
		return millis;
	}

	private static long jersey(int iterations, int threads, int port) throws InterruptedException {
		//ClientConfig clientConfig = new DefaultClientConfig();
		//clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		//Client client = Client.create(clientConfig);
		Client client = Client.create();
		//NonBlockingClient client = NonBlockingClient.create();
		//ApacheHttpClient4 client = ApacheHttpClient4.create();

		WebResource webResource = client.resource("http://localhost:" + port + "/rest/");
		AtomicInteger counter = doResource(webResource, 10, 1);//little warmup
		synchronized (counter) {
			counter.wait();
		}
		long start = System.currentTimeMillis();
		counter = doResource(webResource, iterations, threads);
		synchronized (counter) {
			counter.wait();
		}
		long millis = System.currentTimeMillis() - start;
		client.destroy();
		return millis;
	}

	private static AtomicInteger doResource(final WebResource webResource, int iterations, int threadCount) {
		final AtomicInteger counter = new AtomicInteger(iterations);
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread() {

				@Override
				public void run() {
					while (counter.getAndDecrement() > 0) {
						Builder builder = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
						doEntity(builder, 1);
					}
					synchronized (counter) {
						System.out.println("exit " + getName());
						counter.notify();
					}
				}
			};
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].setDaemon(true);
			threads[i].setName("http-" + i);
			System.out.println("start " + threads[i].getName());
			threads[i].start();
		}
		return counter;
	}

	private static void doEntity(Builder builder, int iterations) {
		for (int i = 0; i < iterations; i++) {
			ClientResponse response = builder.put(ClientResponse.class, bean);
			TestObject object = response.getEntity(TestObject.class);
			//System.out.println(object);
		}
	}

	private static AtomicInteger doSender(final HttpSender sender, int iterations, int threadCount) {
		final AtomicInteger counter = new AtomicInteger(iterations);
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread() {

				@Override
				public void run() {
					while (counter.getAndDecrement() > 0) {
						PutRequest request = sender.PUT("/rest/").body(bean, MediaType.APPLICATION_JSON).build();
						doRequest(sender, request, 1);
					}
					synchronized (counter) {
						System.out.println("exit " + getName());
						counter.notify();
					}
				}
			};
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].setDaemon(true);
			threads[i].setName("http-" + i);
			System.out.println("start " + threads[i].getName());
			threads[i].start();
		}
		return counter;
	}

	private static void doRequest(HttpSender sender, SenderRequest request, int iterations) {
		for (int i = 0; i < iterations; i++) {
			ExtractedBodyResponse<TestObject> extract = sender.extract(request, TestObject.class);
			//System.out.println(extract.getBody());
		}
	}

	@XmlRootElement
	public static class TestObject implements Serializable {

		private String string;

		private Date date;

		private int number;

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		@Override
		public String toString() {
			return "TestObject [string=" + string + ", date=" + date + ", number=" + number + "]";
		}
	}
}
