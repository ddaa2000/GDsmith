package org.example.gdsmith.memGraph.oracle;

import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.schema.IPropertyInfo;
import org.example.gdsmith.memGraph.MemGraphSchema;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.memGraph.MemGraphGlobalState;
import org.example.gdsmith.cypher.gen.query.RandomQueryGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemGraphAlwaysTrueOracle implements TestOracle {

    private final MemGraphGlobalState globalState;
    private RandomQueryGenerator<MemGraphSchema, MemGraphGlobalState> randomQueryGenerator;

    public MemGraphAlwaysTrueOracle(MemGraphGlobalState globalState){
        this.globalState = globalState;
        //todo 整个oracle的check会被执行多次，一直是同一个oracle实例，因此oracle本身可以管理种子库
        this.randomQueryGenerator = new RandomQueryGenerator<MemGraphSchema, MemGraphGlobalState>();
    }

    @Override
    public void check() throws Exception {
        //todo oracle 的检测逻辑，会被调用多次
        IClauseSequence sequence = randomQueryGenerator.generateQuery(globalState);
        StringBuilder sb = new StringBuilder();
        sequence.toTextRepresentation(sb);
        
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
            while(matcher.find()){
                if (!coveredProperty.contains(matcher.group(2))) {
                    coveredProperty.add(matcher.group(2));
                }
            }

            for (String name: coveredProperty) {
                found:{
                    for (CypherSchema.CypherLabelInfo label: labels) {
                        List<IPropertyInfo> props = label.getProperties();
                        for (IPropertyInfo prop: props) {
                            if (Objects.equals(prop.getKey(), name)) {
                                ((CypherSchema.CypherPropertyInfo)prop).addFreq();
                                break found;
                            }
                        }
                    }
                    for (CypherSchema.CypherRelationTypeInfo relation: relations) {
                        List<IPropertyInfo> props = relation.getProperties();
                        for (IPropertyInfo prop: props) {
                            if (Objects.equals(prop.getKey(), name)) {
                                ((CypherSchema.CypherPropertyInfo)prop).addFreq();
                                break found;
                            }
                        }
                    }
                }
            }
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
