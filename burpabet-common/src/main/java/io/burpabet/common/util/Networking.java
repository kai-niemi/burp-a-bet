package io.burpabet.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

public abstract class Networking {
    private Networking() {
    }

    public static String getLocalIP() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("checkip.amazonaws.com", 80));
            return socket.getLocalAddress().getHostAddress();
        }
    }

    public static String getPublicIP() throws IOException {
        URL url = new URL("http://checkip.amazonaws.com/");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.readLine();
        }
    }
}
