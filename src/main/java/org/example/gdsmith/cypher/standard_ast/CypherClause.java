package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.ICypherClause;
import org.example.gdsmith.cypher.ast.IIdentifier;
import org.example.gdsmith.cypher.ast.analyzer.*;
import org.example.gdsmith.cypher.ast.analyzer.*;

import java.util.ArrayList;
import java.util.List;

public abstract class CypherClause implements IClauseAnalyzer {
    protected final Symtab symtab;
    protected ICypherClause nextClause = null, prevClause = null;

    public CypherClause(boolean extendParent){
        symtab = new Symtab(this, extendParent);
    }

    @Override
    public void setNextClause(ICypherClause next) {
        this.nextClause = next;
        if(next != null) {
            next.setPrevClause(this);
        }
    }

    @Override
    public ICypherClause getNextClause() {
        return nextClause;
    }

    @Override
    public void setPrevClause(ICypherClause prev) {
        this.prevClause = prev;
    }

    @Override
    public ICypherClause getPrevClause() {
        return this.prevClause;
    }

    @Override
    public List<IAliasAnalyzer> getLocalAliases() {
        return symtab.getLocalAliasDefs();
    }

    @Override
    public List<INodeAnalyzer> getLocalNodeIdentifiers() {
        return symtab.getLocalNodePatterns();
    }

    @Override
    public List<IRelationAnalyzer> getLocalRelationIdentifiers() {
        return symtab.getLocalRelationPatterns();
    }

    @Override
    public List<IAliasAnalyzer> getAvailableAliases() {
        return symtab.getAvailableAliasDefs();
    }

    @Override
    public List<INodeAnalyzer> getAvailableNodeIdentifiers() {
        return symtab.getAvailableNodePatterns();
    }

    @Override
    public List<IRelationAnalyzer> getAvailableRelationIdentifiers() {
        return symtab.getAvailableRelationPatterns();
    }

    @Override
    public List<IAliasAnalyzer> getExtendableAliases() {
        if(prevClause == null)
            return new ArrayList<>();
        return prevClause.toAnalyzer().getAvailableAliases();
    }

    @Override
    public List<INodeAnalyzer> getExtendableNodeIdentifiers() {
        if(prevClause == null)
            return new ArrayList<>();
        return prevClause.toAnalyzer().getAvailableNodeIdentifiers();
    }

    @Override
    public List<IRelationAnalyzer> getExtendableRelationIdentifiers() {
        if(prevClause == null)
            return new ArrayList<>();
        return prevClause.toAnalyzer().getAvailableRelationIdentifiers();
    }

    @Override
    public List<IIdentifierAnalyzer> getAvailableIdentifiers(){
        List<IIdentifierAnalyzer> identifierAnalyzers = new ArrayList<>();
        identifierAnalyzers.addAll(getAvailableNodeIdentifiers());
        identifierAnalyzers.addAll(getAvailableRelationIdentifiers());
        identifierAnalyzers.addAll(getAvailableAliases());
        return identifierAnalyzers;
    }

    @Override
    public List<IIdentifierAnalyzer> getLocalIdentifiers(){
        List<IIdentifierAnalyzer> identifierAnalyzers = new ArrayList<>();
        identifierAnalyzers.addAll(getLocalNodeIdentifiers());
        identifierAnalyzers.addAll(getLocalRelationIdentifiers());
        identifierAnalyzers.addAll(getLocalAliases());
        return identifierAnalyzers;
    }

    @Override
    public List<IIdentifierAnalyzer> getExtendableIdentifiers(){
        List<IIdentifierAnalyzer> identifierAnalyzers = new ArrayList<>();
        identifierAnalyzers.addAll(getExtendableNodeIdentifiers());
        identifierAnalyzers.addAll(getExtendableRelationIdentifiers());
        identifierAnalyzers.addAll(getExtendableAliases());
        return identifierAnalyzers;
    }

    @Override
    public IIdentifierAnalyzer getIdentifierAnalyzer(String name){
        List<IIdentifierAnalyzer> identifierAnalyzers = getAvailableIdentifiers();
        for(IIdentifierAnalyzer identifierAnalyzer: identifierAnalyzers){
            if(identifierAnalyzer.getName().equals(name)){
                return identifierAnalyzer;
            }
        }
        return null;
    }

    @Override
    public IIdentifierAnalyzer getIdentifierAnalyzer(IIdentifier identifier){
        return getIdentifierAnalyzer(identifier.getName());
    }

}
