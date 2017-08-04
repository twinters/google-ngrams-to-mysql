package be.thomaswinters.googlengrams;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import be.thomaswinters.pos.WordTypeCalculator;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class NgramConstrainedLoader extends NgramLoader {

	private final static String ALL_WORDS = "^[a-zA-Z\\-]+$";
	private final static String LOWERCASE_WORDS = "^[a-z\\-]+$";
	private Pattern wordPattern;

	private final int minYear;
	private final int maxYear;
	private final long minOccurrences;
	private final Function<List<String>, Boolean> constrainer;

	public NgramConstrainedLoader(NgramCsvReader reader, NgramMySQLConnector connector, int minYear, int maxYear,
			int minOccurrences, Function<List<String>, Boolean> constrainer, boolean onlyAllowLowercase) {
		super(reader, connector);
		this.minYear = minYear;
		this.maxYear = maxYear;
		this.minOccurrences = minOccurrences;
		this.constrainer = constrainer;
		
		if (onlyAllowLowercase) {
			wordPattern = Pattern.compile(LOWERCASE_WORDS);
		} else {
			wordPattern =  Pattern.compile(ALL_WORDS);			
		}
	}

	public NgramConstrainedLoader(NgramCsvReader reader, NgramMySQLConnector connector, int minYear, int maxYear,
			int minOccurrences, boolean onlyAllowLowercase) {
		this(reader, connector, minYear, maxYear, minOccurrences, (e -> true), onlyAllowLowercase);
	}

	public boolean isAlphabetical(String word) {
		return wordPattern.matcher(word).matches();
	}

	public boolean isAcceptedYear(int year) {
		return minYear <= year && year <= maxYear;
	}

	private List<String> lastChecked = new ArrayList<>();
	private boolean lastCheckedResult;
	
	public boolean shouldStore(List<String> words) {
		if (lastChecked.equals(words)) {
			return lastCheckedResult;
		}
		boolean result =
		// isAcceptedYear(year) &&
		words.stream().allMatch(e -> isAlphabetical(e)) && constrainer.apply(words);
		
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
		if (
				shouldStore(words)) {
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

	private static final Dictionary dictionary = new Dictionary(new File("./dict/"));
	static {
		try {
			dictionary.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static final MaxentTagger tagger = new MaxentTagger("stanford-pos/english-bidirectional-distsim.tagger");
	private static final WordTypeCalculator wordTypeCalculator = new WordTypeCalculator(dictionary, tagger);
	
	
	
	public static boolean isWordOfType(POS partOfSpeech, String word) {
		return wordTypeCalculator.getWordTypes(word).contains(partOfSpeech);
//		IIndexWord idxWord = dictionary.getIndexWord(word, partOfSpeech);
//		if (idxWord == null || idxWord.getWordIDs().isEmpty()) {
//			return false;
//		}
//		return idxWord.getWordIDs().size() > 0;
	}

	public static boolean isAdjectiveNounCombination(List<String> words) {
		String word1 = words.get(0);
		String word2 = words.get(1);

		return isWordOfType(POS.ADJECTIVE, word1) && isWordOfType(POS.NOUN, word2);
	}

	public static void main(String[] args)
			throws NumberFormatException, ClassNotFoundException, URISyntaxException, SQLException {

		int n = 2;
//		int minOccurrences = 5;
		int minOccurrences = 100;

//		int minYear = 1970;
		int minYear = 0;
		int maxYear = 2008;
		
		int begin = 0;
		int end = 100;

		System.out.println("START " + n + " grams");

		for (int i = begin; i < end; i++) {
			System.out.println("Starting " + i);
			NgramConstrainedLoader loader = new NgramConstrainedLoader(
					new NgramCsvReader("C:\\Users\\Thomas\\Desktop\\" + n + "gram\\googlebooks-eng-1M-" + n
							+ "gram-20090715-" + i + ".csv"),
					new NgramMySQLConnector(n, "localhost", 3306, "ngram", "ngram", "ngram"), minYear, maxYear,
					minOccurrences, NgramConstrainedLoader::isAdjectiveNounCombination, true);
			loader.execute();
		}
		System.out.println("Finished");

	}

}
