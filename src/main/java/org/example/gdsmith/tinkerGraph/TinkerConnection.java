package org.example.gdsmith.tinkerGraph;

import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.cypher.CypherConnection;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.opencypher.gremlin.client.CypherGremlinClient;
import org.opencypher.gremlin.translation.TranslationFacade;

import java.util.Arrays;
import java.util.List;

public class TinkerConnection extends CypherConnection {


    private Cluster cluster;

    public TinkerConnection(Cluster cluster){
        this.cluster = cluster;
    }

    public TinkerConnection(){
    }


    @Override
    public String getDatabaseVersion() throws Exception {
        //todo complete
        return "";
    }

    @Override
    public void close() throws Exception {
        Client gremlinClient = cluster.connect();
        gremlinClient.submit("MATCH (n) DETACH DELETE n");
        gremlinClient.close();
        cluster.close();
    }

    @Override
    public void executeStatement(String arg) throws Exception{
        String cypher = arg;
        Client gremlinClient = cluster.connect();
        CypherGremlinClient translatingGremlinClient = CypherGremlinClient.translating(gremlinClient);
        String gremlin = (new TranslationFacade()).toGremlinGroovy(cypher);
        System.out.println(gremlin);
        translatingGremlinClient.submit(cypher).all();
    }

    @Override
    public List<GDSmithResultSet> executeStatementAndGet(String arg) throws Exception{
        String cypher = arg;
        Client gremlinClient = cluster.connect();
        CypherGremlinClient translatingGremlinClient = CypherGremlinClient.translating(gremlinClient);
        String gremlin = (new TranslationFacade()).toGremlinGroovy(cypher);
        System.out.println(gremlin);
        return Arrays.asList(new GDSmithResultSet(translatingGremlinClient.submit(cypher).all()));
    }
}
