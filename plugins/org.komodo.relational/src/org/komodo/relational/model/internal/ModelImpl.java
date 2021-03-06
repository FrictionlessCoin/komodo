/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.model.internal;

import java.util.ArrayList;
import java.util.List;
import org.komodo.core.KomodoLexicon;
import org.komodo.relational.Messages.Relational;
import org.komodo.relational.internal.RelationalModelFactory;
import org.komodo.relational.internal.RelationalObjectImpl;
import org.komodo.relational.internal.TypeResolver;
import org.komodo.relational.model.Function;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.Procedure;
import org.komodo.relational.model.Table;
import org.komodo.relational.model.View;
import org.komodo.repository.ObjectImpl;
import org.komodo.spi.KException;
import org.komodo.spi.Messages;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.Property;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.utils.ArgCheck;
import org.modeshape.sequencer.ddl.dialect.teiid.TeiidDdlLexicon.CreateProcedure;
import org.modeshape.sequencer.ddl.dialect.teiid.TeiidDdlLexicon.CreateTable;
import org.modeshape.sequencer.teiid.lexicon.CoreLexicon;
import org.modeshape.sequencer.teiid.lexicon.VdbLexicon;

/**
 * An implementation of a relational model.
 */
public final class ModelImpl extends RelationalObjectImpl implements Model {

    /**
     * The resolver of a {@link Model}.
     */
    public static final TypeResolver RESOLVER = new TypeResolver() {

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#resolvable(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.Repository, org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public boolean resolvable( final UnitOfWork transaction,
                                   final Repository repository,
                                   final KomodoObject kobject ) {
            try {
                ObjectImpl.validateType(transaction, repository, kobject, VdbLexicon.Vdb.DECLARATIVE_MODEL);
                return true;
            } catch (final Exception e) {
                // not resolvable
            }

            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#resolve(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.Repository, org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public Model resolve( final UnitOfWork transaction,
                              final Repository repository,
                              final KomodoObject kobject ) throws KException {
            return new ModelImpl(transaction, repository, kobject.getAbsolutePath());
        }

    };

    /**
     * @param uow
     *        the transaction (can be <code>null</code> if update should be automatically committed)
     * @param repository
     *        the repository where the relational object exists (cannot be <code>null</code>)
     * @param workspacePath
     *        the workspace relative path (cannot be empty)
     * @throws KException
     *         if an error occurs or if node at specified path is not a model
     */
    public ModelImpl( final UnitOfWork uow,
                      final Repository repository,
                      final String workspacePath ) throws KException {
        super(uow, repository, workspacePath);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#addFunction(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public Function addFunction( final UnitOfWork uow,
                                 final String functionName ) throws KException {
        ArgCheck.isNotEmpty(functionName, "functionName"); //$NON-NLS-1$
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-addFunction", false, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("addFunction: transaction = {0}, functionName = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         functionName);
        }

        try {
            final Function result = RelationalModelFactory.createFunction(transaction, getRepository(), this, functionName);

            if (uow == null) {
                transaction.commit();
            }

            return result;
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#addProcedure(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public Procedure addProcedure( final UnitOfWork uow,
                                   final String procedureName ) throws KException {
        ArgCheck.isNotEmpty(procedureName, "procedureName"); //$NON-NLS-1$
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-addProcedure", false, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("addProcedure: transaction = {0}, procedureName = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         procedureName);
        }

        try {
            final Procedure result = RelationalModelFactory.createProcedure(transaction, getRepository(), this, procedureName);

            if (uow == null) {
                transaction.commit();
            }

            return result;
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#addTable(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public Table addTable( final UnitOfWork uow,
                           final String tableName ) throws KException {
        ArgCheck.isNotEmpty(tableName, "tableName"); //$NON-NLS-1$
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-addTable", false, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("addTable: transaction = {0}, tableName = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         tableName);
        }

        try {
            final Table result = RelationalModelFactory.createTable(transaction, getRepository(), this, tableName);

            if (uow == null) {
                transaction.commit();
            }

            return result;
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#addView(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public View addView( final UnitOfWork uow,
                         final String viewName ) throws KException {
        ArgCheck.isNotEmpty(viewName, "viewName"); //$NON-NLS-1$
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-viewName", false, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("addView: transaction = {0}, viewName = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         viewName);
        }

        try {
            final View result = RelationalModelFactory.createView(transaction, getRepository(), this, viewName);

            if (uow == null) {
                transaction.commit();
            }

            return result;
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.repository.ObjectImpl#getChildren(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public KomodoObject[] getChildren( final UnitOfWork uow ) throws KException {
        final Function[] functions = getFunctions(uow);
        final Procedure[] procedures = getProcedures(uow);
        final Table[] tables = getTables(uow);
        final View[] views = getViews(uow);

        final KomodoObject[] result = new KomodoObject[functions.length + procedures.length + tables.length + views.length];
        System.arraycopy(functions, 0, result, 0, functions.length);
        System.arraycopy(procedures, 0, result, functions.length, procedures.length);
        System.arraycopy(tables, 0, result, functions.length + procedures.length, tables.length);
        System.arraycopy(views, 0, result, functions.length + procedures.length + tables.length, views.length);

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getChildrenOfType(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public KomodoObject[] getChildrenOfType( final UnitOfWork uow,
                                             final String type ) throws KException {

        if (CreateProcedure.FUNCTION_STATEMENT.equals(type)) {
            return getFunctions(uow);
        }

        if (CreateProcedure.PROCEDURE_STATEMENT.equals(type)) {
            return getProcedures(uow);
        }

        if (CreateTable.TABLE_STATEMENT.equals(type)) {
            return getTables(uow);
        }

        if (CreateTable.VIEW_STATEMENT.equals(type)) {
            return getViews(uow);
        }

        return KomodoObject.EMPTY_ARRAY;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#getFunctions(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Function[] getFunctions( final UnitOfWork uow ) throws KException {
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-getFunctions", true, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getFunctions: transaction = {0}", transaction.getName()); //$NON-NLS-1$
        }

        try {
            final List< Function > result = new ArrayList< Function >();

            for (final KomodoObject kobject : super.getChildrenOfType(transaction, CreateProcedure.FUNCTION_STATEMENT)) {
                final Function function = new FunctionImpl(transaction, getRepository(), kobject.getAbsolutePath());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getFunctions: transaction = {0}, found function = {1}", //$NON-NLS-1$
                                 transaction.getName(),
                                 kobject.getAbsolutePath());
                }

                result.add(function);
            }

            if (uow == null) {
                transaction.commit();
            }

            if (result.isEmpty()) {
                return Function.NO_FUNCTIONS;
            }

            return result.toArray(new Function[result.size()]);
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    @Override
    public String getModelDefinition( final UnitOfWork uow ) throws KException {
        final String modelDefn = getObjectProperty(uow, Property.ValueType.STRING, "getModelDefinition", //$NON-NLS-1$
                                                   KomodoLexicon.VdbModel.MODEL_DEFINITION);

        return modelDefn == null ? EMPTY_STRING : modelDefn;
    }

    @Override
    public String getModelType( final UnitOfWork uow ) throws KException {
        final String modelType = getObjectProperty(uow, Property.ValueType.STRING, "getModelType", //$NON-NLS-1$
                                                   CoreLexicon.JcrId.MODEL_TYPE);

        return modelType == null ? EMPTY_STRING : modelType;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#getProcedures(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Procedure[] getProcedures( final UnitOfWork uow ) throws KException {
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-getProcedures", true, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getProcedures: transaction = {0}", transaction.getName()); //$NON-NLS-1$
        }

        try {
            final List< Procedure > result = new ArrayList< Procedure >();

            for (final KomodoObject kobject : super.getChildrenOfType(transaction, CreateProcedure.PROCEDURE_STATEMENT)) {
                final Procedure procedure = new ProcedureImpl(transaction, getRepository(), kobject.getAbsolutePath());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getProcedures: transaction = {0}, found procedure = {1}", //$NON-NLS-1$
                                 transaction.getName(),
                                 kobject.getAbsolutePath());
                }

                result.add(procedure);
            }

            if (uow == null) {
                transaction.commit();
            }

            if (result.isEmpty()) {
                return Procedure.NO_PROCEDURES;
            }

            return result.toArray(new Procedure[result.size()]);
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#getTables(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Table[] getTables( final UnitOfWork uow ) throws KException {
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-getTables", true, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getTables: transaction = {0}", transaction.getName()); //$NON-NLS-1$
        }

        try {
            final List< Table > result = new ArrayList< Table >();

            for (final KomodoObject kobject : super.getChildrenOfType(transaction, CreateTable.TABLE_STATEMENT)) {
                final Table table = new TableImpl(transaction, getRepository(), kobject.getAbsolutePath());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getTables: transaction = {0}, found procedure = {1}", //$NON-NLS-1$
                                 transaction.getName(),
                                 kobject.getAbsolutePath());
                }

                result.add(table);
            }

            if (uow == null) {
                transaction.commit();
            }

            if (result.isEmpty()) {
                return Table.NO_TABLES;
            }

            return result.toArray(new Table[result.size()]);
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#getViews(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public View[] getViews( final UnitOfWork uow ) throws KException {
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-getViews", true, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getViews: transaction = {0}", transaction.getName()); //$NON-NLS-1$
        }

        try {
            final List< View > result = new ArrayList< View >();

            for (final KomodoObject kobject : super.getChildrenOfType(transaction, CreateTable.VIEW_STATEMENT)) {
                final View view = new ViewImpl(transaction, getRepository(), kobject.getAbsolutePath());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getViews: transaction = {0}, found view = {1}", //$NON-NLS-1$
                                 transaction.getName(),
                                 kobject.getAbsolutePath());
                }

                result.add(view);
            }

            if (uow == null) {
                transaction.commit();
            }

            if (result.isEmpty()) {
                return View.NO_VIEWS;
            }

            return result.toArray(new View[result.size()]);
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#removeFunction(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void removeFunction( final UnitOfWork uow,
                                final String functionName ) throws KException {
        ArgCheck.isNotEmpty(functionName, "functionName"); //$NON-NLS-1$
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-removeFunction", false, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeFunction: transaction = {0}, functionName = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         functionName);
        }

        boolean found = false;

        try {
            final Function[] functions = getFunctions(transaction);

            if (functions.length != 0) {
                for (final Function function : functions) {
                    if (functionName.equals(function.getName(transaction))) {
                        removeChild(transaction, functionName);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                throw new KException(Messages.getString(Relational.FUNCTION_NOT_FOUND_TO_REMOVE, functionName));
            }

            if (uow == null) {
                transaction.commit();
            }
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#removeProcedure(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void removeProcedure( final UnitOfWork uow,
                                 final String procedureName ) throws KException {
        ArgCheck.isNotEmpty(procedureName, "procedureName"); //$NON-NLS-1$
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-removeProcedure", false, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeProcedure: transaction = {0}, procedureName = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         procedureName);
        }

        boolean found = false;

        try {
            final Procedure[] procedures = getProcedures(transaction);

            if (procedures.length != 0) {
                for (final Procedure procedure : procedures) {
                    if (procedureName.equals(procedure.getName(transaction))) {
                        removeChild(transaction, procedureName);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                throw new KException(Messages.getString(Relational.PROCEDURE_NOT_FOUND_TO_REMOVE, procedureName));
            }

            if (uow == null) {
                transaction.commit();
            }
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#removeTable(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void removeTable( final UnitOfWork uow,
                             final String tableName ) throws KException {
        ArgCheck.isNotEmpty(tableName, "tableName"); //$NON-NLS-1$
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-removeTable", false, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeTable: transaction = {0}, tableName = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         tableName);
        }

        boolean found = false;

        try {
            final Table[] tables = getTables(transaction);

            if (tables.length != 0) {
                for (final Table table : tables) {
                    if (tableName.equals(table.getName(transaction))) {
                        removeChild(transaction, tableName);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                throw new KException(Messages.getString(Relational.TABLE_NOT_FOUND_TO_REMOVE, tableName));
            }

            if (uow == null) {
                transaction.commit();
            }
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Model#removeView(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void removeView( final UnitOfWork uow,
                            final String viewName ) throws KException {
        ArgCheck.isNotEmpty(viewName, "viewName"); //$NON-NLS-1$
        UnitOfWork transaction = uow;

        if (transaction == null) {
            transaction = getRepository().createTransaction("modelimpl-removeView", false, null); //$NON-NLS-1$
        }

        assert (transaction != null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeView: transaction = {0}, viewName = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         viewName);
        }

        boolean found = false;

        try {
            final View[] views = getViews(transaction);

            if (views.length != 0) {
                for (final View view : views) {
                    if (viewName.equals(view.getName(transaction))) {
                        removeChild(transaction, viewName);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                throw new KException(Messages.getString(Relational.VIEW_NOT_FOUND_TO_REMOVE, viewName));
            }

            if (uow == null) {
                transaction.commit();
            }
        } catch (final Exception e) {
            throw handleError(uow, transaction, e);
        }
    }

    @Override
    public void setModelDefinition( final UnitOfWork uow,
                                    final String modelDefinition ) throws KException {
        setObjectProperty(uow, "setModelDefinition", KomodoLexicon.VdbModel.MODEL_DEFINITION, modelDefinition); //$NON-NLS-1$
    }

    @Override
    public void setModelType( final UnitOfWork uow,
                              final String modelType ) throws KException {
        setObjectProperty(uow, "setModelType", CoreLexicon.JcrId.MODEL_TYPE, modelType); //$NON-NLS-1$
    }

}
