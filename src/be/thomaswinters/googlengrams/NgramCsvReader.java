package be.thomaswinters.googlengrams;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

public class NgramCsvReader {

	private final String ngramCsvPath;
	private final String seperator;

	public NgramCsvReader(String ngramCsvPath, String seperator) {
		this.ngramCsvPath = ngramCsvPath;
		this.seperator = seperator;
	}

	public NgramCsvReader(String ngramCsvPath) {
		this(ngramCsvPath, "\t");
	}

	public void convert(Function<String, Function<Integer, Consumer<Integer>>> consumer) {
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(ngramCsvPath));
			while ((line = br.readLine()) != null) {
				String[] args = line.split(seperator);
				consumer.apply(args[0]).apply(Integer.parseInt(args[1])).accept(Integer.parseInt(args[2]));
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
		NgramCsvReader reader = new NgramCsvReader(
				"C:\\Users\\Thomas\\Desktop\\1grams\\googlebooks-eng-1M-1gram-20090715-0.csv");
		reader.convert(e -> f -> g -> System.out.println(e + "->" + f + "->" + g));
	}
}
