package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.dsl.*;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.dsl.IIdentifierBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClauseSequence implements IClauseSequence {

    List<ICypherClause> clauses = new ArrayList<>();
    IIdentifierBuilder identifierBuilder;

    public ClauseSequence(IIdentifierBuilder identifierBuilder){
        this.identifierBuilder = identifierBuilder;
    }

    public static IClauseSequenceBuilder createClauseSequenceBuilder() {
        return new ClauseSequenceBuilder();
    }

    public static ClauseSequenceBuilder createClauseSequenceBuilder(IClauseSequence sequence) {
        if(!(sequence instanceof ClauseSequence)){
            throw new RuntimeException();
        }
        return new ClauseSequenceBuilder((ClauseSequence) sequence);
    }

    @Override
    public List<ICypherClause> getClauseList() {
        return clauses;
    }

    @Override
    public IIdentifierBuilder getIdentifierBuilder(){
        return identifierBuilder;
    }

    @Override
    public void setClauseList(List<ICypherClause> clauses) {
        this.clauses = clauses;
    }

    @Override
    public ClauseSequence getCopy() {
        ClauseSequence clauseSequence = new ClauseSequence(identifierBuilder.getCopy());
        clauses.stream().forEach(c->{clauseSequence.addClause(c.getCopy());});
        return clauseSequence;
    }

    public void addClause(ICypherClause clause){
        if(clauses.size() != 0 ) {
            clauses.get(clauses.size() - 1).setNextClause(clause);
        }
        clauses.add(clause);
    }

    public void addClauseAt(ICypherClause clause, int index){
        if(index != 0 ) {
            clauses.get(index - 1).setNextClause(clause);
        }
        clauses.add(index, clause);
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        for(int i = 0; i < clauses.size(); i ++){
            clauses.get(i).toTextRepresentation(sb);
            if(i != clauses.size() - 1){
                sb.append(" ");
            }
        }
    }

    public static class ClauseSequenceBuilder implements IClauseSequenceBuilder, IClauseSequenceBuilder.IOngoingReturn,
            IClauseSequenceBuilder.IOngoingWith, IClauseSequenceBuilder.IOngoingMatch{

        protected final IdentifierBuilder identifierBuilder;
        private final ClauseSequence clauseSequence;

        @Override
        public ClauseSequenceBuilder orderBy(boolean isDesc, IExpression...expressions) {
            ICypherClause clause = clauseSequence.clauses.get(clauseSequence.clauses.size()-1);
            if(clause instanceof IWith){
                ((IWith) clause).setOrderBy(Arrays.asList(expressions), isDesc);
            }
            if(clause instanceof IReturn){
                ((IReturn) clause).setOrderBy(Arrays.asList(expressions), isDesc);
            }
            return this;
        }

        @Override
        public ClauseSequenceBuilder limit(IExpression expression) {
            ICypherClause clause = clauseSequence.clauses.get(clauseSequence.clauses.size()-1);
            if(clause instanceof IWith){
                ((IWith) clause).setLimit(expression);
            }
            if(clause instanceof IReturn){
                ((IReturn) clause).setLimit(expression);
            }
            return this;
        }

        @Override
        public ClauseSequenceBuilder skip(IExpression expression) {
            ICypherClause clause = clauseSequence.clauses.get(clauseSequence.clauses.size()-1);
            if(clause instanceof IWith){
                ((IWith) clause).setLimit(expression);
            }
            if(clause instanceof IReturn){
                ((IReturn) clause).setLimit(expression);
            }
            return this;
        }

        @Override
        public ClauseSequenceBuilder distinct() {
            ICypherClause clause = clauseSequence.clauses.get(clauseSequence.clauses.size()-1);
            if(clause instanceof IWith){
                ((IWith) clause).setDistinct(true);
            }
            if(clause instanceof IReturn){
                ((IReturn) clause).setDistinct(true);
            }
            return this;
        }

        public static class IdentifierBuilder implements IIdentifierBuilder {
            public int nodeNum = 0, relationNum = 0, aliasNum = 0;

            private IdentifierBuilder(){

            }

            public String getNewNodeName(){
                nodeNum++;
                return "n"+(nodeNum - 1);
            }

            public String getNewRelationName(){
                relationNum++;
                return "r"+(relationNum - 1);
            }

            public String getNewAliasName(){
                aliasNum++;
                return "a"+(aliasNum - 1);
            }

            @Override
            public IIdentifierBuilder getCopy() {
                IdentifierBuilder identifierBuilder = new IdentifierBuilder();
                identifierBuilder.nodeNum = this.nodeNum;
                identifierBuilder.aliasNum = this.aliasNum;
                identifierBuilder.relationNum = this.relationNum;
                return identifierBuilder;
            }
        }

        private ClauseSequenceBuilder(){
            identifierBuilder = new IdentifierBuilder();
            clauseSequence = new ClauseSequence(identifierBuilder);
        }

        /**
         * 从一个sequence开始继续生成，sequence必须是一个查询语句
         * @param sequence
         */
        private ClauseSequenceBuilder(ClauseSequence sequence){
            clauseSequence = (ClauseSequence) sequence.getCopy();
            identifierBuilder = (IdentifierBuilder) clauseSequence.identifierBuilder;
            if(clauseSequence.clauses.get(clauseSequence.clauses.size()-1) instanceof IReturn){
                clauseSequence.clauses.remove(clauseSequence.clauses.size()-1);
            }
        }


        @Override
        public IIdentifierBuilder getIdentifierBuilder() {
            return identifierBuilder;
        }

        public IOngoingMatch MatchClause(){
            return MatchClause(null);
        }

        public IOngoingMatch MatchClause(IExpression condition, IPattern...patternTuple){
            IMatch match = new Match();
            match.setPatternTuple(Arrays.asList(patternTuple));
            match.setCondition(condition);
            clauseSequence.addClause(match);
            return this;
        }

        public IOngoingMatch OptionalMatchClause(){
            return OptionalMatchClause(null);
        }

        public IOngoingMatch OptionalMatchClause(IExpression condition, IPattern...patternTuple){
            IMatch match = new Match();
            match.setPatternTuple(Arrays.asList(patternTuple));
            match.setCondition(condition);
            match.setOptional(true);
            clauseSequence.addClause(match);
            return this;
        }

        @Override
        public IClauseSequenceBuilder UnwindClause() {
            return UnwindClause(null);
        }

        @Override
        public IClauseSequenceBuilder UnwindClause(IRet listAsAlias) {
            IUnwind unwind = new Unwind();
            unwind.setListAsAliasRet(listAsAlias);
            clauseSequence.addClause(unwind);
            return this;
        }

        public IOngoingWith WithClause(){
            return WithClause(null);
        }


        public IOngoingWith WithClause(IExpression condition, IRet ...aliasTuple){
            IWith with = new With();
            with.setCondition(condition);
            with.setReturnList(Arrays.asList(aliasTuple));
            clauseSequence.addClause(with);
            return this;
        }

        /*public ClauseSequence build(IConditionGenerator conditionGenerator, IAliasGenerator aliasGenerator,
                                    IPatternGenerator patternGenerator, Neo4jSchema schema){
            new QueryFiller(clauseSequence, patternGenerator, conditionGenerator, aliasGenerator,
                    schema, identifierBuilder).startVisit();
            return clauseSequence;
        }*/

        public ClauseSequence build(){
            return clauseSequence;
        }

        @Override
        public int getLength() {
            return clauseSequence.getClauseList().size();
        }


        public IOngoingReturn ReturnClause(IRet ...returnList){
            IReturn returnClause = new Return();
            returnClause.setReturnList(Arrays.asList(returnList));
            clauseSequence.addClause(returnClause);
            return this;
        }

        public ClauseSequenceBuilder CreateClause(IPattern pattern){
            ICreate create = new Create();
            create.setPattern(pattern);
            clauseSequence.addClause(create);
            return this;
        }

        public ClauseSequenceBuilder CreateClause(){
            return CreateClause(null);
        }

        public ClauseSequenceBuilder MergeClause(IPattern pattern){
            IMerge merge = new Merge();
            merge.setPattern(pattern);
            clauseSequence.addClause(merge);
            return this;
        }

        public ClauseSequenceBuilder MergeClause(){
            return MergeClause(null);
        }

    }

}
