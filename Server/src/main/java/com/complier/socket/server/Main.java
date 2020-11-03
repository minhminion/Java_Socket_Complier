package com.complier.socket.server;

public class Main {
    public static void main (String args[]) {
        Server server = new Server();
        server.start(5000);
    }
}
