/* Generated By:JJTree: Do not edit this line. Alter.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.komodo.modeshape.teiid.sql.lang;

import java.util.List;
import org.komodo.modeshape.teiid.parser.LanguageVisitor;
import org.komodo.modeshape.teiid.parser.TeiidParser;
import org.komodo.modeshape.teiid.sql.symbol.Expression;
import org.komodo.modeshape.teiid.sql.symbol.GroupSymbol;
import org.komodo.spi.query.sql.lang.IAlter;

/**
 *
 * @param <T>
 */
public abstract class Alter<T extends Command> extends Command implements IAlter<Expression, LanguageVisitor> {

    /**
     * @param p
     * @param id
     */
    public Alter(TeiidParser p, int id) {
        super(p, id);
    }

    /**
     * @return the target
     */
    public GroupSymbol getTarget() {
        return this.getTarget();
    }

    /**
     * @param target the target to set
     */
    public void setTarget(GroupSymbol target) {
    }

    /**
     * @return the definition
     */
    public T getDefinition() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param definition the definition to set
     */
    public void setDefinition(T definition) {
    }

    @Override
    public List<Expression> getProjectedSymbols() {
        return getUpdateCommandSymbol();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.getDefinition() == null) ? 0 : this.getDefinition().hashCode());
        result = prime * result + ((this.getTarget() == null) ? 0 : this.getTarget().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Alter other = (Alter)obj;
        if (this.getDefinition() == null) {
            if (other.getDefinition() != null) return false;
        } else if (!this.getDefinition().equals(other.getDefinition())) return false;
        if (this.getTarget() == null) {
            if (other.getTarget() != null) return false;
        } else if (!this.getTarget().equals(other.getTarget())) return false;
        return true;
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(LanguageVisitor visitor) {
        visitor.visit(this);
    }
}
/* JavaCC - OriginalChecksum=4c2a7e700d4af2b1569d4947a1d82223 (do not edit this line) */