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
package com.hs.mail.imap.message;

import java.util.Hashtable;
import java.util.Map;

import com.hs.mail.exception.LookupException;
import com.hs.mail.imap.message.builder.ImapRequestBuilder;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.parser.ParseException;
import com.hs.mail.imap.server.codec.ImapMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Jan 28, 2010
 *
 */
public class ImapRequestFactory {
	
	public static ImapRequest createImapRequest(ImapMessage message)
			throws LookupException, ParseException {
		String tag = message.getTokens().remove().value;
		String command = message.getTokens().remove().value;
		ImapRequestBuilder builder = lookup(tag, command);
		return builder.createRequest(tag, command, message);
	}
	
	public static ImapRequestBuilder lookup(String tag, String command) {
		ImapRequestBuilder builder = commandMap.get(command.toLowerCase());
		if (null == builder)
			throw new LookupException((tag == null ? "*" : tag)
					+ " BAD Unknown command: \"" + command + "\"");
		return builder;
	}

	static private Map<String, ImapRequestBuilder> commandMap = new Hashtable<String, ImapRequestBuilder>();
	static {
		commandMap.put("append",
				new com.hs.mail.imap.message.builder.AppendRequestBuilder());
		commandMap.put("authenticate",
				new com.hs.mail.imap.message.builder.AuthenticateRequestBuilder());
		commandMap.put("capability",
				new com.hs.mail.imap.message.builder.CapabilityRequestBuilder());
		commandMap.put("check",
				new com.hs.mail.imap.message.builder.CheckRequestBuilder());
		commandMap.put("close",
				new com.hs.mail.imap.message.builder.CloseRequestBuilder());
		commandMap.put("copy",
				new com.hs.mail.imap.message.builder.CopyRequestBuilder());
		commandMap.put("create",
				new com.hs.mail.imap.message.builder.CreateRequestBuilder());
		commandMap.put("delete",
				new com.hs.mail.imap.message.builder.DeleteRequestBuilder());
		commandMap.put("examine",
				new com.hs.mail.imap.message.builder.ExamineRequestBuilder());
		commandMap.put("expunge",
				new com.hs.mail.imap.message.builder.ExpungeRequestBuilder());
		commandMap.put("fetch",
				new com.hs.mail.imap.message.builder.FetchRequestBuilder());
		commandMap.put("list",
				new com.hs.mail.imap.message.builder.ListRequestBuilder());
		commandMap.put("login",
				new com.hs.mail.imap.message.builder.LoginRequestBuilder());
		commandMap.put("logout",
				new com.hs.mail.imap.message.builder.LogoutRequestBuilder());
		commandMap.put("lsub",
				new com.hs.mail.imap.message.builder.LsubRequestBuilder());
		commandMap.put("noop",
				new com.hs.mail.imap.message.builder.NoopRequestBuilder());
		commandMap.put("rename",
				new com.hs.mail.imap.message.builder.RenameRequestBuilder());
		commandMap.put("search",
				new com.hs.mail.imap.message.builder.SearchRequestBuilder());
		commandMap.put("select",
				new com.hs.mail.imap.message.builder.SelectRequestBuilder());
		commandMap.put("status",
				new com.hs.mail.imap.message.builder.StatusRequestBuilder());
		commandMap.put("store",
				new com.hs.mail.imap.message.builder.StoreRequestBuilder());
		commandMap.put("subscribe",
				new com.hs.mail.imap.message.builder.SubscribeRequestBuilder());
		commandMap.put("unsubscribe",
				new com.hs.mail.imap.message.builder.UnsubscribeRequestBuilder());
		commandMap.put("uid", 
				new com.hs.mail.imap.message.builder.UidRequestBuilder());
		commandMap.put("getquota",
				new com.hs.mail.imap.message.builder.ext.GetQuotaRequestBuilder());
		commandMap.put("getquotaroot",
				new com.hs.mail.imap.message.builder.ext.GetQuotaRootRequestBuilder());
		commandMap.put("namespace",
				new com.hs.mail.imap.message.builder.ext.NamespaceRequestBuilder());
		commandMap.put("setquota",
				new com.hs.mail.imap.message.builder.ext.SetQuotaRequestBuilder());
		commandMap.put("sort",
				new com.hs.mail.imap.message.builder.ext.SortRequestBuilder());
		commandMap.put("xrevoke", 
				new com.hs.mail.imap.message.builder.custom.XRevokeRequestBuilder());
	}

}
