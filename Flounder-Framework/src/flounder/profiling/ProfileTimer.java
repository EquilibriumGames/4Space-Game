package flounder.profiling;

import flounder.logger.*;

/**
 * Can be used to record various timings within the framework.
 */
public class ProfileTimer {
	private int invocations;
	private float totalTime;
	private float startTime;

	/**
	 * Creates a new profiling timer.
	 */
	public ProfileTimer() {
		invocations = 0;
		totalTime = 0.0f;
		startTime = 0.0f;
	}

	/**
	 * Starts a new invocation.
	 */
	public void startInvocation() {
		startTime = System.nanoTime();
	}

	/**
	 * Stops the current Invocation.
	 */
	public void stopInvocation() {
		if (startTime == 0) {
			FlounderLogger.error("Stop Invocation called without matching start invocation!");
			assert (startTime != 0); // Stops from running faulty data.
		}

		invocations++;
		totalTime += System.nanoTime() - startTime;
		startTime = 0;
	}

	/**
	 * Gets the total time taken in ms, and resets the timer.
	 *
	 * @return Returns the total time taken in ms, and resets the timer.
	 */
	public float reset() {
		float timeMs = (float) ((totalTime / 1000000.0) / ((float) invocations));
		invocations = 0;
		totalTime = 0;
		startTime = 0;
		return timeMs;
	}
}
