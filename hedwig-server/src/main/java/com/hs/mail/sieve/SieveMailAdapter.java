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
package com.hs.mail.sieve;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.field.address.MailboxList;
import org.apache.james.mime4j.field.address.parser.ParseException;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.parser.Field;
import org.apache.jsieve.exception.InternetAddressException;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.Action;
import org.apache.jsieve.mail.AddressImpl;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.mail.SieveMailException;

import com.hs.mail.mailet.MailetContext;
import com.hs.mail.smtp.message.SmtpMessage;

public class SieveMailAdapter implements MailAdapter {

	/**
	 * The Message being adapted.
	 */
	private SmtpMessage fieldMessage;
	/**
	 * List of Actions to perform.
	 */
	private List fieldActions;
    /**
     * The MailetContext.
     */
    private MailetContext fieldMailetContext;
	/**
	 * The sole intended recipient for the message being adapted;
	 */
	private String soleRecipient;
	/**
	 * The ID of sole intended recipient for the message being adapted;
	 */
	private long soleRecipientID;
	/**
     * Constructor for SieveMailAdapter.
     */
	public SieveMailAdapter(MailetContext aMailetContext, String soleRecipient,
			long soleRecipientID) {
		super();
		setMailetContext(aMailetContext);
		setSoleRecipient(soleRecipient);
		setSoleRecipientID(soleRecipientID);
	}

    /**
     * @return Returns the fieldMessage.
     */
    public SmtpMessage getMessage() {
		return fieldMessage;
	}

    /**
     * @param fieldMessage The fieldMessage to set.
     */
	public void setMessage(SmtpMessage aMessage) {
		this.fieldMessage = aMessage;
        clearActions();
	}

	/**
	 * @return Returns the soleRecipient.
	 */
	public String getSoleRecipient() {
		return soleRecipient;
	}

	/**
	 * @param soleRecipient
	 *            The soleRecipient to set.
	 */
	public void setSoleRecipient(String soleRecipient) {
		this.soleRecipient = soleRecipient;
	}

	/**
	 * @return Returns the ID of soleRecipient.
	 */
	public long getSoleRecipientID() {
		return soleRecipientID;
	}

	/**
	 * @param soleRecipientID
	 *            The ID of soleRecipient to set.
	 */
	public void setSoleRecipientID(long soleRecipientID) {
		this.soleRecipientID = soleRecipientID;
	}

	/**
	 * Returns the List of actions.
	 * 
	 * @return List
	 */
	public List getActions() {
		List actions = null;
		if (null == (actions = getActionsBasic())) {
			updateActions();
			return getActions();
		}
		return actions;
	}

	/**
	 * Remove all actions.
	 * 
	 */
	private void clearActions() {
		getActions().clear();
	}

	/**
	 * Returns a new List of actions.
	 * 
	 * @return List
	 */
	protected List computeActions() {
		return new ArrayList();
	}

	/**
	 * Returns the List of actions.
	 * 
	 * @return List
	 */
	private List getActionsBasic() {
		return fieldActions;
	}

    /**
     * Adds an Action.
     * 
     * @param action The action to set
     */
	public void addAction(Action action) {
        getActions().add(action);
	}

    /**
     * @see org.apache.jsieve.mail.MailAdapter#executeActions()
     */
	public void executeActions() throws SieveException {
		ListIterator actionsIter = getActionsIterator();
		while (actionsIter.hasNext()) {
			Action action = (Action) actionsIter.next();
			try {
				ActionDispatcher.getInstance().execute(action, this,
						getMailetContext());
			} catch (IllegalArgumentException e) {
                throw new SieveException(e.getMessage());
			} catch (NoSuchMethodException e) {
                throw new SieveException(e.getMessage());
			} catch (IllegalAccessException e) {
                throw new SieveException(e.getMessage());
			} catch (InvocationTargetException e) {
                throw new SieveException(e.getMessage());
			}
		}
	}

	/**
	 * Sets the actions.
	 * 
	 * @param actions
	 *            The actions to set
	 */
	protected void setActions(List actions) {
		fieldActions = actions;
	}

	/**
	 * Updates the actions.
	 */
	protected void updateActions() {
		setActions(computeActions());
	}
	
    /**
     * @see org.apache.jsieve.mail.MailAdapter#getActionsIterator()
     */
	public ListIterator getActionsIterator() {
        return getActions().listIterator();
	}

	public Object getContent() throws SieveMailException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getContentType() throws SieveMailException {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * @see org.apache.jsieve.mail.MailAdapter#getHeader(String)
     */
	public List getHeader(String name) throws SieveMailException {
		List<Field> fields = getHeader().getFields(name);
		List headers = new ArrayList(fields.size());
		for (Field field : fields) {
			headers.add(field.getBody());
		}
		return headers;
	}

    /**
     * @see org.apache.jsieve.mail.MailAdapter#getHeaderNames()
     */
	public List getHeaderNames() throws SieveMailException {
		List<Field> fields = getHeader().getFields();
		Set headerNames = new HashSet();
		for (Field field : fields) {
			headerNames.add(field.getName());
		}
		return new ArrayList(headerNames);
	}

    /**
     * @see org.apache.jsieve.mail.MailAdapter#getMatchingHeader(String)
     */
	public List getMatchingHeader(String name) throws SieveMailException {
		Iterator headerNamesIter = getHeaderNames().iterator();
		List matchedHeaderValues = new ArrayList(32);
		while (headerNamesIter.hasNext()) {
			String headerName = (String) headerNamesIter.next();
			if (headerName.trim().equalsIgnoreCase(name)) {
				matchedHeaderValues.addAll(getHeader(headerName));
			}
		}
		return matchedHeaderValues;
	}

    /**
     * @see org.apache.jsieve.mail.MailAdapter#getSize()
     */
	public int getSize() throws SieveMailException {
		try {
			return (int) fieldMessage.getMailMessage().getSize();
		} catch (IOException e) {
			throw new SieveMailException(e);
		}
	}

	public Address[] parseAddresses(String headerName)
			throws SieveMailException, InternetAddressException {
		try {
			Field field = getHeader().getField(headerName);
            MailboxList list = AddressList.parse(field.getBody()).flatten();
            final int size = list.size();
            final Address[] results = new Address[size];
            for (int i=0;i<size;i++) {
                final Mailbox mailbox = list.get(i);
                results[i] = new AddressImpl(mailbox.getLocalPart(), mailbox.getDomain());
            }
            return null;
        } catch (ParseException e) {
            throw new InternetAddressException(e);
        }
	}

	private Header getHeader() {
		try {
			return fieldMessage.getMailMessage().getHeader().getHeader();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the mailetContext.
	 * 
	 * @return MailetContext
	 */
	public MailetContext getMailetContext() {
		return fieldMailetContext;
	}

	/**
	 * Sets the mailetContext.
	 * 
	 * @param mailetContext
	 *            The mailetContext to set
	 */
	protected void setMailetContext(MailetContext mailetContext) {
		fieldMailetContext = mailetContext;
	}

}
