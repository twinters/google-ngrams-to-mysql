package be.thomaswinters.googlengrams;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class NgramLoader {

	private final static String acceptedWords = "^[a-zA-Z\\-]+$";
	private final static Pattern wordPattern = Pattern.compile(acceptedWords);
	private final static int YEAR = 2008;
	private final static int MINIMAL_OCCURRENCES = 40;

	private final NgramCsvReader reader;
	private final NgramMySQLConnector connector;

	public NgramLoader(NgramCsvReader reader, NgramMySQLConnector connector) {
		this.reader = reader;
		this.connector = connector;
	}

	public NgramLoader() throws NumberFormatException, ClassNotFoundException, URISyntaxException, SQLException {
		this(new NgramCsvReader(System.getenv("ngram_csv_file")),
				new NgramMySQLConnector(System.getenv("ngram_db_host"),
						Integer.parseInt(System.getenv("ngram_db_port")), System.getenv("ngram_db_username"),
						System.getenv("ngram_db_password"), System.getenv("ngram_db_databaseName")));
	}

	public void execute() {
		reader.convert(e -> f -> g -> store(e, f, g));

	}

	private void store(String word, int year, int count) {
		if (shouldStore(word, year, count)) {
			connector.addCount(word, count);
		}
	}

	public boolean isAlphabetical(String word) {
		return wordPattern.matcher(word).matches();
	}

	public boolean isAcceptedYear(int year) {
		return year == YEAR;
	}

	public boolean hasEnoughCounts(int count) {
		return count >= MINIMAL_OCCURRENCES;
	}

	public boolean shouldStore(String word, int year, int count) {
		return isAlphabetical(word) && isAcceptedYear(year) && hasEnoughCounts(count);
	}

	public static void main(String[] args)
			throws NumberFormatException, ClassNotFoundException, URISyntaxException, SQLException {
		NgramLoader loader = new NgramLoader();
		loader.execute();
	}
}
