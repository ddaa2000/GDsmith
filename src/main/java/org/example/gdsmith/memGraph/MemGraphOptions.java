package org.example.gdsmith.memGraph;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.JsonObject;
import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.OracleFactory;
import org.example.gdsmith.cypher.dsl.IQueryGenerator;
import org.example.gdsmith.memGraph.oracle.MemGraphAlwaysTrueOracle;
import org.example.gdsmith.common.oracle.TestOracle;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Parameters(separators = "=", commandDescription = "MemGraph (default port: " + MemGraphOptions.DEFAULT_PORT
        + ", default host: " + MemGraphOptions.DEFAULT_HOST)
public class MemGraphOptions implements DBMSSpecificOptions<MemGraphOptions.MemGraphOracleFactory> {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 7687; //todo 改

    @Parameter(names = "--oracle")
    public List<MemGraphOracleFactory> oracles = Arrays.asList(MemGraphOracleFactory.ALWAYS_TRUE);

    @Parameter(names = "--host")
    public String host = DEFAULT_HOST;

    @Parameter(names = "--port")
    public int port = DEFAULT_PORT;

    @Parameter(names = "--username")
    public String username = "sqlancer";

    @Parameter(names = "--password")
    public String password = "sqlancer";

    @Parameter(names = "--restart-command")
    public String restartCommand = "";

    public static MemGraphOptions parseOptionFromFile(JsonObject jsonObject){
        MemGraphOptions options = new MemGraphOptions();
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
    public List<MemGraphOracleFactory> getTestOracleFactory() {
        return oracles;
    }

    @Override
    public IQueryGenerator getQueryGenerator() {
        return null;
    }

    public enum MemGraphOracleFactory implements OracleFactory<MemGraphGlobalState> {

        ALWAYS_TRUE {

            @Override
            public TestOracle create(MemGraphGlobalState globalState) throws SQLException {
                return new MemGraphAlwaysTrueOracle(globalState);
            }
        }
    }
}
