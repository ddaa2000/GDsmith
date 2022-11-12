package org.example.gdsmith.composite.oracle;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.common.oracle.TestOracle;
import org.example.gdsmith.composite.CompositeConnection;
import org.example.gdsmith.composite.CompositeGlobalState;
import org.example.gdsmith.composite.CompositeSchema;
import org.example.gdsmith.cypher.CypherQueryAdapter;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.dsl.QueryFiller;
import org.example.gdsmith.cypher.gen.alias.RandomAliasGenerator;
import org.example.gdsmith.cypher.gen.condition.RandomConditionGenerator;
import org.example.gdsmith.cypher.gen.list.RandomListGenerator;
import org.example.gdsmith.cypher.gen.pattern.RandomPatternGenerator;
import org.example.gdsmith.cypher.gen.query.RandomQueryGenerator;
import org.example.gdsmith.cypher.standard_ast.*;
import org.example.gdsmith.cypher.standard_ast.ClauseSequence;
import org.example.gdsmith.cypher.standard_ast.IClauseSequenceBuilder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CompositeMCTSOracle implements TestOracle {
    private static CompositeGlobalState globalState;
    private static RandomQueryGenerator<CompositeSchema, CompositeGlobalState> randomQueryGenerator;
    private static final int nActions = 5;    //十个子节点
    private static final int nItes = 1000;    //迭代次数
    private static final int nRollout = 5;    //Rollout次数
    private static final double epsilon = 1e-6;
    private static final Randomly r = new Randomly();

    public static double maxDc = 0d;
    public static double maxDr = 0d;
    public static double maxDa = 0d;
    public static int numofTimeOut = 0;
    public static int numofQueries = 0;
    public static String maxSeq = null;
    public static Long maxT1 = 0L;
    public static Long maxT2 = 0L;
    public CompositeMCTSOracle(CompositeGlobalState globalState){
        CompositeMCTSOracle.globalState = globalState;
        //todo 整个oracle的check会被执行多次，一直是同一个oracle实例，因此oracle本身可以管理种子库
        randomQueryGenerator = new RandomQueryGenerator<>();
    }

    public static class TreeNode {
        public IClauseSequence seq;        //从根结点到当前节点的子句序列
        public TreeNode[] children;        //该结点的五个子节点
        public int nVisits;         //总的访问次数
        public double totValue;     //最大性能差异

        private IClauseSequence generateClauseSequence() {
            IClauseSequenceBuilder builder;
            IClauseSequence newSeq = null;
            String clause;
            if (seq == null) {
                builder = ClauseSequence.createClauseSequenceBuilder();
                clause = Randomly.fromList(Arrays.asList("MATCH", "OPTIONAL MATCH", "UNWIND"));
                switch (clause) {
                    case "MATCH":
                        newSeq = builder.MatchClause().build();
                        break;
                    case "OPTIONAL MATCH":
                        newSeq = builder.OptionalMatchClause().build();
                        break;
                    case "UNWIND":
                        newSeq = builder.UnwindClause().build();
                        break;
                }
            } else {
                builder = ClauseSequence.createClauseSequenceBuilder(seq);
                List<ICypherClause> clauseList = seq.getClauseList();
                int len = clauseList.size();
                if (clauseList.get(len - 1) instanceof IMatch) {
                    if (!((IMatch) clauseList.get(len - 1)).isOptional()) {
                        clause = Randomly.fromList(Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND"));
                        switch (clause) {
                            case "MATCH":
                                newSeq = builder.MatchClause().build();
                                break;
                            case "OPTIONAL MATCH":
                                newSeq = builder.OptionalMatchClause().build();
                                break;
                            case "WITH":
                                newSeq = builder.WithClause().build();
                                break;
                            case "UNWIND":
                                newSeq = builder.UnwindClause().build();
                                break;
                        }
                    } else {
                        clause = Randomly.fromList(Arrays.asList("OPTIONAL MATCH", "WITH"));
                        switch (clause) {
                            case "OPTIONAL MATCH":
                                newSeq = builder.OptionalMatchClause().build();
                                break;
                            case "WITH":
                                newSeq = builder.WithClause().build();
                                break;
                        }
                    }
                } else if ((clauseList.get(len - 1) instanceof IWith) || (clauseList.get(len - 1) instanceof IUnwind)) {
                    clause = Randomly.fromList(Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND"));
                    switch (clause) {
                        case "MATCH":
                            newSeq = builder.MatchClause().build();
                            break;
                        case "OPTIONAL MATCH":
                            newSeq = builder.OptionalMatchClause().build();
                            break;
                        case "WITH":
                            newSeq = builder.WithClause().build();
                            break;
                        case "UNWIND":
                            newSeq = builder.UnwindClause().build();
                            break;
                    }
                }
            }
            //todo: newSeq还可以是this.seq中最后一个clause中的sub-clause，如WHERE
            CompositeSchema schema = globalState.getSchema();
            new QueryFiller<>(newSeq,
                    new RandomPatternGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    new RandomConditionGenerator<>(schema, false),
                    new RandomAliasGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    new RandomListGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    schema, builder.getIdentifierBuilder()).startVisit();
            return newSeq;
        }

        private IClauseSequence generateQuery() {
            List<ICypherClause> clauseList = seq.getClauseList();
            int len = clauseList.size();
            if (clauseList.get(len - 1) instanceof IReturn) {
                return seq;
            } else {
                IClauseSequence sequence = null;
                IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder(seq);
                int numOfClauses = Randomly.smallNumber();
                if (clauseList.get(len - 1) instanceof IMatch) {
                    if (!((IMatch) clauseList.get(len - 1)).isOptional()) {
                        sequence = randomQueryGenerator.generateClauses(builder, numOfClauses, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND")).ReturnClause().build();
                    } else {
                        sequence = randomQueryGenerator.generateClauses(builder, numOfClauses, Arrays.asList("OPTIONAL MATCH", "WITH")).ReturnClause().build();
                    }
                } else if ((clauseList.get(len - 1) instanceof IWith) || (clauseList.get(len - 1) instanceof IUnwind)) {
                    sequence = randomQueryGenerator.generateClauses(builder, numOfClauses, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND")).ReturnClause().build();
                }
                CompositeSchema schema = globalState.getSchema();
                new QueryFiller<>(sequence,
                        new RandomPatternGenerator<>(schema, builder.getIdentifierBuilder(), false),
                        new RandomConditionGenerator<>(schema, false),
                        new RandomAliasGenerator<>(schema, builder.getIdentifierBuilder(), false),
                        new RandomListGenerator<>(schema, builder.getIdentifierBuilder(), false),
                        schema, builder.getIdentifierBuilder()).startVisit();
                return sequence;
            }
        }

        public boolean isLeaf() {  //是不是下面没有子结点
            return children == null;
        }

        public TreeNode select() {   //按照uct公式计算每个子节点，找出最大值，返回该结点。
            TreeNode selected = null;
            double bestValue = -Double.MAX_VALUE;
            for (TreeNode c: children) {      //计算每个孩子的uct的值
                double uctValue = c.totValue / (c.nVisits + epsilon) +
                        Math.sqrt(2 * Math.log(nVisits + 1) / (c.nVisits + epsilon));
                if (uctValue > bestValue) {
                    selected = c;
                    bestValue = uctValue;
                }
            }
            return selected;
        }

        public void expand() {  //扩展当前结点的5个孩子结点
            children = new TreeNode[nActions];  //扩展当前结点的子节点，扩展5个孩子
            for(int i = 0; i < nActions; i++) {
                children[i] = new TreeNode();  //对于一个类的数组，中间每一个都要进行初始化
                children[i].seq = generateClauseSequence();
                children[i].nVisits = 0;
            }
        }

        public double rollOut() throws Exception {
            double maxReward = 0d;
            for (int i = 0; i < nRollout; i++) {
                System.out.println("当前Rollout次数为：" + i);
                IClauseSequence query = generateQuery();
                StringBuilder sb = new StringBuilder();
                query.toTextRepresentation(sb);
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
                if (reward > maxReward) {
                    maxReward = reward;
                }
                numofQueries++;
                if (time1 > CompositeConnection.TIMEOUT && time2 > CompositeConnection.TIMEOUT) {
                    numofTimeOut++;
                }
            }
            System.out.println("相对性能差异为：" + maxDr);
            System.out.println("绝对性能差异为：" + maxDa);
            System.out.println("当前最大结合性能差异为：" + maxDc);
            return maxReward;
        }

        public void updateState(double value) {
            nVisits++;   // 该节点的访问次数+1
            totValue = value; //该节点的性能差异最大数据更新
        }

        public void selectAction() throws Exception {  //这里是最关键的函数
            List<TreeNode> visited = new LinkedList<>(); //存储访问路径上面的结点
            TreeNode cur = this; //当前结点
            visited.add(this);
            while(!cur.isLeaf()){  //如果当前结点不是最底层节点
                cur = cur.select();  //往下走，把当前结点设置为uct最大的那个子结点
                visited.add(cur);   //把选择过的结点都加到visited队列里面
            }
            if (cur.nVisits > 0) {
                cur.expand();
                cur = cur.select();
                visited.add(cur);
            }
            double value = cur.rollOut();
            for (TreeNode node: visited){    //搜索路径上面的每个结点都要重新更新值
                node.updateState(value);
            }
        }
    }

    @Override
    public void check() throws Exception {
        TreeNode tree = new TreeNode();
        tree.totValue = 0d;
        tree.nVisits = 1;
        for (int i = 0; i < nItes; i++) {
            System.out.println("**********************************************************");
            System.out.println("当前迭代次数为：" + i);
            tree.selectAction();
        }
    }
}
