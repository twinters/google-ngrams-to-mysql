package thomaswinters.googlengrams;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

/**
 * Simple class that has a CSV reader to read Google n-gram formatted files and output this to a MySQL database
 * @author Thomas Winters
 *
 */
public abstract class NgramLoader {

	private final NgramCsvReader reader;
	private final NgramMySQLConnector connector;

	public NgramLoader(NgramCsvReader reader, NgramMySQLConnector connector) {
		this.reader = reader;
		this.connector = connector;
	}

	public NgramLoader() throws NumberFormatException, ClassNotFoundException, URISyntaxException, SQLException {
		this(new NgramCsvReader(new File(System.getenv("ngram_csv_file"))),
				new NgramMySQLConnector(1, System.getenv("ngram_db_host"),
						Integer.parseInt(System.getenv("ngram_db_port")), System.getenv("ngram_db_username"),
						System.getenv("ngram_db_password"), System.getenv("ngram_db_databaseName")));
	}

	public void execute() {

		reader.convert(e -> f -> g -> store(e, f, g));

		try {
			endStoring();
			connector.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}

	}

	protected void store(List<String> words, int year, long count) {
		connector.addCount(words, count);
	}
	
	protected abstract void endStoring();


}
