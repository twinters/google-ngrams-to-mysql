package be.thomaswinters.googlengrams;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.beust.jcommander.JCommander;

import be.thomaswinters.googlengrams.arguments.ConstrainedLoaderArguments;

public class NgramConstrainedLoader extends NgramLoader {

	private Pattern wordPattern;

	private final int minYear;
	private final int maxYear;
	private final long minOccurrences;
	private final Function<List<String>, Boolean> constrainer;

	public NgramConstrainedLoader(NgramCsvReader reader, NgramMySQLConnector connector, int minYear, int maxYear,
			int minOccurrences, Function<List<String>, Boolean> constrainer, String allowedRegex) {
		super(reader, connector);
		this.minYear = minYear;
		this.maxYear = maxYear;
		this.minOccurrences = minOccurrences;
		this.constrainer = constrainer;

		this.wordPattern = Pattern.compile(allowedRegex);
	}

	public NgramConstrainedLoader(NgramCsvReader reader, NgramMySQLConnector connector, int minYear, int maxYear,
			int minOccurrences, String allowedRegex) {
		this(reader, connector, minYear, maxYear, minOccurrences, (e -> true), allowedRegex);
	}

	protected boolean matchesRegex(String word) {
		return wordPattern.matcher(word).matches();
	}

	protected boolean isAcceptedYear(int year) {
		return minYear <= year && year <= maxYear;
	}

	private List<String> lastChecked = new ArrayList<>();
	private boolean lastCheckedResult;

	public boolean shouldStore(List<String> words, int year) {
		if (lastChecked.equals(words)) {
			return lastCheckedResult;
		}
		boolean result = isAcceptedYear(year) && words.stream().allMatch(e -> matchesRegex(e))
				&& constrainer.apply(words);

		lastChecked = words;
		lastCheckedResult = result;

		return result;
	}

	public boolean shouldStoreCount(long count) {
		return count >= minOccurrences;
	}

	private List<String> last = new ArrayList<>();
	private long lastCount = 0;

	@Override
	protected void store(List<String> words, int year, long count) {
		if (shouldStore(words, year)) {
			if (!words.equals(last)) {
				if (!last.isEmpty() && shouldStoreCount(lastCount)) {
					super.store(last, 0, lastCount);
				}
				last = words;
				lastCount = count;
			} else {
				lastCount += count;
			}
		}
	}

	@Override
	protected void endStoring() {
		super.store(last, 0, lastCount);
		last = new ArrayList<>();
		lastCount = 0;
	}

	public static void main(String[] args)
			throws NumberFormatException, ClassNotFoundException, URISyntaxException, SQLException {

		ConstrainedLoaderArguments arguments = new ConstrainedLoaderArguments();
		JCommander.newBuilder().addObject(arguments).build().parse(args);

		System.out.println("START " + arguments.getN() + " grams");

		for (int i = arguments.getBegin(); i < arguments.getEnd(); i++) {
			System.out.println("Starting " + i);
			NgramConstrainedLoader loader = new NgramConstrainedLoader(
					new NgramCsvReader(new File(arguments.getFolder() + arguments.getFilePrefix() + i + ".csv")),
					new NgramMySQLConnector(arguments.getN(), arguments.getHost(), arguments.getPort(),
							arguments.getUsername(), arguments.getPassword(), arguments.getDatabaseName()),
					arguments.getMinYear(), arguments.getMaxYear(), arguments.getMinOccurrences(),
					arguments.getConstrainer(), arguments.getAllowedRegex());
			loader.execute();
		}
		System.out.println("Finished");

	}

}
