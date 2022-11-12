package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.*;

import java.util.ArrayList;
import java.util.List;

public class Symtab implements ICypherSymtab {

    private final IClauseAnalyzer parentClause;
    private boolean extendParent = false;

    private List<IPattern> patterns = new ArrayList<>();
    private List<IRet> aliasDefinitions = new ArrayList<>();


    public Symtab(IClauseAnalyzer parentClause, boolean extendParent){
        this.parentClause = parentClause;
        this.extendParent = extendParent;
    }


    @Override
    public List<IPattern> getPatterns() {
        return patterns;
    }

    @Override
    public void setPatterns(List<IPattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public List<IRet> getAliasDefinitions() {
        return aliasDefinitions;
    }

    @Override
    public void setAliasDefinition(List<IRet> aliasDefinitions) {
        this.aliasDefinitions = aliasDefinitions;
    }

    @Override
    public List<IAliasAnalyzer> getLocalAliasDefs() {
        if(parentClause == null){
            return new ArrayList<>();
        }
        List<IAliasAnalyzer> aliases = new ArrayList<>();
        if(parentClause instanceof IWith || parentClause instanceof IReturn || parentClause instanceof IUnwind){
            for(IRet aliasDef : aliasDefinitions){
                if(aliasDef.isAlias()){
                    aliases.add(new AliasAnalyzer((IAlias) aliasDef.getIdentifier(),
                            new ContextInfo(parentClause,parentClause.getExtendableIdentifiers())));
                }
                if(aliasDef.isAll()){
                    if(parentClause.getPrevClause() == null){
                        return new ArrayList<>();
                    }
                    return parentClause.getPrevClause().toAnalyzer().getAvailableAliases();
                }
            }
            linkAliasDefinitions(aliases);
        }
        List<IAliasAnalyzer> result = new ArrayList<>();
        result.addAll(aliases);
        return result;
    }

    @Override
    public List<IAliasAnalyzer> getAvailableAliasDefs() {
        if(!extendParent || parentClause.getPrevClause() == null){
            return getLocalAliasDefs();
        }
        List<IAliasAnalyzer> aliases = getLocalAliasDefs();
        for(IAliasAnalyzer alias : aliases){
            alias.setFormerDef(null);
        }
        List<IAliasAnalyzer> extendedAliases = parentClause.getPrevClause().toAnalyzer().getAvailableAliases();
        for(IAliasAnalyzer extendedAlias : extendedAliases){
            if(!aliases.contains(extendedAlias)){
                aliases.add(extendedAlias);
            }
            else{
                IAliasAnalyzer alias = aliases.get(aliases.indexOf(extendedAlias));
                if(alias.getExpression() == null){
                    alias.setFormerDef(extendedAlias);
                }
            }
        }
        return aliases;
    }


    private void linkNodeDefinitions(List<INodeAnalyzer> curNodes){
        if(parentClause == null){
            return;
        }

        List<INodeAnalyzer> prevDefs = new ArrayList<>();
        if(parentClause.getPrevClause() != null){
            prevDefs = parentClause.getPrevClause().toAnalyzer().getAvailableNodeIdentifiers();
        }

        for(INodeAnalyzer node : curNodes){
            node.setFormerDef(null);
        }
        for(INodeAnalyzer prevDef : prevDefs){
            if(curNodes.contains(prevDef)){
                INodeAnalyzer node = curNodes.get(curNodes.indexOf(prevDef));
                node.setFormerDef(prevDef);
            }
        }
    }

    private void linkRelationDefinitions(List<IRelationAnalyzer> curRelations){
        if(parentClause == null){
            return;
        }

        List<IRelationAnalyzer> prevDefs = new ArrayList<>();
        if(parentClause.getPrevClause() != null){
            prevDefs = parentClause.getPrevClause().toAnalyzer().getAvailableRelationIdentifiers();
        }

        for(IRelationAnalyzer relation : curRelations){
            relation.setFormerDef(null);
        }
        for(IRelationAnalyzer prevDef : prevDefs){
            if(curRelations.contains(prevDef)){
                IRelationAnalyzer relation = curRelations.get(curRelations.indexOf(prevDef));
                relation.setFormerDef(prevDef);
            }
        }
    }

    private void linkAliasDefinitions(List<IAliasAnalyzer> curAlias){
        if(parentClause == null){
            return;
        }

        List<IAliasAnalyzer> prevDefs = new ArrayList<>();
        if(parentClause.getPrevClause() != null){
            prevDefs = parentClause.getPrevClause().toAnalyzer().getAvailableAliases();
        }

        for(IAliasAnalyzer alias : curAlias){
            alias.setFormerDef(null);
        }
        for(IAliasAnalyzer prevDef : prevDefs){
            if(curAlias.contains(prevDef)){
                IAliasAnalyzer alias = curAlias.get(curAlias.indexOf(prevDef));
                if(alias.getExpression() == null){
                    alias.setFormerDef(prevDef);
                }
            }
        }
    }

    @Override
    public List<INodeAnalyzer> getLocalNodePatterns() {
        if(parentClause == null)
            return null;

        List<INodeAnalyzer> nodes = new ArrayList<>();


        if(parentClause instanceof IMatch){
            for(IPattern pattern : patterns){
                for(IPatternElement patternElement : pattern.getPatternElements()){
                    if(patternElement instanceof INodeIdentifier && !patternElement.isAnonymous()){
                        nodes.add(new NodeAnalyzer((INodeIdentifier) patternElement,
                                new ContextInfo(parentClause, parentClause.getExtendableIdentifiers())));
                    }
                }
            }
            linkNodeDefinitions(nodes);
            return nodes;
        }


        if(parentClause instanceof IWith || parentClause instanceof IReturn){
            for(IRet aliasDef : aliasDefinitions){
                if(aliasDef.isNodeIdentifier()){
                    nodes.add(new NodeAnalyzer((INodeIdentifier) aliasDef.getIdentifier(),
                            new ContextInfo(parentClause, parentClause.getExtendableIdentifiers())));
                }
                if(aliasDef.isAll() && parentClause.getPrevClause() != null){
                    return parentClause.getPrevClause().toAnalyzer().getAvailableNodeIdentifiers();
                }
            }
            linkNodeDefinitions(nodes);
        }

        return nodes;
    }

    @Override
    public List<IRelationAnalyzer> getLocalRelationPatterns() {
        if(parentClause == null)
            return null;

        List<IRelationAnalyzer> relations = new ArrayList<>();

        List<IRelationAnalyzer> extendedRelations = new ArrayList<>();
        if(parentClause.getPrevClause() != null){
            extendedRelations = parentClause.getPrevClause().toAnalyzer().getAvailableRelationIdentifiers();
        }

        if(parentClause instanceof IMatch){
            for(IPattern pattern : patterns){
                for(IPatternElement patternElement : pattern.getPatternElements()){
                    if(patternElement instanceof IRelationIdentifier && !patternElement.isAnonymous()){
                        relations.add(new RelationAnalyzer((IRelationIdentifier) patternElement,
                                new ContextInfo(parentClause, parentClause.getExtendableIdentifiers())));
                    }
                }
            }
            linkRelationDefinitions(relations);
            return relations;
        }

        if(parentClause instanceof IWith || parentClause instanceof IReturn){
            for(IRet aliasDef : aliasDefinitions){
                if(aliasDef.isRelationIdentifier()){
                    relations.add(new RelationAnalyzer((IRelationIdentifier) aliasDef.getIdentifier(),
                            new ContextInfo(parentClause, parentClause.getExtendableIdentifiers())));
                }
                if(aliasDef.isAll() && parentClause.getPrevClause() != null){
                    return parentClause.getPrevClause().toAnalyzer().getAvailableRelationIdentifiers();
                }
            }
            linkRelationDefinitions(relations);
        }

        return relations;


    }

    @Override
    public List<INodeAnalyzer> getAvailableNodePatterns() {
        if(!extendParent || parentClause.getPrevClause() == null){
            return getLocalNodePatterns();
        }
        List<INodeAnalyzer> nodes = getLocalNodePatterns();
        for(INodeAnalyzer node : nodes){
            node.setFormerDef(null);
        }
        List<INodeAnalyzer> extendedNodes = parentClause.getPrevClause().toAnalyzer().getAvailableNodeIdentifiers();
        for(INodeAnalyzer extendedNode : extendedNodes){
            boolean containing = nodes.contains(extendedNode);
            boolean containing2 = nodes.contains(extendedNode);
            if(!containing){
                nodes.add(extendedNode);
            }
            else {
                boolean containing3 = nodes.contains(extendedNode);
                INodeAnalyzer node = nodes.get(nodes.indexOf(extendedNode));
                node.setFormerDef(extendedNode);
            }
        }
        return nodes;
    }

    @Override
    public List<IRelationAnalyzer> getAvailableRelationPatterns() {
        if(!extendParent || parentClause.getPrevClause() == null){
            return getLocalRelationPatterns();
        }
        List<IRelationAnalyzer> relations = getLocalRelationPatterns();
        for(IRelationAnalyzer relation : relations){
            relation.setFormerDef(null);
        }
        List<IRelationAnalyzer> extendedRelations = parentClause.getPrevClause().toAnalyzer().getAvailableRelationIdentifiers();
        for(IRelationAnalyzer extendedRelation : extendedRelations){
            if(!relations.contains(extendedRelation)){
                relations.add(extendedRelation);
            }
            else {
                IRelationAnalyzer relationPattern = relations.get(relations.indexOf(extendedRelation));
                relationPattern.setFormerDef(extendedRelation);
            }
        }
        return relations;
    }

    @Override
    public List<IIdentifierAnalyzer> getLocalIdentifiers() {
        List<IIdentifierAnalyzer> identifierAnalyzers = new ArrayList<>();
        identifierAnalyzers.addAll(getLocalNodePatterns());
        identifierAnalyzers.addAll(getLocalRelationPatterns());
        identifierAnalyzers.addAll(getLocalAliasDefs());
        return identifierAnalyzers;
    }

    @Override
    public List<IIdentifierAnalyzer> getAvailableIdentifiers() {
        List<IIdentifierAnalyzer> identifierAnalyzers = new ArrayList<>();
        identifierAnalyzers.addAll(getAvailableNodePatterns());
        identifierAnalyzers.addAll(getAvailableRelationPatterns());
        identifierAnalyzers.addAll(getAvailableAliasDefs());
        return identifierAnalyzers;
    }

}
