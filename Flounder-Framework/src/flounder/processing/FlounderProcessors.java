package flounder.processing;

import flounder.framework.*;
import flounder.logger.*;
import flounder.processing.glProcessing.*;
import flounder.profiling.*;

/**
 * Manages the all framework request processors.
 */
public class FlounderProcessors extends IModule {
	private static final FlounderProcessors instance = new FlounderProcessors();

	private RequestProcessor requestProcessor;
	private GlRequestProcessor glRequestProcessor;

	/**
	 * Creates all framework request processors.
	 */
	public FlounderProcessors() {
		super(ModuleUpdate.UPDATE_PRE, FlounderLogger.class, FlounderProfiler.class);
	}

	@Override
	public void init() {
		this.requestProcessor = new RequestProcessor();
		this.glRequestProcessor = new GlRequestProcessor();
		requestProcessor.init();
		glRequestProcessor.init();
	}

	@Override
	public void run() {
		requestProcessor.update();
		glRequestProcessor.update();
	}

	@Override
	public void profile() {
		requestProcessor.profile();
		glRequestProcessor.profile();
	}

	/**
	 * Sends a new resource request to queue.
	 *
	 * @param request The resource request to add.
	 */
	public static void sendRequest(ResourceRequest request) {
		instance.requestProcessor.addRequestToQueue(request);
	}

	/**
	 * Sends a new request into queue.
	 *
	 * @param request The request to add.
	 */
	public static void sendGLRequest(GlRequest request) {
		instance.glRequestProcessor.addRequestToQueue(request);
	}

	@Override
	public IModule getInstance() {
		return instance;
	}

	@Override
	public void dispose() {
		requestProcessor.dispose();
		glRequestProcessor.dispose();
	}
}
