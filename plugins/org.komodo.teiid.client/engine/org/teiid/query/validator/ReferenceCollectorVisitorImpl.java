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

package org.teiid.query.validator;

import java.util.ArrayList;
import java.util.List;

import org.komodo.spi.query.sql.ReferenceCollectorVisitor;
import org.komodo.spi.runtime.version.TeiidVersion;
import org.teiid.query.parser.TCLanguageVisitorImpl;
import org.teiid.query.sql.lang.BaseLanguageObject;
import org.teiid.query.sql.navigator.DeepPreOrderNavigator;
import org.teiid.query.sql.symbol.ReferenceImpl;


/**
 * <p>This visitor class will traverse a language object tree and collect all
 * references it finds.  </p>
 *
 * <p>The easiest way to use this visitor is to call the static methods which create
 * the visitor (and possibly the collection), run the visitor, and return the collection.
 * The public visit() methods should NOT be called directly.</p>
 */
public class ReferenceCollectorVisitorImpl extends TCLanguageVisitorImpl
    implements ReferenceCollectorVisitor<BaseLanguageObject, ReferenceImpl> {

    private List<ReferenceImpl> references = new ArrayList<ReferenceImpl>();

    /**
     * @param teiidVersion
     */
    public ReferenceCollectorVisitorImpl(TeiidVersion teiidVersion) {
        super(teiidVersion);
    }

    /**
     * Get the references collected by the visitor.  This should best be called
     * after the visitor has been run on the language object tree.
     * @return Collection of {@link org.teiid.query.sql.symbol.ElementSymbolImpl}
     */
    public List<ReferenceImpl> getReferences() {
        return this.references;
    }

    /**
     * Visit a language object and collect symbols.  This method should <b>NOT</b> be
     * called directly.
     * @param obj Language object
     */
    @Override
    public void visit(ReferenceImpl obj) {
        this.references.add(obj);
    }

    @Override
    public List<ReferenceImpl> findReferences(BaseLanguageObject obj) {
        DeepPreOrderNavigator.doVisit(obj, this);
        return getReferences();
    }

    /**
     * Helper to quickly get the references from obj in a collection.
     * @param obj Language object
     * @return List of {@link org.teiid.query.sql.symbol.ReferenceImpl}
     */
    public static List<ReferenceImpl> getReferences(BaseLanguageObject obj) {
        ReferenceCollectorVisitorImpl visitor = new ReferenceCollectorVisitorImpl(obj.getTeiidVersion());
        return visitor.findReferences(obj);
    }
    
}

