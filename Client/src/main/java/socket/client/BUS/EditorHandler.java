package socket.client.BUS;

import socket.client.GUI.Editor;
import socket.commons.enums.Action;
import socket.commons.enums.Language;
import socket.commons.request.CompileRequest;
import socket.commons.request.Request;
import socket.commons.response.CompileResponse;
import socket.commons.response.MessageResponse;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class EditorHandler {
    private Editor editor;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public EditorHandler(Editor editor) {
        this.editor = editor;
    }

    private void sendRequest (Request req) throws IOException {
        this.out.writeObject(req);
        this.out.flush();
    }

    public void startConnection(String ip, int port) {
        try {
//          Create Client socket
            clientSocket = new Socket(ip, port);
            if(clientSocket.isConnected()) {
                editor.setHandler(this);
            }
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            new ResponseProcess().start();
        } catch (UnknownHostException e ) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() throws IOException {
        if (this.in != null) {
            this.in.close();
        }
        if (this.out != null) {
            this.out.close();
        }

        if (this.clientSocket != null) {
            this.clientSocket.close();
        }
    }

    public void compileCode(Language currentLanguage, String code) {
        try {
            sendRequest(CompileRequest
                        .builder()
                        .action(Action.COMPILE_CODE)
                        .code(code)
                        .language(currentLanguage)
                        .build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create a thread to listen response
    private class ResponseProcess extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    Object object = in.readObject();
                    if (ObjectUtils.isEmpty(object)) {
                        continue;
                    }

                    if (object instanceof MessageResponse) {
                        MessageResponse messageResponse = (MessageResponse) object;
                        System.out.println("Message from server " + messageResponse.getMessage());
                    } else if (object instanceof CompileResponse) {
                        CompileResponse compileResponse = (CompileResponse) object;
                        if(editor != null) {
                            editor.setText(compileResponse.getCode());
                            editor.getConsole().addText(compileResponse.getOutput());
                        }
//                        System.out.println( "After Formatter \n" +
//                                "=============== \n"+
//                                compileResponse.getCode()+"\n"+
//                                "===== [OUTPUT] =====\n"+
//                                compileResponse.getOutput()
//                        );
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public Socket getClientSocket () {
        return clientSocket;
    }

}
