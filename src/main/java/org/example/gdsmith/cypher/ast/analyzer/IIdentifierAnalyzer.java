package org.example.gdsmith.cypher.ast.analyzer;

import org.example.gdsmith.cypher.ast.IExpression;
import org.example.gdsmith.cypher.ast.IIdentifier;

public interface IIdentifierAnalyzer extends IIdentifier {
    /**
     * AST中上一次对同Identifier的定义
     * @return
     */
    IIdentifierAnalyzer getFormerDef();

    /**
     * AST中对应的Identifier节点，修改会反映在原AST中
     * @return
     */
    IIdentifier getSource();

    /**
     * 获取该Identifier在AST中所属的Expression
     * @return 如果该Identifier在AST中是在Expression中出现的引用，返回该Expression，否则返回null
     */
    IExpression getSourceRefExpression();

    /**
     * 获取该Identifier所属上下文信息
     * @return
     */
    IContextInfo getContextInfo();
}
