package org.example.gdsmith.gremlin;

import org.example.gdsmith.GlobalState;
import org.example.gdsmith.common.query.ExpectedErrors;
import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.common.query.Query;
import org.example.gdsmith.cypher.CypherConnection;

import java.util.List;

public class CypherBasedGremlinQueryAdapter extends Query<CypherConnection> {

    private final String gremlinQuery;

    public CypherBasedGremlinQueryAdapter(String cypherQuery) {
        this.gremlinQuery = CypherGremlinTranslater.translate(cypherQuery);
    }

    private String canonicalizeString(String s) {
        if (s.endsWith(";")) {
            return s;
        } else if (!s.contains("--")) {
            return s + ";";
        } else {
            // query contains a comment
            return s;
        }
    }

    @Override
    public String getLogString() {
        return getQueryString();
    }

    @Override
    public String getQueryString() {
        return gremlinQuery;
    }

    @Override
    public String getUnterminatedQueryString() {
        return canonicalizeString(gremlinQuery);
    }

    @Override
    public boolean couldAffectSchema() {
        return false;
    }

    @Override
    public <G extends GlobalState<?, ?, CypherConnection>> boolean execute(G globalState, String... fills) throws Exception {
        System.out.println(gremlinQuery);
        globalState.getConnection().executeStatement(gremlinQuery);
        return true;
    }

    @Override
    public <G extends GlobalState<?, ?, CypherConnection>> List<GDSmithResultSet> executeAndGet(G globalState, String... fills) throws Exception {
        return globalState.getConnection().executeStatementAndGet(gremlinQuery);
    }

    @Override
    public ExpectedErrors getExpectedErrors() {
        //todo complete
        return null;
    }
}
