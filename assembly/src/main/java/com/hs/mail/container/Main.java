/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hs.mail.container;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Main {

    private File appHome;
	private ClassLoader classLoader;
	private List classpaths = new ArrayList(5);

	public static void main(String[] args) {
		Main app = new Main();

		if (args.length < 1) {
			System.out.println("Main class was not specified");
			System.exit(0);
		}
		
		// Convert arguments to collection for easier management
		List tokens = new LinkedList(Arrays.asList(args));
		String taskClass = (String) tokens.remove(0);
	
		// Add the following to the classpath:
		// ${app.home}/lib
		//
		app.addClassPath(new File(app.getAppHome(), "lib"));
		
		try {
			app.runTaskClass(taskClass, tokens);
		} catch (ClassNotFoundException e) {
			System.out.println("Could not load class: " + e.getMessage());
			try {
				ClassLoader cl = app.getClassLoader();
				if (cl != null) {
					System.out.println("Class loader setup: ");
					printClassLoaderTree(cl);
				}
			} catch (MalformedURLException me) {
			}
		} catch (Throwable e) {
			System.out.println("Failed to execute main task. Reason: " + e);
		}
	}

	/**
	 * Print out what's in the classloader tree being used. 
	 */
	private static int printClassLoaderTree(ClassLoader cl) {
		int depth = 0;
		if (cl.getParent() != null) {
			depth = printClassLoaderTree(cl.getParent()) + 1;
		}
		
		StringBuffer indent = new StringBuffer();
		for (int i = 0; i < depth; i++) {
			indent.append("  ");
		}
		
		if (cl instanceof URLClassLoader) {
			URLClassLoader ucl = (URLClassLoader) cl;
			System.out.println(indent + cl.getClass().getName() + " {");
			URL[] urls = ucl.getURLs();
			for (int i = 0; i < urls.length; i++) {
				System.out.println(indent + "  " + urls[i]);
			}
			System.out.println(indent + "}");
		} else {
			System.out.println(indent + cl.getClass().getName());
		}
		return depth;
	}

	public void runTaskClass(String taskClass, List tokens) throws Throwable {
		ClassLoader cl = getClassLoader();

		// Use reflection to run the task.
		try {
			String[] args = (String[]) tokens.toArray(new String[tokens.size()]);
			Class task = cl.loadClass(taskClass);
			Method runTask = task.getMethod("main", new Class[] { String[].class });
			runTask.invoke(task.newInstance(), new Object[] { args });
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
    private void addClassPath(File file) {
        classpaths.add(file);
	}

	public ClassLoader getClassLoader() throws MalformedURLException {
		if (classLoader == null) {
			// Setup the ClassLoader
			classLoader = Main.class.getClassLoader();
			if (!classpaths.isEmpty()) {
				ArrayList urls = new ArrayList();
				for (Iterator iter = classpaths.iterator(); iter.hasNext();) {
					File dir = (File) iter.next();
					if (dir.isDirectory()) {
						File[] files = dir.listFiles();
						if (files != null) {
							// Sort the jars so that classpath built is
							// consistently in the same order. Also allows us to
							// use jar names to control classpath order.
							Arrays.sort(files, new Comparator() {
								public int compare(Object o1, Object o2) {
									File f1 = (File) o1;
									File f2 = (File) o2;
									return f1.getName().compareTo(f2.getName());
								}
							});
							for (int j = 0; j < files.length; j++) {
								if (files[j].getName().endsWith(".zip")
										|| files[j].getName().endsWith(".jar")) {
									urls.add(files[j].toURL());
								}
							}
						}
					}
				}
				URL u[] = new URL[urls.size()];
				urls.toArray(u);
				classLoader = new URLClassLoader(u, classLoader);
			}
			Thread.currentThread().setContextClassLoader(classLoader);
		}
		return classLoader;
	}
	
	public void setAppHome(File appHome) {
		this.appHome = appHome;
	}

	public File getAppHome() {
		if (appHome == null) {
			if (System.getProperty("app.home") != null) {
				appHome = new File(System.getProperty("app.home"));
			}
			if (appHome == null) {
				// guess from the location of the jar
				URL url = Main.class.getClassLoader().getResource("com/hs/mail/container/Main.class");
				if (url != null) {
					try {
						JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
						url = jarConnection.getJarFileURL();
						URI baseURI = new URI(url.toString()).resolve("..");
						appHome = new File(baseURI).getCanonicalFile();
						System.setProperty("app.home", appHome.getAbsolutePath());
					} catch (Exception ignored) {
					}
				}
			}
			if (appHome == null) {
				appHome = new File("../.");
				System.setProperty("app.home", appHome.getAbsolutePath());
			}
		}
		return appHome;
	}

}
