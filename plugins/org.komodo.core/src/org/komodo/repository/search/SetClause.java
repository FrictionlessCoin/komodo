/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.repository.search;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.komodo.utils.ArgCheck;
import org.komodo.utils.StringUtils;

/**
 * A Set Clause
 *
 * alias.property IN (value1, value2, value3)
 */
class SetClause extends Clause implements PropertyClause {

    private final Set<String> values = new LinkedHashSet<String>();

    /**
     * Constructor
     * @param parent parent searcher
     * @param operator the logical operator preceding this clause (can be null if this is the only clause)
     * @param alias the alias
     * @param property the property
     * @param values the value(s)
     */
    public SetClause(ObjectSearcher parent, LogicalOperator operator, String alias, String property, String... values) {
        super(parent, operator);

        ArgCheck.isNotNull(property);
        ArgCheck.isNotEmpty(values, "Where Set clause requires at least 1 value"); //$NON-NLS-1$

        setAlias(alias);
        setProperty(PROPERTY, property);

        for (String value : values)
            this.values.add(value);
    }

    /**
     * @return the property
     */
    @Override
    public String getProperty() {
        return properties.get(PROPERTY);
    }

    /**
     * @return the values
     */
    public Set<String> getValues() {
        return this.values;
    }

    /**
     * Add a value to set of values
     * 
     * @param value the value
     */
    public void addValue(String value) {
        values.add(value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.values == null) ? 0 : this.values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SetClause other = (SetClause)obj;
        if (this.values == null) {
            if (other.values != null)
                return false;
        } else if (!this.values.equals(other.values))
            return false;
        return true;
    }

    @Override
    public String clauseString(int position) {
        StringBuffer buffer = new StringBuffer();

        appendLogicalOperator(position, buffer);

        setAlias(checkWhereAlias(getAlias()));

        if (! StringUtils.isEmpty(getAlias())) {
            buffer.append(getAlias());
            buffer.append(DOT);
        }

        buffer.append(OPEN_SQUARE_BRACKET);
        buffer.append(getProperty());
        buffer.append(CLOSE_SQUARE_BRACKET);
        buffer.append(SPACE);
        buffer.append(IN);
        buffer.append(SPACE);

        buffer.append(OPEN_BRACKET);
        Iterator<String> valuesIter = getValues().iterator();
        appendStringValues(buffer, valuesIter);
        buffer.append(CLOSE_BRACKET);

        return buffer.toString();
    }
}