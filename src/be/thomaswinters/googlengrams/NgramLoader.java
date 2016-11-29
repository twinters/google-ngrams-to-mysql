package be.thomaswinters.googlengrams;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public abstract class NgramLoader {

	private final NgramCsvReader reader;
	private final NgramMySQLConnector connector;

	public NgramLoader(NgramCsvReader reader, NgramMySQLConnector connector) {
		this.reader = reader;
		this.connector = connector;
	}

	public NgramLoader() throws NumberFormatException, ClassNotFoundException, URISyntaxException, SQLException {
		this(new NgramCsvReader(System.getenv("ngram_csv_file")),
				new NgramMySQLConnector(1, System.getenv("ngram_db_host"),
						Integer.parseInt(System.getenv("ngram_db_port")), System.getenv("ngram_db_username"),
						System.getenv("ngram_db_password"), System.getenv("ngram_db_databaseName")));
	}

	public void execute() {
		reader.convert(e -> f -> g -> store(e, f, g));
		try {
			connector.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	}

	private void store(List<String> words, int year, int count) {
		if (shouldStore(words, year, count)) {
			connector.addCount(words, count);
		}
	}

	public abstract boolean shouldStore(List<String> words, int year, int count);
}
