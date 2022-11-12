package org.example.gdsmith.cypher.gen.expr;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.*;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.standard_ast.Alias;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.expr.*;
import org.example.gdsmith.neo4j.schema.Neo4jSchema;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.analyzer.IAliasAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IClauseAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.INodeAnalyzer;
import org.example.gdsmith.cypher.ast.analyzer.IRelationAnalyzer;
import org.example.gdsmith.cypher.standard_ast.expr.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RandomExpressionGenerator<S extends CypherSchema<?,?>>
{
    IClauseAnalyzer clauseAnalyzer;
    S schema;
    public RandomExpressionGenerator(IClauseAnalyzer clauseAnalyzer, S schema){
        this.clauseAnalyzer = clauseAnalyzer;
        this.schema = schema;
    }

    private IExpression generateNumberAgg(){
        Randomly randomly = new Randomly();
        int randNum = randomly.getInteger(0, 50);
        //int randNum = randomly.getInteger(0, 10); //todo
        IExpression param = generateUseVar(CypherType.NUMBER);
        if(param == null){
            param = generateConstExpression(CypherType.NUMBER);
        }
        if( randNum < 10){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.MAX_NUMBER, Arrays.asList(param));
        }
        if( randNum < 20){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.MIN_NUMBER, Arrays.asList(param));
        }
        if( randNum < 30){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.AVG, Arrays.asList(param));
        }
        if( randNum < 40){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.SUM, Arrays.asList(param));
        }
        if( randNum < 50){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.COLLECT, Arrays.asList(param));
        }

        if( randNum < 60){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.ST_DEV, Arrays.asList(param));
        }
        if( randNum < 70){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.ST_DEV_P, Arrays.asList(param));
        }
        if( randNum < 80){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.PERCENTILE_COUNT_NUMBER, Arrays.asList(param));
        }
        if( randNum < 90){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.PERCENTILE_COUNT_STRING, Arrays.asList(param));
        }
        if( randNum < 100){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.PERCENTILE_DISC_NUMBER, Arrays.asList(param));
        }
        return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.PERCENTILE_DISC_STRING, Arrays.asList(param));
    }

    private IExpression generateStringAgg(){
        Randomly randomly = new Randomly();
        int randNum = randomly.getInteger(0, 20);
        //int randNum = randomly.getInteger(0, 10);
        IExpression param = generateUseVar(CypherType.STRING);
        if(param == null){
            param = generateConstExpression(CypherType.STRING);
        }
        if( randNum < 10){
            return new CallExpression(Neo4jSchema.Neo4jBuiltInFunctions.MAX_STRING, Arrays.asList(param));
        }
        if( randNum < 20){
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

    private IExpression generateConstExpression(CypherType type){
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
        Randomly randomly = new Randomly();
        int randomNum = randomly.getInteger(1,4);
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
        Randomly randomly = new Randomly();

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

        return availableExpressions.get(randomly.getInteger(0, availableExpressions.size()));
    }

    private IExpression booleanExpression(int depth){
        Randomly randomly = new Randomly();
        int expressionChoice = randomly.getInteger(0, 100);
        if(depth == 0 || expressionChoice < 30){
            //深度用尽，快速收束，对于BOOLEAN而言： 返回true/false，返回boolean类型property，返回boolean变量引用
            int randomNum = randomly.getInteger(0,100);
            if(randomNum < 20){
                return generateConstExpression(CypherType.BOOLEAN);
            }
            return generateUseVar(CypherType.BOOLEAN);
            //todo Is_NULL的单独处理逻辑
        }

        //尚有深度

        if(expressionChoice < 50){
            return BinaryComparisonExpression.randomComparison(numberExpression(depth - 1), numberExpression(depth - 1));
        }
        if(expressionChoice < 60){
            return BinaryComparisonExpression.randomComparison(stringExpression(depth - 1), stringExpression(depth - 1));
        }
        if(expressionChoice < 70){
            return StringMatchingExpression.randomMatching(stringExpression(depth - 1), stringExpression(depth - 1));
        }
        if(expressionChoice < 80){
            return SingleLogicalExpression.randomLogical(booleanExpression(depth - 1));
        }
        return BinaryLogicalExpression.randomLogical(booleanExpression(depth - 1), booleanExpression(depth - 1));
    }

    private IExpression stringExpression(int depth){
        Randomly randomly = new Randomly();
        int expressionChoice = randomly.getInteger(0, 100);
        if(depth == 0 || expressionChoice < 70){
            //深度用尽，快速收束，对于string而言： 返回随机字符串，返回string类型property，返回string变量引用
            int randomNum = randomly.getInteger(0,100);
            if(randomNum < 20){
                return generateConstExpression(CypherType.STRING);
            }
            return generateUseVar(CypherType.STRING);
        }
        return new StringCatExpression(stringExpression(depth - 1), stringExpression(depth - 1));
    }

    private IExpression numberExpression(int depth){
        Randomly randomly = new Randomly();
        int expressionChoice = randomly.getInteger(0, 100);
        if(depth == 0 || expressionChoice < 50){
            //深度用尽，快速收束，对于string而言： 返回随机字符串，返回string类型property，返回string变量引用
            int randomNum = randomly.getInteger(0,100);
            if(randomNum < 20){
                return generateConstExpression(CypherType.NUMBER);
            }
            return generateUseVar(CypherType.NUMBER);
        }
        return generateConstExpression(CypherType.NUMBER);
        //return BinaryNumberExpression.randomBinaryNumber(numberExpression(depth - 1), numberExpression(depth - 1));
    }

}
