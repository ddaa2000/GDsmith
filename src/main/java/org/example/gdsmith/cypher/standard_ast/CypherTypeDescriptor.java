package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.ICypherType;
import org.example.gdsmith.cypher.ast.analyzer.*;
import org.example.gdsmith.cypher.ast.analyzer.*;

public class CypherTypeDescriptor implements ICypherTypeDescriptor {

    private ICypherType type;
    private IMapDescriptor mapDescriptor = null;
    private IListDescriptor listDescriptor = null;
    private INodeAnalyzer nodeAnalyzer = null;
    private IRelationAnalyzer relationAnalyzer = null;

    public CypherTypeDescriptor(ICypherType type){
        this.type = type;
    }

    public CypherTypeDescriptor(INodeAnalyzer nodeAnalyzer){
        this.nodeAnalyzer = nodeAnalyzer;
        type = CypherType.NODE;
    }

    public CypherTypeDescriptor(IRelationAnalyzer relationAnalyzer){
        this.relationAnalyzer = relationAnalyzer;
        type = CypherType.RELATION;
    }

    public CypherTypeDescriptor(IMapDescriptor mapDescriptor){
        this.mapDescriptor = mapDescriptor;
        type = CypherType.MAP;
    }

    public CypherTypeDescriptor(IListDescriptor listDescriptor){
        this.listDescriptor = listDescriptor;
        type = CypherType.LIST;
    }

    @Override
    public ICypherType getType() {
        return type;
    }

    @Override
    public boolean isBasicType() {
        return !isNodeOrRelation() && !isMap() && !isList() && type != CypherType.UNKNOWN;
    }

    @Override
    public boolean isNodeOrRelation() {
        return type == CypherType.NODE || type == CypherType.RELATION;
    }

    @Override
    public boolean isNode() {
        return type == CypherType.NODE;
    }

    @Override
    public boolean isRelation() {
        return type == CypherType.RELATION;
    }

    @Override
    public boolean isList() {
        return type == CypherType.LIST;
    }

    @Override
    public boolean isMap() {
        return type == CypherType.MAP;
    }

    @Override
    public IListDescriptor getListDescriptor() {
        return listDescriptor;
    }

    @Override
    public IMapDescriptor getMapDescriptor() {
        return mapDescriptor;
    }

    @Override
    public INodeAnalyzer getNodeAnalyzer() {
        return nodeAnalyzer;
    }

    @Override
    public IRelationAnalyzer getRelationAnalyzer() {
        return relationAnalyzer;
    }
}
