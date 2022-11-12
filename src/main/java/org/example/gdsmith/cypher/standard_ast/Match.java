package org.example.gdsmith.cypher.standard_ast;


import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.IMatchAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Match extends CypherClause implements IMatchAnalyzer {
    private boolean isOptional = false;
    private IExpression condition = null;

    public Match(){
        super(true);
    }

    @Override
    public List<IPattern> getPatternTuple() {
        return symtab.getPatterns();
    }

    @Override
    public void setPatternTuple(List<IPattern> patternTuple) {
        //符号表同步更新
        symtab.setPatterns(patternTuple);
    }

    @Override
    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public void setOptional(boolean optional) {
        this.isOptional = optional;
    }

    @Override
    public IExpression getCondition() {
        return condition;
    }

    @Override
    public void setCondition(IExpression condition) {
        this.condition = condition;
    }

    @Override
    public IMatchAnalyzer toAnalyzer() {
        return this;
    }

    @Override
    public ICypherClause getCopy() {
        Match match = new Match();
        match.isOptional = isOptional;
        if(symtab != null){
            match.symtab.setPatterns(symtab.getPatterns().stream().map(p->p.getCopy()).collect(Collectors.toList()));
            match.symtab.setAliasDefinition(symtab.getAliasDefinitions().stream().map(a->a.getCopy()).collect(Collectors.toList()));
        }
        if(condition != null){
            match.condition = condition.getCopy();
        }
        else {
            match.condition = null;
        }
        return match;
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        if(isOptional()){
            sb.append("OPTIONAL ");
        }
        sb.append("MATCH ");
        List<IPattern> patterns = getPatternTuple();
        List<INodeIdentifier> nodePatterns = new ArrayList<>();
        List<IRelationIdentifier> relationPatterns = new ArrayList<>();

        for(int i = 0; i < patterns.size(); i++){
            IPattern pattern = patterns.get(i);
            pattern.toTextRepresentation(sb);
            if(i != patterns.size() - 1){
                sb.append(", ");
            }
        }
        if(condition != null){
            sb.append(" WHERE ");
            condition.toTextRepresentation(sb);
        }
    }

    @Override
    public List<IPattern> getLocalPatternContainsIdentifier(IIdentifier identifier) {
        List<IPattern> patterns = symtab.getPatterns();
        List<IPattern> result = new ArrayList<>();
        for(IPattern pattern: patterns){
            for(IPatternElement element: pattern.getPatternElements()){
                if(element.equals(identifier)){
                    result.add(pattern);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public IMatch getSource() {
        return this;
    }
}
