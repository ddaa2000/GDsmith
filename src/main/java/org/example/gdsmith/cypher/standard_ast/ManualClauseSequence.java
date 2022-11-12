package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.ast.ICypherClause;
import org.example.gdsmith.cypher.dsl.IIdentifierBuilder;

import java.util.List;

public class ManualClauseSequence implements IClauseSequence {

    private String query;

    public ManualClauseSequence(String query){
        this.query = query;
    }

    @Override
    public List<ICypherClause> getClauseList() {
        return null;
    }

    @Override
    public IIdentifierBuilder getIdentifierBuilder() {
        return null;
    }

    @Override
    public void setClauseList(List<ICypherClause> clauses) {

    }

    @Override
    public void addClause(ICypherClause clause) {

    }

    @Override
    public void addClauseAt(ICypherClause clause, int index) {

    }

    @Override
    public IClauseSequence getCopy() {
        return null;
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append(query);
    }
}
