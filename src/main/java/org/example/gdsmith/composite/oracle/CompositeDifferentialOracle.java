package org.example.gdsmith.composite.oracle;

import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.composite.CompositeSchema;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.composite.CompositeGlobalState;
import org.example.gdsmith.cypher.dsl.IQueryGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;

import java.util.List;
import java.util.concurrent.CompletionException;

public class CompositeDifferentialOracle implements TestOracle {

    private final CompositeGlobalState globalState;
    private IQueryGenerator<CompositeSchema, CompositeGlobalState> queryGenerator;

    public CompositeDifferentialOracle(CompositeGlobalState globalState){
        this.globalState = globalState;
        //todo 整个oracle的check会被执行多次，一直是同一个oracle实例，因此oracle本身可以管理种子库
        this.queryGenerator = globalState.getDbmsSpecificOptions().getQueryGenerator();
    }

    @Override
    public void check() throws Exception {
        //todo oracle 的检测逻辑，会被调用多次
        IClauseSequence sequence = queryGenerator.generateQuery(globalState);
        StringBuilder sb = new StringBuilder();
        sequence.toTextRepresentation(sb);
        System.out.println(sb);
        List<GDSmithResultSet> results;
        int resultLength = 0;
        try {
            results = globalState.executeStatementAndGet(new CypherQueryAdapter(sb.toString()));        //todo 判断不同的resultSet返回是否一致
            for(int i = 1; i < results.size(); i++) {
                if (!results.get(i).compareWithOutOrder(results.get(i - 1))) {
                    String msg = "The contents of the result sets mismatch!\n";
                    msg = msg + "First: " + results.get(i - 1).getRowNum() + " --- " + results.get(i - 1).resultToStringList() + "\n";
                    msg = msg + "Second: " + results.get(i).getRowNum() + " --- " + results.get(i).resultToStringList() + "\n";
                    throw new AssertionError(msg);
                }
            }
            resultLength = results.get(0).getRowNum();
        } catch (CompletionException e) {
            System.out.println("该Cypher查询不支持转换为Gremlin！");
            System.out.println(e.getMessage());
        }
        boolean isBugDetected = false;
        //todo 上层通过抛出的异常检测是否通过，所以这里可以捕获并检测异常的类型，可以计算一些统计数据，然后重抛异常

        List<CypherSchema.CypherLabelInfo> labels = globalState.getSchema().getLabels();
        List<CypherSchema.CypherRelationTypeInfo> relations = globalState.getSchema().getRelationTypes();
        if (resultLength > 0) {
            queryGenerator.addExecutionRecord(sequence, isBugDetected, resultLength);//添加seed
        }
    }
}
