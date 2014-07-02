/*************************************************************************************
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
 ************************************************************************************/
package org.komodo.spi.query.sql.lang;

import java.util.List;

import org.komodo.spi.query.sql.ILanguageVisitor;


/**
 *
 */
public interface IOrderBy<E extends IExpression, O extends IOrderByItem, LV extends ILanguageVisitor> 
    extends ILanguageObject<LV> {

    /** Constant for the ascending value */
    public static final boolean ASC = true;

    /** Constant for the descending value */
    public static final boolean DESC = false;
    
    /**
     * Returns the number of elements in ORDER BY.
     * 
     * @return Number of variables in ORDER BY
     */
    int getVariableCount();

    /**
     * Get the order by items
     * 
     * @return list of order by items
     */
    List<O> getOrderByItems();
    
    /**
     * Adds a new variable to the list of order by elements.
     * 
     * @param expression to add
     */
    void addVariable(E expression);

    /**
     * Adds a new variable to the list of order by elements
     * 
     * @param element
     * @param orderType
     */
    void addVariable(E element, boolean orderType);
}