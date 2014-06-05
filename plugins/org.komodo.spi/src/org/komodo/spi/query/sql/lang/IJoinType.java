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
package org.komodo.spi.query.sql.lang;

import org.komodo.spi.query.sql.ILanguageVisitor;

/**
 *
 */
public interface IJoinType <LV extends ILanguageVisitor> extends ILanguageObject<LV> {

    /**
     * Delineation of the category of join type
     */
    enum Types {
        /** Represents an inner join:  a INNER JOIN b */
        JOIN_INNER(false),

        /** Represents a right outer join:  a RIGHT OUTER JOIN b */
        JOIN_RIGHT_OUTER(true),

        /** Represents a left outer join:  a LEFT OUTER JOIN b */
        JOIN_LEFT_OUTER(true),

        /** Represents a full outer join:  a FULL OUTER JOIN b */
        JOIN_FULL_OUTER(true),

        /** Represents a cross join:  a CROSS JOIN b */
        JOIN_CROSS(false),

        /** Represents a union join:  a UNION JOIN b - not used after rewrite */
        JOIN_UNION(true),

        /** internal SEMI Join type */
        JOIN_SEMI(false),

        /** internal ANTI SEMI Join type */
        JOIN_ANTI_SEMI(true);

        private final boolean outer;

        private Types(boolean outer) {
            this.outer = outer;
        }

        public int getTypeCode() {
            return this.ordinal();
        }
        
        public boolean isOuter() {
            return this.outer;
        }
    }
    
    /**
     * Used only for comparison during equals, not by users of this class
     * 
     * @return Type code for object
     */
    int getTypeCode();

    /**
     * Check if this join type is an outer join.
     * 
     * @return True if left/right/full outer, false if inner/cross
     */
    boolean isOuter();
}
