/* Generated By:JJTree: Do not edit this line. XMLSerialize.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.teiid.query.sql.symbol;

import org.komodo.spi.query.sql.symbol.XMLSerialize;
import org.teiid.core.types.DefaultDataTypeManager;
import org.teiid.query.parser.TCLanguageVisitorImpl;
import org.teiid.query.parser.TeiidClientParser;
import org.teiid.query.sql.lang.SimpleNode;

/**
 *
 */
public class XMLSerializeImpl extends SimpleNode implements BaseExpression, XMLSerialize<TCLanguageVisitorImpl> {

    private Boolean document;

    private Boolean declaration;

    private BaseExpression expression;

    private String typeString;

    private Class<?> type;

    private String version;

    private String encoding;

    /**
     * @param p
     * @param id
     */
    public XMLSerializeImpl(TeiidClientParser p, int id) {
        super(p, id);
    }

    /**
     * @return the document
     */
    public Boolean getDocument() {
        return this.document;
    }

    /**
     * @param document the document to set
     */
    public void setDocument(Boolean document) {
        this.document = document;
    }

    /**
     * @return the declaration
     */
    public Boolean getDeclaration() {
        return this.declaration;
    }

    /**
     * @param declaration the declaration to set
     */
    public void setDeclaration(Boolean declaration) {
        this.declaration = declaration;
    }

    /**
     * @return the expression
     */
    public BaseExpression getExpression() {
        return this.expression;
    }

    /**
     * @param expression the expression to set
     */
    public void setExpression(BaseExpression expression) {
        this.expression = expression;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public Class<?> getType() {
        if (type == null) {
            if (typeString == null) {
                type = DefaultDataTypeManager.DefaultDataTypes.CLOB.getClass();
            } else {
                type = parser.getDataTypeService().getDataTypeClass(typeString);
            }
        }
        return type;
    }

    /**
     * @param typeString
     */
    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }
    
    /**
     * @return type string
     */
    public String getTypeString() {
        return typeString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.declaration == null) ? 0 : this.declaration.hashCode());
        result = prime * result + ((this.document == null) ? 0 : this.document.hashCode());
        result = prime * result + ((this.encoding == null) ? 0 : this.encoding.hashCode());
        result = prime * result + ((this.expression == null) ? 0 : this.expression.hashCode());
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        result = prime * result + ((this.typeString == null) ? 0 : this.typeString.hashCode());
        result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        XMLSerializeImpl other = (XMLSerializeImpl)obj;
        if (this.declaration == null) {
            if (other.declaration != null) return false;
        } else if (!this.declaration.equals(other.declaration)) return false;
        if (this.document == null) {
            if (other.document != null) return false;
        } else if (!this.document.equals(other.document)) return false;
        if (this.encoding == null) {
            if (other.encoding != null) return false;
        } else if (!this.encoding.equals(other.encoding)) return false;
        if (this.expression == null) {
            if (other.expression != null) return false;
        } else if (!this.expression.equals(other.expression)) return false;
        if (this.type == null) {
            if (other.type != null) return false;
        } else if (!this.type.equals(other.type)) return false;
        if (this.typeString == null) {
            if (other.typeString != null) return false;
        } else if (!this.typeString.equals(other.typeString)) return false;
        if (this.version == null) {
            if (other.version != null) return false;
        } else if (!this.version.equals(other.version)) return false;
        return true;
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(TCLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public XMLSerializeImpl clone() {
        XMLSerializeImpl clone = new XMLSerializeImpl(this.parser, this.id);

        if(getExpression() != null)
            clone.setExpression(getExpression().clone());
        if(getEncoding() != null)
            clone.setEncoding(getEncoding());
        clone.setDocument(getDocument());
        clone.setDeclaration(getDeclaration());
        if(getVersion() != null)
            clone.setVersion(getVersion());
        if(getTypeString() != null)
            clone.setTypeString(getTypeString());

        return clone;
    }

}
/* JavaCC - OriginalChecksum=2db0f3a9bb9c785d22f2f82535419c33 (do not edit this line) */
