import socket.server.ServerHandler.Server;

public class ServerLauncher {
    public static void main (String args[]) {
        Server server = new Server();
        server.start(5000);

    }
}
