package org.example.gdsmith.cypher;

public interface ICypherSupport {

    //clauses
    boolean supportMatch();
    boolean supportUnwind();
    boolean supportInQueryCall();

    boolean supportWith();

    boolean supportCreate();
    boolean supportMerge();
    boolean supportSet();
    boolean supportDelete();
    boolean supportRemove();

    //expressions
    boolean supportOr();
    boolean supportXor();
    boolean supportAnd();
    boolean supportNot();
    boolean supportComparison();
    boolean supportAddOrSub();
    boolean supportMulOrDiv();
    boolean supportPowerOf();
    boolean supportUnaryAddOrSub();
    boolean supportCheckNull();
    boolean supportListIn();
    boolean supportListGetByIndex();
    boolean supportListGetBySlice();
    boolean supportStringStartsWith();
    boolean supportStringEndsWith();
    boolean supportStringContains();

    //type
    boolean supportNumber();
    boolean supportString();
    boolean supportList();
    boolean supportMap();

    //query options
    boolean supportMatchWhere();
    boolean supportOptionalMatch();

    boolean supportWithDistinct();
    boolean supportWithOrderBy();
    boolean supportWithLimit();
    boolean supportWithSkip();
    boolean supportWithWhere();

    boolean supportReturnDistinct();
    boolean supportReturnOrderBy();
    boolean supportReturnLimit();
    boolean supportReturnSkip();

}
