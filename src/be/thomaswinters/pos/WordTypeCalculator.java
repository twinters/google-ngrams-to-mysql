package be.thomaswinters.pos;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class WordTypeCalculator {

	private final Dictionary dictionary;
	private final MaxentTagger tagger;

	private static final Map<POS, Set<String>> BLACKLIST;
	static {
		Builder<POS, Set<String>> blacklistB = ImmutableMap.builder();
		blacklistB.put(POS.ADJECTIVE, new HashSet<>(Arrays.asList("on", "off", "in", "out", "under", "least", "about",
				"whatever", "all", "up", "i", "both", "just", "like", "in","such","much","no","very","many")));
		blacklistB.put(POS.NOUN, new HashSet<>(Arrays.asList("a","like", "i","have","as", "longer", "in", "more", "it","don","or","so","over")));
		blacklistB.put(POS.ADVERB, new HashSet<>(Arrays.asList("")));
		blacklistB.put(POS.VERB, new HashSet<>(Arrays.asList("")));

		BLACKLIST = blacklistB.build();
	}

	private static final Map<POS, Set<String>> WHITELIST;
	static {
		Builder<POS, Set<String>> whitelistB = ImmutableMap.builder();
		whitelistB.put(POS.ADJECTIVE, new HashSet<>(Arrays.asList("exploding", "ground","minding","bi","meta","beating","dumped","secured","crunchy","substituted", "steeped","hung", "misspelled","flavored","blowing","soon","check")));
		whitelistB.put(POS.NOUN, new HashSet<>(Arrays.asList("blunt","skirts","myself","humping")));
		whitelistB.put(POS.ADVERB, new HashSet<>(Arrays.asList("")));
		whitelistB.put(POS.VERB, new HashSet<>(Arrays.asList("")));

		WHITELIST = whitelistB.build();
	}

	public WordTypeCalculator(Dictionary dictionary, MaxentTagger tagger) {
		this.dictionary = dictionary;
		this.tagger = tagger;
	}

	/**
	 * Uses WordNet to detect the Part-of-Speech of a word, as well as the whitelist
	 * 
	 * @param word
	 * @return
	 */
	public Collection<POS> getWordTypes(String word) {
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

		result.addAll(WHITELIST.entrySet().stream().filter(e -> e.getValue().contains(word.trim().toLowerCase()))
				.map(e -> e.getKey()).collect(Collectors.toList()));

		return result;
	}

	public Optional<POS> tagWordWithStanfordPOS(String word) {
		return convertToWordnetPOS(tagWordWithStanford(word));
	}

	/**
	 * Tags a word with a tag found on
	 * http://www.comp.leeds.ac.uk/amalgam/tagsets/upenn.html
	 * 
	 * @param word
	 * @return
	 */
	public String tagWordWithStanford(String word) {
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
	public List<TaggedWord> tagSentenceWithStanford(String sentence) {
		List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sentence));

		List<TaggedWord> result = new ArrayList<>();
		for (List<HasWord> hasWordSentence : sentences) {
			List<TaggedWord> tSentence = tagger.tagSentence(hasWordSentence);
			result.addAll(tSentence);
		}
		return result;
	}

	public boolean isAllowedToBeOfType(String word, POS pos) {
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