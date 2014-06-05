/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.komodo.spi.query;

import java.util.List;

import org.komodo.spi.query.metadata.IMetadataID;
import org.komodo.spi.query.metadata.IQueryNode;
import org.komodo.spi.query.metadata.IStoredProcedureInfo;
import org.komodo.spi.query.sql.lang.IBetweenCriteria;
import org.komodo.spi.query.sql.lang.ICommand;
import org.komodo.spi.query.sql.lang.ICompareCriteria;
import org.komodo.spi.query.sql.lang.ICompoundCriteria;
import org.komodo.spi.query.sql.lang.ICriteria;
import org.komodo.spi.query.sql.lang.IDelete;
import org.komodo.spi.query.sql.lang.IExistsCriteria;
import org.komodo.spi.query.sql.lang.IExpression;
import org.komodo.spi.query.sql.lang.IFrom;
import org.komodo.spi.query.sql.lang.IFromClause;
import org.komodo.spi.query.sql.lang.IGroupBy;
import org.komodo.spi.query.sql.lang.IInsert;
import org.komodo.spi.query.sql.lang.IIsNullCriteria;
import org.komodo.spi.query.sql.lang.IJoinPredicate;
import org.komodo.spi.query.sql.lang.IJoinType;
import org.komodo.spi.query.sql.lang.ILanguageObject;
import org.komodo.spi.query.sql.lang.IMatchCriteria;
import org.komodo.spi.query.sql.lang.INotCriteria;
import org.komodo.spi.query.sql.lang.IOption;
import org.komodo.spi.query.sql.lang.IOrderBy;
import org.komodo.spi.query.sql.lang.IQuery;
import org.komodo.spi.query.sql.lang.IQueryCommand;
import org.komodo.spi.query.sql.lang.ISPParameter;
import org.komodo.spi.query.sql.lang.ISelect;
import org.komodo.spi.query.sql.lang.ISetCriteria;
import org.komodo.spi.query.sql.lang.ISetQuery;
import org.komodo.spi.query.sql.lang.IStoredProcedure;
import org.komodo.spi.query.sql.lang.ISubqueryCompareCriteria;
import org.komodo.spi.query.sql.lang.ISubqueryFromClause;
import org.komodo.spi.query.sql.lang.ISubquerySetCriteria;
import org.komodo.spi.query.sql.lang.IUnaryFromClause;
import org.komodo.spi.query.sql.lang.IUpdate;
import org.komodo.spi.query.sql.lang.ISPParameter.ParameterInfo;
import org.komodo.spi.query.sql.lang.ISetQuery.Operation;
import org.komodo.spi.query.sql.proc.IAssignmentStatement;
import org.komodo.spi.query.sql.proc.IBlock;
import org.komodo.spi.query.sql.proc.ICommandStatement;
import org.komodo.spi.query.sql.proc.ICreateProcedureCommand;
import org.komodo.spi.query.sql.proc.IDeclareStatement;
import org.komodo.spi.query.sql.proc.IRaiseStatement;
import org.komodo.spi.query.sql.symbol.IAggregateSymbol;
import org.komodo.spi.query.sql.symbol.IAliasSymbol;
import org.komodo.spi.query.sql.symbol.IConstant;
import org.komodo.spi.query.sql.symbol.IElementSymbol;
import org.komodo.spi.query.sql.symbol.IExpressionSymbol;
import org.komodo.spi.query.sql.symbol.IFunction;
import org.komodo.spi.query.sql.symbol.IGroupSymbol;
import org.komodo.spi.query.sql.symbol.IMultipleElementSymbol;
import org.komodo.spi.query.sql.symbol.IReference;
import org.komodo.spi.query.sql.symbol.IScalarSubquery;
import org.komodo.spi.query.sql.symbol.IAggregateSymbol.Type;

/**
 *
 */
public interface IQueryFactory<E extends IExpression, 
                                                      SES extends IExpression /* SingleElementSymbol */,
                                                      F extends IFromClause,
                                                      ES extends IElementSymbol,
                                                      C extends ICommand,
                                                      QC extends IQueryCommand,
                                                      CR extends ICriteria,
                                                      CO extends IConstant,
                                                      B extends IBlock,
                                                      SS extends ILanguageObject /* SelectSymbol */,
                                                      GS extends IGroupSymbol,
                                                      JT extends IJoinType> {
    
    /**
     * Create a new function
     * 
     * @param name
     * @param arguments
     * 
     * @return instance of {@link IFunction}
     */
    IFunction createFunction(String name, List<? extends E> arguments);
    
    /**
     * Create a new aggregate symbol
     * 
     * @param functionName 
     * @param functionType 
     * @param isDistinct 
     * @param expression
     * 
     * @return instance of {@link IAggregateSymbol}
     */
    IAggregateSymbol createAggregateSymbol(String functionName, Type functionType, boolean isDistinct, E expression);
    
    /**
     * Create a new element symbol
     * 
     * @param name
     * 
     * @return instance of {@link IElementSymbol}
     */
    IElementSymbol createElementSymbol(String name);

    /**
     * Create a new element symbol
     * 
     * @param name
     * @param displayFullyQualified True if should display fully qualified
     * 
     * @return instance of {@link IElementSymbol}
     */
    IElementSymbol createElementSymbol(String name, boolean displayFullyQualified);
    
    /**
     * Create an alias symbol
     * 
     * @param name
     * @param symbol
     * 
     * @return instance of {@link IAliasSymbol}
     */
    IAliasSymbol createAliasSymbol(String name, SES symbol);
    
    /**
     * Create a new group symbol
     * 
     * @param name
     * 
     * @return instance of {@link IGroupSymbol}
     */
    IGroupSymbol createGroupSymbol(String name);
    
    /**
     * Create a new group symbol
     * 
     * @param name
     * @param definition
     * 
     * @return instance of {@link IGroupSymbol}
     */
    IGroupSymbol createGroupSymbol(String name, String definition);
    
    /**
     * Create an expression symbol
     * 
     * @param name
     * @param expression
     * 
     * @return instance of {@link IExpressionSymbol}
     */
    IExpressionSymbol createExpressionSymbol(String name, E expression);
    
    /**
     * Create an multiple element symbol
     * 
     * @return instance of {@link IMultipleElementSymbol}
     */
    IMultipleElementSymbol createMultipleElementSymbol();
    
    /**
     * Create a new constant
     * 
     * @param value
     * 
     * @return instance of {@link IConstant}
     */
    IConstant createConstant(Object value);

    /**
     * Create a new declare statement.
     * 
     * @param variable The <code>ElementSymbol</code> object that is the variable
     * @param valueType The type of this variable
     * 
     * @return instance of  {@link IDeclareStatement}
     */
    IDeclareStatement createDeclareStatement(ES variable, String valueType);
    
    /**
     * Create a command statement
     * 
     * @param command
     * 
     * @return instance of {@link ICommandStatement}
     */
    ICommandStatement createCommandStatement(C command);
    
    /**
     * Create a raise statement
     * 
     * @param expression
     * 
     * @return instance of {@link IRaiseStatement}
     */
    IRaiseStatement createRaiseStatement(E expression);
    
    /**
     * Create a query
     * 
     * @return instance of {@link IQuery}
     */
    IQuery createQuery();

    /**
     * Create a set query
     * 
     * @param operation
     * @param all
     * @param leftQuery 
     * @param rightQuery 
     * 
     * @return instance of {@link ISetQuery}
     */
    ISetQuery createSetQuery(Operation operation, 
                                              boolean all, 
                                              QC leftQuery, 
                                              QC rightQuery);

    /**
     * Create a set query
     * 
     * @param operation
     * 
     * @return instance of {@link ISetQuery}
     */
    ISetQuery createSetQuery(Operation operation);
    
    /**
     * Create a compare criteria
     * 
     * @return instance of {@link ICompareCriteria}
     */
    ICompareCriteria createCompareCriteria();

    /**
     * Create a compare criteria
     * 
     * @param expression1
     * @param operator
     * @param expression2
     * 
     * @return instance of {@link ICompareCriteria}
     */
    ICompareCriteria createCompareCriteria(E expression1, 
                                                                   int operator, 
                                                                   E expression2);
    
    /**
     * Create an is null criteria
     * 
     * @return instance of {@link IIsNullCriteria}
     */
    IIsNullCriteria createIsNullCriteria();

    /**
     * Create an is null criteria
     * 
     * @param expression 
     * 
     * @return instance of {@link IIsNullCriteria}
     */
    IIsNullCriteria createIsNullCriteria(E expression);
    
    /**
     * Create a not criteria
     * 
     * @return instance of {@link INotCriteria}
     */
    INotCriteria createNotCriteria();
    
    /**
     * Create a not criteria
     * 
     * @param criteria 
     * 
     * @return instance of {@link INotCriteria}
     */
    INotCriteria createNotCriteria(CR criteria);
    
    /**
     * Create a match criteria
     * 
     * @return instance of {@link IMatchCriteria}
     */
    IMatchCriteria createMatchCriteria();

    /**
     * Create a set criteria
     * 
     * @return instance of {@link ISetCriteria}
     */
    ISetCriteria createSetCriteria();

    /**
     * Create a subquery set criteria
     * 
     * @return instance of {@link ISubquerySetCriteria}
     */
    ISubquerySetCriteria createSubquerySetCriteria();

    /**
     * Create a subquery set criteria
     * 
     * @param expression 
     * @param command 
     * 
     * @return instance of {@link ISubquerySetCriteria}
     */
    ISubquerySetCriteria createSubquerySetCriteria(E expression, QC command);
    
    /**
     * Create a subquery compare criteria
     * 
     * @param leftExpression
     * @param command
     * @param operator
     * @param predicateQuantifier
     * 
     * @return instance of {@link ISubqueryCompareCriteria}
     */
    ISubqueryCompareCriteria createSubqueryCompareCriteria(E leftExpression, QC command, int operator, int predicateQuantifier);
    
    /**
     * Create a scalar sub query
     * 
     * @param queryCommand
     * 
     * @return instance of {@link IScalarSubquery}
     */
    IScalarSubquery createScalarSubquery(QC queryCommand);
    
    /**
     * Create an in-between criteria
     * 
     * @param elementSymbol
     * @param constant1
     * @param constant2
     * 
     * @return instance of {@link IBetweenCriteria}
     */
    IBetweenCriteria createBetweenCriteria(ES elementSymbol,
                                                                   CO constant1,
                                                                   CO constant2);

    /**
     * Create a compound criteria
     * 
     * @param operator
     * @param criteria
     * 
     * @return instance of {@link ICompoundCriteria}
     */
    ICompoundCriteria createCompoundCriteria(int operator, List<? extends CR> criteria);

    /**
     * Create an exists criteria
     * 
     * @param queryCommand
     * 
     * @return instance of {@link IExistsCriteria}
     */
    IExistsCriteria createExistsCriteria(QC queryCommand);
    
    /**
     * Create a block
     * 
     * @return instance of {@link IBlock}
     */
    IBlock createBlock();

    /**
     * Create a create-procedure statement
     * 
     * @param block
     * 
     * @return instance of {@link ICreateProcedureCommand}
     */
    ICreateProcedureCommand createCreateProcedureCommand(B block);

    /**
     * Create an assignment statement
     * 
     * @param elementSymbol 
     * @param expression
     * 
     * @return instance of {@link IAssignmentStatement}
     */
    IAssignmentStatement createAssignmentStatement(ES elementSymbol, E expression);

    /**
     * Create an assignment statement
     * 
     * @param elementSymbol 
     * @param queryCommand
     * 
     * @return instance of {@link IAssignmentStatement}
     */
    IAssignmentStatement createAssignmentStatement(ES elementSymbol, QC queryCommand);

    /**
     * Create a select
     * 
     * @return instance of {@link ISelect}
     */
    ISelect createSelect();
    
    /**
     * Create a select
     * 
     * @param symbols
     * 
     * @return instance of {@link ISelect}
     */
    ISelect createSelect(List<? extends SS> symbols);

    /**
     * Create a from
     * 
     * @return instance of {@link IFrom}
     */
    IFrom createFrom();

    /**
     * Create a from
     * 
     * @param fromClauses
     * 
     * @return instance of {@link IFrom}
     */
    IFrom createFrom(List<? extends F> fromClauses);
    
    /**
     * Create a unary from clause
     * 
     * @param symbol
     * 
     * @return instance of {@link IUnaryFromClause}
     */
    IUnaryFromClause createUnaryFromClause(GS symbol);

    /**
     * Create a subquery from clause
     * 
     * @param name
     * @param command
     * 
     * @return instance of {@link ISubqueryFromClause}
     */
    ISubqueryFromClause createSubqueryFromClause(String name, QC command);


    /**
     * Create a join type
     * 
     * @param joinType 
     * 
     * @return instance of {@link IJoinType}
     */
    IJoinType getJoinType(IJoinType.Types joinType);
    
    /**
     * Create a join predicate
     * 
     * @param leftClause 
     * @param rightClause 
     * @param joinType
     * 
     * @return instance of {@link IJoinPredicate}
     */
    IJoinPredicate createJoinPredicate(F leftClause,
                                                         F rightClause, 
                                                         JT joinType);
    /**
     * Create a join predicate
     * 
     * @param leftClause 
     * @param rightClause 
     * @param joinType
     * @param criteria 
     * 
     * @return instance of {@link IJoinPredicate}
     */    
    IJoinPredicate createJoinPredicate(F leftClause, 
                                                         F rightClause,
                                                         JT joinType,
                                                         List<CR> criteria);
    
    /**
     * Create a group by
     * 
     * @return instance of {@link IGroupBy}
     */
    IGroupBy createGroupBy();

    /**
     * Create an order by
     * 
     * @return instance of {@link IOrderBy}
     */
    IOrderBy createOrderBy();
    
    /**
     * Create an option
     * 
     * @return instance of {@link IOption}
     */
    IOption createOption();
    
    /**
     * Create an update
     * 
     * @return instance of {@link IUpdate}
     */
    IUpdate createUpdate();
    
    /**
     * Create a delete     
     * 
     * @return instance of {@link IDelete}
     */
    IDelete createDelete();
    
    /**
     * Create an insert     
     * 
     * @return instance of {@link IInsert}
     */
    IInsert createInsert();
    
    /**
     * Create a stored procedure
     * 
     * @return instance of {@link IStoredProcedure}
     */
    IStoredProcedure createStoredProcedure();

    /**
     * Create a stored procedure parameter
     * 
     * @param index
     * @param expression
     * 
     * @return instance of {@link ISPParameter}
     */
    ISPParameter createSPParameter(int index, E expression);

    /**
     * Create a stored procedure parameter
     * 
     * @param index 
     * @param parameterType 
     * @param name
     * 
     * @return instance of {@link ISPParameter}
     */
    ISPParameter createSPParameter(int index, ParameterInfo parameterType, String name);
    
    /**
     * Create a reference
     * 
     * @param index
     * 
     * @return instance of {@link IReference}
     */
    IReference createReference(int index);

    /**
     * Create a metadata id
     * 
     * @param upperCase
     * @param clazz
     * 
     * @return instance of {@link IMetadataID}
     */
    IMetadataID createMetadataID(String upperCase, Class clazz);

    /**
     * Create a stored procedure info
     * 
     * @return instance of {@link IStoredProcedureInfo}
     */
    IStoredProcedureInfo createStoredProcedureInfo();

    /**
     * Create a query node
     * 
     * @param queryPlan
     * 
     * @return instance of {@link IQueryNode}
     */
    IQueryNode createQueryNode(String queryPlan);

}
