package org.example.gdsmith.cypher.oracle;

import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.dsl.IQueryGenerator;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.exceptions.ResultMismatchException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletionException;

public class DifferentialNonEmptyBranchOracle <G extends CypherGlobalState<?,S>, S extends CypherSchema<G,?>> implements TestOracle {

    private final G globalState;
    private IQueryGenerator<S, G> queryGenerator;

    public static final int BRANCH_PAIR_SIZE = 65536;
    public static final  int BRANCH_SIZE = 1000000;

    public static final int PORT = 9009;
    public static final byte CLEAR = 1, PRINT_MEM = 2;


    public DifferentialNonEmptyBranchOracle(G globalState, IQueryGenerator<S,G> generator){
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
        List<GDSmithResultSet> results;
        int resultLength = 0;

        byte[] branchCoverage = new byte[BRANCH_SIZE];
        byte[] branchPairCoverage = new byte[BRANCH_PAIR_SIZE];

        try {
//            if(globalState.getOptions().getCoverage_port() != 0){
//                try(Socket clientSocket = new Socket("127.0.0.1", globalState.getOptions().getCoverage_port())){
//                    OutputStream os = clientSocket.getOutputStream();
//                    InputStream is = clientSocket.getInputStream();
//                    os.write(CLEAR);
//                    os.flush();
//                    is.readAllBytes();
//                    clientSocket.shutdownInput();
//                    clientSocket.shutdownOutput();
//                }
//            }


            results = globalState.executeStatementAndGet(new CypherQueryAdapter(sb.toString()));        //todo 判断不同的resultSet返回是否一致

            if(globalState.getOptions().getCoverage_port() != 0) {
                try (Socket clientSocket = new Socket("127.0.0.1", globalState.getOptions().getCoverage_port())){
                    OutputStream os = clientSocket.getOutputStream();
                    InputStream is = clientSocket.getInputStream();
                    os.write(PRINT_MEM);
                    os.flush();


                    byte[] allBytes = is.readAllBytes();
                    for(int j = 0; j < BRANCH_PAIR_SIZE; j++){
                        branchPairCoverage[j] = allBytes[j];
                    }

                    int coveredBranch = 0;
                    for(int j = 0; j < BRANCH_SIZE; j++){
                        branchCoverage[j] = allBytes[j+BRANCH_PAIR_SIZE];
                        if(allBytes[j+BRANCH_PAIR_SIZE] != 0){
                            coveredBranch++;
                        }
                    }
                    System.out.println(""+coveredBranch+"\n");
                    clientSocket.shutdownInput();
                    clientSocket.shutdownOutput();
                }
            }

            boolean found = false;
            StringBuilder msgSb = new StringBuilder();
            for(int i = 1; i < results.size(); i++) {
                if (!results.get(i).compareWithOutOrder(results.get(i - 1))) {
                    if(!found){
                        msgSb.append("The contents of the result sets mismatch!\n");
                        found = true;
                    }
                    String msg = "";
                    msg = msg + "Difference between " + (i-1) + " and " + i;
                    msg = msg + "First: " + results.get(i - 1).getRowNum() + " --- " + results.get(i - 1).resultToStringList() + "\n";
                    msg = msg + "Second: " + results.get(i).getRowNum() + " --- " + results.get(i).resultToStringList() + "\n";
                    msgSb.append(msg);
                }
            }
            if(found){
                throw new ResultMismatchException(msgSb.toString());
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

        queryGenerator.addNewRecord(sequence, isBugDetected, resultLength, branchCoverage, branchPairCoverage);
    }
}
