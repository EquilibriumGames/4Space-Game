package flounder.logger;

import flounder.framework.*;
import flounder.profiling.*;

import java.io.*;
import java.util.*;

/**
 * Various utility functions for debugging.
 */
public class FlounderLogger extends IModule {
	private final static FlounderLogger instance = new FlounderLogger();

	public static final boolean LOG_TO_CONSOLE = true;
	public static final boolean LOG_TO_FILE = true;

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	private StringBuilder saveData;
	private int linesPrinted;

	/**
	 * Creates a new logger manager.
	 */
	public FlounderLogger() {
		super(ModuleUpdate.ALWAYS, FlounderProfiler.class);
	}

	@Override
	public void init() {
		this.saveData = new StringBuilder();
		this.linesPrinted = 0;
	}

	@Override
	public void run() {
	}

	@Override
	public void profile() {
		FlounderProfiler.add("Logger", "Log To Console", LOG_TO_CONSOLE);
		FlounderProfiler.add("Logger", "Log To Files", LOG_TO_FILE);
		FlounderProfiler.add("Logger", "Lines Recorded", linesPrinted);
	}

	/**
	 * Logs registration info strings sent into a .log file, and if {@code LOG_TO_CONSOLE} is enabled it will also be logged to the IDE's console.
	 *
	 * @param value Text or numbers being added to the log file and possibly to the IDES console.
	 * @param <T> The object type to be logged.
	 */
	public static <T> void register(T value) {
		if (LOG_TO_CONSOLE) {
			if (getString(value).isEmpty()) {
				System.out.println();
			} else {
				System.out.println(ANSI_GREEN + "REGISTER [" + getDateString() + "]: " + ANSI_RESET + getString(value));
			}
		}

		if (LOG_TO_FILE) {
			if (getString(value).isEmpty()) {
				instance.saveData.append("\n");
			} else {
				instance.saveData.append("REGISTER [" + getDateString() + "]: " + getString(value).replaceAll("\u001B\\[[\\d;]*[^\\d;]", "") + "\n");
			}
		}

		instance.linesPrinted += getString(value).split("\n").length;
	}

	/**
	 * Logs strings sent into a .log file, and if {@code LOG_TO_CONSOLE} is enabled it will also be logged to the IDE's console.
	 *
	 * @param value Text or numbers being added to the log file and possibly to the IDES console.
	 * @param <T> The object type to be logged.
	 */
	public static <T> void log(T value) {
		if (LOG_TO_CONSOLE) {
			if (getString(value).isEmpty()) {
				System.out.println();
			} else {
				System.out.println(ANSI_YELLOW + "LOG [" + getDateString() + "]: " + ANSI_RESET + getString(value));
			}
		}

		if (LOG_TO_FILE) {
			if (getString(value).isEmpty()) {
				instance.saveData.append("\n");
			} else {
				instance.saveData.append("LOG [" + getDateString() + "]: " + getString(value).replaceAll("\u001B\\[[\\d;]*[^\\d;]", "") + "\n");
			}
		}

		instance.linesPrinted += getString(value).split("\n").length;
	}

	/**
	 * Warning logs strings sent into javas console, and if {@code LOG_TO_FILE} is enabled it will also be logged to a log file.
	 *
	 * @param value Warnings being added to the log file and possibly to your IDES console.
	 * @param <T> The object type to be logged.
	 */
	public static <T> void warning(T value) {
		if (LOG_TO_CONSOLE) {
			if (getString(value).isEmpty()) {
				System.out.println();
			} else {
				System.out.println(ANSI_PURPLE + "WARNING [" + getDateString() + "]: " + ANSI_RESET + getString(value));
			}
		}

		if (LOG_TO_FILE) {
			instance.saveData.append("WARNING [" + getDateString() + "]: " + getString(value).replaceAll("\u001B\\[[\\d;]*[^\\d;]", "") + "\n");
		}

		instance.linesPrinted += getString(value).split("\n").length;
	}

	/**
	 * Error logs strings sent into javas console, and if {@code LOG_TO_FILE} is enabled it will also be logged to a log file.
	 *
	 * @param value Errors being added to the log file and possibly to your IDES console.
	 * @param <T> The object type to be logged.
	 */
	public static <T> void error(T value) {
		if (LOG_TO_CONSOLE) {
			if (getString(value).isEmpty()) {
				System.out.println();
			} else {
				System.out.println(ANSI_RED + "ERROR [" + getDateString() + "]: " + ANSI_RESET + getString(value));
			}
		}


		if (LOG_TO_FILE) {
			instance.saveData.append("ERROR [" + getDateString() + "]: " + getString(value).replaceAll("\u001B\\[[\\d;]*[^\\d;]", "") + "\n");
		}

		instance.linesPrinted += getString(value).split("\n").length;
	}

	/**
	 * Exception logs strings sent into javas console, and if {@code LOG_TO_FILE} is enabled it will also be logged to a log file.
	 *
	 * @param exception The exception added to the log file and possibly to your IDES console.
	 */
	public static void exception(Exception exception) {
		if (LOG_TO_CONSOLE) {
			System.err.println(ANSI_PURPLE + "EXCEPTION [" + getDateString() + "]: " + ANSI_RESET + getString(exception));
			exception.printStackTrace();
		}

		if (LOG_TO_FILE) {
			instance.saveData.append("EXCEPTION [" + getDateString() + "]: " + getString(exception) + "\n");
		}

		instance.linesPrinted += getString(exception).split("\n").length;
	}

	/**
	 * Gets a string from a generic.
	 *
	 * @param value The value to get the string from.
	 * @param <T> The generic type.
	 *
	 * @return The string found.
	 */
	private static <T> String getString(T value) {
		if (value == null) {
			return "NULL";
		}

		return value.toString();
	}

	/**
	 * Gets the string of the current date.
	 *
	 * @return Returns the string of the current date as [hour:minute:second | day/month/year].
	 */
	private static String getDateString() {
		int hour = Calendar.getInstance().get(Calendar.HOUR);
		int minute = Calendar.getInstance().get(Calendar.MINUTE);
		int second = Calendar.getInstance().get(Calendar.SECOND) + 1;
		return hour + "." + minute + "." + second;
	}

	/**
	 * Finds / Generates the logs save folder.
	 *
	 * @return The path to the folder.
	 *
	 * @throws IOException Failed to create / find folder.
	 */
	private String getLogsSave() throws IOException {
		File saveDirectory = new File(FlounderFramework.getRoamingFolder().getPath(), "logs");

		if (!saveDirectory.exists()) {
			System.out.println("Creating directory: " + saveDirectory);

			try {
				saveDirectory.mkdir();
			} catch (SecurityException e) {
				error("Filed to create logging folder: " + saveDirectory.getAbsolutePath());
				exception(e);
			}
		}

		String result = saveDirectory + "/" + Calendar.getInstance().get(Calendar.HOUR) + "." + Calendar.getInstance().get(Calendar.MINUTE) + "." + (Calendar.getInstance().get(Calendar.SECOND) + 1) + "-" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "." + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "." + Calendar.getInstance().get(Calendar.YEAR) + ".log";
		File resultingFile = new File(result);

		if (!resultingFile.exists()) {
			resultingFile.createNewFile();
		}

		FileOutputStream outputFile = new FileOutputStream(resultingFile, false);
		outputFile.close();

		return result;
	}

	@Override
	public IModule getInstance() {
		return instance;
	}

	@Override
	public void dispose() {
		if (LOG_TO_FILE) {
			try (PrintWriter out = new PrintWriter(getLogsSave())) {
				for (String line : saveData.toString().split("\n")) {
					out.println(line);
				}
			} catch (IOException e) {
				System.err.println("Could not save logs!");
				e.printStackTrace();
			}
		}
	}
}
