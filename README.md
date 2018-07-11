# Google-Ngrams-to-MySQL

Java converter of Google Ngrams format to a MySQL database

These scripts allow Google Ngrams of any size to be stored in a MySQL database for easier access.
It also allows to constrain what kind of tuples (not) to store, e.g. on a minimum count, what years, only certain part of speech etc.

## How to install

In order to use this library, you need to install [Maven](https://maven.apache.org/).
After installing Maven, you need it to download the dependencies specified in `pom.xml`.

## How to run

The MySQL database needs to be initialised with the database model stored in `database-model.mwb`.

The main method of `NGramConstrainedLoader` can be run in order to start the storing process with several different arguments.

| Argument      | Description               |
| --------------- |---------------------------|
|-folder | Folder of the n-gram files|
|-filePrefix | Prefix of the n-gram files|
|-n | Size of the n-gram mode, e.g. 2-gram.|
|-minOccurrences | Minimum frequency in order to be stored when all occurrences over all the allowed years are summed|
|-minYear | Minimum year to get frequencies of|
|-maxYear | Maximum year to get frequencies of|
|-beginIndex | Index of the file to start from, this allows for partially loading the data such that it can be continued later. |
|-endIndex | Index of the file to end with|
|-constrainer | Constraint for storing. Currently implemented: 'all' and 'adjectivenoun'.|
|-allowedRegex | Constrains every words of a stored tuple to adhere to these regex. Implemented shortcut handles: 'all', 'allwords' and 'lowercase'|
|-sqlHost | Host of the SQL database|
|-sqlPort | Port of the SQL database|
|-sqlUsername | Username of the SQL database|
|-sqlPassword | Password of the SQL database|
|-sqlDb | Database of the SQL database|


In your Java implementation, the `NgramMySQLConnector` class can be used to easily query the constructed database.
