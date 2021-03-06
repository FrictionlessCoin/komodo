/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.komodo.shell;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.komodo.shell.commands.core.StatusCommand;

/**
 * Test Class to test StatusCommand
 * 
 */
@SuppressWarnings("javadoc")
public class StatusCommandTest extends AbstractCommandTest {

	private static final String STATUS_COMMANDS1 = "statusCommands1.txt"; //$NON-NLS-1$
	private static final String STATUS_COMMANDS2 = "statusCommands2.txt"; //$NON-NLS-1$
	private static final String STATUS_COMMANDS3 = "statusCommands3.txt"; //$NON-NLS-1$
	private static final String INDENT = getIndentStr();
	
	/**
	 * Test for StatusCommand
	 */
	public StatusCommandTest( ) {
		super();
	}
	
    @Test
    public void testStatus1() throws Exception {
    	setup(STATUS_COMMANDS1, StatusCommand.class);
    	
    	execute();
    	
    	String expectedOutput = INDENT+"Current Repo    : local Repository\n"+ //$NON-NLS-1$
                                        INDENT+"Current Teiid Instance  : None set\n"+ //$NON-NLS-1$
                                        INDENT+"Current Context : [tko:komodo]\n"; //$NON-NLS-1$

    	String writerOutput = getCommandOutput();
    	assertEquals(expectedOutput,writerOutput);
    	assertEquals("tko:komodo", wsStatus.getCurrentContext().getFullName()); //$NON-NLS-1$
    }

    @Test
    public void testStatus2() throws Exception {
    	setup(STATUS_COMMANDS2, StatusCommand.class);
    	
    	execute();
    	
    	String expectedOutput = INDENT+"Current Repo    : local Repository\n"+ //$NON-NLS-1$
                                INDENT+"Current Teiid Instance  : None set\n"+ //$NON-NLS-1$
    	                        INDENT+"Current Context : [tko:komodo/tko:workspace/myModel]\n"; //$NON-NLS-1$
    	String writerOutput = getCommandOutput();
    	assertEquals(expectedOutput,writerOutput);
    	assertEquals("tko:komodo/tko:workspace/myModel", wsStatus.getCurrentContext().getFullName()); //$NON-NLS-1$
    }

    @Test
    public void testStatus3() throws Exception {
        setup(STATUS_COMMANDS3, StatusCommand.class);
        
        execute();
        
        String expectedOutput = INDENT+"Current Repo    : local Repository\n"+ //$NON-NLS-1$
                                INDENT+"Current Teiid Instance  : [mms://localhost:9999 : Not Connected]\n"+ //$NON-NLS-1$
                                INDENT+"Current Teiid Instance Jdbc  : [jdbc:teiid:<vdbname>@mms://localhost:31000 : Not Connected]\n"+ //$NON-NLS-1$
                                INDENT+"Current Context : [tko:komodo/tko:workspace/TestTeiid]\n"; //$NON-NLS-1$
        String writerOutput = getCommandOutput();
        assertEquals(expectedOutput,writerOutput);
        assertEquals("tko:komodo/tko:workspace/TestTeiid", wsStatus.getCurrentContext().getFullName()); //$NON-NLS-1$
    }
}
