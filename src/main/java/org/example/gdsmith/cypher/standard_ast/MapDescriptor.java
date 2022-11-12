package org.example.gdsmith.cypher.standard_ast;

import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.gdsmith.cypher.ast.analyzer.IMapDescriptor;

import java.util.Map;

public class MapDescriptor implements IMapDescriptor {
    boolean isMapSizeUnknown; //变长的，运行时才知道长度的Map？
    Map<String, ICypherTypeDescriptor> memberTypes; //知道所有元素的Map，这里存储每个元素的类型信息
    ICypherTypeDescriptor sameType; //同质的元素类型，仅同质时有意义
    boolean isMembersWithSameType; //元素是否同质，仅变长时此字段有意义

    public MapDescriptor(Map<String, ICypherTypeDescriptor> memberTypes){
        this.memberTypes = memberTypes;
        isMapSizeUnknown = false;
        sameType = null;
        isMembersWithSameType = false;
    }

    public MapDescriptor(ICypherTypeDescriptor sameType){
        this.sameType = sameType;
        memberTypes = null;
        isMapSizeUnknown = true;
        isMembersWithSameType = true;
    }

    public MapDescriptor(){
        isMapSizeUnknown = true;
        isMembersWithSameType = false;
        sameType = null;
        memberTypes = null;
    }

    @Override
    public boolean isMapSizeUnknown() {
        return isMapSizeUnknown;
    }

    @Override
    public Map<String, ICypherTypeDescriptor> getMapMemberTypes() {
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
