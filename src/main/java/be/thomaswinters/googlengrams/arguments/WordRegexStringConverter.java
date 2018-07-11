package be.thomaswinters.googlengrams.arguments;

import com.beust.jcommander.IStringConverter;

public class WordRegexStringConverter implements IStringConverter<String> {

	public final static String ALL = "^.*$";
	public final static String ALL_WORDS = "^[a-zA-Z\\-]+$";
	public final static String LOWERCASE_WORDS = "^[a-z\\-]+$";
	
	@Override
	public String convert(String value) {

		switch (value.toLowerCase()) {
		case "all":
			return ALL;
		case "allwords":
			return ALL_WORDS;
		case "lowercase":
			return LOWERCASE_WORDS;
		default:
			return value;
		}
	}



}
