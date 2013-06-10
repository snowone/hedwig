/*
 * Copyright 2010 the original author or authors.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.hs.mail.smtp.spool;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

/**
 * Class to watch files under some conditions
 * 
 * @author Won Chul Doh
 * @since Jun 3, 2010
 * 
 */
public class FileWatcher implements Watcher {
	
	static Logger logger = Logger.getLogger(FileWatcher.class);
	
    private List<Consumer> consumers = null;
    private MainJob mainJob = null;
    private File targetDir = null;
    private long watchInterval = 30000;
    private long processInterval = 10;
    private boolean includeDirectory = false;
    private File failureDirFile = null;
    private Comparator<File> fileComparator = null;

	public long getWatchInterval() {
		return watchInterval;
	}

	public void setWatchInterval(long millis) {
		this.watchInterval = millis;
	}

	public List<Consumer> getConsumers() {
		return consumers;
	}

	public void setConsumers(List<Consumer> consumers) {
		this.consumers = consumers;
	}

	public void setTarget(String target) {
		this.targetDir = new File(target);
	}
	
	public void setTargetDir(File targetDir) {
		this.targetDir = targetDir;
	}
	
    public void setFailureDir(String failureDir) {
        this.failureDirFile = new File(failureDir);
    }
    
	public void setFileComparator(Comparator<File> fileComparator) {
		this.fileComparator = fileComparator;
	}

	public void start() {
		if (targetDir != null) {
			try {
				if (null == failureDirFile) {
					failureDirFile = new File(targetDir.getParent(), "failure");
				}
				FileUtils.forceMkdir(failureDirFile);
			} catch (Exception e) {
				logger.error("Cannot create failure directory "
						+ failureDirFile);
				return;
			}
			mainJob = new MainJob(this);
			new Thread(mainJob).start();
		}
	}

    class MainJob implements Runnable {

        private boolean stopRequested = false;
        private Watcher watcher;

        public MainJob (Watcher watcher) {
        	this.watcher = watcher;
        }
        
        public void stop() {
        	this.stopRequested = true;
        }
        
        public void run() {
			while (!stopRequested) {
				processMessage();
				synchronized (this) {
					try {
						wait(watchInterval);
					} catch (InterruptedException e) {
					}
				}
			}
		}
        
        private void processMessage() {
            long currentWatchInterval = watchInterval;
            File [] files = null;

        	if (targetDir.isDirectory()) {
        		files = targetDir.listFiles();
        	}
            
			if (!ArrayUtils.isEmpty(files)) {
				if (fileComparator != null) {
					Arrays.sort(files, fileComparator);
				}
				for (File file : files) {
	                if (!includeDirectory && file.isDirectory())
	                    continue;
	
					if (logger.isInfoEnabled())
						logger.info("Watcher will process the working file: "
								+ file);

					processWorkingFile(file);
	                
					// If the consumer script changes the watchInterval, the
					// processing must be suspended.
					if (watchInterval != currentWatchInterval) {
						break;
					}
	                
					// To relax CPU, wait 0.1 process interval after processing
					// a file.
					if (processInterval > 0) {
						synchronized (this) {
							try {
								wait(processInterval);
							} catch (Exception e) {
							}
						}
	                }
				}
			}
        }
        
        private void processWorkingFile(File workingFile) {
        	synchronized (workingFile) {
        		int sc = consumeFile(workingFile);

				if (sc == Consumer.CONSUME_SUCCEEDED) {
					try {
						FileUtils.forceDelete(workingFile);
					} catch (IOException e) {
						logger.warn("Cannot delete " + workingFile);
					}
				} else if (sc == Consumer.CONSUME_ERROR_FAIL) {
					try {
						FileUtils.forceDelete(workingFile);
					} catch (IOException e) {
						logger.warn("Cannot delete " + workingFile);
					}
				} else if (sc == Consumer.CONSUME_ERROR_MOVE) {
					try {
						if (workingFile.isFile()) {
							FileUtils.moveFile(workingFile, failureDirFile);
						} else {
							FileUtils.moveDirectory(workingFile, failureDirFile);
						}
					} catch (IOException e) {
						logger.warn("Cannot move " + workingFile + " to " + failureDirFile);
					}
				}
			}
        }
        
		private int consumeFile(File file) {
			int sc = Consumer.CONSUME_SUCCEEDED;
			for (Consumer consumer : consumers) {
				try {
					sc = consumer.consume(watcher, file);
				} catch (Exception e) {
					sc = Consumer.CONSUME_ERROR_KEEP;
					logger.warn("Error during consuming: " + e.getMessage());
				}
			}
			return sc;
		}

    }
    	
}
