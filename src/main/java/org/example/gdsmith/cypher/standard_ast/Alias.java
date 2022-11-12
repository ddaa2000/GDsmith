package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.dsl.IIdentifierBuilder;
import org.example.gdsmith.cypher.ast.IAlias;
import org.example.gdsmith.cypher.ast.ICypherType;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.IIdentifier;

public class Alias implements IAlias {
    protected String name;
    protected IExpression expression;

    public static Alias createIdentifierRef(IIdentifier alias){
        return new Alias(alias.getName(), null);
    }

    public static Alias createExpressionAlias(IExpression expression, IIdentifierBuilder identifierBuilder){
        return new Alias(identifierBuilder.getNewAliasName(), expression);
    }

    Alias(String name, IExpression expression){
        this.name = name;
        this.expression = expression;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ICypherType getType() {
        return CypherType.UNKNOWN;
    }

    @Override
    public IIdentifier getCopy() {
        Alias alias;
        if(expression != null){
            alias = new Alias(name, expression.getCopy());
        }
        else {
            alias = new Alias(name, null);
        }
        return alias;
    }

    @Override
    public IExpression getExpression() {
        return expression;
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        if(expression != null){
            expression.toTextRepresentation(sb);
            sb.append(" AS ");
        }
        sb.append(name);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Alias)){
            return false;
        }
        if(getName().equals(((Alias)o).getName())){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return getName().hashCode();
    }

}
