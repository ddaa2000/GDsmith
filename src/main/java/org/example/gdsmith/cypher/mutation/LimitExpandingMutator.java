package org.example.gdsmith.cypher.mutation;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.ast.ICypherClause;
import org.example.gdsmith.cypher.ast.IReturn;
import org.example.gdsmith.cypher.ast.IWith;
import org.example.gdsmith.cypher.dsl.ClauseVisitor;
import org.example.gdsmith.cypher.dsl.IContext;
import org.example.gdsmith.cypher.mutation.LimitExpandingMutator.LimitExpandingMutatorContext;

import java.util.ArrayList;
import java.util.List;

public class LimitExpandingMutator extends ClauseVisitor<LimitExpandingMutatorContext> implements IClauseMutator {

    public List<ICypherClause> clausesWithLimit = new ArrayList<>();

    public LimitExpandingMutator(IClauseSequence clauseSequence) {
        super(clauseSequence, new LimitExpandingMutatorContext());
    }

    @Override
    public void mutate() {
        startVisit();
    }

    public static class LimitExpandingMutatorContext implements IContext {

    }

    @Override
    public void visitWith(IWith withClause, LimitExpandingMutatorContext context) {
        if(withClause.getLimit() != null){
            clausesWithLimit.add(withClause);
        }

    }

    @Override
    public void visitReturn(IReturn returnClause, LimitExpandingMutatorContext context) {
        if (returnClause.getLimit() != null) {
            clausesWithLimit.add(returnClause);
        }
    }

    @Override
    public void postProcessing(LimitExpandingMutatorContext context) {
        if(clausesWithLimit.size() == 0){
            return;
        }

        ICypherClause clause = clausesWithLimit.get(new Randomly().getInteger(0, clausesWithLimit.size()));

        if(clause instanceof IWith){
            ((IWith) clause).setLimit(null);
            return;
        }
        if(clause instanceof IReturn){
            ((IReturn) clause).setLimit(null);
            return;
        }

        throw new RuntimeException();
    }
}
