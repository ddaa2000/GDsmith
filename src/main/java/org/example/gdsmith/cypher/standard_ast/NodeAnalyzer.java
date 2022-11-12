package org.example.gdsmith.cypher.standard_ast;


import org.example.gdsmith.cypher.ICypherSchema;
import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.analyzer.IContextInfo;
import org.example.gdsmith.cypher.ast.analyzer.INodeAnalyzer;
import org.example.gdsmith.cypher.schema.ILabelInfo;
import org.example.gdsmith.cypher.schema.IPropertyInfo;
import org.example.gdsmith.cypher.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NodeAnalyzer extends NodeIdentifier implements INodeAnalyzer {
    INodeAnalyzer formerDef = null;
    INodeIdentifier source;
    IExpression sourceExpression;
    IContextInfo contextInfo; //创建analyzer时的上下文快照

    NodeAnalyzer(INodeIdentifier nodeIdentifier, IContextInfo contextInfo){
        this(nodeIdentifier, contextInfo, null);
    }

    NodeAnalyzer(INodeIdentifier nodeIdentifier, IContextInfo contextInfo, IExpression sourceExpression) {
        super(nodeIdentifier.getName(), nodeIdentifier.getLabels(), nodeIdentifier.getProperties());
        source = nodeIdentifier;
        this.sourceExpression = sourceExpression;
        this.contextInfo = contextInfo;
    }

    @Override
    public INodeIdentifier getSource() {
        return source;
    }

    @Override
    public IExpression getSourceRefExpression() {
        return sourceExpression;
    }

    @Override
    public IContextInfo getContextInfo() {
        return contextInfo;
    }

    @Override
    public INodeAnalyzer getFormerDef() {
        return formerDef;
    }


    @Override
    public void setFormerDef(INodeAnalyzer formerDef) {
        this.formerDef = formerDef;
    }

    @Override
    public List<ILabel> getAllLabelsInDefChain() {
        List<ILabel> labels = new ArrayList<>(this.labels);
        if(formerDef != null){
            labels.addAll(formerDef.getLabels());
            labels = labels.stream().distinct().collect(Collectors.toList());
        }
        return labels;
    }

    @Override
    public List<IProperty> getAllPropertiesInDefChain() {
        List<IProperty> properties = new ArrayList<>(this.properties);
        if(formerDef != null){
            properties.addAll(formerDef.getProperties());
        }
        return properties;
    }

    @Override
    public List<IPropertyInfo> getAllPropertiesAvailable(ICypherSchema schema) {
        List<IPropertyInfo> propertyInfos = new ArrayList<>();
        for(ILabel label : getAllLabelsInDefChain()){
            if(schema.containsLabel(label)){
                ILabelInfo labelInfo = schema.getLabelInfo(label);
                propertyInfos.addAll(labelInfo.getProperties());
            }
        }
        return propertyInfos;
    }

    @Override
    public List<IPropertyInfo> getAllPropertiesWithType(ICypherSchema schema, ICypherType type) {
        return getAllPropertiesAvailable(schema).stream().filter(p->p.getType()==type).collect(Collectors.toList());
    }


}
