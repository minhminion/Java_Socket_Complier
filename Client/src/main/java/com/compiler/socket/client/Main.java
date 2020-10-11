package com.compiler.socket.client;

public class Main {
    public static void main(String args[]) {
        Client client = new Client();
        client.startConnection("0.0.0.0", 5000);
    }
}
