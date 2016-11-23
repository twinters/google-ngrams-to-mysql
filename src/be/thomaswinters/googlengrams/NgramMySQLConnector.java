package be.thomaswinters.googlengrams;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class NgramMySQLConnector {


	/*-********************************************-*
	 *  Instance variables
	*-********************************************-*/
	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private final String databaseName;

	private final Connection connection;
		
	private final int n;
	private final String addCountQuery;
	
	/*-********************************************-*/

	/*-********************************************-*
	 *  Constructor
	*-********************************************-*/
	public NgramMySQLConnector(int n, String host, int port, String username, String password, String databaseName)
			throws ClassNotFoundException, URISyntaxException, SQLException {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.databaseName = databaseName;
		connection = createConnection();
		this.n = n;
		this.addCountQuery = buildAddQuery(n);
	}
	/*-********************************************-*/

	/*-********************************************-*
	 *  Creating the connection
	*-********************************************-*/
	private Connection createConnection() throws ClassNotFoundException, URISyntaxException, SQLException {
		// Create driver
		Class.forName("com.mysql.jdbc.Driver");

		String jdbUrl = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;// +
																					// jdbUri.getPath();
		return DriverManager.getConnection(jdbUrl, username, password);
	}
	
	protected Connection getConnection() {
		return connection;
	}
	/*-********************************************-*/

	
	private String buildAddQuery(int n) {
		if (n==1) {
			return "insert into 1grams (word, count) values (?, ?)";
		}
		StringBuilder b = new StringBuilder();
		b.append("insert into ");
		b.append(n);
		b.append("grams (");
		for (int i = 1; i <= n; i++) {
			b.append("word" + i+ ", ");
		}
		b.append("count) values (");
		for (int i = 1; i <= n+1; i++) {
			b.append("?");
			if (i <= n) {
				b.append(", ");
			}
		}
		b.append(")");
		return b.toString();
	}

	/*-********************************************-*
	 *  Mutators
	*-********************************************-*/
	public void addCount(List<String> words, long count) {
		try {
			// create the mysql insert preparedstatement
			PreparedStatement addUsedTweetPS = getConnection().prepareStatement(addCountQuery);
			for (int i = 1; i <= n; i++) {
				addUsedTweetPS.setString(i, words.get(i-1));				
			}
			addUsedTweetPS.setLong(n+1, count);

			// execute the preparedstatement
			addUsedTweetPS.execute();
		} catch (Exception e) {
			System.err.println("Got an exception!");
			e.printStackTrace();
		}
	}
	/*-********************************************-*/

	public static void main(String[] args) throws ClassNotFoundException, URISyntaxException, SQLException {
		new NgramMySQLConnector(1, System.getenv("ngram_db_host"), Integer.parseInt(System.getenv("ngram_db_port")),
				System.getenv("ngram_db_username"), System.getenv("ngram_db_password"),
				System.getenv("ngram_db_databaseName"));
	}

	public void close() throws SQLException {
		connection.close();
	}

}
