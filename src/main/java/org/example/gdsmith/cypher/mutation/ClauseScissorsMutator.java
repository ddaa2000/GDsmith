package org.example.gdsmith.cypher.mutation;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.dsl.ClauseVisitor;
import org.example.gdsmith.cypher.dsl.IContext;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.mutation.ClauseScissorsMutator.ClauseScissorsMutatorContext;
import org.example.gdsmith.cypher.standard_ast.Return;

import java.util.List;
import java.util.stream.Collectors;

public class ClauseScissorsMutator <S extends CypherSchema<?,?>> extends ClauseVisitor<ClauseScissorsMutatorContext<S>> implements IClauseMutator {

    Randomly randomly;

    public ClauseScissorsMutator(IClauseSequence clauseSequence) {
        super(clauseSequence, new ClauseScissorsMutatorContext<>());
        randomly = new Randomly();
    }

    @Override
    public void visitMatch(IMatch matchClause, ClauseScissorsMutatorContext<S> context) {
        return;
    }

    @Override
    public void visitWith(IWith withClause, ClauseScissorsMutatorContext<S> context) {
        if(randomly.getInteger(0, 100) < 50){
            int presentInedx = getPresentIndex();
            if(presentInedx != 0){
                Return returnClause = new Return();
                returnClause.setReturnList(withClause.getReturnList().stream().map(r->r.getCopy()).collect(Collectors.toList()));
                List<ICypherClause> clauses = getClauseSequence().getClauseList();
                clauses.get(presentInedx).setPrevClause(null);
                clauses.get(presentInedx - 1).setNextClause(returnClause);
                clauses.set(presentInedx, returnClause);
                getClauseSequence().setClauseList(clauses.subList(0, presentInedx + 1));
                stopVisit();
                return;
            }
        }
    }

    public void mutate(){
        reverseVisit();
    }

    @Override
    public void visitReturn(IReturn returnClause, ClauseScissorsMutatorContext<S> context) {
        return;
    }

    @Override
    public void visitCreate(ICreate createClause, ClauseScissorsMutatorContext<S> context) {
        return;
    }

    @Override
    public void visitUnwind(IUnwind unwindClause, ClauseScissorsMutatorContext<S> context) {
        return;
    }

    public static class ClauseScissorsMutatorContext<S extends CypherSchema<?,?>> implements IContext {
        private ClauseScissorsMutatorContext(){

        }

    }
}
