/*
 * Licensed Materials - Property of IBM
 *
 * (C) Copyright IBM Corp. 2014. All Rights Reserved.
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Prints the list of available network adapters with IPv4 addresses.
 */
public class NetworkInterfaces {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class methods
	 */

	public static void main(String[] args) {
		printInterfaces();
	}

	public static void printInterfaces() {

		try {

//			String format_IF_V4_V6_MAC_NAME = "%-9s %-15s %-39s %-23s %s";
//			System.out.println(String.format(format_IF_V4_V6_MAC_NAME, "INTERFACE", "IPv4 ADDRESS", "IPv6 ADDRESS", "MAC",
//					"DISPLAY NAME"));
//			System.out.println(String.format(format_IF_V4_V6_MAC_NAME, "---------", "---------------",
//					"---------------------------------------", "-----------------------", "------------"));

			String format_IF_V4_NAME = "%-9s %-15s %s";
			System.out.println(String.format(format_IF_V4_NAME, "INTERFACE", "IPv4 ADDRESS", "DISPLAY NAME"));
			System.out.println(String.format(format_IF_V4_NAME, "---------", "---------------", "------------"));

			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {

				boolean print = false;
				NetworkInterface inter = interfaces.nextElement();
				ArrayList<String> ipv4Addresses = new ArrayList<String>();
				ArrayList<String> ipv6Addresses = new ArrayList<String>();
				Enumeration<InetAddress> addresses = inter.getInetAddresses();

				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					ArrayList<String> addressBuffer = null;

					if (addr instanceof Inet4Address) {
						addressBuffer = ipv4Addresses;
					} else if (addr instanceof Inet6Address) {
						addressBuffer = ipv6Addresses;
					}

					addressBuffer.add(addr.getHostAddress());
				}

				/* If this is a loopback interface... */
				if (ipv4Addresses.contains("127.0.0.1")) {
					/* Ignore */
					continue;
				}

				byte[] macBytes = inter.getHardwareAddress();
				StringBuilder mac = new StringBuilder(18);

				if (macBytes != null) {
					for (byte b : macBytes) {
						if (mac.length() > 0) {
							mac.append(':');
						}
						mac.append(String.format("%02x", b));
					}
				}

				String ipv4 = ipv4Addresses.size() > 0 ? ipv4Addresses.get(0) : "";
				String ipv6 = ipv6Addresses.size() > 0 ? ipv6Addresses.get(0) : "";
//				String row = String.format(format_IF_V4_V6_MAC_NAME, inter.getName(), ipv4, ipv6, mac.toString(), inter
//						.getDisplayName());
				String row = String.format(format_IF_V4_NAME, inter.getName(), ipv4, inter.getDisplayName());
				System.out.println(row);

				for (int a = 1; a < ipv4Addresses.size() || a < ipv6Addresses.size(); a++) {

					ipv4 = ipv4Addresses.size() > a ? ipv4Addresses.get(a) : "";
					ipv6 = ipv6Addresses.size() > a ? ipv6Addresses.get(a) : "";
//					String subrow = String.format(format_IF_V4_V6_MAC_NAME, "", ipv4, ipv6, "", "");
					String subrow = String.format(format_IF_V4_NAME, "", ipv4, "", "");
					System.out.println(subrow);
				}
			}

		} catch (SocketException e) {

			e.printStackTrace();

		}
	}
}