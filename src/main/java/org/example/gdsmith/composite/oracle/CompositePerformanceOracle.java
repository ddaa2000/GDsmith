package org.example.gdsmith.composite.oracle;

import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.composite.CompositeConnection;
import org.example.gdsmith.composite.CompositeGlobalState;
import org.example.gdsmith.composite.CompositeSchema;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.dsl.IQueryGenerator;
import org.example.gdsmith.cypher.dsl.QueryFiller;
import org.example.gdsmith.cypher.gen.alias.RandomAliasGenerator;
import org.example.gdsmith.cypher.gen.condition.RandomConditionGenerator;
import org.example.gdsmith.cypher.gen.list.RandomListGenerator;
import org.example.gdsmith.cypher.gen.pattern.RandomPatternGenerator;
import org.example.gdsmith.cypher.standard_ast.ClauseSequence;
import org.example.gdsmith.cypher.standard_ast.IClauseSequenceBuilder;

import java.util.*;

public class CompositePerformanceOracle implements TestOracle {

    private final CompositeGlobalState globalState;
    private final IQueryGenerator<CompositeSchema, CompositeGlobalState> queryGenerator;
    public static final int groupsize = 100000;	//染色体数（群体中个体数）

    public List<WCInput> group = new ArrayList<>();

    public static int numofTimeOut = 0;
    public static double maxDc = 0d;
    public static double maxDr = 0d;
    public static double maxDa = 0d;
    public static int numofQueries = 0;
    public static String maxSeq = null;
    public static Long maxT1 = 0L;
    public static Long maxT2 = 0L;

    public static class WCInput implements Comparable{
        IClauseSequence seq;
        List<Long> results;

        public WCInput(IClauseSequence seq, List<Long> results){
            this.seq = seq;
            this.results = results;
        }

        @Override
        public int compareTo(Object o) {
            WCInput o1 = (WCInput)o;
            double result1 = this.results.get(1) * 1.0 / this.results.get(0);
            double result2 = o1.results.get(1) * 1.0 / o1.results.get(0);
            if (result1 - result2 >= 0) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public CompositePerformanceOracle(CompositeGlobalState globalState){
        this.globalState = globalState;
        //todo 整个oracle的check会被执行多次，一直是同一个oracle实例，因此oracle本身可以管理种子库
        this.queryGenerator = globalState.getDbmsSpecificOptions().getQueryGenerator();
    }

    public void generateQueries() throws Exception {
        CompositeSchema schema = globalState.getSchema();

        for (int i=0; i<groupsize; i++) {
            IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder();
            //IClauseSequence sequence = builder.MatchClause().OptionalMatchClause().WithClause().ReturnClause().build();
            IClauseSequence sequence = queryGenerator.generateQuery(globalState);
            new QueryFiller<>(sequence,
                    new RandomPatternGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    new RandomConditionGenerator<>(schema, false),
                    new RandomAliasGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    new RandomListGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    schema, builder.getIdentifierBuilder()).startVisit();
            StringBuilder sb = new StringBuilder();
            sequence.toTextRepresentation(sb);
            System.out.println("当前查询为：" + sb);
            List<Long> results = globalState.executeStatementAndGetTime(new CypherQueryAdapter(sb.toString()));
            if (results.get(0) < 0 || results.get(1) < 0) {
                continue;
            }
            long time1 = Math.max(results.get(0), 1L); //todo
            long time2 = Math.max(results.get(1), 1L);
            double retime = time1 * 1.0 / time2;
            System.out.println("相对性能差异为：" + retime);
            double abtime = time1 * 1.0 - time2;
            System.out.println("绝对性能差异为：" + abtime);
            double reward = Math.sqrt(retime) * Math.sqrt(abtime);
            if (reward > maxDc) {
                maxDc = reward;
                maxDr = retime;
                maxDa = abtime;
                maxSeq = String.valueOf(sb);
                maxT1 = time1;
                maxT2 = time2;
            }
            numofQueries++;
            if (time1 > CompositeConnection.TIMEOUT && time2 > CompositeConnection.TIMEOUT) {
                numofTimeOut++;
            }
        }
    }
    public List<IClauseSequence> selectQueries() throws Exception {
        return null; //todo
    }

    public List<IClauseSequence> crossoverQueries(List<IClauseSequence> parents) throws Exception {
        return null; //todo
    }

    public List<IClauseSequence> mutateQueries(List<IClauseSequence> parents) throws Exception {
        return null; //todo
    }

    public List<WCInput> calculateFitness(List<IClauseSequence> parents) throws Exception {
        return null; //todo
    }

    public void updateGroup(List<WCInput> results) throws Exception {
        //todo
    }

    @Override
    public void check() throws Exception {
        generateQueries();  //初始化种群
        //Collections.sort(group);
        /*System.out.println("***************************100轮最终结果");
        for (int i=0; i<5; i++) {
            StringBuilder sb = new StringBuilder();
            group.get(i).seq.toTextRepresentation(sb);
            System.out.println(sb + " Results: " + group.get(i).results);
        }
        for (int i=0; i<5; i++) {
            StringBuilder sb = new StringBuilder();
            group.get(group.size()-1-i).seq.toTextRepresentation(sb);
            System.out.println(sb + " Results: " + group.get(group.size()-1-i).results);
        }
        for (int i=0; i<ITERA; i++) { //EA迭代
            List<IClauseSequence> chroms = selectQueries(); //选择
            chroms = crossoverQueries(chroms); //交叉
            chroms = mutateQueries(chroms); //变异
            List<WCInput> results = calculateFitness(chroms); //执行查询，计算适应值
            updateGroup(results); //更新种群
        }

        for (int i=0; i<groupsize; i++) {
            //输出worst-case inputs
        }*/
    }
}
