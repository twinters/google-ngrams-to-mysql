package be.thomaswinters.pos;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Calculates word types based on evidence in WordNet and in the Stanford POS
 * tagger. It also uses a whitelist and a blacklist for exceptions
 * 
 * @author Thomas Winters
 *
 */
public class WordTypeCalculator {

	private final Dictionary dictionary;
	private final MaxentTagger tagger;

	private static final Map<POS, Set<String>> BLACKLIST;
	static {
		Builder<POS, Set<String>> blacklistB = ImmutableMap.builder();
		blacklistB.put(POS.ADJECTIVE, new HashSet<>(Arrays.asList(
				// Numbers
				"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve",
				"thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty", "thirty",
				"fourty", "fifty", "sixty", "seventy", "eigty", "ninety", "hundred", "thousand", "million", "billion",
				"first", "second", "third", "fourth", "fifty", "sixth", "seventh", "eighth", "nineth", "tenth", "last",

				// Quantifiers
				"all", "any", "my", "other", "another", "only", "each", "every", "individual", "both", "each", "same",
				"every", "many", "some", "more", "half", "quarter", "no", "such", "much", "very", "whole", "neither",
				"kind", "what", "fewer", "various", "entire", "cherry",

				// Pronouns
				"his", "her", "our", "your", "own",

				// Directions
				"up", "down", "left", "right", "on", "off", "in", "out", "under", "above", "top", "bottom", "over",
				"under", "about",

				// Comparative
				"least", "less", "more", "most", "best", "better", "strongest", "greatest", "weakest", "biggest",
				"bigger", "largest", "large", "smallest", "smaller", "stronger",

				// Letters
				"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
				"v", "w", "x", "y", "z",

				// Verbs
				"giving", "seeing", "meaning", "present", "looking", "go", "fly", "becoming",

				"about", "whatever", "i", "just", "like", "in", "after", "no", "through", "then", "same", "most",
				"made", "true", "next", "set", "said", "then", "spare", "here", "there", "lay", "star", "unlike",
				"whatever", "likely", "even", "meet", "now", "union", "favorite", "away", "former", "latter", "quality",
				"mere", "few", "enough", "welcome", "soon")));
		blacklistB.put(POS.NOUN, new HashSet<>(Arrays.asList(
				// Numbers
				"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve",
				"thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty", "thirty",
				"fourty", "fifty", "sixty", "seventy", "eigty", "ninety", "hundred", "thousand", "million", "billion",
				"first", "second", "third", "fourth", "fifty", "sixth", "seventh", "eighth", "nineth", "tenth",
				"single", "double", "full",

				// Directions
				"up", "down", "left", "right", "on", "off", "in", "out", "under", "above", "top", "bottom", "over",
				"under", "about", "north", "south", "east", "west", "round", "square", "upwards",

				// Letters
				"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
				"v", "w", "x", "y", "z",

				// Verbs
				"be", "do", "will", "going", "drinking", "must", "bend", "plays", "found", "feel",

				// Colours
				"red", "green", "blue", "yellow", "purple", "orange", "teale", "black", "white",

				// Adjectives
				"wild", "uncut", "main", "plain", "straight", "raw",

				//
				"something", "a", "an", "like", "i", "have", "as", "longer", "in", "more", "it", "don", "or", "so",
				"over", "who", "might", "are", "deep", "large", "now", "while", "get", "getting", "ill", "over",
				"there", "here", "then", "inside", "kind", "sort", "two", "may", "as", "little", "local", "major",
				"may", "ten", "say", "large", "even", "high", "slight", "extra", "fine", "well", "at", "modern",
				"black", "deep", "poor", "it", "recent", "might", "short", "can", "better", "far", "common", "rough",
				"prior", "more", "like", "few", "recent", "basic", "need", "middle", "open", "ethnic", "he", "she",
				"ill", "quick", "good", "anti", "last", "there", "sweet", "now", "adams", "still", "being", "out", "no",
				"me", "old", "io", "ol", "same", "enough", "thou")));
		blacklistB.put(POS.ADVERB, new HashSet<>(Arrays.asList("")));
		blacklistB.put(POS.VERB, new HashSet<>(Arrays.asList("")));

		BLACKLIST = blacklistB.build();
	}

	private static final Map<POS, Set<String>> WHITELIST;
	static {
		Builder<POS, Set<String>> whitelistB = ImmutableMap.builder();
		whitelistB.put(POS.ADJECTIVE,
				new HashSet<>(Arrays.asList("exploding", "ground", "minding", "bi", "meta", "beating", "dumped",
						"secured", "crunchy", "substituted", "steeped", "hung", "misspelled", "flavored", "blowing",
						"soon", "check")));
		whitelistB.put(POS.NOUN,
				new HashSet<>(Arrays.asList("blunt", "skirts", "myself", "humping", "channels", "finger", "end")));
		whitelistB.put(POS.ADVERB, new HashSet<>(Arrays.asList("")));
		whitelistB.put(POS.VERB, new HashSet<>(Arrays.asList("")));

		WHITELIST = whitelistB.build();
	}

	public WordTypeCalculator(Dictionary dictionary, MaxentTagger tagger) {
		this.dictionary = dictionary;
		this.tagger = tagger;
	}

	private final Cache<String, Collection<POS>> cache = CacheBuilder.newBuilder().maximumSize(100).build();

	/**
	 * Uses WordNet to detect the Part-of-Speech of a word, as well as the whitelist
	 * 
	 * @param word
	 * @return
	 */
	public Collection<POS> getWordTypes(String word) {
		try {
			return cache.get(word, () -> {
				Set<POS> result = new HashSet<>(4);

				if (word.trim().isEmpty()) {
					return result;
				}

				for (POS pos : POS.values()) {
					IIndexWord wd = dictionary.getIndexWord(word, pos);
					if (wd != null && !BLACKLIST.get(pos).contains(word)) {
						result.add(pos);
					}
				}

				Optional<POS> stanfordPOS = tagWordWithStanfordPOS(word);
				if (stanfordPOS.isPresent() && !BLACKLIST.get(stanfordPOS.get()).contains(word)) {
					result.add(stanfordPOS.get());
				}

				result.addAll(
						WHITELIST.entrySet().stream().filter(e -> e.getValue().contains(word.trim().toLowerCase()))
								.map(e -> e.getKey()).collect(Collectors.toList()));

				return result;
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calculates a tag based on the Stanford POS tagger
	 * 
	 * @param word
	 * @return
	 */
	private Optional<POS> tagWordWithStanfordPOS(String word) {
		return convertToWordnetPOS(tagWordWithStanford(word));
	}

	/**
	 * Tags a word with a tag found on
	 * http://www.comp.leeds.ac.uk/amalgam/tagsets/upenn.html
	 * 
	 * @param word
	 * @return
	 */
	private String tagWordWithStanford(String word) {
		List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(word));
		List<HasWord> sentence = sentences.get(0);
		List<TaggedWord> tSentence = tagger.tagSentence(sentence);
		String tag = tSentence.get(0).tag();

		return tag;
	}

	/**
	 * Tags a sentence with tags found on
	 * http://www.comp.leeds.ac.uk/amalgam/tagsets/upenn.html
	 * 
	 * @param word
	 * @return
	 */
	private List<TaggedWord> tagSentenceWithStanford(String sentence) {
		List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sentence));

		List<TaggedWord> result = new ArrayList<>();
		for (List<HasWord> hasWordSentence : sentences) {
			List<TaggedWord> tSentence = tagger.tagSentence(hasWordSentence);
			result.addAll(tSentence);
		}
		return result;
	}

	/**
	 * Checks if a word is allowed to have a certain tag for a word according to the
	 * blacklist
	 * 
	 * @param word
	 * @param pos
	 * @return
	 */
	private boolean isAllowedToBeOfType(String word, POS pos) {
		return !BLACKLIST.get(pos).contains(word.toLowerCase().trim());
	}

	public Collection<String> getWordsOfTypes(List<POS> allowedTypes, String sentence) {

		HashSet<String> result = new HashSet<>();
		result.addAll(tagSentenceWithStanford(sentence).stream()
				// Filter those that have the right tag
				.filter(hasWord -> convertToWordnetPOS(hasWord.tag()).isPresent()
						&& isAllowedToBeOfType(hasWord.word(), convertToWordnetPOS(hasWord.tag()).get())
						&& allowedTypes.contains(convertToWordnetPOS(hasWord.tag()).get()))
				// Map to the word
				.map(hasWord -> hasWord.word())
				// Collect them
				.collect(Collectors.toList()));

		List<String> words = Arrays.asList(sentence.split(" "));
		List<String> allowedWords = words.stream()
				.filter(word -> !Collections.disjoint(getWordTypes(word).stream()
						.filter(pos -> isAllowedToBeOfType(word, pos)).collect(Collectors.toList()), allowedTypes))
				.collect(Collectors.toList());
		result.addAll(allowedWords);

		return result;

	}

	/**
	 * Converts Stanford tagging string to WordNet Part of Speech tags.
	 * 
	 * @param tag
	 * @return
	 */
	private Optional<POS> convertToWordnetPOS(String tag) {
		if (tag.startsWith("NN")) {
			return Optional.of(POS.NOUN);
		}
		if (tag.startsWith("VB")) {
			return Optional.of(POS.VERB);
		}
		if (tag.startsWith("JJ")) {
			return Optional.of(POS.ADJECTIVE);
		}
		if (tag.startsWith("RB")) {
			return Optional.of(POS.ADVERB);
		}
		return Optional.empty();

	}

}
