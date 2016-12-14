package com.supermy.flume.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.flume.interceptor.TimestampInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TimestampBodyInterceptor implements Interceptor {

	private final Logger logger = LoggerFactory
			.getLogger(TimestampBodyInterceptor.class);
	private final static byte[] DEFAULT_SEPARATOR = "-".getBytes();
	private final byte[] separator;
	// Declaring these here to aid with efficiency later
	private byte[] timeBytes;
	private ByteArrayOutputStream outputStream;
	private int outputBodyLength;

	/**
	 * Only {@link TimestampInterceptor.Builder} can build me
	 */
	private TimestampBodyInterceptor(String separator) {
		this.separator = StringUtils.isEmpty(separator)
				? DEFAULT_SEPARATOR
				: separator.getBytes();
	}

	@Override
	public void initialize() {
	}

	/**
	 * Modifies events in-place.
	 */
	@Override
	public Event intercept(Event event) {
		byte[] eventBody = event.getBody();
		event.setBody(appendTimestampToBody(eventBody, System.nanoTime()));
		return event;
	}

	/**
	 * Concatenates the body, a separator and the timestamp.
	 *
	 * @param startEventBody
	 * @param time
	 * @return
	 */
	protected byte[] appendTimestampToBody(byte[] startEventBody, long time) {
		try {
			this.timeBytes = Long.toString(time).getBytes();
			this.outputBodyLength = startEventBody.length
					+ this.separator.length + this.timeBytes.length;
			this.outputStream = new ByteArrayOutputStream(
					this.outputBodyLength);
			this.outputStream.write(startEventBody);
			this.outputStream.write(this.separator);
			this.outputStream.write(this.timeBytes);
			return this.outputStream.toByteArray();
		} catch (IOException ex) {
			this.logger.error("Couldn't add timestamp to body", ex);
			throw new RuntimeException("Couldn't add timestamp to body", ex);
		}
	}

	/**
	 * Delegates to {@link #intercept(Event)} in a loop.
	 *
	 * @param events
	 * @return
	 */
	@Override
	public List<Event> intercept(List<Event> events) {
		for (Event event : events) {
			intercept(event);
		}
		return events;
	}

	@Override
	public void close() {
	}

	/**
	 * Builder which builds new instances of the TimestampBodyInterceptor.
	 */
	public static class Builder implements Interceptor.Builder {

		private String separator;

		@Override
		public Interceptor build() {
			return new TimestampBodyInterceptor(this.separator);
		}

		@Override
		public void configure(Context context) {
			this.separator = context.getString(Constants.SEPARATOR);
		}
	}

	public static class Constants {
		public static String SEPARATOR = "separator";
	}
}
