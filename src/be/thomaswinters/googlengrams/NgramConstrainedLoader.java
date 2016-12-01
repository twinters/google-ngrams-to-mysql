package be.thomaswinters.googlengrams;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NgramConstrainedLoader extends NgramLoader {

	private final static String acceptedWords = "^[a-zA-Z\\-]+$";
	private final static Pattern wordPattern = Pattern.compile(acceptedWords);
	private final int minYear;
	private final int maxYear;
	private final long minOccurrences;

	public NgramConstrainedLoader(NgramCsvReader reader, NgramMySQLConnector connector, int minYear, int maxYear,
			int minOccurrences) {
		super(reader, connector);
		this.minYear = minYear;
		this.maxYear = maxYear;
		this.minOccurrences = minOccurrences;
	}

	public boolean isAlphabetical(String word) {
		return wordPattern.matcher(word).matches();
	}

	public boolean isAcceptedYear(int year) {
		return minYear <= year && year <= maxYear;
	}

	public boolean shouldStore(List<String> words, int year) {
		return words.stream().allMatch(e -> isAlphabetical(e) && e.length() > 0) && isAcceptedYear(year);
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
				if (!last.isEmpty() && shouldStoreCount(count)) {
					super.store(last, 0, lastCount);
				}
				last = words;
				lastCount = count;
			} else {
				lastCount += count;
			}
		}
	}

	// private void endStoring() {
	// connector.addCount(last, lastCount);
	// last = new ArrayList<>();
	// lastCount = 0;
	// }

	public static void main(String[] args)
			throws NumberFormatException, ClassNotFoundException, URISyntaxException, SQLException {

		System.out.println("START 1 grams");
		int n = 1;
		int amountOfFiles = 10;

		for (int i = 0; i < amountOfFiles; i++) {
			System.out.println("Starting " + i);
			NgramLoader loader = new NgramConstrainedLoader(
					new NgramCsvReader("C:\\Users\\Thomas\\Desktop\\" + n + "gram\\googlebooks-eng-1M-" + n
							+ "gram-20090715-" + i + ".csv"),
					new NgramMySQLConnector(n, "localhost", 3306, "ngram", "ngram", "ngram"), 1970, 2008, 1);
			loader.execute();
		}
		System.out.println("Finished");

	}

}
