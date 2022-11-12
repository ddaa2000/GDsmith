package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.gdsmith.cypher.ast.analyzer.IListDescriptor;

import java.util.List;

public class ListDescriptor implements IListDescriptor {
    boolean isListSizeUnknown; //变长的，运行时才知道长度的List？
    List<ICypherTypeDescriptor> memberTypes; //知道所有元素的List，这里存储每个元素的类型信息
    ICypherTypeDescriptor sameType; //同质的元素类型，仅同质时有意义
    boolean isMembersWithSameType; //元素是否同质，仅变长时此字段有意义

    public ListDescriptor(List<ICypherTypeDescriptor> memberTypes){
        this.memberTypes = memberTypes;
        isListSizeUnknown = false;
        sameType = null;
        isMembersWithSameType = false;
    }

    public ListDescriptor(ICypherTypeDescriptor sameType){
        this.sameType = sameType;
        memberTypes = null;
        isListSizeUnknown = true;
        isMembersWithSameType = true;
    }

    public ListDescriptor(){
        isListSizeUnknown = true;
        isMembersWithSameType = false;
        sameType = null;
        memberTypes = null;
    }

    @Override
    public boolean isListLengthUnknown() {
        return isListSizeUnknown;
    }

    @Override
    public List<ICypherTypeDescriptor> getListMemberTypes() {
        return memberTypes;
    }

    @Override
    public boolean isMembersWithSameType() {
        return isMembersWithSameType;
    }

    @Override
    public ICypherTypeDescriptor getSameMemberType() {
        return sameType;
    }
}
