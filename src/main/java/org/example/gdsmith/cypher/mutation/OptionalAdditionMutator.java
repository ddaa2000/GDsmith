package org.example.gdsmith.cypher.mutation;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.ast.IMatch;
import org.example.gdsmith.cypher.dsl.ClauseVisitor;
import org.example.gdsmith.cypher.dsl.IContext;
import org.example.gdsmith.cypher.mutation.OptionalAdditionMutator.OptionalAdditionMutatorContext;

import java.util.ArrayList;
import java.util.List;

public class OptionalAdditionMutator extends ClauseVisitor<OptionalAdditionMutatorContext> implements IClauseMutator  {

    public List<IMatch> matchList = new ArrayList<>();

    public OptionalAdditionMutator(IClauseSequence clauseSequence) {
        super(clauseSequence, new OptionalAdditionMutatorContext());
    }

    @Override
    public void mutate() {
        startVisit();
    }

    public static class OptionalAdditionMutatorContext implements IContext {

    }

    @Override
    public void visitMatch(IMatch matchClause, OptionalAdditionMutatorContext context) {
        if(!matchClause.isOptional()){
            matchList.add(matchClause);
        }
    }

    @Override
    public void postProcessing(OptionalAdditionMutatorContext context) {
        if(matchList.size() == 0){
            return;
        }

        IMatch match = matchList.get(new Randomly().getInteger(0, matchList.size()));
        match.setOptional(true);
    }
}
