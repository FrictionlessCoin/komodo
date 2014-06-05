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
package org.komodo.spi.xml;

/**
 *
 */
public interface IMappingNode {
    
    /** The default build in type */
    String DEFAULT_BUILT_IN_TYPE = ""; //$NON-NLS-1$
    
    /** The default minimum bound of the cardinality of a node. */
    Integer DEFAULT_CARDINALITY_MINIMUM_BOUND = new Integer(1);
    
    /** The default maximum bound of the cardinality of a node. */
    Integer DEFAULT_CARDINALITY_MAXIMUM_BOUND = new Integer(1);
    
    /** The default value for recursion limit */
    Integer DEFAULT_RECURSION_LIMIT = new Integer(10);
    
    /** The default value for is nillable */
    Boolean DEFAULT_IS_NILLABLE = Boolean.FALSE;
    
    /** The default value for exception on recursion limit */
    Boolean DEFAULT_EXCEPTION_ON_RECURSION_LIMIT = Boolean.FALSE;
    
    /**
     * PRESERVE -No normalization is done, the value is not changed.
     * REPLACE - All occurrences of tab, line feed and carriage return are replaced with space
     * COLLAPSE - After the processing implied by replace, contiguous sequences of space are 
     * collapsed to a single space, and leading and trailing spaces are removed.
     */
    String NORMALIZE_TEXT_PRESERVE = "preserve"; //$NON-NLS-1$
    String NORMALIZE_TEXT_REPLACE  = "replace"; //$NON-NLS-1$
    String NORMALIZE_TEXT_COLLAPSE = "collapse"; //$NON-NLS-1$
    
    /** The default value for normalize text */
    String DEFAULT_NORMALIZE_TEXT = NORMALIZE_TEXT_PRESERVE; 

    /**
     * @param excludeFromDocument
     */
    void setExclude(boolean excludeFromDocument);

    /**
     * @return
     */
    IMappingNode clone();

}
