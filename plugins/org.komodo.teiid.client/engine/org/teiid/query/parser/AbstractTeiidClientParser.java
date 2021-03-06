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

package org.teiid.query.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.komodo.spi.annotation.Removed;
import org.komodo.spi.annotation.Since;
import org.komodo.spi.runtime.version.TeiidVersion;
import org.komodo.spi.runtime.version.DefaultTeiidVersion.Version;
import org.teiid.core.types.DefaultDataTypeManager;
import org.teiid.core.util.StringUtil;
import org.teiid.language.SQLConstants;
import org.teiid.metadata.AbstractMetadataRecord;
import org.teiid.metadata.BaseColumn;
import org.teiid.metadata.Column;
import org.teiid.metadata.Column.SearchType;
import org.teiid.metadata.Datatype;
import org.teiid.metadata.FunctionMethod;
import org.teiid.metadata.FunctionMethod.PushDown;
import org.teiid.metadata.FunctionParameter;
import org.teiid.metadata.KeyRecord;
import org.teiid.metadata.MetadataFactory;
import org.teiid.metadata.Procedure;
import org.teiid.metadata.ProcedureParameter;
import org.teiid.metadata.ProcedureParameter.Type;
import org.teiid.metadata.Table;
import org.teiid.query.metadata.DDLConstants;
import org.teiid.query.metadata.SystemMetadata;
import org.teiid.query.parser.TeiidNodeFactory.ASTNodes;
import org.teiid.query.sql.lang.AlterTriggerImpl;
import org.teiid.query.sql.lang.CommandImpl;
import org.teiid.query.sql.lang.BaseLanguageObject;
import org.teiid.query.sql.lang.SPParameterImpl;
import org.teiid.query.sql.lang.SetQueryImpl;
import org.teiid.query.sql.lang.SourceHintImpl;
import org.teiid.query.sql.lang.StoredProcedureImpl;
import org.teiid.query.sql.symbol.ElementSymbolImpl;
import org.teiid.query.sql.symbol.BaseExpression;
import org.teiid.query.sql.symbol.GroupSymbolImpl;
import org.teiid.runtime.client.Messages;
import org.teiid.runtime.client.TeiidClientException;

public abstract class AbstractTeiidClientParser implements TeiidClientParser {

    protected Pattern udtPattern = Pattern.compile("(\\w+)\\s*\\(\\s*(\\d+),\\s*(\\d+),\\s*(\\d+)\\)"); //$NON-NLS-1$
    
    protected Pattern SOURCE_HINT = Pattern.compile("\\s*sh(\\s+KEEP ALIASES)?\\s*(?::((?:'[^']*')+))?\\s*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL); //$NON-NLS-1$
    
    protected Pattern SOURCE_HINT_ARG = Pattern.compile("\\s*([^: ]+)(\\s+KEEP ALIASES)?\\s*:((?:'[^']*')+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL); //$NON-NLS-1$

    protected TeiidVersion version;

    private MetadataFactory metadataFactory;

    /**
     * @return teiid instance version
     */
    @Override
    public TeiidVersion getVersion() {
        return version;
    }

    /**
     * @param teiidVersion
     */
    public void setVersion(TeiidVersion teiidVersion) {
        this.version = teiidVersion;
    }

    /**
     * The version of this parser must be greater than
     * or equal to the given version.
     *
     * @param requiredVersionEnum
     */
    protected boolean versionLessThan(Version requiredVersionEnum) {
        return getVersion().isLessThan(requiredVersionEnum.get());
    }

    /**
     * The version of this parser must be  than
     * or equal to the given version.
     *
     * @param requiredVersionEnum
     */
    protected boolean versionAtLeast(Version requiredVersionEnum) {
        return ! versionLessThan(requiredVersionEnum);
    }

    @Override
    public DefaultDataTypeManager getDataTypeService() {
        return DefaultDataTypeManager.getInstance(getVersion());
    }

    @Override
    public <T extends BaseLanguageObject> T createASTNode(ASTNodes nodeType) {
        return TeiidNodeFactory.getInstance().create(this, nodeType);
    };

	protected String prependSign(String sign, String literal) {
		if (sign != null && sign.charAt(0) == '-') {
			return sign + literal;
		}
		return literal;
	}

	@Since(Version.TEIID_8_0)
	protected void convertToParameters(List<BaseExpression> values, StoredProcedureImpl storedProcedure, int paramIndex) {
		for (BaseExpression value : values) {
			SPParameterImpl parameter = new SPParameterImpl(this, paramIndex++, value);
			parameter.setParameterType(SPParameterImpl.IN);
			storedProcedure.addParameter(parameter);
		}
	}

    private int parseNumericValue(CharSequence string, StringBuilder sb, int i, int value,
                                                                         int possibleDigits, int radixExp) {
        for (int j = 0; j < possibleDigits; j++) {
            if (i + 1 == string.length()) {
                break;
            }
            char digit = string.charAt(i + 1);
            int val = Character.digit(digit, 1 << radixExp);
            if (val == -1) {
                break;
            }
            i++;
            value = (value << radixExp) + val;
        }
        sb.append((char)value);
        return i;
    }

	/**
     * Unescape the given string
     * @param string
     * @param quoteChar
     * @param useAsciiExcapes
     * @param sb a scratch buffer to use
     * @return
     */
    private String unescape(CharSequence string, int quoteChar, boolean useAsciiEscapes, StringBuilder sb) {
        boolean escaped = false;
        
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (escaped) {
                switch (c) {
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'u':
                    i = parseNumericValue(string, sb, i, 0, 4, 4);
                    //TODO: this should probably be strict about needing 4 digits
                    break;
                default:
                    if (c == quoteChar) {
                        sb.append(quoteChar);
                    } else if (useAsciiEscapes) {
                        int value = Character.digit(c, 8);
                        if (value == -1) {
                            sb.append(c);
                        } else {
                            int possibleDigits = value < 3 ? 2:1;
                            int radixExp = 3;
                            i = parseNumericValue(string, sb, i, value, possibleDigits, radixExp);
                        }
                    }
                }
                escaped = false;
            } else {
                if (c == '\\') {
                    escaped = true;
                } else if (c == quoteChar) {
                    break;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

	private String unescape(String string) {
        return unescape(string, -1, true, new StringBuilder());
    }

	private String removeEscapeChars(String str, String tickChar) {
        return StringUtil.replaceAll(str, tickChar + tickChar, tickChar);
    }

	protected String normalizeStringLiteral(String s) {
		int start = 1;
		boolean unescape = false;
  		if (s.charAt(0) == 'N') {
  			start++;
  		} else if (s.charAt(0) == 'E') {
  			start++;
  			unescape = true;
  		}
  		char tickChar = s.charAt(start - 1);
  		s = s.substring(start, s.length() - 1);
  		String result = removeEscapeChars(s, String.valueOf(tickChar));
  		if (unescape) {
  			result = unescape(result);
  		}
  		return result;
	}
	
	/**
	 * @param s
	 * @return normalized string id
	 */
	protected String normalizeId(String s) {
		if (s.indexOf('"') == -1) {
			return s;
		}
		List<String> nameParts = new LinkedList<String>();
		while (s.length() > 0) {
			if (s.charAt(0) == '"') {
				boolean escape = false;
				for (int i = 1; i < s.length(); i++) {
					if (s.charAt(i) != '"') {
						continue;
					}
					escape = !escape;
					boolean end = i == s.length() - 1;
					if (end || (escape && s.charAt(i + 1) == '.')) {
				  		String part = s.substring(1, i);
				  		s = s.substring(i + (end?1:2));
				  		nameParts.add(removeEscapeChars(part, "\"")); //$NON-NLS-1$
				  		break;
					}
				}
			} else {
				int index = s.indexOf('.');
				if (index == -1) {
					nameParts.add(s);
					break;
				} 
				nameParts.add(s.substring(0, index));
				s = s.substring(index + 1);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> i = nameParts.iterator(); i.hasNext();) {
			sb.append(i.next());
			if (i.hasNext()) {
				sb.append('.');
			}
		}
		return sb.toString();
	}
	
    /**
     * Check if this is a valid string literal
     * @param id Possible string literal
     */
    protected boolean isStringLiteral(String str, ParseInfo info) {
    	if (info.useAnsiQuotedIdentifiers() || str.charAt(0) != '"' || str.charAt(str.length() - 1) != '"') {
    		return false;
    	}
    	int index = 1;
    	while (index < str.length() - 1) {
    		index = str.indexOf('"', index);
    		if (index == -1 || index + 1 == str.length()) {
    			return true;
    		}
    		if (str.charAt(index + 1) != '"') {
    			return false;
    		}
    		index += 2;
    	}
    	return true;
    }    

    protected String validateName(String id, boolean element) throws Exception {
        if(id.indexOf('.') != -1) { 
            Messages.TeiidParser key = Messages.TeiidParser.Invalid_alias;
            if (element) {
                key = Messages.TeiidParser.Invalid_short_name;
            }
            throw new TeiidClientException(Messages.getString(key, id)); 
        }
        return id;
    }

    @Removed(Version.TEIID_8_0)
    protected String matchesAny(String arg, String ... expected) {
        for (String string : expected) {
            if (string.equalsIgnoreCase(arg)) {
                return arg;
            }
        }
        return null;
    }

    @Override
    public CommandImpl procedureBodyCommand(ParseInfo parseInfo) throws Exception {
        throw new UnsupportedOperationException("Not supported in Teiid Version " + getVersion()); //$NON-NLS-1$
    }

    /**
     * @param factory
     * @throws Exception 
     */
    @Override
    public void parseMetadata(MetadataFactory factory) throws Exception {
        throw new UnsupportedOperationException("Not supported in Teiid Version " + getVersion()); //$NON-NLS-1$
    }

    @Since(Version.TEIID_8_0)
    protected void setColumnOptions(BaseColumn c) {
    	Map<String, String> props = c.getProperties();
		setCommonProperties(c, props);
		
    	String v = props.remove(DDLConstants.RADIX); 
    	if (v != null) {
    		c.setRadix(Integer.parseInt(v));
    	}
    	
    	if (c instanceof Column) {
    		setColumnOptions((Column)c, props);
    	}
    }

	@Since(Version.TEIID_8_0)
    protected void removeColumnOption(String key, BaseColumn c) {
    	if (c.getProperty(key, false) != null) {
    		c.setProperty(key, null);
    	}    	
		removeCommonProperty(key, c);
		
    	if (key.equals(DDLConstants.RADIX)) {
    		c.setRadix(0);
    	}
    	
    	if (c instanceof Column) {
    		removeColumnOption(key, (Column)c);
    	}
    }    
    
	@Since(Version.TEIID_8_0)
    protected void removeColumnOption(String key, Column c) {
        if (key.equals(DDLConstants.CASE_SENSITIVE)) {
        	c.setCaseSensitive(false);
        }
    	
    	if (key.equals(DDLConstants.SELECTABLE)) {
    		c.setSelectable(true);
    	}
    	
    	if (key.equals(DDLConstants.UPDATABLE)) {
    		c.setUpdatable(false);
    	}
    	
    	if (key.equals(DDLConstants.SIGNED)) {
    		c.setSigned(false);
    	}
    	
    	if (key.equals(DDLConstants.CURRENCY)) {
    		c.setSigned(false);
    	}

    	if (key.equals(DDLConstants.FIXED_LENGTH)) {
    		c.setFixedLength(false);
    	}
    	
    	if (key.equals(DDLConstants.SEARCHABLE)) {
    		c.setSearchType(null);
    	}
    	
    	if (key.equals(DDLConstants.MIN_VALUE)) {
    		c.setMinimumValue(null);
    	}
    	
    	if (key.equals(DDLConstants.MAX_VALUE)) {
    		c.setMaximumValue(null);
    	}
    	
    	if (key.equals(DDLConstants.CHAR_OCTET_LENGTH)) {
    		c.setCharOctetLength(0);
    	}
        
    	if (key.equals(DDLConstants.NATIVE_TYPE)) {
    		c.setNativeType(null);
    	}

    	if (key.equals(DDLConstants.NULL_VALUE_COUNT)) {
    		c.setNullValues(-1);
    	}
    	
    	if (key.equals(DDLConstants.DISTINCT_VALUES)) {
    		c.setDistinctValues(-1);
    	}

    	if (key.equals(DDLConstants.UDT)) {
			c.setDatatype(null);
			c.setLength(0);
			c.setPrecision(0);
			c.setScale(0);
    	}    	
    }

    @Since(Version.TEIID_8_0)
    private void setColumnOptions(Column c, Map<String, String> props) {
        String v = props.remove(DDLConstants.CASE_SENSITIVE); 
        if (v != null) {
            c.setCaseSensitive(isTrue(v));
        }
        
        v = props.remove(DDLConstants.SELECTABLE);
        if (v != null) {
            c.setSelectable(isTrue(v));
        }
        
        v = props.remove(DDLConstants.UPDATABLE); 
        if (v != null) {
            c.setUpdatable(isTrue(v));
        }
        
        v = props.remove(DDLConstants.SIGNED);
        if (v != null) {
            c.setSigned(isTrue(v));
        }
        
        v = props.remove(DDLConstants.CURRENCY);
        if (v != null) {
            c.setSigned(isTrue(v));
        }

        v = props.remove(DDLConstants.FIXED_LENGTH);
        if (v != null) {
            c.setFixedLength(isTrue(v));
        }
        
        v = props.remove(DDLConstants.SEARCHABLE);
        if (v != null) {
            c.setSearchType(StringUtil.caseInsensitiveValueOf(SearchType.class, v));
        }
        
        v = props.remove(DDLConstants.MIN_VALUE);
        if (v != null) {
            c.setMinimumValue(v);
        }
        
        v = props.remove(DDLConstants.MAX_VALUE);
        if (v != null) {
            c.setMaximumValue(v);
        }
        
        v = props.remove(DDLConstants.CHAR_OCTET_LENGTH);
        if (v != null) {
            c.setCharOctetLength(Integer.parseInt(v));
        }
        
        v = props.remove(DDLConstants.NATIVE_TYPE);
        if (v != null) {
            c.setNativeType(v);
        }

        v = props.remove(DDLConstants.NULL_VALUE_COUNT); 
        if (v != null) {
            c.setNullValues(Integer.parseInt(v));
        }
        
        v = props.remove(DDLConstants.DISTINCT_VALUES); 
        if (v != null) {
            c.setDistinctValues(Integer.parseInt(v));
        }

        v = props.remove(DDLConstants.UDT); 
        if (v != null) {
            Matcher matcher = udtPattern.matcher(v);
            Map<String, Datatype> datatypes = SystemMetadata.getInstance(getVersion()).getSystemStore().getDatatypes();
            if (matcher.matches() && datatypes.get(matcher.group(1)) != null) {
                c.setDatatype(datatypes.get(matcher.group(1)));
                c.setLength(Integer.parseInt(matcher.group(2)));
                c.setPrecision(Integer.parseInt(matcher.group(3)));
                c.setScale(Integer.parseInt(matcher.group(4)));
            }
            else {
                throw new RuntimeException(Messages.getString(Messages.TeiidParser.udt_format_wrong, c.getName()));
            }
        }
    }

    @Since(Version.TEIID_8_0)
    protected void setCommonProperties(AbstractMetadataRecord c, Map<String, String> props) {
        String v = props.remove(DDLConstants.UUID); 
        if (v != null) {
            c.setUUID(v);
        }
        
        v = props.remove(DDLConstants.ANNOTATION); 
        if (v != null) {
            c.setAnnotation(v);
        }
        
        v = props.remove(DDLConstants.NAMEINSOURCE); 
        if (v != null) {
            c.setNameInSource(v);
        }
    }

    @Since(Version.TEIID_8_0)
    protected void removeCommonProperty(String key, AbstractMetadataRecord c) {
		if (key.equals(DDLConstants.UUID)) {
			c.setUUID(null);
		}
		
    	if (key.equals(DDLConstants.ANNOTATION)) {
    		c.setAnnotation(null);
    	}
		
		if (key.equals(DDLConstants.NAMEINSOURCE)) {
			c.setNameInSource(null);
		}
	}

    @Since(Version.TEIID_8_0)
    protected void setTableOptions(Table table) {
        Map<String, String> props = table.getProperties();
        setCommonProperties(table, props);
        
        String value = props.remove(DDLConstants.MATERIALIZED); 
        if (value != null) {
            table.setMaterialized(isTrue(value));
        }
        
        value = props.remove(DDLConstants.MATERIALIZED_TABLE); 
        if (value != null) {
            Table mattable = new Table();
            mattable.setName(value);
            table.setMaterializedTable(mattable);
        }
        
        value = props.remove(DDLConstants.UPDATABLE); 
        if (value != null) {
            table.setSupportsUpdate(isTrue(value));
        }
        
        value = props.remove(DDLConstants.CARDINALITY); 
        if (value != null) {
            table.setCardinality(Integer.parseInt(value));
        }
    }

	@Since(Version.TEIID_8_0)
	protected void removeTableOption(String key, Table table) {
    	if (table.getProperty(key, false) != null) {
    		table.setProperty(key, null);
    	}
    	removeCommonProperty(key, table);
    	
    	if (key.equals(DDLConstants.MATERIALIZED)) {
    		table.setMaterialized(false);
    	}
    	
    	if (key.equals(DDLConstants.MATERIALIZED_TABLE)) {
    		table.setMaterializedTable(null);
    	}
    	
    	if (key.equals(DDLConstants.UPDATABLE)) {
    		table.setSupportsUpdate(false);
    	}
    	
    	if (key.equals(DDLConstants.CARDINALITY)) {
    		table.setCardinality(-1);
    	}    	
    }

	@Since(Version.TEIID_8_0)
	protected void replaceProcedureWithFunction(MetadataFactory factory,
			Procedure proc) {
		FunctionMethod method = new FunctionMethod();
		method.setName(proc.getName());
		method.setPushdown(proc.isVirtual()?FunctionMethod.PushDown.CAN_PUSHDOWN:FunctionMethod.PushDown.MUST_PUSHDOWN);
		
		ArrayList<FunctionParameter> ins = new ArrayList<FunctionParameter>();
		for (ProcedureParameter pp:proc.getParameters()) {
			if (pp.getType() == ProcedureParameter.Type.InOut || pp.getType() == ProcedureParameter.Type.Out) {
				throw  new RuntimeException(Messages.getString(Messages.TeiidParser.function_in, proc.getName()));
			}
			
			FunctionParameter fp = new FunctionParameter(pp.getName(), pp.getRuntimeType(), pp.getAnnotation());
			if (pp.getType() == ProcedureParameter.Type.In) {
				fp.setVarArg(pp.isVarArg());
				ins.add(fp);
			} else {
				method.setOutputParameter(fp);
			}
		}
		method.setInputParameters(ins);
		
		if (proc.getResultSet() != null || method.getOutputParameter() == null) {
			throw  new RuntimeException(Messages.getString(Messages.TeiidParser.function_return, proc.getName()));
		}
		
		method.setAnnotation(proc.getAnnotation());
		method.setNameInSource(proc.getNameInSource());
		method.setUUID(proc.getUUID());
		
		Map<String, String> props = proc.getProperties();

		String value = props.remove(DDLConstants.CATEGORY); 
		method.setCategory(value);
		
		value = props.remove(DDLConstants.DETERMINISM); 
		if (value != null) {
			method.setDeterminism(FunctionMethod.Determinism.valueOf(value.toUpperCase()));
		}
		
		value = props.remove(DDLConstants.JAVA_CLASS); 
		method.setInvocationClass(value);
		
		value = props.remove(DDLConstants.JAVA_METHOD); 
		method.setInvocationMethod(value);
		
		for (String key:props.keySet()) {
			value = props.get(key);
			method.setProperty(key, value);
		}
		
		FunctionMethod.convertExtensionMetadata(proc, method);
		if (method.getInvocationMethod() != null) {
    		method.setPushdown(PushDown.CAN_PUSHDOWN);
    	}
		factory.getSchema().addFunction(method);
		factory.getSchema().getProcedures().remove(proc.getName());
	}

	@Since(Version.TEIID_8_0)
	protected void setProcedureOptions(Procedure proc) {
    	Map<String, String> props = proc.getProperties();
    	setCommonProperties(proc, props);
    	
    	String value = props.remove("UPDATECOUNT"); //$NON-NLS-1$
    	if (value != null) {
    		proc.setUpdateCount(Integer.parseInt(value));
    	}
    }

	@Since(Version.TEIID_8_0)
	protected void removeOption(String option, AbstractMetadataRecord record) {
    	if (record instanceof Table) {
    		removeTableOption(option, (Table)record);
    	}
    	if (record instanceof Procedure) {
    		removeProcedureOption(option, (Procedure)record);
    	}
    	if (record instanceof BaseColumn) {
    		removeColumnOption(option, (BaseColumn)record);
    	}
    }

	@Since(Version.TEIID_8_0)
	protected void setOptions(AbstractMetadataRecord record) {
    	if (record instanceof Table) {
    		setTableOptions((Table)record);
    	}
    	if (record instanceof Procedure) {
    		setProcedureOptions((Procedure)record);
    	}
    	if (record instanceof BaseColumn) {
    		setColumnOptions((BaseColumn)record);
    	}
    }

	@Since(Version.TEIID_8_0)
	protected void removeProcedureOption(String key, Procedure proc) {
    	if (proc.getProperty(key, false) != null) {
    		proc.setProperty(key, null);
    	}    	
    	removeCommonProperty(key, proc);
    	
    	if (key.equals("UPDATECOUNT")) { //$NON-NLS-1$
    		proc.setUpdateCount(1);
    	}
    }    

    @Since(Version.TEIID_8_0)
	protected boolean isTrue(final String text) {
        return Boolean.valueOf(text);
    }    

    @Since(Version.TEIID_8_0)
	protected AbstractMetadataRecord getChild(String name, AbstractMetadataRecord record, boolean parameter) {
    	if (record instanceof Table) {
    		if (parameter) {
    			throw  new RuntimeException(Messages.getString(Messages.TeiidParser.alter_table_param, name, record.getName())); 
    		}
    		return getColumn(name, (Table)record);
    	}
		return getColumn(name, (Procedure)record, parameter);
    	//TODO: function is not supported yet because we store by uid, which should instead be a more friendly "unique name"
    }

    @Since(Version.TEIID_8_0)
	protected Column getColumn(String columnName, Table table) {
		Column c = table.getColumnByName(columnName);
		if (c != null) {
			return c;
		}
		throw  new RuntimeException(Messages.getString(Messages.TeiidParser.no_column, columnName, table.getName())); 
	}

    @Since(Version.TEIID_8_0)
	protected AbstractMetadataRecord getColumn(String paramName, Procedure proc, boolean parameter) {
		if (proc.getResultSet() != null) {
			Column result = proc.getResultSet().getColumnByName(paramName);
			if (result != null) {
				return result;
			}
		}
		if (parameter) {
			List<ProcedureParameter> params = proc.getParameters();
			for (ProcedureParameter param:params) {
				if (param.getName().equalsIgnoreCase(paramName)) {
					return param;
				}
			}
		}
		throw  new RuntimeException(Messages.getString(Messages.TeiidParser.alter_procedure_param_doesnot_exist, paramName, proc.getName()));
	}
	
    @Since(Version.TEIID_8_0)
	protected FunctionParameter getParameter(String paramName, FunctionMethod func) {
		List<FunctionParameter> params = func.getInputParameters();
		for (FunctionParameter param:params) {
			if (param.getName().equalsIgnoreCase(paramName)) {
				return param;
			}
		}
		throw  new RuntimeException(Messages.getString(Messages.TeiidParser.alter_function_param_doesnot_exist, paramName, func.getName()));
	}	
	
    @Since(Version.TEIID_8_0)
	protected void createDDLTrigger(MetadataFactory schema, AlterTriggerImpl trigger) {
		GroupSymbolImpl group = trigger.getTarget();
		
		Table table = schema.getSchema().getTable(group.getName());
		if (trigger.getEvent().equals(Table.TriggerEvent.INSERT)) {
			table.setInsertPlan(trigger.getDefinition().toString());
		}
		else if (trigger.getEvent().equals(Table.TriggerEvent.UPDATE)) {
			table.setUpdatePlan(trigger.getDefinition().toString());
		}
		else if (trigger.getEvent().equals(Table.TriggerEvent.DELETE)) {
			table.setDeletePlan(trigger.getDefinition().toString());
		}
	}

    @Since(Version.TEIID_8_0)
    protected BaseColumn addProcColumn(MetadataFactory factory, Procedure proc, String name, ParsedDataType type, boolean rs) {
        BaseColumn column = null;
        if (rs) {
            column = factory.addProcedureResultSetColumn(name, type.getType(), proc);
        } else {
            boolean added = false;
            for (ProcedureParameter pp : proc.getParameters()) {
                if (pp.getType() == Type.ReturnValue) {
                    added = true;
                    if (pp.getDatatype() != factory.getDataTypes().get(type.getType())) {
                        throw new RuntimeException(Messages.getString(Messages.TeiidParser.proc_type_conflict, proc.getName(), pp.getDatatype(), type.getType()));
                    }
                }
            }
            if (!added) {
                column = factory.addProcedureParameter(name, type.getType(), ProcedureParameter.Type.ReturnValue, proc);
            }
        }
        setTypeInfo(type, column);
        return column;
    }

	@Since(Version.TEIID_8_0)
    protected void setTypeInfo(ParsedDataType type, BaseColumn column) {
        if (type.getLength() != null){
            column.setLength(type.getLength());
        }
        if (type.getScale() != null){
            column.setScale(type.getScale());
        }   
        if (type.getPrecision() != null){
            column.setPrecision(type.getPrecision());
        }
    }

	@Since(Version.TEIID_8_0)
	protected String resolvePropertyKey(MetadataFactory factory, String key) {
	 	int index = key.indexOf(':');
	 	if (index > 0 && index < key.length() - 1) {
	 		String prefix = key.substring(0, index);
	 		String uri = MetadataFactory.BUILTIN_NAMESPACES.get(prefix);
	 		if (uri == null) {
	 			uri = factory.getNamespaces().get(prefix);
	 		}
	 		if (uri != null) {
	 			key = '{' +uri + '}' + key.substring(index + 1, key.length());
	 		}
	 		//TODO warnings or errors if not resolvable 
	 	}
	 	return key;
	}

    @Since(Version.TEIID_8_0)
    protected KeyRecord addFBI(MetadataFactory factory, List<BaseExpression> expressions, Table table, String name) {
        List<String> columnNames = new ArrayList<String>(expressions.size());
        List<Boolean> nonColumnExpressions = new ArrayList<Boolean>(expressions.size());
        boolean fbi = false;
        for (int i = 0; i < expressions.size(); i++) {
            BaseExpression ex = expressions.get(i);
            if (ex instanceof ElementSymbolImpl) {
                columnNames.add(((ElementSymbolImpl)ex).getName());
                nonColumnExpressions.add(Boolean.FALSE);
            } else {
                columnNames.add(ex.toString());
                nonColumnExpressions.add(Boolean.TRUE);
                fbi = true;
            }
        }
        return factory.addFunctionBasedIndex(name != null?name:(SQLConstants.NonReserved.INDEX+(fbi?table.getFunctionBasedIndexes().size():table.getIndexes().size())), columnNames, nonColumnExpressions, table);
    }

    @Since(Version.TEIID_8_0)
    protected MetadataFactory getTempMetadataFactory() {
        if (this.metadataFactory == null) {
            this.metadataFactory = new MetadataFactory(version, "temp", 1, "temp", //$NON-NLS-1$ //$NON-NLS-2$
                                                       SystemMetadata.getInstance(getVersion()).getRuntimeTypeMap(), null, null);
        }
        return this.metadataFactory;
    }

    @Since(Version.TEIID_8_5)
    protected void setSourceHint(SourceHintImpl sourceHint, CommandImpl command) {
        if (sourceHint == null)
            return;

        if (command instanceof SetQueryImpl) {
            ((SetQueryImpl)command).getProjectedQuery().setSourceHint(sourceHint);
        } else {
            command.setSourceHint(sourceHint);
        }
    }

    @Since(Version.TEIID_8_5)
    protected List<BaseExpression> arrayExpressions(List<BaseExpression> expressions, BaseExpression expr) {
        if (expressions == null) {
            expressions = new ArrayList<BaseExpression>();
        }
        if (expr != null) {
            expressions.add(expr);
        }
        return expressions;
    }
}
