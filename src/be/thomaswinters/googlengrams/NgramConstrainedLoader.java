package be.thomaswinters.googlengrams;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

public class NgramConstrainedLoader extends NgramLoader {

	private final static String acceptedWords = "^[a-zA-Z\\-]+$";
	private final static Pattern wordPattern = Pattern.compile(acceptedWords);
	private final int minYear;
	private final int maxYear;
	private final int minOccurrences;

	public NgramConstrainedLoader(List<NgramCsvReader> readers, NgramMySQLConnector connector, int minYear,
			int maxYear, int minOccurrences) {
		super(readers, connector);
		this.minYear = minYear;
		this.maxYear = maxYear;
		this.minOccurrences = minOccurrences;
	}

	public NgramConstrainedLoader(NgramCsvReader reader, NgramMySQLConnector connector, int minYear, int maxYear,
			int minOccurrences) {
		this(Arrays.asList(reader), connector, minYear, maxYear, minOccurrences);
	}

	public boolean isAlphabetical(String word) {
		return wordPattern.matcher(word).matches();
	}

	public boolean isAcceptedYear(int year) {
		return minYear <= year && year <= maxYear;
	}

	public boolean hasEnoughCounts(int count) {
		return count >= minOccurrences;
	}

	@Override
	public boolean shouldStore(List<String> words, int year, int count) {
		return words.stream().allMatch(e -> isAlphabetical(e) && e.length() > 0) && isAcceptedYear(year)
				&& hasEnoughCounts(count);
	}

	public static void main(String[] args)
			throws NumberFormatException, ClassNotFoundException, URISyntaxException, SQLException {
		List<NgramCsvReader> readers = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			readers.add(new NgramCsvReader(System.getenv("ngram_csv_file_prefix") + i + ".csv"));
		}

		NgramLoader loader = new NgramConstrainedLoader(readers,
				new NgramMySQLConnector(2, System.getenv("ngram_db_host"),
						Integer.parseInt(System.getenv("ngram_db_port")), System.getenv("ngram_db_username"),
						System.getenv("ngram_db_password"), System.getenv("ngram_db_databaseName")),
				2000, 2008, 1);
		loader.execute();
	}
}
