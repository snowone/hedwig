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
package com.hs.mail.imap.dao;

import java.util.List;

import com.hs.mail.imap.mailbox.UidToMsnMapper;
import com.hs.mail.imap.message.search.SearchKey;
import com.hs.mail.imap.message.search.SortKey;

/**
 * 
 * @author Won Chul Doh
 * @since Apr 5, 2010
 *
 */
public interface SearchDao {

	public List<Long> query(UidToMsnMapper map, long mailboxid, SearchKey key);

	public List<Long> sort(long mailboxID, SortKey key);

}
