package org.example.gdsmith.cypher;

import org.example.gdsmith.common.log.Loggable;
import org.example.gdsmith.common.log.LoggableFactory;
import org.example.gdsmith.common.log.LoggedString;
import org.example.gdsmith.common.query.Query;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CypherLoggableFactory extends LoggableFactory {

    @Override
    protected Loggable createLoggable(String input, String suffix) {
        String completeString = input;
        if (!input.endsWith(";")) {
            completeString += ";";
        }
        if (suffix != null && suffix.length() != 0) {
            completeString += suffix;
        }
        return new LoggedString(completeString);
    }

    @Override
    public CypherQueryAdapter getQueryForStateToReproduce(String queryString) {
        return new CypherQueryAdapter(queryString);
    }

    @Override
    public CypherQueryAdapter commentOutQuery(Query<?> query) {
        String queryString = query.getLogString();
        String newQueryString = "-- " + queryString;
        return new CypherQueryAdapter(newQueryString);
    }

    @Override
    protected Loggable infoToLoggable(String time, String databaseName, String databaseVersion, long seedValue) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- Time: " + time + "\n");
        sb.append("-- Database: " + databaseName + "\n");
        sb.append("-- Database version: " + databaseVersion + "\n");
        sb.append("-- seed value: " + seedValue + "\n");
        return new LoggedString(sb.toString());
    }

    @Override
    public Loggable convertStacktraceToLoggable(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return new LoggedString("--" + sw.toString().replace("\n", "\n--"));
    }
}