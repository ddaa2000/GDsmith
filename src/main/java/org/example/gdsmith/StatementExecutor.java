package org.example.gdsmith;

import java.util.ArrayList;
import java.util.List;

import org.example.gdsmith.common.query.Query;

public class StatementExecutor<G extends GlobalState<?, ?, ?>, A extends AbstractAction<G>> {

    private final G globalState;
    private final A[] actions;
    private final ActionMapper<G, A> mapping;
    private final AfterQueryAction queryConsumer;

    @FunctionalInterface
    public interface AfterQueryAction {
        void notify(Query<?> q) throws Exception;
    }

    @FunctionalInterface
    public interface ActionMapper<T, A> {
        int map(T globalState, A action);
    }

    public StatementExecutor(G globalState, A[] actions, ActionMapper<G, A> mapping, AfterQueryAction queryConsumer) {
        this.globalState = globalState;
        this.actions = actions.clone();
        this.mapping = mapping;
        this.queryConsumer = queryConsumer;
    }

    @SuppressWarnings("unchecked")
    public void executeStatements() throws Exception { //todo ?
        Randomly r = globalState.getRandomly();
        int[] nrRemaining = new int[actions.length];
        List<A> availableActions = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < actions.length; i++) {
            A action = actions[i];
            int nrPerformed = mapping.map(globalState, action);//这是啥意思
            if (nrPerformed != 0) {
                availableActions.add(action);//这也没用过呀
            }
            nrRemaining[i] = nrPerformed;
            total += nrPerformed;
        }
        while (total != 0) {
            A nextAction = null;
            int selection = r.getInteger(0, total);
            int previousRange = 0;
            int i;
            for (i = 0; i < nrRemaining.length; i++) {
                if (previousRange <= selection && selection < previousRange + nrRemaining[i]) {
                    nextAction = actions[i];
                    break;
                } else {
                    previousRange += nrRemaining[i];
                }
            }
            assert nextAction != null;
            assert nrRemaining[i] > 0;
            nrRemaining[i]--;
            @SuppressWarnings("rawtypes")
            Query query = null;
            try {
                boolean success;
                int nrTries = 0;
                do {
                    query = nextAction.getQuery(globalState); //获取query语句
                    success = globalState.executeStatement(query); //check是否执行成功
                } while (nextAction.canBeRetried() && !success
                        && nrTries++ < globalState.getOptions().getNrStatementRetryCount());
            } catch (IgnoreMeException e) {

            }
            if (query != null && query.couldAffectSchema()) {
                globalState.updateSchema(); //应该是同步更新globalState记载的状态？
                queryConsumer.notify(query); //todo ？
            }
            total--;
        }
    }
}
