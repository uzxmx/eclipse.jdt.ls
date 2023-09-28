/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ls.core.internal;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class LanguageServer implements IApplication {

  private volatile boolean shutdown;
  private long parentProcessId;
  private final Object waitLock = new Object();

	@Override
	public Object start(IApplicationContext context) throws Exception {
			java.nio.file.Files.write(java.nio.file.Paths.get(System.getProperty("user.home"), "tmp/foo.log"), "before startLanguageServer\n".getBytes(), java.nio.file.StandardOpenOption.APPEND);


    JavaLanguageServerPlugin.startLanguageServer(this);
		JavaLanguageServerPlugin.logInfo("after startLanguageServer foo");
    synchronized(waitLock){
          while (!shutdown) {
            try {
              context.applicationRunning();
              JavaLanguageServerPlugin.logInfo("Main thread is waiting 222");
              waitLock.wait();
            } catch (InterruptedException e) {
              JavaLanguageServerPlugin.logException(e.getMessage(), e);
            }
          }
    }
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
    synchronized(waitLock){
      waitLock.notifyAll();
    }
	}

	public void exit() {
    shutdown = true;
    JavaLanguageServerPlugin.logInfo("Shutdown received... waking up main thread");
    synchronized(waitLock){
      waitLock.notifyAll();
    }
  }

	public void setParentProcessId(long pid) {
		this.parentProcessId = pid;
	}

	/**
	 * @return the parentProcessId
	 */
	long getParentProcessId() {
		return parentProcessId;
	}
}
