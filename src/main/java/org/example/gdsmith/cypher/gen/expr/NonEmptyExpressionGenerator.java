package org.example.gdsmith.cypher.gen.expr;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.*;
import org.example.gdsmith.cypher.gen.assertion.BooleanAssertion;
import org.example.gdsmith.cypher.gen.assertion.ComparisonAssertion;
import org.example.gdsmith.cypher.gen.assertion.ExpressionAssertion;
import org.example.gdsmith.cypher.gen.assertion.StringMatchingAssertion;
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

import java.util.*;
import java.util.stream.Collectors;

public class NonEmptyExpressionGenerator<S extends CypherSchema<?,?>>
{
    IClauseAnalyzer clauseAnalyzer;
    S schema;

    private Map<String, Object> varToProperties = new HashMap<>();

    private Randomly randomly = new Randomly();

    public NonEmptyExpressionGenerator(IClauseAnalyzer clauseAnalyzer, S schema, Map<String, Object> varToProperties){
        this.clauseAnalyzer = clauseAnalyzer;
        this.schema = schema;
        this.varToProperties = varToProperties;
    }


//    private class SuperStringOf implements ExpressionAssertion{
//        String subString;
//    }
//
//    private class SubStringOf implements ExpressionAssertion{
//        String superString;
//    }


    private IExpression generateNumberAgg(){
        Randomly randomly = new Randomly();
        int randNum = randomly.getInteger(0, 50);
        //int randNum = randomly.getInteger(0, 10); //todo
        IExpression param = generateUseVar(CypherType.NUMBER, null);
        if(param == null){
            param = generateNumberConst(null);
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
        IExpression param = generateUseVar(CypherType.STRING, null);
        if(param == null){
            param = generateStringConst(null);
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

//    private IExpression generateConstExpression(CypherType type){
//        switch (type){
//            case NUMBER:
//                return new ConstExpression((int)randomly.getInteger());
//            case STRING:
//                return new ConstExpression(randomly.getString());
//            case BOOLEAN:
//                return new ConstExpression(randomly.getInteger(0, 100) < 50);
//            default:
//                //todo 对其他类型random的支持
//                return null;
//        }
//    }

    private IExpression generateStringConst(ExpressionAssertion expressionAssertion){
        if(expressionAssertion == null){
            return new ConstExpression(randomly.getString());
        }

        if(expressionAssertion instanceof StringMatchingAssertion){
            StringMatchingAssertion assertion = (StringMatchingAssertion) expressionAssertion;
            Object stringObj = assertion.getString();
            if(stringObj == ExprVal.UNKNOWN){
                return new ConstExpression(randomly.getString());
            }
            String string = (String) stringObj;
            String candidate = "";
            switch (assertion.getOperation()){
                case CONTAINS:
                    if(assertion.isTarget()){
                        return new ConstExpression(randomly.getString() + string + randomly.getString());
                    }
                    for(int i = 0; i < 50; i++){
                        candidate = randomly.getString();
                        if(!string.contains(candidate)){
                            return new ConstExpression(candidate);
                        }
                    }
                    return new ConstExpression(candidate);
                case STARTS_WITH:
                    if(assertion.isTarget()){
                        return new ConstExpression(string + randomly.getString());
                    }
                    for(int i = 0; i < 50; i++){
                        candidate = randomly.getString();
                        if(!string.startsWith(candidate)){
                            return new ConstExpression(candidate);
                        }
                    }
                    return new ConstExpression(candidate);
                case ENDS_WITH:
                    if(assertion.isTarget()){
                        return new ConstExpression(randomly.getString() + string);
                    }
                    for(int i = 0; i < 50; i++){
                        candidate = randomly.getString();
                        if(!string.endsWith(candidate)){
                            return new ConstExpression(candidate);
                        }
                    }
                    return new ConstExpression(candidate);
            }
        }

        if(expressionAssertion instanceof ComparisonAssertion){
            String candidate = "";
            ComparisonAssertion assertion = (ComparisonAssertion) expressionAssertion;
            if(assertion.getLeftOp() == ExprVal.UNKNOWN){
                return new ConstExpression(randomly.getString());
            }
            if(assertion.getLeftOp() == BinaryComparisonExpression.BinaryComparisonOperation.EQUAL && assertion.trueTarget() ||
                assertion.getLeftOp() == BinaryComparisonExpression.BinaryComparisonOperation.NOT_EQUAL && !assertion.trueTarget()){
                return new ConstExpression(assertion.getLeftOp());
            }
            for(int i = 0; i < 50; i++){
                candidate = randomly.getString();
                if(expressionAssertion.check(candidate)){
                    return new ConstExpression(candidate);
                }
            }
            return new ConstExpression(candidate);
        }

        return new ConstExpression(randomly.getString());
    }

    private IExpression generateNumberConst(ComparisonAssertion comparisonAssertion){
        if(comparisonAssertion != null){
            int leftOp = (int)comparisonAssertion.getLeftOp();
            BinaryComparisonExpression.BinaryComparisonOperation operation = comparisonAssertion.getOperation();
            if(!comparisonAssertion.trueTarget()){
                operation = operation.reverse();
            }

            switch (operation){
                case EQUAL:
                    return new ConstExpression(leftOp);
                case HIGHER:
                    if(leftOp != Integer.MIN_VALUE){
                        return new ConstExpression(randomly.getInteger(Integer.MIN_VALUE, leftOp));
                    }
                    else{
                        return new ConstExpression(leftOp);
                    }
                case HIGHER_OR_EQUAL:
                    if(leftOp != Integer.MIN_VALUE){
                        return new ConstExpression(randomly.getInteger(Integer.MIN_VALUE, Math.max(leftOp + 1, leftOp)));
                    }
                    else{
                        return new ConstExpression(leftOp);
                    }
                case SMALLER:
                    if(leftOp != Integer.MAX_VALUE){
                        return new ConstExpression(randomly.getInteger(leftOp + 1, Integer.MAX_VALUE));
                    }
                    else{
                        return new ConstExpression(leftOp);
                    }
                case SMALLER_OR_EQUAL:
                    if(leftOp != Integer.MAX_VALUE){
                        return new ConstExpression(randomly.getInteger(leftOp, Integer.MAX_VALUE));
                    }
                    else{
                        return new ConstExpression(leftOp);
                    }
                case NOT_EQUAL:{
                    int x = randomly.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE);
                    if(randomly.getInteger(0, 100) < 50){
                        x = x + 1;
                    }
                    if(x != leftOp){
                        return new ConstExpression(x);
                    }
                    return new ConstExpression(x + 1);
                }
            }
        }
        return new ConstExpression((int)randomly.getInteger());
    }

    private IExpression generateBooleanConst(BooleanAssertion booleanAssertion){
        if(booleanAssertion != null){
            return new ConstExpression(booleanAssertion.getValue());
        }
        return new ConstExpression(randomly.getInteger(0, 100) < 50);
    }

    public IExpression generateCondition(int depth){
        return booleanExpression(depth, new BooleanAssertion(true));
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
                return booleanExpression(depth, null);
            case STRING:
                return stringExpression(depth, null);
            case NUMBER:
                return numberExpression(depth, null);
            default:
                return null;
        }
    }

    private IExpression generateUseVar(CypherType type, ExpressionAssertion assertion){
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

        List<IExpression> checkedAvailableExpressions = availableExpressions.stream().filter(e->{
            if(e.getValue(varToProperties) == null){
                return false;
            }
            if(assertion == null){
                return true;
            }
            return assertion.check(e.getValue(varToProperties));
        }).collect(Collectors.toList());

        if(checkedAvailableExpressions.size() == 0){
            switch (type){
                case BOOLEAN:
                    return generateBooleanConst((BooleanAssertion) assertion);
                case NUMBER:
                    return generateNumberConst((ComparisonAssertion) assertion);
                case STRING:
                    return generateStringConst(assertion);
            }
        }

        return checkedAvailableExpressions.get(randomly.getInteger(0, checkedAvailableExpressions.size()));
    }

    private IExpression booleanExpression(int depth, BooleanAssertion booleanAssertion){
        Randomly randomly = new Randomly();
        int expressionChoice = randomly.getInteger(0, 100);
        if(depth == 0 || expressionChoice < 30){
            //深度用尽，快速收束，对于BOOLEAN而言： 返回true/false，返回boolean类型property，返回boolean变量引用
            int randomNum = randomly.getInteger(0,100);
            if(randomNum < 20){
                return generateBooleanConst(booleanAssertion);
            }
            return generateUseVar(CypherType.BOOLEAN, booleanAssertion);
            //todo Is_NULL的单独处理逻辑
        }

        //尚有深度

        boolean target = booleanAssertion == null ? randomly.getInteger(0, 100) < 50 : booleanAssertion.getValue();

        if(expressionChoice < 50){
            IExpression numberExpr = numberExpression(depth - 1, null);
            if(numberExpr.getValue(varToProperties) == ExprVal.UNKNOWN){
                return BinaryComparisonExpression.randomComparison(numberExpr, numberExpression(depth - 1, null));
            }

            BinaryComparisonExpression.BinaryComparisonOperation op = BinaryComparisonExpression.randomOperation();
            return new BinaryComparisonExpression(numberExpr, numberExpression(depth - 1,
                    new ComparisonAssertion(op, numberExpr.getValue(varToProperties), target)), op);
        }
        if(expressionChoice < 60){
            IExpression stringExpr = stringExpression(depth - 1, null);
            if(stringExpr.getValue(varToProperties) == ExprVal.UNKNOWN){
                return BinaryComparisonExpression.randomComparison(stringExpr, stringExpression(depth - 1, null));
            }

            BinaryComparisonExpression.BinaryComparisonOperation op = BinaryComparisonExpression.randomOperation();
            return new BinaryComparisonExpression(stringExpr, stringExpression(depth - 1,
                    new ComparisonAssertion(op, stringExpr.getValue(varToProperties), target)), op);
        }
        if(expressionChoice < 70){
            IExpression stringExpr = stringExpression(depth - 1, null);
            if(stringExpr.getValue(varToProperties) == ExprVal.UNKNOWN){
                return StringMatchingExpression.randomMatching(stringExpr, stringExpression(depth - 1, null));
            }

            StringMatchingExpression.StringMatchingOperation op = StringMatchingExpression.randomOperation();
            return new StringMatchingExpression(stringExpr, stringExpression(depth - 1,
                    new StringMatchingAssertion(op, stringExpr.getValue(varToProperties), target)), op);
        }
        if(expressionChoice < 80){
            return new SingleLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(target)), SingleLogicalExpression.SingleLogicalOperation.NOT);
        }

        BinaryLogicalExpression.BinaryLogicalOperation op = BinaryLogicalExpression.randomOp();
        switch (op){
            case AND:
                if(target){
                    return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(true)),
                            booleanExpression(depth - 1, new BooleanAssertion(true)),
                            op);
                }
                else {
                    if (randomly.getInteger(0, 100) < 50) {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(false)),
                                booleanExpression(depth - 1, null),
                                op);
                    } else {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, null),
                                booleanExpression(depth - 1, new BooleanAssertion(false)),
                                op);
                    }
                }
            case OR:
                if(target){
                    if (randomly.getInteger(0, 100) < 50) {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(true)),
                                booleanExpression(depth - 1, null),
                                op);
                    } else {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, null),
                                booleanExpression(depth - 1, new BooleanAssertion(true)),
                                op);
                    }
                }
                else {
                    return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(false)),
                            booleanExpression(depth - 1, new BooleanAssertion(false)),
                            op);
                }
            case XOR:
                if(target){
                    if (randomly.getInteger(0, 100) < 50) {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(true)),
                                booleanExpression(depth - 1, new BooleanAssertion(false)),
                                op);
                    } else {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(false)),
                                booleanExpression(depth - 1, new BooleanAssertion(true)),
                                op);
                    }
                }
                else {
                    if (randomly.getInteger(0, 100) < 50) {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(false)),
                                booleanExpression(depth - 1, new BooleanAssertion(false)),
                                op);
                    } else {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(true)),
                                booleanExpression(depth - 1, new BooleanAssertion(true)),
                                op);
                    }
                }
            default:
                throw new RuntimeException();
        }
    }

    private IExpression stringExpression(int depth, ExpressionAssertion expressionAssertion){
        Randomly randomly = new Randomly();
        int expressionChoice = randomly.getInteger(0, 100);
        if(depth == 0 || expressionChoice < 70){
            //深度用尽，快速收束，对于string而言： 返回随机字符串，返回string类型property，返回string变量引用
            int randomNum = randomly.getInteger(0,100);
            if(randomNum < 20){
                return generateStringConst(expressionAssertion);
            }
            return generateUseVar(CypherType.STRING, expressionAssertion);
        }

        IExpression left = stringExpression(depth - 1, null);
        Object leftObj = left.getValue(varToProperties);
        if(leftObj == ExprVal.UNKNOWN){
            return new StringCatExpression(left, stringExpression(depth - 1, null));
        }
        else{
            if(expressionAssertion instanceof StringMatchingAssertion){
                if(((StringMatchingAssertion) expressionAssertion).isTarget()){
                    switch (((StringMatchingAssertion)expressionAssertion).getOperation()){
                        case STARTS_WITH:
                            return new StringCatExpression(stringExpression(depth - 1, expressionAssertion),
                                    stringExpression(depth - 1, null));
                        case ENDS_WITH:
                            return new StringCatExpression(stringExpression(depth - 1, null),
                                    stringExpression(depth - 1, expressionAssertion));
                        case CONTAINS:
                            int randNum = randomly.getInteger(0, 100);
                            Object stringObj = ((StringMatchingAssertion) expressionAssertion).getString();
                            if(stringObj == ExprVal.UNKNOWN){
                                return new StringCatExpression(stringExpression(depth - 1, null),
                                        stringExpression(depth - 1, null));
                            }
                            String string = (String) stringObj;
                            if(randNum < 30){
                                return new StringCatExpression(stringExpression(depth - 1, null),
                                        stringExpression(depth - 1, expressionAssertion));
                            }
                            else if(randNum < 60 || string.length() == 0){
                                return new StringCatExpression(stringExpression(depth - 1, expressionAssertion),
                                        stringExpression(depth - 1, null));
                            }
                            else{
                                int index = randomly.getInteger(0, string.length());
                                String first = string.substring(0, index);
                                String second = string.substring(index, string.length());
                                return new StringCatExpression(stringExpression(depth - 1, new StringMatchingAssertion(StringMatchingExpression.StringMatchingOperation.ENDS_WITH, first, true)),
                                        stringExpression(depth - 1, new StringMatchingAssertion(StringMatchingExpression.StringMatchingOperation.STARTS_WITH, second, true)));
                            }
                    }
                }
                else{
                    //not perfect
                    return new StringCatExpression(stringExpression(depth - 1, null), stringExpression(depth - 1, null));
                }
            }
            //not perfect, need comparison
            return new StringCatExpression(stringExpression(depth - 1, null), stringExpression(depth - 1, null));
        }

    }

    private IExpression numberExpression(int depth, ComparisonAssertion comparisonAssertion){
        Randomly randomly = new Randomly();
        int expressionChoice = randomly.getInteger(0, 100);
        if(depth == 0 || expressionChoice < 70){
            //深度用尽，快速收束，对于string而言： 返回随机字符串，返回string类型property，返回string变量引用
            int randomNum = randomly.getInteger(0,100);
            if(randomNum < 20){
                return generateNumberConst(comparisonAssertion);
            }
            return generateUseVar(CypherType.NUMBER, comparisonAssertion);
        }
        return generateNumberConst(comparisonAssertion);
        //return BinaryNumberExpression.randomBinaryNumber(numberExpression(depth - 1), numberExpression(depth - 1));
    }

}
