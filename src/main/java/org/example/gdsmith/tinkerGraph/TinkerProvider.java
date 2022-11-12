package org.example.gdsmith.tinkerGraph;

import com.google.gson.JsonObject;
import org.example.gdsmith.AbstractAction;
import org.example.gdsmith.MainOptions;
import org.example.gdsmith.common.log.LoggableFactory;
import org.example.gdsmith.cypher.*;
import org.example.gdsmith.cypher.*;
import org.example.gdsmith.tinkerGraph.gen.TinkerGraphGenerator;
import org.example.gdsmith.tinkerGraph.gen.TinkerNodeGenerator;
import org.example.gdsmith.tinkerGraph.schema.TinkerSchema;
import org.apache.tinkerpop.gremlin.driver.Cluster;

import java.util.List;

public class TinkerProvider extends CypherProviderAdapter<TinkerGlobalState, TinkerSchema, TinkerOptions> {
    public TinkerProvider() {
        super(TinkerGlobalState.class, TinkerOptions.class);
    }

    @Override
    public TinkerOptions generateOptionsFromConfig(JsonObject config) {
        return TinkerOptions.parseOptionFromFile(config);
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, TinkerOptions specificOptions) throws Exception {
        String username = specificOptions.getUsername();
        String password = specificOptions.getPassword();
        String host = specificOptions.getHost();
        int port = specificOptions.getPort();
        if (host == null) {
            host = TinkerOptions.DEFAULT_HOST;
        }
        if (port == MainOptions.NO_SET_PORT) {
            port = TinkerOptions.DEFAULT_PORT;
        }

        String url = String.format("bolt://%s:%d", host, port);
        Cluster cluster;
        if(specificOptions.configFile != null){
            cluster = Cluster.open(specificOptions.configFile);
        }
        else{
            cluster = Cluster.open();
        }
        TinkerConnection con = new TinkerConnection(cluster);
        con.executeStatement("MATCH (n) DETACH DELETE n");
        return con;
    }

    enum Action implements AbstractAction<TinkerGlobalState> {
        CREATE(TinkerNodeGenerator::createNode);

        private final CypherQueryProvider<TinkerGlobalState> cypherQueryProvider;

        //SQLQueryProvider是一个接口，返回SQLQueryAdapter
        Action(CypherQueryProvider<TinkerGlobalState> cypherQueryProvider) {
            this.cypherQueryProvider = cypherQueryProvider;
        }

        @Override
        public CypherQueryAdapter getQuery(TinkerGlobalState globalState) throws Exception {
            return cypherQueryProvider.getQuery(globalState);
        }
    }

    @Override
    public CypherConnection createDatabase(TinkerGlobalState globalState) throws Exception {
        return createDatabaseWithOptions(globalState.getOptions(), globalState.getDbmsSpecificOptions());
    }

    @Override
    public String getDBMSName() {
        return "tinkergraph";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }

    @Override
    protected void checkViewsAreValid(TinkerGlobalState globalState) {

    }

    @Override
    public void generateDatabase(TinkerGlobalState globalState) throws Exception {
        List<CypherQueryAdapter> queries = TinkerGraphGenerator.createGraph(globalState);
        for(CypherQueryAdapter query : queries){
            globalState.executeStatement(query);
        }
    }
}
