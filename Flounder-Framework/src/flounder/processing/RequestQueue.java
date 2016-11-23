package flounder.processing;

import java.util.*;

/**
 * Holds resource requests that are in queue.
 */
public class RequestQueue {
	private List<ResourceRequest> requestQueue;

	/**
	 * Creates a new request queue.
	 */
	public RequestQueue() {
		requestQueue = new ArrayList<>();
	}

	/**
	 * Adds a new resource request to queue.
	 *
	 * @param request The resource request to add.
	 */
	public synchronized void addRequest(ResourceRequest request) {
		requestQueue.add(request);
	}

	/**
	 * Gets the next item in queue and then removes it from this list.
	 *
	 * @return The next item in queue and then removes it from this list.
	 */
	public synchronized ResourceRequest acceptNextRequest() {
		return requestQueue.remove(0);
	}

	/**
	 * Gets if there are any items left in queue.
	 *
	 * @return Returns true if there are any items left in queue.
	 */
	public synchronized boolean hasRequests() {
		return !requestQueue.isEmpty();
	}

	/**
	 * Gets the number of objects in queue.
	 *
	 * @return The number of objects in queue.
	 */
	public synchronized int count() {
		return requestQueue.size();
	}

	/**
	 * Clears the request queue.
	 */
	public synchronized void clear() {
		requestQueue.clear();
	}
}
