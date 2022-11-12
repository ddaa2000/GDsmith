package org.example.gdsmith.agensGraph;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.JsonObject;
import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.OracleFactory;
import org.example.gdsmith.agensGraph.oracle.AgensGraphAlwaysTrueOracle;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.cypher.dsl.IQueryGenerator;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Parameters(separators = "=", commandDescription = "AgensGraph (default port: " + AgensGraphOptions.DEFAULT_PORT
        + ", default host: " + AgensGraphOptions.DEFAULT_HOST)
public class AgensGraphOptions implements DBMSSpecificOptions<AgensGraphOptions.AgensGraphOracleFactory> {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 15432; //todo 改

    @Parameter(names = "--oracle")
    public List<AgensGraphOracleFactory> oracles = Arrays.asList(AgensGraphOracleFactory.ALWAYS_TRUE);

    @Parameter(names = "--host")
    public String host = DEFAULT_HOST;

    @Parameter(names = "--port")
    public int port = DEFAULT_PORT;

    @Parameter(names = "--username")
    public String username = "postgres";

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

    public static AgensGraphOptions parseOptionFromFile(JsonObject jsonObject){
        AgensGraphOptions options = new AgensGraphOptions();
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
        return options;
    }

    @Override
    public List<AgensGraphOracleFactory> getTestOracleFactory() {
        return oracles;
    }

    @Override
    public IQueryGenerator getQueryGenerator() {
        return null;
    }

    public enum AgensGraphOracleFactory implements OracleFactory<AgensGraphGlobalState> {

        ALWAYS_TRUE {

            @Override
            public TestOracle create(AgensGraphGlobalState globalState) throws SQLException {
                return new AgensGraphAlwaysTrueOracle(globalState);
            }
        }
    }
}
