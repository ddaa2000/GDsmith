package org.example.gdsmith.cypher.gen.expr;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.*;
import org.example.gdsmith.cypher.gen.EnumerationSeq;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.Alias;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.expr.*;
import org.example.gdsmith.cypher.schema.IPropertyInfo;
import org.example.gdsmith.cypher.standard_ast.expr.*;
import org.example.gdsmith.neo4j.schema.Neo4jSchema;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumerationExpressionGenerator<S extends CypherSchema<?,?>>
{
    private EnumerationSeq enumerationSeq;
    IClauseAnalyzer clauseAnalyzer;
    S schema;
    public EnumerationExpressionGenerator(IClauseAnalyzer clauseAnalyzer, EnumerationSeq enumerationSeq, S schema){
        this.clauseAnalyzer = clauseAnalyzer;
        this.schema = schema;
        this.enumerationSeq = enumerationSeq;
    }

    private IExpression generateNumberAgg(){
        IExpression param = generateUseVar(CypherType.NUMBER);
        if(param == null){
            param = generateConstExpression(CypherType.NUMBER);
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.MAX_NUMBER, Arrays.asList(param));
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.MIN_NUMBER, Arrays.asList(param));
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.AVG, Arrays.asList(param));
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.SUM, Arrays.asList(param));
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.COLLECT, Arrays.asList(param));
        }

        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.ST_DEV, Arrays.asList(param));
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.ST_DEV_P, Arrays.asList(param));
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.PERCENTILE_COUNT_NUMBER, Arrays.asList(param));
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.PERCENTILE_COUNT_STRING, Arrays.asList(param));
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.PERCENTILE_DISC_NUMBER, Arrays.asList(param));
        }
        return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.PERCENTILE_DISC_STRING, Arrays.asList(param));
    }

    private IExpression generateStringAgg(){
        IExpression param = generateUseVar(CypherType.STRING);
        if(param == null){
            param = generateConstExpression(CypherType.STRING);
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.MAX_STRING, Arrays.asList(param));
        }
        if(enumerationSeq.getDecision()){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.MIN_STRING, Arrays.asList(param));
        }
        return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.COLLECT, Arrays.asList(param));
    }

    public IExpression generateFunction(CypherType type){
        if(type == CypherType.NUMBER){
            return generateNumberAgg();
        }
        return generateStringAgg();
    }

    private IExpression generateUseAlias(CypherType type){
        List<IAliasAnalyzer> aliasAnalyzers = clauseAnalyzer.getAvailableAliases();
        aliasAnalyzers = aliasAnalyzers.stream().filter(a->a.analyzeType(schema).getType()==type).collect(Collectors.toList());
        IAliasAnalyzer aliasAnalyzer =  aliasAnalyzers.stream().findAny().orElse(null);
        if(aliasAnalyzer!=null){
            return new IdentifierExpression(Alias.createIdentifierRef(aliasAnalyzer));
        }
        return generateConstExpression(type);
    }

    private IExpression generateGetProperty(CypherType type){

        List<INodeAnalyzer> nodeAnalyzers = clauseAnalyzer.getAvailableNodeIdentifiers();
        List<IRelationAnalyzer> relationAnalyzers = clauseAnalyzer.getAvailableRelationIdentifiers();

        List<IIdentifierAnalyzer> identifierAnalyzers = new ArrayList<>();

        identifierAnalyzers.addAll(nodeAnalyzers.stream()
                .filter(n->n.getAllPropertiesWithType(schema,type).size()>0).collect(Collectors.toList()));
        identifierAnalyzers.addAll(relationAnalyzers.stream().
                filter(r->(r.getAllPropertiesWithType(schema,type).size()>0 && r.isSingleRelation())).collect(Collectors.toList()));

        IIdentifierAnalyzer identifierAnalyzer = enumerationSeq.getElement(identifierAnalyzers);

        if(identifierAnalyzer instanceof IRelationAnalyzer){
            List<IPropertyInfo> propertyInfos = ((IRelationAnalyzer) identifierAnalyzer).getAllPropertiesWithType(schema, type);
            return new GetPropertyExpression(new IdentifierExpression(identifierAnalyzer),
                    enumerationSeq.getElement(propertyInfos).getKey());
        }
        if(identifierAnalyzer instanceof INodeAnalyzer){
            List<IPropertyInfo> propertyInfos = ((INodeAnalyzer) identifierAnalyzer).getAllPropertiesWithType(schema, type);
            return new GetPropertyExpression(new IdentifierExpression(identifierAnalyzer),
                    enumerationSeq.getElement(propertyInfos).getKey());
        }

        return generateConstExpression(type);
    }

    public IExpression generateConstExpression(CypherType type){
        Randomly randomly = new Randomly();
        switch (type){
            case NUMBER:
                return new ConstExpression((int)randomly.getInteger());
            case STRING:
                return new ConstExpression(randomly.getString());
            case BOOLEAN:
                return new ConstExpression(randomly.getInteger(0, 100) < 50);
            default:
                //todo 对其他类型random的支持
                return null;
        }
    }

    public IExpression generateCondition(int depth){
        return booleanExpression(depth);
    }

    public IExpression generateListWithBasicType(int depth, CypherType type){
        int randomNum = enumerationSeq.getRange(4);
        List<IExpression> expressions = new ArrayList<>();
        for(int i = 0; i < randomNum; i++){
            //todo 更复杂的列表生成
            expressions.add(basicTypeExpression(depth, type));
        }
        return new CreateListExpression(expressions);
    }

    private IExpression basicTypeExpression(int depth, CypherType type){
        switch (type){
            case BOOLEAN:
                return booleanExpression(depth);
            case STRING:
                return stringExpression(depth);
            case NUMBER:
                return numberExpression(depth);
            default:
                return null;
        }
    }

    private IExpression generateUseVar(CypherType type){

        List<IExpression> availableExpressions = new ArrayList<>();


        List<IAliasAnalyzer> aliasAnalyzers = clauseAnalyzer.getAvailableAliases();
        availableExpressions.addAll(aliasAnalyzers.stream().filter(a->a.analyzeType(schema).getType()==type).map(a->new IdentifierExpression(Alias.createIdentifierRef(a)))
                .collect(Collectors.toList()));


        List<INodeAnalyzer> nodeAnalyzers = clauseAnalyzer.getAvailableNodeIdentifiers();
        nodeAnalyzers.addAll(aliasAnalyzers.stream().filter(a->a.analyzeType(schema).getType()==CypherType.NODE).map(
                a-> a.analyzeType(schema).getNodeAnalyzer()
        ).collect(Collectors.toList()));

        List<IRelationAnalyzer> relationAnalyzers = clauseAnalyzer.getAvailableRelationIdentifiers();
        relationAnalyzers.addAll(aliasAnalyzers.stream().filter(a->a.analyzeType(schema).getType()==CypherType.RELATION).map(
                a-> a.analyzeType(schema).getRelationAnalyzer()
        ).collect(Collectors.toList()));

        nodeAnalyzers.stream().forEach(
                n->{
                    n.getAllPropertiesWithType(schema,type).forEach(
                            p-> {
                                availableExpressions.add(new GetPropertyExpression(new IdentifierExpression(n),
                                        p.getKey()));
                            }
                    );
                }
        );

        relationAnalyzers.stream().forEach(
                r->{
                    r.getAllPropertiesWithType(schema,type).forEach(
                            p-> {
                                availableExpressions.add(new GetPropertyExpression(new IdentifierExpression(r),
                                        p.getKey()));
                            }
                    );
                }
        );

        if(availableExpressions.size() == 0){
            return generateConstExpression(type);
        }

        return enumerationSeq.getElement(availableExpressions);
    }

    private IExpression booleanExpression(int depth){
        if(depth == 0 || enumerationSeq.getDecision()){
            //深度用尽，快速收束，对于BOOLEAN而言： 返回true/false，返回boolean类型property，返回boolean变量引用
            if(enumerationSeq.getDecision()){
                return generateConstExpression(CypherType.BOOLEAN);
            }
            return generateUseVar(CypherType.BOOLEAN);
            //todo Is_NULL的单独处理逻辑
        }

        //尚有深度

        if(enumerationSeq.getDecision()){
            return BinaryComparisonExpression.randomComparison(numberExpression(depth - 1), numberExpression(depth - 1));
        }
        if(enumerationSeq.getDecision()){
            return BinaryComparisonExpression.randomComparison(stringExpression(depth - 1), stringExpression(depth - 1));
        }
        if(enumerationSeq.getDecision()){
            return StringMatchingExpression.randomMatching(stringExpression(depth - 1), stringExpression(depth - 1));
        }
        if(enumerationSeq.getDecision()){
            return SingleLogicalExpression.randomLogical(booleanExpression(depth - 1));
        }
        return BinaryLogicalExpression.randomLogical(booleanExpression(depth - 1), booleanExpression(depth - 1));
    }

    private IExpression stringExpression(int depth){
        if(depth == 0 || enumerationSeq.getDecision()){
            //深度用尽，快速收束，对于string而言： 返回随机字符串，返回string类型property，返回string变量引用
            if(enumerationSeq.getDecision()){
                return generateConstExpression(CypherType.STRING);
            }
            return generateUseVar(CypherType.STRING);
        }
        return new StringCatExpression(stringExpression(depth - 1), stringExpression(depth - 1));
    }

    private IExpression numberExpression(int depth){
        if(depth == 0 || enumerationSeq.getDecision()){
            //深度用尽，快速收束，对于string而言： 返回随机字符串，返回string类型property，返回string变量引用
            if(enumerationSeq.getDecision()){
                return generateConstExpression(CypherType.NUMBER);
            }
            return generateUseVar(CypherType.NUMBER);
        }
        return generateConstExpression(CypherType.NUMBER);
    }

}
