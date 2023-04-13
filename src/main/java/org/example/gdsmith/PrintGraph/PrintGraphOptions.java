package org.example.gdsmith.PrintGraph;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.JsonObject;
import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.OracleFactory;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.cypher.dsl.IQueryGenerator;
import org.example.gdsmith.PrintGraph.oracle.PrintGraphAlwaysTrueOracle;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Parameters(separators = "=", commandDescription = "PrintGraph (default port: " + PrintGraphOptions.DEFAULT_PORT
        + ", default host: " + PrintGraphOptions.DEFAULT_HOST)
public class PrintGraphOptions implements DBMSSpecificOptions<PrintGraphOptions.PrintGraphOracleFactory> {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 6379; //todo 改

    public static PrintGraphOptions parseOptionFromFile(JsonObject jsonObject){
        PrintGraphOptions options = new PrintGraphOptions();
        if(jsonObject.has("host")){
            options.host = jsonObject.get("host").getAsString();
        }
        if(jsonObject.has("port")){
            options.port = jsonObject.get("port").getAsInt();
        }
        if(jsonObject.has("username")){
            options.username = jsonObject.get("username").getAsString();
        }
        if(jsonObject.has("password")){
            options.password = jsonObject.get("password").getAsString();
        }
        if(jsonObject.has("restart-command")){
            options.restartCommand = jsonObject.get("restart-command").getAsString();
        }
        return options;
    }

    @Parameter(names = "--restart-command")
    public String restartCommand = "";

    @Parameter(names = "--oracle")
    public List<PrintGraphOracleFactory> oracles = Arrays.asList(PrintGraphOracleFactory.ALWAYS_TRUE);

    @Parameter(names = "--host")
    public String host = DEFAULT_HOST;

    @Parameter(names = "--port")
    public int port = DEFAULT_PORT;

    @Parameter(names = "--username")
    public String username = "neo4j";

    @Parameter(names = "--password")
    public String password = "sqlancer";

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    @Override
    public List<PrintGraphOracleFactory> getTestOracleFactory() {
        return oracles;
    }

    @Override
    public IQueryGenerator getQueryGenerator() {
        return null;
    }

    public enum PrintGraphOracleFactory implements OracleFactory<PrintGraphGlobalState> {

        ALWAYS_TRUE {

            @Override
            public TestOracle create(PrintGraphGlobalState globalState) throws SQLException {
                return new PrintGraphAlwaysTrueOracle(globalState);
            }
        }
    }
}
