package org.example.gdsmith.cypher.mutation;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.ast.IMatch;
import org.example.gdsmith.cypher.dsl.ClauseVisitor;
import org.example.gdsmith.cypher.dsl.IContext;
import org.example.gdsmith.cypher.mutation.OptionalRemovalMutator.OptionalRemovalMutatorContext;

import java.util.ArrayList;
import java.util.List;

public class OptionalRemovalMutator extends ClauseVisitor<OptionalRemovalMutatorContext> implements IClauseMutator  {

    public List<IMatch> matchList = new ArrayList<>();

    public OptionalRemovalMutator(IClauseSequence clauseSequence) {
        super(clauseSequence, new OptionalRemovalMutatorContext());
    }

    @Override
    public void mutate() {
        startVisit();
    }

    public static class OptionalRemovalMutatorContext implements IContext {

    }

    @Override

    public void visitMatch(IMatch matchClause, OptionalRemovalMutatorContext context) {
        if(matchClause.isOptional()){
            matchList.add(matchClause);
        }
    }

    @Override
    public void postProcessing(OptionalRemovalMutatorContext context) {
        if(matchList.size() == 0){
            return;
        }

        IMatch match = matchList.get(new Randomly().getInteger(0, matchList.size()));
        match.setOptional(false);
    }
}
