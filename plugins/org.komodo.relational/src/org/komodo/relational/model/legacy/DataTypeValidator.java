/*
 * JBoss, Home of Professional Open Source.
*
* See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
*
* See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
*/
package org.komodo.relational.model.legacy;

import org.komodo.spi.outcome.Outcome;

/**
 *
 */
public interface DataTypeValidator {

	/**
     * Validate the DataType
     * @param dataType the DataType
     * @return the validation outcome
     */
    public Outcome validate(DataType dataType);

}
