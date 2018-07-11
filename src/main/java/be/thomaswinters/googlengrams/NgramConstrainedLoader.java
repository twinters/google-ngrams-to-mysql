package be.thomaswinters.googlengrams;

import be.thomaswinters.googlengrams.arguments.ConstrainedLoaderArguments;
import com.beust.jcommander.JCommander;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;


/**
 * This class loads only a selected few of the tuples of provided n-grams.
 *
 * @author Thomas Winters
 */
public class NgramConstrainedLoader extends NgramLoader {

    private final int minYear;
    private final int maxYear;
    private final long minOccurrences;
    private final Function<List<String>, Boolean> constrainer;
    private Pattern wordPattern;
    private List<String> lastChecked = new ArrayList<>();
    private boolean lastCheckedResult;
    private List<String> last = new ArrayList<>();
    private long lastCount = 0;

    public NgramConstrainedLoader(NgramCsvReader reader, NgramMySQLConnector connector, int minYear, int maxYear,
                                  int minOccurrences, Function<List<String>, Boolean> constrainer, String allowedRegex) {
        super(reader, connector);
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.minOccurrences = minOccurrences;
        this.constrainer = constrainer;

        this.wordPattern = Pattern.compile(allowedRegex);
    }
    public NgramConstrainedLoader(NgramCsvReader reader, NgramMySQLConnector connector, int minYear, int maxYear,
                                  int minOccurrences, String allowedRegex) {
        this(reader, connector, minYear, maxYear, minOccurrences, (e -> true), allowedRegex);
    }

    public static void load(ConstrainedLoaderArguments arguments)
            throws ClassNotFoundException, URISyntaxException, SQLException {
        System.out.println("START " + arguments.getN() + " grams");

        for (int i = arguments.getBegin(); i < arguments.getEnd(); i++) {
            System.out.println("Starting " + i);
            NgramConstrainedLoader loader = new NgramConstrainedLoader(
                    new NgramCsvReader(new File(arguments.getFolder(), arguments.getFilePrefix() + i + ".csv")),
                    new NgramMySQLConnector(arguments.getN(), arguments.getHost(), arguments.getPort(),
                            arguments.getUsername(), arguments.getPassword(), arguments.getDatabaseName()),
                    arguments.getMinYear(), arguments.getMaxYear(), arguments.getMinOccurrences(),
                    arguments.getConstrainer(), arguments.getAllowedRegex());
            loader.execute();
        }

        System.out.println("Finished");

    }

    public static void main(String[] args)
            throws NumberFormatException, ClassNotFoundException, URISyntaxException, SQLException {
        ConstrainedLoaderArguments arguments = new ConstrainedLoaderArguments();
        JCommander.newBuilder().addObject(arguments).build().parse(args);

        load(arguments);
    }

    protected boolean matchesRegex(String word) {
        return wordPattern.matcher(word).matches();
    }

    protected boolean isAcceptedYear(int year) {
        return minYear <= year && year <= maxYear;
    }

    private boolean shouldStore(List<String> words, int year) {
        if (lastChecked.equals(words)) {
            return lastCheckedResult;
        }
        boolean result = isAcceptedYear(year) && words.stream().allMatch(e -> matchesRegex(e))
                && constrainer.apply(words);

        lastChecked = words;
        lastCheckedResult = result;

        return result;
    }

    private boolean shouldStoreCount(long count) {
        return count >= minOccurrences;
    }

    @Override
    protected void store(List<String> words, int year, long count) {
        if (shouldStore(words, year)) {
            if (!words.equals(last)) {
                if (!last.isEmpty() && shouldStoreCount(lastCount)) {
                    super.store(last, 0, lastCount);
                }
                last = words;
                lastCount = count;
            } else {
                lastCount += count;
            }
        }
    }

    @Override
    protected void endStoring() {
        super.store(last, 0, lastCount);
        last = new ArrayList<>();
        lastCount = 0;
    }

}
