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
package com.hs.mail.util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class InetAddressMatcher {
	
	private List<Integer> addresses = new ArrayList<Integer>();
	private List<Integer> netmasks = new ArrayList<Integer>();
	
	public InetAddressMatcher(String networks) {
		StringTokenizer st = new StringTokenizer(networks, ", ");
		while (st.hasMoreElements()) {
			String s = st.nextToken().trim();
			int i = s.indexOf('/');
			if (i != -1) {
				addresses.add(toByte(s.substring(0, i)));
				String mask = s.substring(i + 1);
				if (mask.indexOf('.') == -1) {
					int nm = Integer.parseInt(mask);
					netmasks.add((nm == 0) ? 0 : 0xffffffff << (32 - nm));
				} else {
					netmasks.add(toByte(mask));
				}
			} else {
				addresses.add(toByte(s));
				netmasks.add(0xffffffff);
			}
		}
	}
	
	public static int toByte(String address) {
		StringTokenizer st = new StringTokenizer(address, ".");
		int bytes = Integer.parseInt(st.nextToken()) << 24
				| Integer.parseInt(st.nextToken()) << 16
				| Integer.parseInt(st.nextToken()) << 8
				| Integer.parseInt(st.nextToken());
		return bytes;
	}
	
	public boolean matches(InetAddress address) {
		int ipaddr = toByte(address.getHostAddress());
		for (int i = 0; i < addresses.size(); i++) {
			int nm = netmasks.get(i);
			if ((ipaddr & nm) == (addresses.get(i) & nm)) {
				return true;
			}
		}
		return false;
	}

	public static boolean matches(String networks, InetAddress address) {
		return new InetAddressMatcher(networks).matches(address);
	}
	
	private String toString(int addr) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			int x = addr >>> 24;
			addr = addr << 8;
			if (i != 0)
				sb.append('.');
			sb.append(x);
		}
		return sb.toString();
	}
	
	private String toString(int address, int netmask) {
		if (netmask == -1)
			return toString(address);
		else
			return toString(address) + '/' + toString(netmask);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < addresses.size(); i++) {
			if (i != 0)
				sb.append(' ');
			sb.append(toString(addresses.get(i), netmasks.get(i)));
		}
		return sb.toString();
	}

}
