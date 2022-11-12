package org.example.gdsmith.cypher;

import org.example.gdsmith.DBMSSpecificOptions;
import org.example.gdsmith.ExecutionTimer;
import org.example.gdsmith.GlobalState;
import org.example.gdsmith.common.query.Query;
import org.example.gdsmith.common.schema.AbstractSchema;

public abstract class CypherGlobalState <O extends DBMSSpecificOptions<?>, S extends AbstractSchema<?, ?>>
        extends GlobalState<O, S, CypherConnection> {
    @Override
    protected void executeEpilogue(Query<?> q, boolean success, ExecutionTimer timer) throws Exception {
        boolean logExecutionTime = getOptions().logExecutionTime();
        if (success && getOptions().printSucceedingStatements()) {
            System.out.println(q.getQueryString());
        }
        if (logExecutionTime) {
            getLogger().writeCurrent(" -- " + timer.end().asString());
        }
        if (q.couldAffectSchema()) {
            updateSchema();
        }
    }
}
