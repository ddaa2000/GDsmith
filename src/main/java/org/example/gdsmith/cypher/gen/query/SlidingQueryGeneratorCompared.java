package org.example.gdsmith.cypher.gen.query;

import org.example.gdsmith.cypher.CypherGlobalState;
import org.example.gdsmith.cypher.ast.IClauseSequence;
import org.example.gdsmith.cypher.dsl.*;
import org.example.gdsmith.cypher.gen.GraphManager;
import org.example.gdsmith.cypher.gen.alias.RandomAliasGenerator;
import org.example.gdsmith.cypher.gen.condition.RandomConditionGenerator;
import org.example.gdsmith.cypher.gen.list.RandomListGenerator;
import org.example.gdsmith.cypher.gen.pattern.SlidingPatternGenerator;
import org.example.gdsmith.cypher.mutation.expression.*;
import org.example.gdsmith.cypher.oracle.DifferentialNonEmptyBranchOracle;
import org.example.gdsmith.cypher.schema.CypherSchema;
import org.example.gdsmith.cypher.dsl.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SlidingQueryGeneratorCompared<S extends CypherSchema<G,?>,G extends CypherGlobalState<?,S>> extends GraphGuidedQueryGenerator<S,G> {

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


    public SlidingQueryGeneratorCompared(GraphManager graphManager) {
        super(graphManager);
    }

    @Override
    public IPatternGenerator createPatternGenerator(G globalState, IIdentifierBuilder identifierBuilder) {
        return new SlidingPatternGenerator<S>(globalState.getSchema(), varToProperties, graphManager, identifierBuilder, false);
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
        writeInfoln("coverage: "+coverageNum);
        writeInfoln("non_empty_coverage: "+nonEmptyCoverageNum);
        writeInfoln("result_size: "+resultLength);
    }
}
