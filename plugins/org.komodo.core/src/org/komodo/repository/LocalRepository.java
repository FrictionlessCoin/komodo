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
package org.komodo.repository;

import java.net.URL;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.jcr.Session;
import org.komodo.core.KEngine;
import org.komodo.repository.internal.ModeshapeEngineThread;
import org.komodo.repository.internal.ModeshapeEngineThread.Request;
import org.komodo.repository.internal.ModeshapeEngineThread.RequestCallback;
import org.komodo.repository.internal.ModeshapeEngineThread.RequestType;
import org.komodo.spi.KException;
import org.komodo.spi.repository.RepositoryClientEvent;
import org.komodo.utils.ArgCheck;

/**
 * A repository installed on the local machine, using the modeshape engine and repository.
 */
public class LocalRepository extends RepositoryImpl {

    private static String LOCAL_REPOSITORY_CONFIG = "local-repository-config.json"; //$NON-NLS-1$

    /**
     * The default local repository identifier used for the production komodo engine.
     */
    public static final LocalRepositoryId DEFAULT_LOCAL_REPOSITORY_ID = new LocalRepositoryId(
                                                                                              LocalRepository.class.getResource(LOCAL_REPOSITORY_CONFIG),
                                                                                              DEFAULT_LOCAL_WORKSPACE_NAME);

    /**
     * Identifier for the local repository
     */
    public static class LocalRepositoryId implements Id {

        private final URL configPath;

        private final String workspaceName;

        /**
         * @param configPathUrl url of configuration file
         * @param workspaceName name of workspace
         */
        public LocalRepositoryId(final URL configPathUrl, final String workspaceName ) {
            this.configPath = configPathUrl;
            this.workspaceName = workspaceName;
        }

        @Override
        public URL getConfiguration() {
            return configPath;
        }

        @Override
        public String getUrl() {
            return this.configPath.toString();
        }

        @Override
        public String getWorkspaceName() {
            return this.workspaceName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.configPath == null) ? 0 : this.configPath.hashCode());
            result = prime * result + ((this.workspaceName == null) ? 0 : this.workspaceName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LocalRepositoryId other = (LocalRepositoryId)obj;
            if (this.configPath == null) {
                if (other.configPath != null)
                    return false;
            } else if (!this.configPath.equals(other.configPath))
                return false;
            if (this.workspaceName == null) {
                if (other.workspaceName != null)
                    return false;
            } else if (!this.workspaceName.equals(other.workspaceName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "LocalRepositoryId [configPath=" + this.configPath + ", workspaceName=" + this.workspaceName + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private WeakHashMap< Session, UnitOfWork > sessions = new WeakHashMap<>();

    private State state = State.NOT_REACHABLE;

    private ModeshapeEngineThread engineThread;

    /**
     * Create an instance if a local repository using the specified configuration file.
     *
     * @param configPathUrl
     *        the URL of the configuration file (cannot be empty)
     * @param workspaceName
     *        the name of the repository workspace in the configuration file (cannot be empty)
     */
    public LocalRepository( final URL configPathUrl,
                            final String workspaceName ) {
        super(Type.LOCAL, new LocalRepositoryId(configPathUrl, workspaceName));
    }

    /**
     * Create an instance of local repository.
     *
     * @param repositoryId repository configuration of the instance
     */
    public LocalRepository(LocalRepositoryId repositoryId) {
        super(Type.LOCAL, repositoryId);
    }

    /**
     * Create an instance of local repository using default configuration file and workspace name.
     */
    public LocalRepository() {
        this(DEFAULT_LOCAL_REPOSITORY_ID);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( final Object obj ) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || !getClass().equals(obj.getClass())) {
            return false;
        }

        final LocalRepository that = (LocalRepository)obj;
        return getId().equals(that.getId());
    }

    @Override
    public State getState() {
        return state;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean ping() {
        return ((this.engineThread != null) && ((this.engineThread.isAlive())) && this.engineThread.isRunning());
    }

    private Session createSession() throws KException {
        final CountDownLatch latch = new CountDownLatch(1);

        class CreateSessionCallback implements RequestCallback {

            private Throwable error = null;
            private Session result = null;

            @Override
            public void errorOccurred( final Throwable e ) {
                this.error = e;
            }

            Throwable getError() {
                return this.error;
            }

            Session getSession() {
                return this.result;
            }

            @Override
            public void respond( final Object results ) {
                this.result = (Session)results;
                latch.countDown();
            }

        }

        final CreateSessionCallback callback = new CreateSessionCallback();
        this.engineThread.accept(new Request(RequestType.CREATE_SESSION, callback));

        boolean timeout = false;

        try {
            timeout = !latch.await(5, TimeUnit.SECONDS);
        } catch (final Exception e) {
            throw new KException(e);
        }

        if (timeout) {
            throw new KException(Messages.getString(Messages.LocalRepository.Unable_To_Create_Session));
        }

        if (callback.getError() != null) {
            throw new KException(callback.getError());
        }

        return callback.getSession();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#createTransaction(java.lang.String, boolean,
     *      org.komodo.spi.repository.Repository.UnitOfWorkListener)
     */
    @Override
    public UnitOfWork createTransaction( final String name,
                                         final boolean rollbackOnly,
                                         final UnitOfWorkListener callback ) throws KException {
        ArgCheck.isNotEmpty(name, "name"); //$NON-NLS-1$
        LOGGER.debug("creating transaction {0} with rollbackOnly = {1}", name, rollbackOnly); //$NON-NLS-1$
        final Session session = createSession();
        final UnitOfWork uow = new LocalRepositoryTransaction(name, session, rollbackOnly, callback);
        this.sessions.put(session, uow);
        return uow;
    }

    class LocalRepositoryTransaction extends RepositoryImpl.UnitOfWorkImpl {

        LocalRepositoryTransaction( final String uowName,
                                    final Session uowSession,
                                    final boolean uowRollbackOnly,
                                    final UnitOfWorkListener listener) {
            super(uowName, uowSession, uowRollbackOnly, listener);
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.repository.RepositoryImpl.UnitOfWorkImpl#commit()
         */
        @Override
        public void commit() {
            if (isRollbackOnly()) {
                rollback();
            } else {
                final CountDownLatch latch = new CountDownLatch(1);

                class CommitCallback implements RequestCallback {

                    @Override
                    public void errorOccurred( final Throwable error ) {
                        if (getCallback() == null) {
                            KEngine.getInstance().getErrorHandler().error(error);
                            return; // No callback so error can only be logged
                        }

                        getCallback().errorOccurred(error);
                    }

                    @Override
                    public void respond( final Object results ) {
                        latch.countDown();
                    }

                }

                final CommitCallback callback = new CommitCallback();
                LocalRepository.this.engineThread.accept(new ModeshapeEngineThread.SessionRequest(RequestType.COMMIT_SESSION,
                                                                                                  callback, getSession(),
                                                                                                  getName()));

                boolean noTimeout = false;

                try {
                    noTimeout = latch.await(3, TimeUnit.MINUTES);
                } catch (final Exception e) {
                    callback.equals(e);
                }

                if (noTimeout) {
                    callback.respond(null);
                } else {
                    callback.errorOccurred(new KException(Messages.getString(Messages.LocalRepository.Commit_Timeout, getName())));
                }
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.repository.RepositoryImpl.UnitOfWorkImpl#rollback()
         */
        @Override
        public void rollback() {
            final CountDownLatch latch = new CountDownLatch(1);

            class RollbackCallback implements RequestCallback {

                @Override
                public void errorOccurred( final Throwable error ) {
                    if (getCallback() == null) {
                        KEngine.getInstance().getErrorHandler().error(error);
                        return; // No callback so error can only be logged
                    }

                    getCallback().errorOccurred(error);
                }

                @Override
                public void respond( final Object results ) {
                    latch.countDown();
                }

            }

            final RollbackCallback callback = new RollbackCallback();
            LocalRepository.this.engineThread.accept(new ModeshapeEngineThread.SessionRequest(RequestType.ROLLBACK_SESSION,
                                                                                              callback,
                                                                                              getSession(),
                                                                                              getName()));

            boolean noTimeout = false;

            try {
                noTimeout = latch.await(3, TimeUnit.MINUTES);
            } catch (final Exception e) {
                callback.equals(e);
            }

            if (noTimeout) {
                callback.respond(null);
            } else {
                callback.errorOccurred(new KException(Messages.getString(Messages.LocalRepository.Rollback_Timeout, getName())));
            }
        }

    }

    private void createEngineThread() {
        if (engineThread != null && engineThread.isAlive()) return;

        if (engineThread != null && !engineThread.isAlive()) throw new RuntimeException(
                                                                                        Messages.getString(Messages.LocalRepository.EngineThread_Died));

        engineThread = new ModeshapeEngineThread(getId().getConfiguration(), getId().getWorkspaceName());
        engineThread.start();
    }

    private void startRepository() {
        if (this.state == State.REACHABLE) return;

        createEngineThread();

        RequestCallback callback = new RequestCallback() {

            @Override
            public void errorOccurred( final Throwable error ) {
                throw new RuntimeException(error);
            }

            @Override
            public void respond( final Object results ) {
                if (engineThread.isRunning()) {
                    LocalRepository.this.state = State.REACHABLE;
                    notifyObservers();
                }
            }
        };

        engineThread.accept(new Request(RequestType.START, callback));
    }

    private void stopRepository() {
        RequestCallback callback = new RequestCallback() {

            /**
             * {@inheritDoc}
             *
             * @see org.komodo.repository.internal.ModeshapeEngineThread.RequestCallback#errorOccurred(java.lang.Throwable)
             */
            @Override
            public void errorOccurred( final Throwable error ) {
                throw new RuntimeException(error);
            }

            /**
             * {@inheritDoc}
             *
             * @see org.komodo.repository.internal.ModeshapeEngineThread.RequestCallback#respond(java.lang.Object)
             */
            @Override
            public void respond( final Object results ) {
                if (!engineThread.isRunning()) {
                    LocalRepository.this.state = State.NOT_REACHABLE;
                    notifyObservers();
                }
            }
        };

        // check session cache
        if (!this.sessions.isEmpty()) {
            for (final Entry< Session, UnitOfWork > entry : this.sessions.entrySet()) {
                final Session session = entry.getKey();

                // rollback and close all leftover sessions (there should not be any)
                if (session.isLive()) {
                    final UnitOfWork uow = entry.getValue();
                    LOGGER.debug("LocalRepository.stopRepository: closing session for transaction {0}", uow.getName()); //$NON-NLS-1$
                    uow.rollback();
                    session.logout();
                }
            }
        }

        this.engineThread.accept(new Request(RequestType.STOP, callback));
    }

    private void clearRepository() {
        RequestCallback callback = new RequestCallback() {

            /**
             * {@inheritDoc}
             *
             * @see org.komodo.repository.internal.ModeshapeEngineThread.RequestCallback#errorOccurred(java.lang.Throwable)
             */
            @Override
            public void errorOccurred( final Throwable error ) {
                throw new RuntimeException(error);
            }

            /**
             * {@inheritDoc}
             *
             * @see org.komodo.repository.internal.ModeshapeEngineThread.RequestCallback#respond(java.lang.Object)
             */
            @Override
            public void respond( final Object results ) {
                notifyObservers();
            }
        };

        this.engineThread.accept(new Request(RequestType.CLEAR, callback));
    }

    @Override
    public void notify( RepositoryClientEvent event ) {
        if (event.getType() == RepositoryClientEvent.EventType.STARTED) {
            // Start the modeshape engine if not already started
            startRepository();
        } else if (event.getType() == RepositoryClientEvent.EventType.SHUTTING_DOWN) {
            stopRepository();
        } else if (event.getType() == RepositoryClientEvent.EventType.CLEAR) {
            clearRepository();
        }
    }

}
