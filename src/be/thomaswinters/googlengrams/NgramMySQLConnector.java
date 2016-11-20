package be.thomaswinters.googlengrams;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NgramMySQLConnector {

	/*-********************************************-*
	 *  Static variables
	*-********************************************-*/
	private static final String ADD_COUNT_QUERY = "insert into 1grams (word, count) values (?, ?)";
	/*-********************************************-*/

	/*-********************************************-*
	 *  Instance variables
	*-********************************************-*/
	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private final String databaseName;

	private final Connection conn;
	/*-********************************************-*/

	/*-********************************************-*
	 *  Constructor
	*-********************************************-*/
	public NgramMySQLConnector(String host, int port, String username, String password, String databaseName)
			throws ClassNotFoundException, URISyntaxException, SQLException {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.databaseName = databaseName;
		conn = getConnection();
	}
	/*-********************************************-*/

	/*-********************************************-*
	 *  Creating the connection
	*-********************************************-*/
	private Connection getConnection() throws ClassNotFoundException, URISyntaxException, SQLException {
		// Create driver
		Class.forName("com.mysql.jdbc.Driver");

		String jdbUrl = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;// +
																					// jdbUri.getPath();
		return DriverManager.getConnection(jdbUrl, username, password);
	}
	/*-********************************************-*/

	/*-********************************************-*
	 *  Mutators
	*-********************************************-*/
	public void addCount(String word, long count) {
		try {
			// create the mysql insert preparedstatement
			PreparedStatement addUsedTweetPS = conn.prepareStatement(ADD_COUNT_QUERY);
			addUsedTweetPS.setString(1, word);
			addUsedTweetPS.setLong(2, count);

			// execute the preparedstatement
			addUsedTweetPS.execute();

			conn.close();
		} catch (Exception e) {
			System.err.println("Got an exception!");
			System.err.println(e.getMessage());
		}
	}
	/*-********************************************-*/

	public static void main(String[] args) throws ClassNotFoundException, URISyntaxException, SQLException {
		new NgramMySQLConnector(System.getenv("ngram_db_host"), Integer.parseInt(System.getenv("ngram_db_port")),
				System.getenv("ngram_db_username"), System.getenv("ngram_db_password"),
				System.getenv("ngram_db_databaseName"));
	}

}
