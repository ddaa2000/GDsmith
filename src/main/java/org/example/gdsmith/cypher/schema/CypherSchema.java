package org.example.gdsmith.cypher.schema;

import org.example.gdsmith.GlobalState;
import org.example.gdsmith.common.schema.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.ICypherType;
import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.ILabel;
import org.example.gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.gdsmith.cypher.standard_ast.CypherType;
import org.example.gdsmith.cypher.standard_ast.CypherTypeDescriptor;
import org.example.gdsmith.common.schema.AbstractSchema;
import org.example.gdsmith.common.schema.AbstractTable;
import org.example.gdsmith.cypher.ast.IType;

public abstract class CypherSchema<G extends GlobalState<?,?,?>, A extends AbstractTable<?, ?, G>> extends AbstractSchema<G, A> implements ICypherSchema {

    protected List<CypherLabelInfo> labels; //所有的Label信息
    protected List<CypherRelationTypeInfo> relationTypes; //所有的relationship type信息
    protected List<CypherPatternInfo> patternInfos; //存在的pattern结构


    //todo complete
    public CypherSchema(List<A> databaseTables, List<CypherLabelInfo> labels,
                       List<CypherRelationTypeInfo> relationTypes, List<CypherPatternInfo> patternInfos) {
        super(databaseTables);

        this.labels = labels;
        this.relationTypes = relationTypes;
        this.patternInfos = patternInfos;
    }

    public boolean containsLabel(ILabel label){
        for(ILabelInfo labelInfo : labels){
            if (labelInfo.getName().equals(label.getName())){
                return true;
            }
        }
        return false;
    }

    public ILabelInfo getLabelInfo(ILabel label){
        for(ILabelInfo labelInfo : labels){
            if (labelInfo.getName().equals(label.getName())){
                return labelInfo;
            }
        }
        return null;
    }

    public boolean containsRelationType(IType relation){
        if(relation == null){
            return false;
        }
        for(IRelationTypeInfo relationInfo : relationTypes){
            if (relationInfo != null && relationInfo.getName().equals(relation.getName())){
                return true;
            }
        }
        return false;
    }

    public IRelationTypeInfo getRelationInfo(IType relation){
        if(relation == null){
            return null;
        }
        for(IRelationTypeInfo relationInfo : relationTypes){
            if (relationInfo != null && relationInfo.getName().equals(relation.getName())){
                return relationInfo;
            }
        }
        return null;
    }


    public List<CypherLabelInfo> getLabels(){
        return labels;
    }

    public List<CypherRelationTypeInfo> getRelationTypes(){
        return relationTypes;
    }

    public static class CypherPatternInfo implements IPatternInfo {

        private List<IPatternElementInfo> patternElementInfos = new ArrayList<>();

        public CypherPatternInfo(List<IPatternElementInfo> patternElementInfos) {
            this.patternElementInfos = patternElementInfos;
        }

        @Override
        public List<IPatternElementInfo> getPattern() {
            return patternElementInfos;
        }
    }

    public static class CypherRelationTypeInfo implements IRelationTypeInfo {
        private String name;
        private List<IPropertyInfo> properties = new ArrayList<>();

        public CypherRelationTypeInfo(String name, List<IPropertyInfo> properties) {
            this.name = name;
            this.properties = properties;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<IPropertyInfo> getProperties() {
            return properties;
        }

        @Override
        public boolean hasPropertyWithType(ICypherType type) {
            for(IPropertyInfo propertyInfo : properties){
                if(propertyInfo.getType() == type){
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<IPropertyInfo> getPropertiesWithType(ICypherType type) {
            List<IPropertyInfo> returnProperties = new ArrayList<>();
            for(IPropertyInfo propertyInfo : properties){
                if(propertyInfo.getType() == type){
                    returnProperties.add(propertyInfo);
                }
            }
            return returnProperties;
        }
    }

    public static class CypherLabelInfo implements ILabelInfo {
        private String name;
        private List<IPropertyInfo> properties = new ArrayList<>();

        public CypherLabelInfo(String name, List<IPropertyInfo> properties) {
            this.name = name;
            this.properties = properties;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<IPropertyInfo> getProperties() {
            return properties;
        }

        @Override
        public boolean hasPropertyWithType(ICypherType type) {
            for(IPropertyInfo propertyInfo : properties){
                if(propertyInfo.getType() == type){
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<IPropertyInfo> getPropertiesWithType(ICypherType type) {
            List<IPropertyInfo> returnProperties = new ArrayList<>();
            for(IPropertyInfo propertyInfo : properties){
                if(propertyInfo.getType() == type){
                    returnProperties.add(propertyInfo);
                }
            }
            return returnProperties;
        }
    }

    public static class CypherPropertyInfo implements IPropertyInfo, Comparable<CypherPropertyInfo>{
        private String key;
        private CypherType type;
        private boolean isOptional;
        private int freq;

        public CypherPropertyInfo(String key, CypherType type, boolean isOptional) {
            this.key = key;
            this.type = type;
            this.isOptional = isOptional;
            this.freq = 0;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public CypherType getType() {
            return type;
        }

        @Override
        public boolean isOptional() {
            return isOptional;
        }

        public int getFreq() {
            return freq;
        }

        public void addFreq() {
            this.freq++;
        }

        @Override
        public int compareTo(CypherPropertyInfo prop) {
            return this.freq - prop.freq;
        }
    }

    public static abstract class CypherFunctionInfo implements IFunctionInfo {
        private String name;
        private List<IParamInfo> params;
        private CypherType expectedReturnType;

        public CypherFunctionInfo(String name, CypherType expectedReturnType, IParamInfo ...params){
            this.name = name;
            this.params = Arrays.asList(params);
            this.expectedReturnType = expectedReturnType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<IParamInfo> getParams() {
            return params;
        }

        @Override
        public CypherType getExpectedReturnType() {
            return expectedReturnType;
        }
    }


    public enum CypherBuiltInFunctions implements IFunctionInfo{
        AVG("avg", "avg@number", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        MAX_NUMBER("max", "max@number", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        MAX_STRING("max", "max@string", CypherType.STRING, new CypherParamInfo(CypherType.STRING, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        MIN_NUMBER("min", "min@number", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        MIN_STRING("min", "min@string", CypherType.STRING, new CypherParamInfo(CypherType.STRING, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        COUNT("count", "count", CypherType.NUMBER, new CypherParamInfo(CypherType.ANY, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return new CypherTypeDescriptor(CypherType.NUMBER);
            }
        },
        PERCENTILE_COUNT_NUMBER("percentileCount", "percentileCount@number", CypherType.NUMBER,
                new CypherParamInfo(CypherType.NUMBER, false), new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        PERCENTILE_COUNT_STRING("percentileCount", "percentileCount@string", CypherType.NUMBER,
                new CypherParamInfo(CypherType.STRING, false), new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        PERCENTILE_DISC_NUMBER("percentileDisc", "percentileDisc@number", CypherType.NUMBER,
                new CypherParamInfo(CypherType.NUMBER, false), new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        PERCENTILE_DISC_STRING("percentileDisc", "percentileDisct@string", CypherType.NUMBER,
                new CypherParamInfo(CypherType.STRING, false), new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        ST_DEV("stDev", "stDev", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        ST_DEV_P("stDevP", "stDevP", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        SUM("sum", "sum", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        ;

        CypherBuiltInFunctions(String name, String signature, CypherType expectedReturnType, IParamInfo... params){
            this.name = name;
            this.params = Arrays.asList(params);
            this.expectedReturnType = expectedReturnType;
        }

        private String name, signature;
        private List<IParamInfo> params;
        private CypherType expectedReturnType;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getSignature() {
            return signature;
        }

        @Override
        public List<IParamInfo> getParams() {
            return params;
        }

        @Override
        public CypherType getExpectedReturnType() {
            return expectedReturnType;
        }
    }


    public static class CypherParamInfo implements IParamInfo{
        private boolean isOptionalLength;
        private CypherType paramType;

        public CypherParamInfo(CypherType type, boolean isOptionalLength){
            paramType = type;
            this.isOptionalLength = isOptionalLength;
        }

        @Override
        public boolean isOptionalLength() {
            return isOptionalLength;
        }

        @Override
        public CypherType getParamType() {
            return paramType;
        }
    }

    @Override
    public List<ILabelInfo> getLabelInfos() {
        return labels.stream().map(l->l).collect(Collectors.toList());
    }

    @Override
    public List<IRelationTypeInfo> getRelationshipTypeInfos() {
        return relationTypes.stream().map(r->r).collect(Collectors.toList());
    }
}
