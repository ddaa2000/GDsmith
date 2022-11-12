package org.example.gdsmith.arcadeDB.oracle;

import org.example.gdsmith.arcadeDB.ArcadeDBSchema;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.common.query.GDSmithResultSet;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.arcadeDB.ArcadeDBGlobalState;
import org.example.gdsmith.cypher.gen.query.RandomQueryGenerator;

public class ArcadeDBAlwaysTrueOracle implements TestOracle {

    private final ArcadeDBGlobalState globalState;
    private RandomQueryGenerator<ArcadeDBSchema, ArcadeDBGlobalState> randomQueryGenerator;

    public ArcadeDBAlwaysTrueOracle(ArcadeDBGlobalState globalState){
        this.globalState = globalState;
        //todo 整个oracle的check会被执行多次，一直是同一个oracle实例，因此oracle本身可以管理种子库
        this.randomQueryGenerator = new RandomQueryGenerator<ArcadeDBSchema, ArcadeDBGlobalState>();
    }

    @Override
    public void check() throws Exception {
        //todo oracle 的检测逻辑，会被调用多次
        IClauseSequence sequence = randomQueryGenerator.generateQuery(globalState);
        StringBuilder sb = new StringBuilder();
        sequence.toTextRepresentation(sb);
        System.out.println(sb);
        //globalState.executeStatement(new CypherQueryAdapter(sb.toString()));
        GDSmithResultSet r = globalState.executeStatementAndGet(new CypherQueryAdapter(sb.toString())).get(0);
        System.out.println(r.getResult());
    }
}
