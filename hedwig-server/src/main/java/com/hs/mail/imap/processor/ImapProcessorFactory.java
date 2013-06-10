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
package com.hs.mail.imap.processor;

import java.util.Hashtable;
import java.util.Map;

import com.hs.mail.exception.LookupException;
import com.hs.mail.imap.message.request.AppendRequest;
import com.hs.mail.imap.message.request.AuthenticateRequest;
import com.hs.mail.imap.message.request.CapabilityRequest;
import com.hs.mail.imap.message.request.CheckRequest;
import com.hs.mail.imap.message.request.CloseRequest;
import com.hs.mail.imap.message.request.CopyRequest;
import com.hs.mail.imap.message.request.CreateRequest;
import com.hs.mail.imap.message.request.DeleteRequest;
import com.hs.mail.imap.message.request.ExamineRequest;
import com.hs.mail.imap.message.request.ExpungeRequest;
import com.hs.mail.imap.message.request.FetchRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.request.ListRequest;
import com.hs.mail.imap.message.request.LoginRequest;
import com.hs.mail.imap.message.request.LogoutRequest;
import com.hs.mail.imap.message.request.LsubRequest;
import com.hs.mail.imap.message.request.NoopRequest;
import com.hs.mail.imap.message.request.RenameRequest;
import com.hs.mail.imap.message.request.SearchRequest;
import com.hs.mail.imap.message.request.SelectRequest;
import com.hs.mail.imap.message.request.StatusRequest;
import com.hs.mail.imap.message.request.StoreRequest;
import com.hs.mail.imap.message.request.SubscribeRequest;
import com.hs.mail.imap.message.request.UnsubscribeRequest;
import com.hs.mail.imap.message.request.ext.GetQuotaRequest;
import com.hs.mail.imap.message.request.ext.GetQuotaRootRequest;
import com.hs.mail.imap.message.request.ext.NamespaceRequest;
import com.hs.mail.imap.message.request.ext.SetQuotaRequest;
import com.hs.mail.imap.message.request.ext.SortRequest;
import com.hs.mail.imap.processor.ext.GetQuotaProcessor;
import com.hs.mail.imap.processor.ext.GetQuotaRootProcessor;
import com.hs.mail.imap.processor.ext.NamespaceProcessor;
import com.hs.mail.imap.processor.ext.SetQuotaProcessor;
import com.hs.mail.imap.processor.ext.SortProcessor;
import com.hs.mail.imap.processor.fetch.FetchProcessor;

/**
 * <p>
 * Factory class for IMAP command processor
 * </p>
 * 
 * @author Won Chul Doh
 * @since Sep 3, 2010
 *
 */
public class ImapProcessorFactory {

	public static void registerProcessors() {
		registerProcess(AppendRequest.class, new AppendProcessor());
		registerProcess(AuthenticateRequest.class, new AuthenticateProcessor());
		registerProcess(CapabilityRequest.class, new CapabilityProcessor());
		registerProcess(CheckRequest.class, new CheckProcessor());
		registerProcess(CloseRequest.class, new CloseProcessor());
		registerProcess(CopyRequest.class, new CopyProcessor());
		registerProcess(CreateRequest.class, new CreateProcessor());
		registerProcess(DeleteRequest.class, new DeleteProcessor());
		registerProcess(ExamineRequest.class, new ExamineProcessor());
		registerProcess(ExpungeRequest.class, new ExpungeProcessor());
		registerProcess(FetchRequest.class, new FetchProcessor());
		registerProcess(ListRequest.class, new ListProcessor());
		registerProcess(LoginRequest.class, new LoginProcessor());
		registerProcess(LogoutRequest.class, new LogoutProcessor());
		registerProcess(LsubRequest.class, new LsubProcessor());
		registerProcess(NoopRequest.class, new NoopProcessor());
		registerProcess(RenameRequest.class, new RenameProcessor());
		registerProcess(SearchRequest.class, new SearchProcessor());
		registerProcess(SelectRequest.class, new SelectProcessor());
		registerProcess(StatusRequest.class, new StatusProcessor());
		registerProcess(StoreRequest.class, new StoreProcessor());
		registerProcess(SubscribeRequest.class, new SubscribeProcessor());
		registerProcess(UnsubscribeRequest.class, new UnsubscribeProcessor());
		registerProcess(GetQuotaRequest.class, new GetQuotaProcessor());
		registerProcess(GetQuotaRootRequest.class, new GetQuotaRootProcessor());
		registerProcess(NamespaceRequest.class, new NamespaceProcessor());
		registerProcess(SetQuotaRequest.class, new SetQuotaProcessor());
		registerProcess(SortRequest.class, new SortProcessor());
	}

	private static void registerProcess(Class<? extends ImapRequest> clazz,
			ImapProcessor processor) {
		processorMap.put(clazz.getName(), processor);
	}
	
	public static ImapProcessor createImapProcessor(ImapRequest request) {
		ImapProcessor processor = processorMap
				.get(request.getClass().getName());
		if (null == processor)
			throw new LookupException("");
		return processor;
	}
	
	private static Map<String, ImapProcessor> processorMap = new Hashtable<String, ImapProcessor>();
	
}
