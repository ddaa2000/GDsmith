package org.example.gdsmith.agensGraph;

import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.cypher.CypherConnection;
//import org.neo4j.driver.Session;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public class AgensGraphConnection extends CypherConnection {

    private Connection connection;

    public AgensGraphConnection(Connection connection){
        this.connection = connection;
    }

    @Override
    public String getDatabaseVersion() throws Exception {
        return "agensgraph";
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    @Override
    public void executeStatement(String arg) throws Exception{
        Statement stmt = connection.createStatement();
        stmt.execute(arg);
    }

    @Override
    public List<GDSmithResultSet> executeStatementAndGet(String arg) throws Exception{
        Statement stmt = connection.createStatement();
        return Arrays.asList(new GDSmithResultSet(stmt.executeQuery(arg)));
    }
}
