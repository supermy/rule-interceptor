package com.supermy.flume.interceptor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.flume.Event;
import org.apache.flume.event.SimpleEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TimestampBodyInterceptor.class)
public class TestTimestampBodyInterceptor {

	@Test
	public void testAppendTimestampToBody() {
		TimestampBodyInterceptor timestampBodyInterceptor = (TimestampBodyInterceptor) new TimestampBodyInterceptor.Builder()
				.build();
		byte[] inputMessage = "hello".getBytes();
		byte[] expectedOutput = "hello-10045".getBytes();
		byte[] actualOutput = timestampBodyInterceptor
				.appendTimestampToBody(inputMessage, 10045l);

		assertArrayEquals(expectedOutput, actualOutput);
	}

	@Test
	public void testTimestampBodyInterceptor() {
		TimestampBodyInterceptor timestampBodyInterceptor = (TimestampBodyInterceptor) new TimestampBodyInterceptor.Builder()
				.build();

		Event inputEvent = createEvent(false);
		Event expectedEvent = createEvent(true);

		PowerMockito.mockStatic(System.class);
		PowerMockito.when(System.nanoTime()).thenReturn(1001l);

		timestampBodyInterceptor.intercept(inputEvent);

		assertArrayEquals(expectedEvent.getBody(), inputEvent.getBody());
		assertEquals(expectedEvent.getHeaders(), inputEvent.getHeaders());
	}

	private Event createEvent(boolean isExpected) {
		Event event = new SimpleEvent();

		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("TABLE_NAME", "customer_details");
		event.setHeaders(headerMap);

		byte[] body;
		if (isExpected) {
			body = "hello-1001".getBytes();
		} else {
			body = "hello".getBytes();
		}
		event.setBody(body);
		return event;
	}
}