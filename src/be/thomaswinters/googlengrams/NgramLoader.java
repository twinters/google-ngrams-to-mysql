package be.thomaswinters.googlengrams;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public abstract class NgramLoader {

	private final List<NgramCsvReader> readers;
	private final NgramMySQLConnector connector;

	public NgramLoader(Collection<NgramCsvReader> readers, NgramMySQLConnector connector) {
		this.readers = new ArrayList<>(readers);
		this.connector = connector;
	}

	public NgramLoader() throws NumberFormatException, ClassNotFoundException, URISyntaxException, SQLException {
		this(Arrays.asList(new NgramCsvReader(System.getenv("ngram_csv_file"))),
				new NgramMySQLConnector(1, System.getenv("ngram_db_host"),
						Integer.parseInt(System.getenv("ngram_db_port")), System.getenv("ngram_db_username"),
						System.getenv("ngram_db_password"), System.getenv("ngram_db_databaseName")));
	}

	private Multiset<List<String>> result = HashMultiset.create();

	public void execute() {
		readers.stream().forEach(r -> r.convert(e -> f -> g -> store(e, f, g)));
		try {
			result.stream().forEach(e -> connector.addCount(e, result.count(e)));
			connector.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	}

	private void store(List<String> words, int year, int count) {
		if (shouldStore(words, year, count)) {
			result.add(words, count);
			// connector.addCount(words, count);
		}
	}

	public abstract boolean shouldStore(List<String> words, int year, int count);
}
