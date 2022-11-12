package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.IAlias;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.IAliasAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IContextInfo;
import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;

public class AliasAnalyzer extends Alias implements IAliasAnalyzer {
    IAliasAnalyzer formerDef = null;
    IContextInfo contextInfo;
    IExpression sourceExpression; //在AST中所属的expression
    IAlias source;

    AliasAnalyzer(IAlias alias, IContextInfo contextInfo){
        this(alias, contextInfo, null);
    }

    AliasAnalyzer(IAlias alias, IContextInfo contextInfo, IExpression sourceExpression){
        super(alias.getName(), alias.getExpression());
        this.source = alias;
        this.contextInfo = contextInfo;
        this.sourceExpression = sourceExpression;
    }


    @Override
    public IAliasAnalyzer getFormerDef() {
        return formerDef;
    }

    @Override
    public void setFormerDef(IAliasAnalyzer formerDef) {
        this.formerDef = formerDef;
    }

    @Override
    public IExpression getAliasDefExpression() {
        if(formerDef == null){
            return this.expression;
        }
        return formerDef.getAliasDefExpression();
    }

    @Override
    public IAlias getSource() {
        return source;
    }

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema cypherSchema) {
        IExpression expression = getAliasDefExpression();
        if(expression != null){
            return expression.analyzeType(cypherSchema, contextInfo.getIdentifiers());
        }
        return new CypherTypeDescriptor(CypherType.UNKNOWN);
    }


    @Override
    public IExpression getSourceRefExpression() {
        return sourceExpression;
    }

    @Override
    public IContextInfo getContextInfo() {
        return contextInfo;
    }
}
