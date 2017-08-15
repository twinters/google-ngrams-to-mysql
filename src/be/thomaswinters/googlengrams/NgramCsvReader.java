package be.thomaswinters.googlengrams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class that reads an (Google) n-gram file and is able to pass the ngram with
 * its year and count with a lambda function
 * 
 * @author Thomas Winters
 *
 */
public class NgramCsvReader {
	private final File ngramCsvPath;
	private final String seperator;
	private final String wordSeperator;

	public NgramCsvReader(File ngramCsvPath, String seperator, String wordSeperator) {
		this.ngramCsvPath = ngramCsvPath;
		this.seperator = seperator;
		this.wordSeperator = wordSeperator;
	}

	public NgramCsvReader(File ngramCsvPath) {
		this(ngramCsvPath, "\t", " ");
	}

	public void convert(Function<List<String>, Function<Integer, Consumer<Integer>>> consumer) {
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(ngramCsvPath));
			while ((line = br.readLine()) != null) {
				String[] args = line.split(seperator);
				List<String> ngram = Arrays.asList(args[0].split(wordSeperator));
				consumer.apply(ngram).apply(Integer.parseInt(args[1])).accept(Integer.parseInt(args[2]));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		NgramCsvReader reader = new NgramCsvReader(new File(args[0]));
		reader.convert(e -> f -> g -> System.out.println(e + "->" + f + "->" + g));
	}
}
