package org.example.gdsmith.cypher;

import org.example.gdsmith.GDSmithDBConnection;
import org.example.gdsmith.common.query.GDSmithResultSet;

import java.util.List;

public abstract class CypherConnection implements GDSmithDBConnection {

    public void executeStatement(String arg) throws Exception{
        System.out.println("execute statement: "+arg);
    }

    public List<GDSmithResultSet> executeStatementAndGet(String arg) throws Exception{
        System.out.println("execute statement: "+arg);
        return null;
    }

    public List<Long> executeStatementAndGetTime(String arg) throws Exception{
        return null;
    }
}
