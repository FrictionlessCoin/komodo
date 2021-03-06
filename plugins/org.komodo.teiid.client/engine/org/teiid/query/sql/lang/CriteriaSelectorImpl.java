/* Generated By:JJTree: Do not edit this line. CriteriaSelector.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=TeiidNodeFactory,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.teiid.query.sql.lang;

import java.util.ArrayList;
import java.util.List;

import org.komodo.spi.annotation.Removed;
import org.komodo.spi.query.sql.proc.CriteriaSelector;
import org.komodo.spi.runtime.version.DefaultTeiidVersion.Version;
import org.teiid.query.parser.TCLanguageVisitorImpl;
import org.teiid.query.parser.TeiidClientParser;
import org.teiid.query.sql.symbol.ElementSymbolImpl;

/**
 *
 */
@Removed(Version.TEIID_8_0)
public class CriteriaSelectorImpl extends SimpleNode implements CriteriaOperator, CriteriaSelector<TCLanguageVisitorImpl> {

    // type of criteria
    private Operator selectorType = Operator.NO_TYPE;

    // elements on which criteria is present
    private List<ElementSymbolImpl> elements;

    /**
     * @param p
     * @param id
     */
    public CriteriaSelectorImpl(TeiidClientParser p, int id) {
        super(p, id);
    }

    /**
     * Get the type of criteria on the user query's elements
     * @return An int value giving the type of criteria
     */
    public Operator getSelectorType() {
        return this.selectorType;
    }
    
    /**
     * Set the type of criteria on the user query's elements
     * @param type The type of criteria
     */ 
    public void setSelectorType(Operator type) {
        this.selectorType = type;
    }   

    /**
     * Get elements on which criteria is pecified on the user's query
     * @return A collection of elements used in criteria
     */
    public List<ElementSymbolImpl> getElements() {
        return this.elements;
    }

    /**
     * Set elements on which criteria is pecified on the user's query
     * @param elements A collection of elements used in criteria
     */ 
    public void setElements(List<ElementSymbolImpl> elements) {
        this.elements = elements;
    }
    
    /**
     * Add an element to the collection of elements on which
     * criteria is pecified on the user's query
     * @param element The elementSymbol object being added
     */ 
    public void addElement(ElementSymbolImpl element) {
        if(elements == null) {
            elements = new ArrayList();
        }
        elements.add(element);
    }
    
    /**
     * Return a boolean indicating if the seletor has any elements
     * @return A boolean indicating if the seletor has any elements
     */
    public boolean hasElements() {
        return (elements != null && !elements.isEmpty());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.elements == null) ? 0 : this.elements.hashCode());
        result = prime * result + ((this.selectorType == null) ? 0 : this.selectorType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        CriteriaSelectorImpl other = (CriteriaSelectorImpl)obj;
        if (this.elements == null) {
            if (other.elements != null) return false;
        } else if (!this.elements.equals(other.elements)) return false;
        if (this.selectorType != other.selectorType) return false;
        return true;
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(TCLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public CriteriaSelectorImpl clone() {
        CriteriaSelectorImpl clone = new CriteriaSelectorImpl(this.parser, this.id);

        if(getElements() != null)
            clone.setElements(cloneList(getElements()));
        if(getSelectorType() != null)
            clone.setSelectorType(getSelectorType());

        return clone;
    }

}
/* JavaCC - OriginalChecksum=e374010f0e0c1ed5a20b2307bf23e267 (do not edit this line) */
