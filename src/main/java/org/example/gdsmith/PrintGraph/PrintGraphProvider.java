package org.example.gdsmith.PrintGraph;

import com.google.gson.JsonObject;
import org.example.gdsmith.MainOptions;
import org.example.gdsmith.common.log.LoggableFactory;
import org.example.gdsmith.cypher.CypherConnection;
import org.example.gdsmith.cypher.CypherLoggableFactory;
import org.example.gdsmith.cypher.CypherProviderAdapter;

import java.util.List;

public class PrintGraphProvider extends CypherProviderAdapter<PrintGraphGlobalState, PrintGraphSchema, PrintGraphOptions> {
    public PrintGraphProvider() {
        super(PrintGraphGlobalState.class, PrintGraphOptions.class);
    }

    @Override
    public CypherConnection createDatabase(PrintGraphGlobalState globalState) throws Exception {
        return createDatabaseWithOptions(globalState.getOptions(), globalState.getDbmsSpecificOptions());
    }

    @Override
    public String getDBMSName() {
        return "printgraph";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }

    @Override
    protected void checkViewsAreValid(PrintGraphGlobalState globalState) {

    }

    @Override
    public void generateDatabase(PrintGraphGlobalState globalState) throws Exception {

    }

    @Override
    public PrintGraphOptions generateOptionsFromConfig(JsonObject config) {
        return PrintGraphOptions.parseOptionFromFile(config);
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, PrintGraphOptions specificOptions) throws Exception {
        String username = specificOptions.getUsername();
        String password = specificOptions.getPassword();
        String host = specificOptions.getHost();
        int port = specificOptions.getPort();
        if (host == null) {
            host = PrintGraphOptions.DEFAULT_HOST;
        }
        if (port == MainOptions.NO_SET_PORT) {
            port = PrintGraphOptions.DEFAULT_PORT;
        }
        PrintGraphConnection con = null;
        try{
            con = new PrintGraphConnection(specificOptions);
            con.executeStatement("MATCH (n) DETACH DELETE n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
}
