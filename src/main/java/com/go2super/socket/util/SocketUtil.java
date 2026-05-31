package com.go2super.socket.util;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketUtil {

    public static String getRemoteIP(Socket socket) {

        InetSocketAddress identifier = (InetSocketAddress) socket.getRemoteSocketAddress();
        Inet4Address inetAddress = (Inet4Address) identifier.getAddress();

        return inetAddress.toString();

    }

}
