package be.thomaswinters.googlengrams.arguments;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

public class ConstrainedLoaderArguments {

	@Parameter(names = "-folder", description = "Folder of the n-gram files", converter = FileConverter.class)
	private File folder = new File("");

	@Parameter(names = "-filePrefix", description = "Prefix of the n-gram files", converter = FileConverter.class)
	private String filePrefix = "googlebooks-eng-1M-2gram-20090715-";

	@Parameter(names = "-n", description = "Size of the n-gram mode, e.g. 2-gram.")
	private int n = 2;

	@Parameter(names = "-minOccurrences", description = "Minimum frequency in order to be stored when all occurrences over all the allowed years are summed")
	private int minOccurrences = 100;

	@Parameter(names = "-minYear", description = "Minimum year to get frequencies of")
	private int minYear = 0;

	@Parameter(names = "-maxYear", description = "Maximum year to get frequencies of")
	private int maxYear = 2008;

	@Parameter(names = "-beginIndex", description = "Index of the file to start from")
	private int begin = 0;

	@Parameter(names = "-endIndex", description = "Index of the file to end with")
	private int end = 100;

	@Parameter(names = "-constrainer", description = "Constraint for storing. Currently implemented: 'all' and 'adjectivenoun'.", converter = ConstraintStringConverter.class)
	private Function<List<String>, Boolean> constrainer = e -> true;

	@Parameter(names = "-allowedRegex", description = "Constrains every words of a stored tuple to adhere to these regex. Implemented shortcut handles: 'all', 'allwords' and 'lowercase'", converter = WordRegexStringConverter.class)
	private String allowedRegex = WordRegexStringConverter.LOWERCASE_WORDS;

	/*-********************************************-*
	 *  Database
	*-********************************************-*/
	@Parameter(names = "-sqlHost", description = "Host of the SQL database")
	private String host = "localhost";

	@Parameter(names = "-sqlPort", description = "Port of the SQL database")
	private int port = 3306;

	@Parameter(names = "-sqlUsername", description = "Username of the SQL database")
	private String username = "ngram";

	@Parameter(names = "-sqlPassword", description = "Password of the SQL database")
	private String password = "ngram";

	@Parameter(names = "-sqlDb", description = "Database of the SQL database")
	private String databaseName = "ngram";

	/*-********************************************-*/

	/*-********************************************-*
	 *  Getters
	*-********************************************-*/

	public File getFolder() {
		return folder;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public int getN() {
		return n;
	}

	public int getMinOccurrences() {
		return minOccurrences;
	}

	public int getMinYear() {
		return minYear;
	}

	public int getMaxYear() {
		return maxYear;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public Function<List<String>, Boolean> getConstrainer() {
		return constrainer;
	}

	public String getAllowedRegex() {
		return allowedRegex;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	/*-********************************************-*/

}
