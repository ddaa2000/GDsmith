package org.example.gdsmith.cypher.ast.analyzer;

import org.example.gdsmith.cypher.ast.*;
import org.example.gdsmith.cypher.ast.ICypherClause;
import org.example.gdsmith.cypher.ast.IIdentifier;
import org.example.gdsmith.cypher.ast.IPattern;

import java.util.List;

public interface IClauseAnalyzer extends ICypherClause {

    /**
     * 本clause定义的别名
     * @return
     */
    List<IAliasAnalyzer> getLocalAliases();

    /**
     * 本clause定义的node identifier
     * @return
     */
    List<INodeAnalyzer> getLocalNodeIdentifiers();

    /**
     * 本clause定义的relation identifier
     * @return
     */
    List<IRelationAnalyzer> getLocalRelationIdentifiers();

    List<IIdentifierAnalyzer> getLocalIdentifiers();

    /**
     * 本clause的作用域中可以使用的别名，即where中可以使用的，包括了本地定义的和继承自之前clause的
     * @return
     */
    List<IAliasAnalyzer> getAvailableAliases();

    /**
     * 本clause的作用域中可以使用的node identifier，即where中可以使用的，包括了本地定义的和继承自之前clause的
     * @return
     */
    List<INodeAnalyzer> getAvailableNodeIdentifiers();

    /**
     * 本clause的作用域中可以使用的relation identifier，即where中可以使用的，包括了本地定义的和继承自之前clause的
     * @return
     */
    List<IRelationAnalyzer> getAvailableRelationIdentifiers();

    List<IIdentifierAnalyzer> getAvailableIdentifiers();

    /**
     * 本clause继承自上一clause的别名，可以在定义pattern和alias时使用
     * @return
     */
    List<IAliasAnalyzer> getExtendableAliases();

    /**
     * 本clause继承自上一clause的node identifier，可以在定义pattern和alias时使用
     * @return
     */
    List<INodeAnalyzer> getExtendableNodeIdentifiers();

    /**
     * 本clause继承自上一clause的relation identifier，可以在定义pattern和alias时使用
     * @return
     */
    List<IRelationAnalyzer> getExtendableRelationIdentifiers();

    List<IIdentifierAnalyzer> getExtendableIdentifiers();

    /**
     * 查找当前作用域的Identifier
     * @param name Identifier名字
     * @return IdentifierAnalyzer，带有上下文信息
     */
    IIdentifierAnalyzer getIdentifierAnalyzer(String name);

    /**
     * 查找当前作用域的Identifier
     * @param identifier 想要查找的同名identifier
     * @return IdentifierAnalyzer，带有上下文信息
     */
    IIdentifierAnalyzer getIdentifierAnalyzer(IIdentifier identifier);

    /**
     * 查找本地的哪些Pattern包含了特定identifier
     * @param identifier
     * @return
     */
    List<IPattern> getLocalPatternContainsIdentifier(IIdentifier identifier);


    /**
     * 获取原语法树中的ICypherClause节点实例，可以直接修改
     * @return 可修改的ICypherClause，修改反映在原AST上
     */
    ICypherClause getSource();
}
