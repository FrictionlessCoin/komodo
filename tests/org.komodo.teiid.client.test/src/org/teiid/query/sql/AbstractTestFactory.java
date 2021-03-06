/*
 * JBoss, Home of Professional Open Source.
* See the COPYRIGHT.txt file distributed with this work for information
* regarding copyright ownership. Some portions may be licensed
* to Red Hat, Inc. under one or more contributor license agreements.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
* 02110-1301 USA.
*/
package org.teiid.query.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.komodo.spi.query.sql.lang.JoinType.Types;
import org.komodo.spi.query.sql.lang.SPParameter;
import org.komodo.spi.query.sql.lang.SetQuery.Operation;
import org.teiid.core.types.DefaultDataTypeManager;
import org.teiid.query.parser.TCQueryParser;
import org.teiid.query.parser.TeiidNodeFactory.ASTNodes;
import org.teiid.query.sql.lang.ArrayTableImpl;
import org.teiid.query.sql.lang.BetweenCriteriaImpl;
import org.teiid.query.sql.lang.CommandImpl;
import org.teiid.query.sql.lang.CompareCriteriaImpl;
import org.teiid.query.sql.lang.CompoundCriteriaImpl;
import org.teiid.query.sql.lang.CreateImpl;
import org.teiid.query.sql.lang.CriteriaImpl;
import org.teiid.query.sql.lang.CriteriaOperator.Operator;
import org.teiid.query.sql.lang.DeleteImpl;
import org.teiid.query.sql.lang.DropImpl;
import org.teiid.query.sql.lang.DynamicCommandImpl;
import org.teiid.query.sql.lang.ExistsCriteriaImpl;
import org.teiid.query.sql.lang.ExpressionCriteriaImpl;
import org.teiid.query.sql.lang.FromClauseImpl;
import org.teiid.query.sql.lang.FromImpl;
import org.teiid.query.sql.lang.GroupByImpl;
import org.teiid.query.sql.lang.InsertImpl;
import org.teiid.query.sql.lang.IntoImpl;
import org.teiid.query.sql.lang.IsNullCriteriaImpl;
import org.teiid.query.sql.lang.JoinPredicateImpl;
import org.teiid.query.sql.lang.JoinTypeImpl;
import org.teiid.query.sql.lang.MatchCriteriaImpl;
import org.teiid.query.sql.lang.NamespaceItem;
import org.teiid.query.sql.lang.NotCriteriaImpl;
import org.teiid.query.sql.lang.OrderByImpl;
import org.teiid.query.sql.lang.OrderByItemImpl;
import org.teiid.query.sql.lang.ProjectedColumnImpl;
import org.teiid.query.sql.lang.QueryCommandImpl;
import org.teiid.query.sql.lang.QueryImpl;
import org.teiid.query.sql.lang.SPParameterImpl;
import org.teiid.query.sql.lang.SelectImpl;
import org.teiid.query.sql.lang.SetClauseImpl;
import org.teiid.query.sql.lang.SetClauseListImpl;
import org.teiid.query.sql.lang.SetCriteriaImpl;
import org.teiid.query.sql.lang.SetQueryImpl;
import org.teiid.query.sql.lang.StoredProcedureImpl;
import org.teiid.query.sql.lang.SubqueryCompareCriteriaImpl;
import org.teiid.query.sql.lang.SubqueryCompareCriteriaImpl.PredicateQuantifier;
import org.teiid.query.sql.lang.SubqueryFromClauseImpl;
import org.teiid.query.sql.lang.SubquerySetCriteriaImpl;
import org.teiid.query.sql.lang.TextColumnImpl;
import org.teiid.query.sql.lang.TextTableImpl;
import org.teiid.query.sql.lang.UnaryFromClauseImpl;
import org.teiid.query.sql.lang.UpdateImpl;
import org.teiid.query.sql.lang.WithQueryCommandImpl;
import org.teiid.query.sql.lang.XMLColumnImpl;
import org.teiid.query.sql.lang.XMLTableImpl;
import org.teiid.query.sql.proc.AssignmentStatementImpl;
import org.teiid.query.sql.proc.BlockImpl;
import org.teiid.query.sql.proc.BranchingStatementImpl;
import org.teiid.query.sql.proc.BranchingStatementImpl.BranchingMode;
import org.teiid.query.sql.proc.CommandStatementImpl;
import org.teiid.query.sql.proc.CreateProcedureCommandImpl;
import org.teiid.query.sql.proc.DeclareStatementImpl;
import org.teiid.query.sql.proc.ExceptionExpressionImpl;
import org.teiid.query.sql.proc.IfStatementImpl;
import org.teiid.query.sql.proc.LoopStatementImpl;
import org.teiid.query.sql.proc.StatementImpl;
import org.teiid.query.sql.proc.WhileStatementImpl;
import org.teiid.query.sql.symbol.AliasSymbolImpl;
import org.teiid.query.sql.symbol.ArraySymbolImpl;
import org.teiid.query.sql.symbol.BaseAggregateSymbol;
import org.teiid.query.sql.symbol.BaseExpression;
import org.teiid.query.sql.symbol.BaseWindowFunction;
import org.teiid.query.sql.symbol.CaseExpressionImpl;
import org.teiid.query.sql.symbol.ConstantImpl;
import org.teiid.query.sql.symbol.DerivedColumnImpl;
import org.teiid.query.sql.symbol.ElementSymbolImpl;
import org.teiid.query.sql.symbol.FunctionImpl;
import org.teiid.query.sql.symbol.GroupSymbolImpl;
import org.teiid.query.sql.symbol.JSONObjectImpl;
import org.teiid.query.sql.symbol.MultipleElementSymbolImpl;
import org.teiid.query.sql.symbol.ReferenceImpl;
import org.teiid.query.sql.symbol.ScalarSubqueryImpl;
import org.teiid.query.sql.symbol.SearchedCaseExpressionImpl;
import org.teiid.query.sql.symbol.TextLineImpl;
import org.teiid.query.sql.symbol.WindowSpecificationImpl;
import org.teiid.query.sql.symbol.XMLAttributesImpl;
import org.teiid.query.sql.symbol.XMLElementImpl;
import org.teiid.query.sql.symbol.XMLForestImpl;
import org.teiid.query.sql.symbol.XMLNamespacesImpl;
import org.teiid.query.sql.symbol.XMLParseImpl;
import org.teiid.query.sql.symbol.XMLQueryImpl;
import org.teiid.query.sql.symbol.XMLSerializeImpl;

/**
 *
 */
@SuppressWarnings( "javadoc" )
public abstract class AbstractTestFactory {

    private TCQueryParser parser;

    public AbstractTestFactory(TCQueryParser parser) {
        this.parser = parser;
    }

    public <T> T newNode(ASTNodes nodeType) {
        return parser.getTeiidParser().createASTNode(nodeType);
    }

    public GroupSymbolImpl newGroupSymbol(String... groupSymbolProps) {
        String name = groupSymbolProps[0];
        String definition = null;
        if (groupSymbolProps.length > 1)
            definition = groupSymbolProps[1];

        GroupSymbolImpl gs = newNode(ASTNodes.GROUP_SYMBOL);
        gs.setName(name);
        if (definition != null)
            gs.setDefinition(definition);

        return gs;
    }

    public UnaryFromClauseImpl newUnaryFromClause(GroupSymbolImpl groupSymbol) {
        UnaryFromClauseImpl ufc = newNode(ASTNodes.UNARY_FROM_CLAUSE);
        ufc.setGroup(groupSymbol);
        return ufc;
    }

    public UnaryFromClauseImpl newUnaryFromClause(String... groupSymbolProps) {
        return newUnaryFromClause(newGroupSymbol(groupSymbolProps));
    }

    public JoinPredicateImpl newJoinPredicate(FromClauseImpl leftClause, FromClauseImpl rightClause, JoinTypeImpl.Types joinTypeTypes) {
        JoinTypeImpl joinType = newNode(ASTNodes.JOIN_TYPE);
        joinType.setKind(joinTypeTypes);
        JoinPredicateImpl jp = newNode(ASTNodes.JOIN_PREDICATE);
        jp.setLeftClause(leftClause);
        jp.setRightClause(rightClause);
        jp.setJoinType(joinType);

        return jp;
    }

    public JoinPredicateImpl newJoinPredicate(FromClauseImpl leftClause, FromClauseImpl rightClause, JoinTypeImpl.Types joinTypeTypes, List<? extends CriteriaImpl> crits) {
        JoinPredicateImpl jp = newJoinPredicate(leftClause, rightClause, joinTypeTypes);
        jp.setJoinCriteria((List<CriteriaImpl>)crits);
        return jp;
    }

    public FromImpl newFrom() {
        return newNode(ASTNodes.FROM);
    }

    public FromImpl newFrom(List<? extends FromClauseImpl> clauses) {
        FromImpl from = newFrom();
        from.setClauses(clauses);
        return from;
    }

    public SelectImpl newSelect() {
        return newNode(ASTNodes.SELECT);
    }

    public SelectImpl newSelect(List<? extends BaseExpression> symbols) {
        SelectImpl select = newSelect();
        for (BaseExpression symbol : symbols) {
            select.addSymbol(symbol);
        }
        return select;
    }

    public MultipleElementSymbolImpl newMultipleElementSymbol() {
        MultipleElementSymbolImpl mes = newNode(ASTNodes.MULTIPLE_ELEMENT_SYMBOL);
        return mes;
    }

    public MultipleElementSymbolImpl newMultipleElementSymbol(String name) {
        MultipleElementSymbolImpl mes = newMultipleElementSymbol();
        mes.setName(name);
        return mes;
    }

    public BranchingStatementImpl newBranchingStatement() {
        BranchingStatementImpl stmt = newNode(ASTNodes.BRANCHING_STATEMENT);
        return stmt;
    }

    public BranchingStatementImpl newBranchingStatement(BranchingMode mode) {
        BranchingStatementImpl stmt = newBranchingStatement();
        stmt.setMode(mode);
        return stmt;
    }

    public DynamicCommandImpl newDynamicCommand() {
        DynamicCommandImpl dc = newNode(ASTNodes.DYNAMIC_COMMAND);
        return dc;
    }

    public SelectImpl newSelectWithMultileElementSymbol() {
        SelectImpl select = newSelect();
        MultipleElementSymbolImpl all = newMultipleElementSymbol();
        select.addSymbol(all);
        return select;
    }

    public QueryImpl newQuery() {
        QueryImpl query = newNode(ASTNodes.QUERY);
        return query;
    }

    public QueryImpl newQuery(SelectImpl select, FromImpl from) {
        QueryImpl query = newQuery();
        query.setSelect(select);
        query.setFrom(from);
        return query;
    }

    public ExpressionCriteriaImpl newExpressionCriteria(ElementSymbolImpl newElementSymbol) {
        ExpressionCriteriaImpl ec = newNode(ASTNodes.EXPRESSION_CRITERIA);
        ec.setExpression(newElementSymbol);
        return ec;
    }

    public ExceptionExpressionImpl newExceptionExpression() {
        ExceptionExpressionImpl ee = newNode(ASTNodes.EXCEPTION_EXPRESSION);
        return ee;
    }

    public OrderByItemImpl newOrderByItem(BaseExpression symbol, boolean ascending) {
        OrderByItemImpl orderByItem = newNode(ASTNodes.ORDER_BY_ITEM);
        orderByItem.setSymbol(symbol);
        orderByItem.setAscending(ascending);
        return orderByItem;
    }

    public OrderByImpl newOrderBy() {
        OrderByImpl orderBy = newNode(ASTNodes.ORDER_BY);
        return orderBy;
    }

    public OrderByImpl newOrderBy(List<? extends BaseExpression> parameters) {
        OrderByImpl orderBy = newOrderBy();
        List<OrderByItemImpl> orderByItems = new ArrayList<OrderByItemImpl>();
        for (BaseExpression singleElementSymbol : parameters) {
            orderByItems.add(newOrderByItem(singleElementSymbol, true));
        }
        orderBy.getOrderByItems().addAll(orderByItems);

        return orderBy;
    }

    public OrderByImpl newOrderBy(List<? extends BaseExpression> parameters, List<Boolean> orderTypes) {
        OrderByImpl orderBy = newOrderBy();
        List<OrderByItemImpl> orderByItems = new ArrayList<OrderByItemImpl>();
        Iterator<Boolean> typeIter = orderTypes.iterator();
        for (BaseExpression singleElementSymbol : parameters) {
            orderByItems.add(newOrderByItem(singleElementSymbol, typeIter.next()));
        }
        orderBy.getOrderByItems().addAll(orderByItems);

        return orderBy;
    }

    public ElementSymbolImpl newElementSymbol(String symbolName) {
        ElementSymbolImpl elementSymbol = newNode(ASTNodes.ELEMENT_SYMBOL);
        elementSymbol.setName(symbolName);
        return elementSymbol;
    }

    public ElementSymbolImpl newElementSymbol(String shortName, GroupSymbolImpl gs) {
        return newElementSymbol(shortName, gs, DefaultDataTypeManager.DefaultDataTypes.STRING.getTypeClass());
    }

    public ElementSymbolImpl newElementSymbol(String shortName, GroupSymbolImpl gs, Class<?> typeClass) {
        ElementSymbolImpl elementSymbol = newNode(ASTNodes.ELEMENT_SYMBOL);
        elementSymbol.setShortName(shortName);
        elementSymbol.setGroupSymbol(gs);
        elementSymbol.setType(typeClass);
        return elementSymbol;
    }

    public ConstantImpl newConstant(Object literal) {
        ConstantImpl constant = newNode(ASTNodes.CONSTANT);
        constant.setValue(literal);
        return constant;
    }

    public ConstantImpl newConstant(Object literal, Class<?> type) {
        ConstantImpl constant = newConstant(literal);
        constant.setType(type);
        return constant;
    }

    public AliasSymbolImpl newAliasSymbol(String aliasName, BaseExpression expression) {
        AliasSymbolImpl aliasSymbol = newNode(ASTNodes.ALIAS_SYMBOL);
        aliasSymbol.setName(aliasName);
        aliasSymbol.setSymbol(expression);
        return aliasSymbol;
    }

    public AliasSymbolImpl newAliasSymbolWithElementSymbol(String aliasName, String elementSymbolName) {
        return newAliasSymbol(aliasName, newElementSymbol(elementSymbolName));
    }

    public CompareCriteriaImpl newCompareCriteria(BaseExpression leftExpr, Operator operator, BaseExpression rightExpr) {
        CompareCriteriaImpl crit = newNode(ASTNodes.COMPARE_CRITERIA);
        crit.setLeftExpression(leftExpr);
        crit.setOperator(operator);
        crit.setRightExpression(rightExpr);
        return crit;
    }

    public CompareCriteriaImpl newCompareCriteria(String leftExprName, Operator operator, String rightExprName) {
        ElementSymbolImpl left = newElementSymbol(leftExprName);
        ElementSymbolImpl right = newElementSymbol(rightExprName);
        return newCompareCriteria(left, operator, right);
    }

    public CompoundCriteriaImpl newCompoundCriteria(int operator, CriteriaImpl left, CriteriaImpl right) {
        CompoundCriteriaImpl cc = newNode(ASTNodes.COMPOUND_CRITERIA);
        cc.setOperator(operator);
        cc.getCriteria().add(left);
        cc.getCriteria().add(right);
        return cc;
    }

    public TextColumnImpl newTextColumn(String name, String type, int position) {
        TextColumnImpl tc = newNode(ASTNodes.TEXT_COLUMN);
        tc.setName(name);
        tc.setType(type);
        tc.setWidth(position);
        return tc;
    }

    public TextTableImpl newTextTable() {
        TextTableImpl tt = newNode(ASTNodes.TEXT_TABLE);
        return tt;
    }

    public ReferenceImpl newReference(int index) {
        ReferenceImpl reference = newNode(ASTNodes.REFERENCE);
        reference.setIndex(index);
        reference.setPositional(true);
        return reference;
    }

    public ProjectedColumnImpl newProjectedColumn(String name, String type) {
        ProjectedColumnImpl pc = newNode(ASTNodes.PROJECTED_COLUMN);
        pc.setName(name);
        pc.setType(type);
        return pc;
    }

    public IsNullCriteriaImpl newIsNullCriteria(BaseExpression expression) {
        IsNullCriteriaImpl isNullCriteria = newNode(ASTNodes.IS_NULL_CRITERIA);
        isNullCriteria.setExpression(expression);
        return isNullCriteria;
    }

    public NotCriteriaImpl newNotCriteria(CriteriaImpl criteria) {
        NotCriteriaImpl notCriteria = newNode(ASTNodes.NOT_CRITERIA);
        notCriteria.setCriteria(criteria);
        return notCriteria;
    }

    public WithQueryCommandImpl newWithQueryCommand(GroupSymbolImpl groupSymbol, QueryCommandImpl queryExpression) {
        WithQueryCommandImpl withQueryCommand = newNode(ASTNodes.WITH_QUERY_COMMAND);
        withQueryCommand.setGroupSymbol(groupSymbol);
        withQueryCommand.setQueryExpression(queryExpression);
        return withQueryCommand;
    }

    public BlockImpl newBlock() {
        BlockImpl block = newNode(ASTNodes.BLOCK);
        return block;
    }

    public BlockImpl newBlock(CommandStatementImpl cmdStmt) {
        BlockImpl block = newBlock();
        block.addStatement(cmdStmt);
        return block;
    }

    public AssignmentStatementImpl newAssignmentStatement(ElementSymbolImpl var1, CommandImpl command) {
        AssignmentStatementImpl as = newNode(ASTNodes.ASSIGNMENT_STATEMENT);
        as.setVariable(var1);
        as.setCommand(command);
        return as;
    }

    public AssignmentStatementImpl newAssignmentStatement(ElementSymbolImpl var1, BaseExpression expression) {
        AssignmentStatementImpl as = newNode(ASTNodes.ASSIGNMENT_STATEMENT);
        as.setVariable(var1);
        as.setExpression(expression);
        return as;
    }

    public FunctionImpl newFunction(String name, BaseExpression... args) {
        FunctionImpl function = newNode(ASTNodes.FUNCTION);
        function.setName(name);
        function.setArgs(args);
        return function;
    }

    public DerivedColumnImpl newDerivedColumn(String alias, BaseExpression expression) {
        DerivedColumnImpl dc = newNode(ASTNodes.DERIVED_COLUMN);
        dc.setAlias(alias);
        dc.setExpression(expression);
        return dc;
    }

    public JSONObjectImpl newJSONObject(List<DerivedColumnImpl> args) {
        JSONObjectImpl json = newNode(ASTNodes.JSON_OBJECT);
        json.setArgs(args);
        return json;
    }

    public CommandStatementImpl newCommandStatement(CommandImpl cmd) {
        CommandStatementImpl cmdStmt = newNode(ASTNodes.COMMAND_STATEMENT);
        cmdStmt.setCommand(cmd);
        return cmdStmt;
    }

    public GroupByImpl newGroupBy(ElementSymbolImpl... elementSymbols) {
        GroupByImpl groupBy = newNode(ASTNodes.GROUP_BY);

        if (elementSymbols != null) {
            for (ElementSymbolImpl es : elementSymbols) {
                groupBy.addSymbol(es);
            }
        }

        return groupBy;
    }

    public MatchCriteriaImpl newMatchCriteria(BaseExpression left, BaseExpression right) {
        MatchCriteriaImpl crit = newNode(ASTNodes.MATCH_CRITERIA);
        crit.setLeftExpression(left);
        crit.setRightExpression(right);
        return crit;
    }

    public MatchCriteriaImpl newMatchCriteria(BaseExpression left, BaseExpression right, char escapeChar) {
        MatchCriteriaImpl crit = newMatchCriteria(left, right);
        crit.setEscapeChar(escapeChar);
        return crit;
    }

    public CreateProcedureCommandImpl newCreateProcedureCommand() {
        CreateProcedureCommandImpl cpc = newNode(ASTNodes.CREATE_PROCEDURE_COMMAND);
        return cpc;
    }

    public CreateProcedureCommandImpl newCreateProcedureCommand(BlockImpl block) {
        CreateProcedureCommandImpl cpc = newCreateProcedureCommand();
        cpc.setBlock(block);
        return cpc;
    }

    public IfStatementImpl newIfStatement(CriteriaImpl criteria, BlockImpl ifBlock) {
        IfStatementImpl ifStmt = newNode(ASTNodes.IF_STATEMENT);
        ifStmt.setIfBlock(ifBlock);
        ifStmt.setCondition(criteria);
        return ifStmt;
    }

    public LoopStatementImpl newLoopStatement(BlockImpl block, QueryImpl query, String cursorName) {
        LoopStatementImpl loopStmt = newNode(ASTNodes.LOOP_STATEMENT);
        loopStmt.setBlock(block);
        loopStmt.setCommand(query);
        loopStmt.setCursorName(cursorName);
        return loopStmt;
    }

    public DeclareStatementImpl newDeclareStatement(ElementSymbolImpl elementSymbol, String varType) {
        DeclareStatementImpl ds = newNode(ASTNodes.DECLARE_STATEMENT);
        ds.setVariable(elementSymbol);
        ds.setVariableType(varType);
        return ds;
    }

    public DeclareStatementImpl newDeclareStatement(ElementSymbolImpl elementSymbol, String varType, BaseExpression value) {
        DeclareStatementImpl ds = newDeclareStatement(elementSymbol, varType);
        ds.setExpression(value);
        return ds;
    }

    public StoredProcedureImpl newStoredProcedure() {
        StoredProcedureImpl sp = newNode(ASTNodes.STORED_PROCEDURE);
        return sp;
    }

    public SPParameterImpl newSPParameter(int index, BaseExpression expression) {
        SPParameterImpl parameter = new SPParameterImpl(parser.getTeiidParser(), index, expression);
        return parameter;
    }

    public SPParameterImpl newSPParameter(int index, SPParameter.ParameterInfo paramType, String name) {
        SPParameterImpl parameter = new SPParameterImpl(parser.getTeiidParser(), index, paramType.index(), name);
        return parameter;
    }

    public SubqueryFromClauseImpl newSubqueryFromClause(String name, CommandImpl command) {
        SubqueryFromClauseImpl sfc = newNode(ASTNodes.SUBQUERY_FROM_CLAUSE);
        sfc.setName(name);
        sfc.setCommand(command);
        return sfc;
    }

    public ArrayTableImpl newArrayTable() {
        ArrayTableImpl arrayTable = newNode(ASTNodes.ARRAY_TABLE);
        return arrayTable;
    }

    public BetweenCriteriaImpl newBetweenCriteria(BaseExpression expression, BaseExpression lowerExpression, BaseExpression upperExpression) {
        BetweenCriteriaImpl betweenCriteria = newNode(ASTNodes.BETWEEN_CRITERIA);
        betweenCriteria.setLowerExpression(lowerExpression);
        betweenCriteria.setUpperExpression(upperExpression);
        betweenCriteria.setExpression(expression);
        return betweenCriteria;
    }

    public DeleteImpl newDelete(GroupSymbolImpl group, CriteriaImpl criteria) {
        DeleteImpl delete = newNode(ASTNodes.DELETE);
        delete.setGroup(group);
        delete.setCriteria(criteria);
        return delete;
    }

    public InsertImpl newInsert() {
        InsertImpl insert = newNode(ASTNodes.INSERT);
        return insert;
    }

    public UpdateImpl newUpdate() {
        UpdateImpl update = newNode(ASTNodes.UPDATE);
        return update;
    }

    public WhileStatementImpl newWhileStatement(CriteriaImpl criteria, BlockImpl block) {
        WhileStatementImpl whileStatement = newNode(ASTNodes.WHILE_STATEMENT);
        whileStatement.setBlock(block);
        whileStatement.setCondition(criteria);
        return whileStatement;
    }

    public WindowSpecificationImpl newWindowSpecification() {
        WindowSpecificationImpl windowSpecification = newNode(ASTNodes.WINDOW_SPECIFICATION);
        return windowSpecification;
    }

    public IntoImpl newInto(GroupSymbolImpl group) {
        IntoImpl into = newNode(ASTNodes.INTO);
        into.setGroup(group);
        return into;
    }

    public SearchedCaseExpressionImpl newSearchedCaseExpression(List<? extends CriteriaImpl> when, List<? extends BaseExpression> then) {
        SearchedCaseExpressionImpl sce = newNode(ASTNodes.SEARCHED_CASE_EXPRESSION);
        sce.setWhen(when, then);
        return sce;
    }

    public SetClauseImpl newSetClause(ElementSymbolImpl symbol, BaseExpression value) {
        SetClauseImpl setClause = newNode(ASTNodes.SET_CLAUSE);
        setClause.setSymbol(symbol);
        setClause.setValue(value);
        return setClause;
    }

    public SetClauseListImpl newSetClauseList() {
        SetClauseListImpl setClauseList = newNode(ASTNodes.SET_CLAUSE_LIST);
        return setClauseList;
    }

    public SetQueryImpl newSetQuery(QueryCommandImpl leftQuery, Operation operation, QueryCommandImpl rightQuery, boolean all) {
        SetQueryImpl setQuery = newNode(ASTNodes.SET_QUERY);
        setQuery.setAll(all);
        setQuery.setLeftQuery(leftQuery);
        setQuery.setOperation(operation);
        setQuery.setRightQuery(rightQuery);
        return setQuery;
    }

    public TextLineImpl newTextLine() {
        TextLineImpl textLine = newNode(ASTNodes.TEXT_LINE);
        return textLine;
    }

    public ExistsCriteriaImpl newExistsCriteria(QueryCommandImpl queryCommand) {
        ExistsCriteriaImpl existsCriteria = newNode(ASTNodes.EXISTS_CRITERIA);
        existsCriteria.setCommand(queryCommand);
        return existsCriteria;
    }

    public XMLParseImpl newXMLParse() {
        XMLParseImpl xmlParse = newNode(ASTNodes.XML_PARSE);
        return xmlParse;
    }

    public XMLQueryImpl newXMLQuery() {
        XMLQueryImpl xmlQuery = newNode(ASTNodes.XML_QUERY);
        return xmlQuery;
    }

    public XMLTableImpl newXMLTable() {
        XMLTableImpl xmlTable = newNode(ASTNodes.XML_TABLE);
        return xmlTable;
    }

    public XMLAttributesImpl newXMLAttributes(List<DerivedColumnImpl> args) {
        XMLAttributesImpl xmlAttributes = newNode(ASTNodes.XML_ATTRIBUTES);
        xmlAttributes.setArgs(args);
        return xmlAttributes;
    }

    public XMLColumnImpl newXMLColumn(String name, boolean ordinal) {
        XMLColumnImpl xmlColumn = newNode(ASTNodes.XML_COLUMN);
        xmlColumn.setName(name);
        xmlColumn.setOrdinal(ordinal);
        return xmlColumn;
    }

    public XMLSerializeImpl newXMLSerialize() {
        XMLSerializeImpl xmlSerialize = newNode(ASTNodes.XML_SERIALIZE);
        return xmlSerialize;
    }

    public XMLElementImpl newXMLElement(String name, List<BaseExpression> content) {
        XMLElementImpl xmlElement = newNode(ASTNodes.XML_ELEMENT);
        xmlElement.setName(name);
        xmlElement.setContent(content);
        return xmlElement;
    }

    public XMLForestImpl newXMLForest(List<DerivedColumnImpl> derivedColumns) {
        XMLForestImpl xmlForest = newNode(ASTNodes.XML_FOREST);
        xmlForest.setArguments(derivedColumns);
        return xmlForest;
    }

    public XMLNamespacesImpl newXMLNamespaces(List<NamespaceItem> namespaceItems) {
        XMLNamespacesImpl xmlNamespaces = newNode(ASTNodes.XML_NAMESPACES);
        xmlNamespaces.setNamespaces(namespaceItems);
        return xmlNamespaces;
    }

    public SubquerySetCriteriaImpl newSubquerySetCriteria(BaseExpression expression, QueryCommandImpl command) {
        SubquerySetCriteriaImpl ssc = newNode(ASTNodes.SUBQUERY_SET_CRITERIA);
        ssc.setExpression(expression);
        ssc.setCommand(command);
        return ssc;
    }

    public SubqueryCompareCriteriaImpl newSubqueryCompareCriteria(BaseExpression expression, QueryImpl query, Operator operator, PredicateQuantifier quantifier) {
        SubqueryCompareCriteriaImpl scc = newNode(ASTNodes.SUBQUERY_COMPARE_CRITERIA);
        scc.setLeftExpression(expression);
        scc.setOperator(operator);
        scc.setPredicateQuantifier(quantifier);
        scc.setCommand(query);
        return scc;
    }

    public SetCriteriaImpl newSetCriteria(ElementSymbolImpl symbol, List<BaseExpression> values) {
        SetCriteriaImpl sc = newNode(ASTNodes.SET_CRITERIA);
        sc.setExpression(symbol);
        sc.setValues(values);
        return sc;
    }

    public ScalarSubqueryImpl newScalarSubquery(QueryImpl query) {
        ScalarSubqueryImpl scalarSubquery = newNode(ASTNodes.SCALAR_SUBQUERY);
        scalarSubquery.setCommand(query);
        return scalarSubquery;
    }

    public CreateImpl newCreate() {
        return newNode(ASTNodes.CREATE);
    }

    public DropImpl newDrop() {
        return newNode(ASTNodes.DROP);
    }

    public ArraySymbolImpl newArray(Class<?> typeClass, List<BaseExpression> args) {
        ArraySymbolImpl array = newNode(ASTNodes.ARRAY_SYMBOL);
        array.setComponentType(typeClass);
        array.setExpressions(args);
        return array;
    }

    public JoinTypeImpl newJoinType(Types joinKind) {
        JoinTypeImpl joinType = newNode(ASTNodes.JOIN_TYPE);
        joinType.setKind(joinKind);
        return joinType;
    }

    public CaseExpressionImpl newCaseExpression(ElementSymbolImpl es, List<BaseExpression> whenExpressions, List<BaseExpression> thenExpressions) {
        CaseExpressionImpl caseExpression = newNode(ASTNodes.CASE_EXPRESSION);
        caseExpression.setExpression(es);
        caseExpression.setWhen(whenExpressions, thenExpressions);
        return caseExpression;
    }

    public abstract BaseAggregateSymbol newAggregateSymbol(String name, boolean isDistinct, BaseExpression expression);

    public abstract BaseWindowFunction newWindowFunction(String name);

    public abstract BaseExpression wrapExpression(BaseExpression expr, String... exprName);

    public abstract StatementImpl newRaiseStatement(BaseExpression expr);
}
