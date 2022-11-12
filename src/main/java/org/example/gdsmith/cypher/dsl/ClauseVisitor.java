package org.example.gdsmith.cypher.dsl;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.*;

import java.util.List;

public abstract class ClauseVisitor<C extends IContext> {

    protected IClauseSequence clauseSequence;
    private C context;
    private boolean continueVisit = true;
    private int presentIndex = 0;

    public ClauseVisitor(IClauseSequence clauseSequence, C context){
        this.clauseSequence = clauseSequence;
        this.context = context;
    }

    public void startVisit(){
        if(this.clauseSequence.getClauseList() == null || this.clauseSequence.getClauseList().size() == 0){
            return;
        }
        List<ICypherClause> clauses = this.clauseSequence.getClauseList();
        for(int i = 0; i < clauses.size(); i++){
            presentIndex = i;
            visitClause(clauses.get(i));
            if(!continueVisit){
                postProcessing(context);
                return;
            }
        }
        postProcessing(context);
    }

    public void postProcessing(C context){

    }

    public void reverseVisit(){
        if(this.clauseSequence.getClauseList() == null || this.clauseSequence.getClauseList().size() == 0){
            return;
        }
        List<ICypherClause> clauses = this.clauseSequence.getClauseList();
        for(int i = clauses.size() - 1; i >= 0; i--){
            presentIndex = i;
            visitClause(clauses.get(i));
            if(!continueVisit){
                postProcessing(context);
                return;
            }
        }
        postProcessing(context);
    }

    public IClauseSequence getClauseSequence(){
        return clauseSequence;
    }

    protected int getPresentIndex(){
        return presentIndex;
    }

    protected void stopVisit(){
        continueVisit = false;
    }

    public void visitClause(ICypherClause clause){
        if(clause instanceof IMatch){
            visitMatch((IMatch) clause, context);
        }
        else if(clause instanceof IWith){
            visitWith((IWith) clause, context);
        }
        else if(clause instanceof ICreate){
            visitCreate((ICreate) clause, context);
        }
        else if(clause instanceof IReturn){
            visitReturn((IReturn) clause, context);
        }
        else if(clause instanceof IUnwind){
            visitUnwind((IUnwind) clause, context);
        }
    }

    public void visitMatch(IMatch matchClause, C context){}
    public void visitWith(IWith withClause, C context){}
    public void visitReturn(IReturn returnClause, C context){}
    public void visitCreate(ICreate createClause, C context){}
    public void visitUnwind(IUnwind unwindClause, C context){}
    public void visitMerge(IMerge mergeClause, C context){}

}
