package org.example.gdsmith.cypher.oracle;

import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.gen.condition.RandomConditionGenerator;
import org.example.gdsmith.cypher.gen.pattern.RandomPatternGenerator;
import org.example.gdsmith.cypher.gen.query.RandomQueryGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.schema.IPropertyInfo;
import org.example.gdsmith.cypher.standard_ast.Alias;
import org.example.gdsmith.cypher.standard_ast.Match;
import org.example.gdsmith.cypher.standard_ast.Ret;
import org.example.gdsmith.cypher.standard_ast.With;
import org.example.gdsmith.cypher.standard_ast.expr.*;
import org.example.gdsmith.cypher.standard_ast.expr.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoRecOracle <G extends CypherGlobalState<?,S>, S extends CypherSchema<G, ?>> implements TestOracle {
    private final G globalState;
    private RandomQueryGenerator<S, G> randomQueryGenerator;

    public NoRecOracle(G globalState){
        this.globalState = globalState;
        this.randomQueryGenerator = new RandomQueryGenerator<>();
    }

    @Override
    public void check() throws Exception {
        //todo oracle 的检测逻辑，会被调用多次
        IClauseSequence sequence = randomQueryGenerator.generateQuery(globalState);
        IClauseSequence equivalent = null;

        ICypherClause clauseBeforeReturn = sequence.getClauseList().get(sequence.getClauseList().size() - 2);

        IExpression condition = null;

        if(!(clauseBeforeReturn instanceof IMatch) || ((IMatch) clauseBeforeReturn).getCondition() == null){
            Match match = new Match();
            sequence.addClauseAt(match, sequence.getClauseList().size() - 1);

            new RandomPatternGenerator<>(globalState.getSchema(), sequence.getIdentifierBuilder(), false).fillMatchPattern(match.toAnalyzer());
            while (match.getCondition() == null){
                new RandomConditionGenerator<>(globalState.getSchema(), false).fillMatchCondtion(match.toAnalyzer());
            }
            //以上：保证倒数第二个是match，且带where

            condition = match.getCondition();
        }
        else {
            condition = ((IMatch) clauseBeforeReturn).getCondition();
        }

        IReturn lastClause = (IReturn) sequence.getClauseList().get(sequence.getClauseList().size() - 1);
        List<IRet> returnList = new ArrayList<>();
        returnList.add(Ret.createNewExpressionReturnVal(new CallExpression(
                CypherSchema.CypherBuiltInFunctions.COUNT, Arrays.asList(new Star())
        )));
        //修改原clauseSequence末尾为return count(*)
        lastClause.setReturnList(returnList);
        lastClause.setDistinct(false);
        lastClause.setLimit(null);
        lastClause.setOrderBy(new ArrayList<>(), false);
        lastClause.setSkip(null);


        equivalent = sequence.getCopy();

        //with condition as ax
        With with = new With();
        Ret ret = Ret.createNewExpressionAlias(equivalent.getIdentifierBuilder(), condition.getCopy());
        with.setReturnList(new ArrayList<>(Arrays.asList(ret)));
        with.setCondition(new BinaryComparisonExpression(new IdentifierExpression(Alias.createIdentifierRef(ret.getIdentifier())), new ConstExpression(true),
                BinaryComparisonExpression.BinaryComparisonOperation.EQUAL));
        //插入with
        equivalent.getClauseList().add(equivalent.getClauseList().size() - 1, with);

        List<IRet> mutatedReturnList = new ArrayList<>();
        mutatedReturnList.add(Ret.createNewExpressionReturnVal(new CallExpression(
                CypherSchema.CypherBuiltInFunctions.COUNT, Arrays.asList(new Star())
        )));
        IReturn lastClause2 = (IReturn) equivalent.getClauseList().get(equivalent.getClauseList().size() - 1);
        lastClause2.setReturnList(mutatedReturnList);


        if (sequence.getClauseList().size() <= 8) {
            StringBuilder sb = new StringBuilder();
            sequence.toTextRepresentation(sb);
            System.out.println(sb.toString());
            sb.delete(0, sb.length());
            equivalent.toTextRepresentation(sb);
            System.out.println(sb.toString());
           /* sequence.getClauseList().stream().forEach(s->{
                if(s instanceof IMatch){
                    System.out.print("MATCH ");
                }
                if(s instanceof IWith){
                    System.out.print("WITH ");
                }
                if(s instanceof IReturn){
                    System.out.print("RETURN ");
                }
                if(s instanceof IUnwind){
                    System.out.print("UNWIND ");
                }
                System.out.print(" local: ");
                s.toAnalyzer().getLocalAliases().stream().forEach(
                        a->{
                            System.out.print(a.getName()+" ");
                        }
                );
                System.out.print(" extendable: ");
                s.toAnalyzer().getExtendableAliases().stream().forEach(
                        a->{
                            System.out.print(a.getName()+" ");
                        }
                );
                System.out.print(" avaialble: ");
                s.toAnalyzer().getAvailableAliases().stream().forEach(
                        a->{
                            System.out.print(a.getName()+" ");
                        }
                );
                System.out.print("\n");
            });*/

            System.out.println(sb);
            GDSmithResultSet r = globalState.executeStatementAndGet(new CypherQueryAdapter(sb.toString())).get(0);

            boolean isBugDetected = false;
            int resultLength = r.getRowNum();
            //todo 上层通过抛出的异常检测是否通过，所以这里可以捕获并检测异常的类型，可以计算一些统计数据，然后重抛异常

            List<CypherSchema.CypherLabelInfo> labels = globalState.getSchema().getLabels();
            List<CypherSchema.CypherRelationTypeInfo> relations = globalState.getSchema().getRelationTypes();
            if (resultLength > 0) {
                randomQueryGenerator.addExecutionRecord(sequence, isBugDetected, resultLength);//添加seed

                List<String> coveredProperty = new ArrayList<>();
                Pattern allProps = Pattern.compile("(\\.)(k\\d+)(\\))");
                Matcher matcher = allProps.matcher(sb);
                while (matcher.find()) {
                    if (!coveredProperty.contains(matcher.group(2))) {
                        coveredProperty.add(matcher.group(2));
                    }
                }

                for (String name : coveredProperty) {
                    found:
                    {
                        for (CypherSchema.CypherLabelInfo label : labels) {
                            List<IPropertyInfo> props = label.getProperties();
                            for (IPropertyInfo prop : props) {
                                if (Objects.equals(prop.getKey(), name)) {
                                    ((CypherSchema.CypherPropertyInfo) prop).addFreq();
                                    break found;
                                }
                            }
                        }
                        for (CypherSchema.CypherRelationTypeInfo relation : relations) {
                            List<IPropertyInfo> props = relation.getProperties();
                            for (IPropertyInfo prop : props) {
                                if (Objects.equals(prop.getKey(), name)) {
                                    ((CypherSchema.CypherPropertyInfo) prop).addFreq();
                                    break found;
                                }
                            }
                        }
                    }
                }
            }

            /*if (sequence.getClauseList().size() >= 3 && sequence.getClauseList().size() <= 7) {
                numOfTotalQueries[sequence.getClauseList().size() - 3]++;
                if (resultLength > 0) {
                    numOfNonEmptyQueries[sequence.getClauseList().size() - 3]++;
                }
                System.out.println(sequence.getClauseList().size() + " rate is: " + numOfNonEmptyQueries[sequence.getClauseList().size() - 3] * 1.0 / numOfTotalQueries[sequence.getClauseList().size() - 3]);
            }*/

            for (CypherSchema.CypherLabelInfo label: labels) {
                List<IPropertyInfo> props = label.getProperties();
                for (IPropertyInfo prop: props) {
                    System.out.println(label.getName() + ":" + prop.getKey() + ":" + ((CypherSchema.CypherPropertyInfo)prop).getFreq());
                }
            }
            for (CypherSchema.CypherRelationTypeInfo relation: relations) {
                List<IPropertyInfo> props = relation.getProperties();
                for (IPropertyInfo prop: props) {
                    System.out.println(relation.getName() + ":" + prop.getKey() + ":" + ((CypherSchema.CypherPropertyInfo)prop).getFreq());
                }
            }
        }
    }
}
