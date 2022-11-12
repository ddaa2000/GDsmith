package org.example.gdsmith.neo4j.oracle;

import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.schema.IPropertyInfo;
import org.example.gdsmith.neo4j.Neo4jGlobalState;
import org.example.gdsmith.cypher.gen.query.RandomQueryGenerator;
import org.example.gdsmith.neo4j.schema.Neo4jSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Neo4jSmithCrashOracle implements TestOracle {

    private final Neo4jGlobalState globalState;
    private RandomQueryGenerator<Neo4jSchema, Neo4jGlobalState> randomQueryGenerator;
    private int[] numOfNonEmptyQueries;
    private int[] numOfTotalQueries;

    public Neo4jSmithCrashOracle(Neo4jGlobalState globalState){
        this.globalState = globalState;
        //todo 整个oracle的check会被执行多次，一直是同一个oracle实例，因此oracle本身可以管理种子库
        this.randomQueryGenerator = new RandomQueryGenerator<Neo4jSchema, Neo4jGlobalState>();
        numOfNonEmptyQueries = new int[]{0,0,0,0,0};
        numOfTotalQueries = new int[]{0,0,0,0,0};
    }

    @Override
    public void check() throws Exception {
        //todo oracle 的检测逻辑，会被调用多次
        IClauseSequence sequence = randomQueryGenerator.generateQuery(globalState);
        if (sequence.getClauseList().size() <= 8) {
            StringBuilder sb = new StringBuilder();
            sequence.toTextRepresentation(sb);
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

            if (sequence.getClauseList().size() >= 3 && sequence.getClauseList().size() <= 7) {
                numOfTotalQueries[sequence.getClauseList().size() - 3]++;
                if (resultLength > 0) {
                    numOfNonEmptyQueries[sequence.getClauseList().size() - 3]++;
                }
                System.out.println(sequence.getClauseList().size() + " rate is: " + numOfNonEmptyQueries[sequence.getClauseList().size() - 3] * 1.0 / numOfTotalQueries[sequence.getClauseList().size() - 3]);
            }

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
