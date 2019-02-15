/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.net;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import edu.tamu.lenss.madoop4.MadoopConstants;

import edu.tamu.lenss.util.ILogger;
import edu.tamu.lenss.util.IPToolBox;
import edu.tamu.lenss.util.LogFactory;
import edu.tamu.lenss.util.IPToolBox;



//import javax.naming.NamingException;
//import javax.naming.directory.Attributes;
//import javax.naming.directory.DirContext;
//import javax.naming.directory.InitialDirContext;

/**
 * 
 * A class that provides direct and reverse lookup functionalities, allowing
 * the querying of specific network interfaces or nameservers.
 * 
 * 
 */
public class DNS implements MadoopConstants{
  private static final ILogger LOG = LogFactory.getLog(DNS.class);

//  /**
//   * Returns the hostname associated with the specified IP address by the
//   * provided nameserver.
//   *
//   * @param hostIp
//   *            The address to reverse lookup
//   * @param ns
//   *            The host name of a reachable DNS server
//   * @return The host name associated with the provided IP
//   * @throws NamingException
//   *             If a NamingException is encountered
//   */
  public static String reverseDns(InetAddress hostIp, String ns)
//    throws NamingException 
    {
//    //
//    // Builds the reverse IP lookup form
//    // This is formed by reversing the IP numbers and appending in-addr.arpa
//    //
//    String[] parts = hostIp.getHostAddress().split("\\.");
//    String reverseIP = parts[3] + "." + parts[2] + "." + parts[1] + "."
//      + parts[0] + ".in-addr.arpa";
//
//    DirContext ictx = new InitialDirContext();
//    Attributes attribute =
//      ictx.getAttributes("dns://"               // Use "dns:///" if the default
//                         + ((ns == null) ? "" : ns) +
//                         // nameserver is to be used
//                         "/" + reverseIP, new String[] { "PTR" });
//    ictx.close();
//
//    return attribute.get("PTR").get().toString();

//      LOG.info("****<reverseDns>" + hostIp.toString());
      LOG.info("****<reverseDns> " + hostIp.toString());
      //return IPToolBox.RDNS.get(hostIp.toString());
      //Suman's edit
      return hostIp.getHostName();

  }

  /**
   * Returns all the IPs associated with the provided interface, if any, in
   * textual form.
   *

   * @return A string vector of all the IPs associated with the provided
   *         interface
   * @throws UnknownHostException
   *             If an UnknownHostException is encountered in querying the
   *             default interface
   *
   */

  public static String[] getNIFs() throws SocketException{

    Vector<String> infs = new Vector<String>();
    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
    for (NetworkInterface netint : Collections.list(nets)) {
      infs.add(netint.getName());
    }

    return infs.toArray(new String[] {});
  }

  public static String[] getIPs() throws SocketException {
    Vector<String> ips = new Vector<String>();
    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
    for (NetworkInterface netint : Collections.list(nets)) {
      LOG.info("****<getIPs>" + netint.getName());
      Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
      for (InetAddress inetAddress : Collections.list(inetAddresses)) {
          ips.add(inetAddress.toString());
          LOG.info("****<getIPs>" + inetAddress.toString());
      }
    }

    return ips.toArray(new String[] {});

  }

  public static String[] getIPs(String strInterface)
    throws UnknownHostException {
    try {
      NetworkInterface netIF = NetworkInterface.getByName(strInterface);
      LOG.info("****<getIPs> netIF: " + netIF);
      
      if (netIF == null)
        return new String[] { InetAddress.getLocalHost()
                              .getHostAddress() };
      else {
        Vector<String> ips = new Vector<String>();
        Enumeration<InetAddress> e = netIF.getInetAddresses();
        while (e.hasMoreElements()){
          InetAddress addr = e.nextElement();
          if (addr instanceof Inet6Address) continue;
          ips.add(addr.getHostAddress());
        }
        return ips.toArray(new String[] {});
      }
    } catch (SocketException e) {
      return new String[] { InetAddress.getLocalHost().getHostAddress() };
    }
  }

  /**
   * Returns the first available IP address associated with the provided
   * network interface
   * 
   * @param strInterface
   *            The name of the network interface to query (e.g. eth0)
   * @return The IP address in text form
   * @throws UnknownHostException
   *             If one is encountered in querying the default interface
   */
  public static String getDefaultIP(String strInterface)
    throws UnknownHostException {
    String[] ips = getIPs(strInterface);
    return ips[0];
  }

  /**
   * Returns all the host names associated by the provided nameserver with the
   * address bound to the specified network interface
   * 
   * @param strInterface
   *            The name of the network interface to query (e.g. eth0)
   * @param nameserver
   *            The DNS host name
   * @return A string vector of all host names associated with the IPs tied to
   *         the specified interface
   * @throws UnknownHostException
   */
  public static String[] getHosts(String strInterface, String nameserver)
    throws UnknownHostException {
    String[] ips = getIPs(strInterface);
    LOG.info("****<getHosts> ips.length: " + ips.length);
    Vector<String> hosts = new Vector<String>();
    for (int ctr = 0; ctr < ips.length; ctr++)
      try {
        hosts.add(reverseDns(InetAddress.getByName(ips[ctr]),
                             nameserver));
      } catch (Exception e) {
      }

//      LOG.info("****<getHosts> hosts.size(): " + hosts.size());
      LOG.info("****<getHosts> hosts: " + hosts);
    if (hosts.size() == 0)
      return new String[] { InetAddress.getLocalHost().getCanonicalHostName() };
    else
      return hosts.toArray(new String[] {});
  }

  /**
   * Returns all the host names associated by the default nameserver with the
   * address bound to the specified network interface
   * 
   * @param strInterface
   *            The name of the network interface to query (e.g. eth0)
   * @return The list of host names associated with IPs bound to the network
   *         interface
   * @throws UnknownHostException
   *             If one is encountered while querying the deault interface
   * 
   */
  public static String[] getHosts(String strInterface)
    throws UnknownHostException {
    return getHosts(strInterface, null);
  }

  /**
   * Returns the default (first) host name associated by the provided
   * nameserver with the address bound to the specified network interface
   * 
   * @param strInterface
   *            The name of the network interface to query (e.g. eth0)
   * @param nameserver
   *            The DNS host name
   * @return The default host names associated with IPs bound to the network
   *         interface
   * @throws UnknownHostException
   *             If one is encountered while querying the deault interface
   */
  public static String getDefaultHost(String strInterface, String nameserver)
    throws UnknownHostException {
    if (strInterface.equals("default")) 
      return InetAddress.getLocalHost().getCanonicalHostName();

    if (nameserver != null && nameserver.equals("default"))
      return getDefaultHost(strInterface);

    String[] hosts = getHosts(strInterface, nameserver);
    return hosts[0];
  }

  /**
   * Returns the default (first) host name associated by the default
   * nameserver with the address bound to the specified network interface
   * 
   * @param strInterface
   *            The name of the network interface to query (e.g. eth0)
   * @return The default host name associated with IPs bound to the network
   *         interface
   * @throws UnknownHostException
   *             If one is encountered while querying the deault interface
   */
  public static String getDefaultHost(String strInterface)
    throws UnknownHostException {
    return getDefaultHost(strInterface, null);
  }

}
