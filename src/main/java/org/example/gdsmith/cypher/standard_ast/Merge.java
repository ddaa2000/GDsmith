package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.IMergeAnalyzer;
import org.example.gdsmith.cypher.ast.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Merge extends CypherClause implements IMergeAnalyzer {

    public Merge(){
        super(true);
    }

    @Override
    public IPattern getPattern() {
        return symtab.getPatterns().get(0);
    }

    @Override
    public void setPattern(IPattern pattern) {
        //符号表同步更新
        symtab.setPatterns(Arrays.asList(pattern));
    }

    @Override
    public IMergeAnalyzer toAnalyzer() {
        return this;
    }

    @Override
    public ICypherClause getCopy() {
        Merge create = new Merge();
        if(symtab != null){
            create.symtab.setPatterns(symtab.getPatterns().stream().map(p->p.getCopy()).collect(Collectors.toList()));
            create.symtab.setAliasDefinition(symtab.getAliasDefinitions().stream().map(a->a.getCopy()).collect(Collectors.toList()));
        }
        return create;
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append("MERGE ");
        List<INodeIdentifier> nodePatterns = new ArrayList<>();
        List<IRelationIdentifier> relationPatterns = new ArrayList<>();
        getPattern().toTextRepresentation(sb);
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
    public IMerge getSource() {
        return this;
    }
}
