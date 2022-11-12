package org.example.gdsmith;

import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.common.query.Query;
import org.example.gdsmith.common.schema.AbstractSchema;
import org.example.gdsmith.common.schema.AbstractTable;

import java.util.List;

public abstract class GlobalState<O extends DBMSSpecificOptions<?>, S extends AbstractSchema<?, ?>, C extends GDSmithDBConnection> {

    protected C databaseConnection;
    private Randomly r;
    private MainOptions options;
    private O dbmsSpecificOptions;
    private S schema;
    private Main.StateLogger logger;
    private StateToReproduce state;
    private Main.QueryManager<C> manager;
    private String databaseName;

    public void setConnection(C con) {
        this.databaseConnection = con;
    }

    public C getConnection() {
        return databaseConnection;
    }

    @SuppressWarnings("unchecked")
    public void setDbmsSpecificOptions(Object dbmsSpecificOptions) {
        this.dbmsSpecificOptions = (O) dbmsSpecificOptions;
    }

    public O getDbmsSpecificOptions() {
        return dbmsSpecificOptions;
    }

    public void setRandomly(Randomly r) {
        this.r = r;
    }

    public Randomly getRandomly() {
        return r;
    }

    public MainOptions getOptions() {
        return options;
    }

    public void setMainOptions(MainOptions options) {
        this.options = options;
    }

    public void setStateLogger(Main.StateLogger logger) {
        this.logger = logger;
    }

    public Main.StateLogger getLogger() {
        return logger;
    }

    public void setState(StateToReproduce state) {
        this.state = state;
    }

    public StateToReproduce getState() {
        return state;
    }

    public Main.QueryManager<C> getManager() {
        return manager;
    }

    public void setManager(Main.QueryManager<C> manager) {
        this.manager = manager;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    private ExecutionTimer executePrologue(Query<?> q) throws Exception {
        boolean logExecutionTime = getOptions().logExecutionTime();
        ExecutionTimer timer = null;
        if (logExecutionTime) {
            timer = new ExecutionTimer().start();
        }
        if (getOptions().printAllStatements()) {
            System.out.println(q.getLogString());
        }
        if (getOptions().logEachSelect()) {
            if (logExecutionTime) {
                getLogger().writeCurrentNoLineBreak(q.getLogString());
            } else {
                getLogger().writeCurrent(q.getLogString());
            }
        }
        return timer;
    }

    protected abstract void executeEpilogue(Query<?> q, boolean success, ExecutionTimer timer) throws Exception;

    public boolean executeStatement(Query<C> q, String... fills) throws Exception {
        ExecutionTimer timer = executePrologue(q);
        boolean success = manager.execute(q, fills);
        executeEpilogue(q, success, timer);
        return success;
    }

    public List<GDSmithResultSet> executeStatementAndGet(Query<C> q, String... fills) throws Exception {
        ExecutionTimer timer = executePrologue(q);
        List<GDSmithResultSet> results = manager.executeAndGet(q, fills);
        /*boolean success = results != null || results.stream().anyMatch(r->r==null);
        if (success) {
            results.forEach(result->result.registerEpilogue(() -> {
                try {
                    executeEpilogue(q, success, timer);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }));
        }*/
        boolean success = results != null;
        try {
            executeEpilogue(q, success, timer);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return results;
    }

    public List<Long> executeStatementAndGetTime(Query<C> q, String... fills) throws Exception {
        ExecutionTimer timer = executePrologue(q);
        List<Long> results = manager.executeAndGetTime(q, fills);
        boolean success = results != null;
        try {
            executeEpilogue(q, success, timer);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return results;
    }

    public S getSchema() {
        if (schema == null) { // use schema
            try {
                updateSchema();
            } catch (Exception e) {
                throw new AssertionError();
            }
        }
        return schema;
    }

    protected void setSchema(S schema) {
        this.schema = schema;
    } //use schema

    public void updateSchema() throws Exception {
        setSchema(readSchema()); //use schema
        for (AbstractTable<?, ?, ?> table : schema.getDatabaseTables()) { //use table todo 这里可能会引发问题
            table.recomputeCount();
        }
    }

    protected abstract S readSchema() throws Exception;

}
