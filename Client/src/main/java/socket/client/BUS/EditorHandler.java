package socket.client.BUS;

import socket.client.GUI.Editor;
import socket.commons.enums.Action;
import socket.commons.enums.Language;
import socket.commons.request.CompileRequest;
import socket.commons.request.Request;
import socket.commons.request.SendClientSecretKeyRequest;
import socket.commons.request.ServerPublicKeyRequest;
import socket.commons.response.CompileResponse;
import socket.commons.response.MessageResponse;
import org.apache.commons.lang3.ObjectUtils;
import socket.commons.response.RequestPublicKeyResponse;

import javax.crypto.*;
import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;


public class EditorHandler {
    private Editor editor;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private long startTime;
    private PublicKey serverPublicKey;
    private Key mySecretKey;
    public String ip;

    public EditorHandler(Editor editor) {
        this.editor = editor;
        KeyGenerator gen = null;
        try {
            gen = KeyGenerator.getInstance("AES");
            gen.init(128);
            Key sKey = gen.generateKey();
            this.mySecretKey = sKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest (Request req) {
        try {
            Cipher c = null;
            c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, mySecretKey);

            SealedObject so = new SealedObject(req, c);

            this.out.writeObject(so);
            this.out.flush();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private void sendRequestWithoutKey (Request req) {
        try {
            this.out.writeObject(req);
            this.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String helpReconnectMessage () {
        return "To Reconnect: Help -> Reconnect";
    }

    public void startConnection(String ip, int port) {
        try {
//          Create Client socket
            this.ip = ip;
            editor.getConsole().showLoading();
            clientSocket = new Socket(ip, port);
            if(clientSocket.isConnected()) {
                editor.getConsole().disShowLoading();
                editor.setHandler(this);
            }
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());

            new ResponseProcess().start();

            /** Get public key of server to encrypt key and send back */
            sendRequestWithoutKey(ServerPublicKeyRequest
                    .builder()
                    .action(Action.REQUEST_SERVER_PUBLIC_KEY)
                    .build());
        } catch (UnknownHostException e ) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(editor, "Can't connect to server \n"+helpReconnectMessage(), "Error ",JOptionPane.ERROR_MESSAGE);
            editor.getConsole().addText("Can't connect to server");
        }
    }

    public synchronized void setServerPublicKey(PublicKey serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
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

    public void sendSecretKeyToServer() {
        String encryptedKey = null;
        try {
            encryptedKey = encryptSecretKeyUsingServerPublicKey();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        sendRequestWithoutKey(SendClientSecretKeyRequest
                .builder()
                .action(Action.SEND_SECRET_KEY_TO_SERVER)
                .clientSecretKey(encryptedKey)
                .build());
    }

    public String encryptTextUsingServerPublicKey(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, this.serverPublicKey);

        byte[] byteEncrypted = cipher.doFinal(text.getBytes());
        String encrypted =  Base64.getEncoder().encodeToString(byteEncrypted);

        return encrypted;
    }

    public String encryptSecretKeyUsingServerPublicKey() throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, this.serverPublicKey);

        byte[] byteEncrypted = cipher.doFinal(mySecretKey.getEncoded());
        String encrypted =  Base64.getEncoder().encodeToString(byteEncrypted);

        return encrypted;
    }

    public void compileCode(Language currentLanguage, String code) {
        this.startTime = System.nanoTime();
        sendRequest(CompileRequest
                .builder()
                .action(Action.COMPILE_CODE)
                .code(code)
                .language(currentLanguage)
                .build());
    }
    public void formatCode(Language currentLanguage, String code) {
        this.startTime = System.nanoTime();
        sendRequest(CompileRequest
                .builder()
                .action(Action.FORMAT_CODE)
                .code(code)
                .language(currentLanguage)
                .build());
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

                    if (object instanceof SealedObject) {
                        try {
                            SealedObject sealedObject = (SealedObject) object;
                            object = sealedObject.getObject(mySecretKey);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        }
                    }

                    if (object instanceof MessageResponse) {
                        MessageResponse messageResponse = (MessageResponse) object;
                        System.out.println("Message from server " + messageResponse.getMessage());
                    } else if (object instanceof CompileResponse) {
                        CompileResponse compileResponse = (CompileResponse) object;
                        if(editor != null) {
                            editor.setText(compileResponse.getCode());
                            editor.getConsole().clearScreen();
                            long duration = (System.nanoTime() - startTime);
                            editor.getConsole().disShowLoading();
                            editor.getCodeEditor().setCodeEditorTrue();
                            editor.getConsole().addText("TIME EXECUTE: "+(double) duration / 1_000_000_000+" seconds");

                            editor.getConsole().addText(compileResponse.getOutput());
                        }
                    } else if (object instanceof RequestPublicKeyResponse) {
                        RequestPublicKeyResponse response = (RequestPublicKeyResponse) object;
                        X509EncodedKeySpec spec = new X509EncodedKeySpec(response.getPublicKeyBytes());
                        KeyFactory factory = null;
                        try {
                            factory = KeyFactory.getInstance("RSA");
                            PublicKey pubKey = factory.generatePublic(spec);
                            setServerPublicKey(pubKey);

                            /** Send encrypt key to server and start to communicate using that key */
                            sendSecretKeyToServer();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    disconnect();
                } catch (IOException ex) {
                }
                JOptionPane.showMessageDialog(editor, "Disconnect to server \n"+helpReconnectMessage(), "Error ",JOptionPane.ERROR_MESSAGE);
                editor.getConsole().addText("Disconnect to server !");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                try {
                    disconnect();
                } catch (IOException ex) {
                }
                JOptionPane.showMessageDialog(editor, "Disconnect to server \n"+helpReconnectMessage(), "Error ",JOptionPane.ERROR_MESSAGE);
                editor.getConsole().addText("Disconnect to server !");
            }
        }
    }

    public Socket getClientSocket () {
        return clientSocket;
    }

    public void disconnect () throws IOException {
        if(!clientSocket.isClosed()) {
            in.close();
            out.close();
            clientSocket.close();
        }
    }
}
