package com.hs.mail.imap.test;

import java.util.LinkedList;

import org.jboss.netty.buffer.ChannelBuffer;

import com.hs.mail.imap.parser.Token;
import com.hs.mail.imap.server.codec.ImapMessage;

public class MockImapMessage implements ImapMessage {

	private LinkedList<Token> tokens;
	
	public MockImapMessage(LinkedList<Token> tokens) {
		this.tokens = tokens;
	}
	
	public ChannelBuffer getLiteral() {
		return null;
	}

	public long getLiteralLength() {
		return 0;
	}

	public LinkedList<Token> getTokens() {
		return tokens;
	}

	public boolean isNeedContinuationRequest() {
		return false;
	}

	public void setLiteral(ChannelBuffer literal) {
	}

	public void setLiteralLength(long literalLength) {
	}

	public String getCommand() {
		return null;
	}

}
