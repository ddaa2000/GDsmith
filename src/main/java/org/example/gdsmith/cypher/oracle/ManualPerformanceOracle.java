package org.example.gdsmith.cypher.oracle;

import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.composite.CompositeConnection;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.dsl.IQueryGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.exceptions.ResultMismatchException;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

public class ManualPerformanceOracle <G extends CypherGlobalState<?,S>, S extends CypherSchema<G,?>> implements TestOracle {

    private final G globalState;
    private IQueryGenerator<S, G> queryGenerator;

    public static final int BRANCH_PAIR_SIZE = 65536;
    public static final  int BRANCH_SIZE = 1000000;

    public static final int PORT = 9009;
    public static final byte CLEAR = 1, PRINT_MEM = 2;

    private OutputStream outputStream;


    public ManualPerformanceOracle(G globalState, IQueryGenerator<S,G> generator){
        this.globalState = globalState;
        //todo 整个oracle的check会被执行多次，一直是同一个oracle实例，因此oracle本身可以管理种子库
        this.queryGenerator = generator;
    }

    @Override
    public void check() throws Exception {
        //todo oracle 的检测逻辑，会被调用多次
        IClauseSequence sequence = queryGenerator.generateQuery(globalState);
        StringBuilder sb = new StringBuilder();
        sequence.toTextRepresentation(sb);
        System.out.println(sb);
        int resultLength = 0;

        byte[] branchCoverage = new byte[BRANCH_SIZE];
        byte[] branchPairCoverage = new byte[BRANCH_PAIR_SIZE];

        try {
            List<Long> results = globalState.executeStatementAndGetTime(new CypherQueryAdapter(sb.toString()));
            long time1 = Math.max(results.get(0), 1L);
            long time2 = Math.max(results.get(1), 1L);
            double retime = time1 * 1.0 / time2;
            System.out.println("相对性能差异为：" + retime);
        } catch (CompletionException e) {
            System.out.println("该Cypher查询不支持转换为Gremlin！");
            System.out.println(e.getMessage());
        }
        boolean isBugDetected = false;

        queryGenerator.addNewRecord(sequence, isBugDetected, resultLength, branchCoverage, branchPairCoverage);
    }
}
