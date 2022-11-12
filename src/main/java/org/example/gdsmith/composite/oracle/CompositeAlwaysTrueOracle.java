package org.example.gdsmith.composite.oracle;

import org.example.gdsmith.composite.CompositeSchema;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.composite.CompositeGlobalState;
import org.example.gdsmith.cypher.gen.query.RandomQueryGenerator;

public class CompositeAlwaysTrueOracle implements TestOracle {

    private final CompositeGlobalState globalState;
    private RandomQueryGenerator<CompositeSchema, CompositeGlobalState> randomQueryGenerator;

    public CompositeAlwaysTrueOracle(CompositeGlobalState globalState){
        this.globalState = globalState;
        //todo 整个oracle的check会被执行多次，一直是同一个oracle实例，因此oracle本身可以管理种子库
        this.randomQueryGenerator = new RandomQueryGenerator<CompositeSchema, CompositeGlobalState>();
    }

    @Override
    public void check() throws Exception {
//        //todo oracle 的检测逻辑，会被调用多次
//        IClauseSequence sequence = randomQueryGenerator.generateQuery(globalState);
//        StringBuilder sb = new StringBuilder();
//        sequence.toTextRepresentation(sb);
//        System.out.println(sb);
//        globalState.executeStatement(new CypherQueryAdapter(sb.toString()));
//
//        boolean isCoverageIncreasing = false;
//        boolean isBugDetected = false;
//        int resultLength = 0;
//        //todo 上层通过抛出的异常检测是否通过，所以这里可以捕获并检测异常的类型，可以计算一些统计数据，然后重抛异常
//
//        if (isCoverageIncreasing || isBugDetected || resultLength > 0) {
//            randomQueryGenerator.addExecutionRecord(sequence, isBugDetected, resultLength);//添加seed
//        }
    }
}
