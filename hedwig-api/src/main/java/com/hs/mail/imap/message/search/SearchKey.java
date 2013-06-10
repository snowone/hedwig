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
package com.hs.mail.imap.message.search;

/**
 * Search criteria are expressed as a tree of search-keys, forming a parse-tree
 * for the search expression.
 * <p>
 * 
 * Search-keys are represented by this class. This is an abstract class;
 * subclasses implement specific data structures. <p>
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 * 
 */
public abstract class SearchKey {

	public abstract boolean isComposite();

}
