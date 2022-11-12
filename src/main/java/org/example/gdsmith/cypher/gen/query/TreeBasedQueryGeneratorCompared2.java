package org.example.gdsmith.cypher.gen.query;

import org.example.gdsmith.Randomly;
import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.dsl.*;
import org.example.gdsmith.cypher.dsl.*;
import org.example.gdsmith.cypher.gen.alias.RandomAliasGenerator;
import org.example.gdsmith.cypher.gen.condition.RandomConditionGenerator;
import org.example.gdsmith.cypher.gen.list.RandomListGenerator;
import org.example.gdsmith.cypher.gen.SubgraphManager;
import org.example.gdsmith.cypher.gen.pattern.GuidedPatternGenerator;
import org.example.gdsmith.cypher.mutation.ClauseScissorsMutator;
import org.example.gdsmith.cypher.mutation.LimitExpandingMutator;
import org.example.gdsmith.cypher.mutation.OptionalAdditionMutator;
import org.example.gdsmith.cypher.mutation.WhereRemovalMutator;
import org.example.gdsmith.cypher.mutation.expression.*;
import org.example.gdsmith.cypher.mutation.expression.*;
import org.example.gdsmith.cypher.oracle.DifferentialNonEmptyBranchOracle;
import org.example.gdsmith.cypher.schema.CypherSchema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TreeBasedQueryGeneratorCompared2<S extends CypherSchema<G,?>,G extends CypherGlobalState<?,S>> extends SubgraphGuidedQueryGenerator<S,G> {

    //cannot work on concurrent mode
    public static boolean[] totalCoverage = new boolean[DifferentialNonEmptyBranchOracle.BRANCH_SIZE];
    public static boolean[] totalNonEmptyCoverage = new boolean[DifferentialNonEmptyBranchOracle.BRANCH_SIZE];

    public static File coverageFile;

    public static FileOutputStream outputStream;

    public static int coverageNum = 0;
    public static int nonEmptyCoverageNum = 0;

    static {
        coverageFile = new File("coverage_log");
        if(coverageFile.exists()){
            coverageFile.delete();
        }
        try {
            coverageFile.createNewFile();
            outputStream = new FileOutputStream(coverageFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Seed{
        IClauseSequence sequence;
        boolean bugDetected;
        int resultLength;

        byte[] branchInfo;
        byte[] branchPairInfo;

        public Seed(IClauseSequence sequence, boolean bugDetected, int resultLength, byte[] branchInfo, byte[] branchPairInfo){
            this.sequence = sequence;
            this.bugDetected = bugDetected;
            this.resultLength = resultLength;
            this.branchInfo = branchInfo;
            this.branchPairInfo = branchPairInfo;
        }
    }



    protected List<Seed> seeds = new ArrayList<>();


    public TreeBasedQueryGeneratorCompared2(SubgraphManager subgraphManager) {
        super(subgraphManager);
    }

    @Override
    public IPatternGenerator createPatternGenerator(G globalState, IIdentifierBuilder identifierBuilder) {
        return new GuidedPatternGenerator<S>(globalState.getSchema(), varToProperties, subgraphManager, identifierBuilder, false);
    }

    @Override
    public IConditionGenerator createConditionGenerator(G globalState) {
        return new RandomConditionGenerator<>(globalState.getSchema(), false);
    }

    @Override
    public IAliasGenerator createAliasGenerator(G globalState, IIdentifierBuilder identifierBuilder) {
        return new RandomAliasGenerator<>(globalState.getSchema(), identifierBuilder, false);
    }

    @Override
    public IListGenerator createListGenerator(G globalState, IIdentifierBuilder identifierBuilder) {
        return new RandomListGenerator<>(globalState.getSchema(), identifierBuilder, false);
    }
    @Override
    public boolean shouldDoMutation(G globalState) {
        return false;
//        return new Randomly().getInteger(0, 100) < seeds.size();
    }

    @Override
    public IClauseSequence doMutation(G globalState) {
        Randomly r = new Randomly();
        IClauseSequence sequence = null;
        S schema = globalState.getSchema();
        IClauseSequence seedSeq = seeds.get(r.getInteger(0, seeds.size())).sequence;
        IClauseSequence sequenceCopy = seedSeq.getCopy();
        int kind = r.getInteger(0, 9);
        switch (kind){
            case 0:{
                new ClauseScissorsMutator<S>(sequenceCopy).mutate();
                return sequenceCopy;
            }
            case 1:{
                new LimitExpandingMutator(sequenceCopy).mutate();
                return sequenceCopy;
            }
            case 2:{
                new OptionalAdditionMutator(sequenceCopy).mutate();
                return sequenceCopy;
            }
            case 3:{
                new WhereRemovalMutator<>(sequenceCopy).mutate();
                return sequenceCopy;
            }
            case 4:{
                new AndRemovalMutator(sequenceCopy).mutate();
                return sequenceCopy;
            }
            case 5:{
                new ComparisonReverseMutator(sequenceCopy).mutate();
                return sequenceCopy;
            }
            case 6:{
                new ConditionReverseMutator(sequenceCopy).mutate();
                return sequenceCopy;
            }
            case 7:{
                new OrRemovalMutator(sequenceCopy).mutate();
                return sequenceCopy;
            }
            case 8:{
                new StringMatchReductionMutator(sequenceCopy).mutate();
                return sequenceCopy;
            }
            default:
                throw new RuntimeException();
        }
    }

    public static void writeInfoln(String info){
        try {
            outputStream.write((""+System.currentTimeMillis()+"\n"+info+"\n").getBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean checkAndRecordUncoveredBranch(byte[] branchInfo){
        boolean found = false;
        for(int i = 0; i < DifferentialNonEmptyBranchOracle.BRANCH_SIZE; i++){
            if(branchInfo[i] != 0 && !totalCoverage[i]){
                found = true;
                totalCoverage[i] = true;
                coverageNum++;
            }
        }
        return found;
    }

    public static boolean checkAndRecordUncoveredNonEmptyBranch(byte[] branchInfo){
        boolean found = false;
        for(int i = 0; i < DifferentialNonEmptyBranchOracle.BRANCH_SIZE; i++){
            if(branchInfo[i] != 0 && !totalNonEmptyCoverage[i]){
                found = true;
                totalNonEmptyCoverage[i] = true;
                nonEmptyCoverageNum++;
            }
        }

        return found;
    }

    public static boolean checkPossibleUncoveredNonEmptyBranch(byte[] branchInfo){
        boolean found = false;
        int coverageNum = 0;
        for(int i = 0; i < DifferentialNonEmptyBranchOracle.BRANCH_SIZE; i++){
            if(branchInfo[i] != 0 && !totalNonEmptyCoverage[i]){
                found = true;
            }
            if(totalNonEmptyCoverage[i]){
                coverageNum++;
            }
        }
        return found;
    }

    public void reduceSeeds(){
        List<Seed> deletedSeeds = new ArrayList<>();
        for(Seed seed : seeds){
            if(!checkPossibleUncoveredNonEmptyBranch(seed.branchInfo)){
                deletedSeeds.add(seed);
            }
        }
        seeds.removeAll(deletedSeeds);
    }

    @Override
    public void addNewRecord(IClauseSequence sequence, boolean bugDetected, int resultLength, byte[] branchInfo, byte[] branchPairInfo){
        boolean hasNewBranch = checkAndRecordUncoveredBranch(branchInfo);
        if(resultLength != 0){
            boolean hasPossibleNonEmptyBranch = checkAndRecordUncoveredNonEmptyBranch(branchInfo);
            reduceSeeds();
        }
        else{
//            for (SubgraphTreeNode treeNode : subgraphManager.getTreeNodes()) {
//                treeNode.printInfo();
//            }
            boolean hasPossibleNonEmptyBranch = checkPossibleUncoveredNonEmptyBranch(branchInfo);
            if(hasPossibleNonEmptyBranch){
//                seeds.add(new Seed(sequence, bugDetected, resultLength, branchInfo, branchPairInfo));
            }
        }
        writeInfoln("coverage: "+coverageNum);
        writeInfoln("non_empty_coverage: "+nonEmptyCoverageNum);
        writeInfoln("result_size: "+resultLength);
    }
}
