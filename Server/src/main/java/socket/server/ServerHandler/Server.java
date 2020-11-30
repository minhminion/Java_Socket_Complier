package socket.server.ServerHandler;

import socket.commons.request.SendClientSecretKeyRequest;
import socket.commons.response.RequestPublicKeyResponse;
import socket.server.ApiHandler.APIRequest;
import socket.commons.enums.Language;
import socket.commons.enums.StatusCode;
import socket.commons.request.CompileRequest;
import socket.commons.request.MessageRequest;
import socket.commons.request.Request;
import socket.commons.response.CompileResponse;
import socket.commons.response.MessageResponse;
import socket.commons.response.Response;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static socket.commons.enums.Action.*;

public class Server {
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clientHandlers;
    private MyKeyGenerator keyGen;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public Server() {
        this.clientHandlers = new HashMap<>();
        generateKeys();
    }

    public void generateKeys() {
        try {
            keyGen = new MyKeyGenerator(2048);
            keyGen.createKeys();

            this.publicKey = keyGen.getPublicKey();
            this.privateKey = keyGen.getPrivateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public void start(int port) {
        System.out.println("Server Starting !!!");
        try {
            serverSocket = new ServerSocket(port);
            System.out.println(serverSocket.getLocalSocketAddress().toString());
            System.out.println(serverSocket.getInetAddress().getHostAddress().toString());

            while (true) {
                ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
                clientHandler.start();
                String clientUid = clientHandler.getUid();
                String address = clientHandler.clientSocket.getLocalSocketAddress().toString();
                System.out.println("Connected to Client ["+clientUid+"] on " + address.substring(1));
                this.clientHandlers.put(clientUid, clientHandler);
            }
        } catch (IOException e) {

        }
    }


    @Getter
    @Setter
    private class ClientHandler extends Thread{
        private Socket clientSocket;
        private String uid;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private Key clientSecretKey;

        public ClientHandler(Socket clientSocket) throws IOException{
            this.clientSocket = clientSocket;
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            this.uid = UUID.randomUUID().toString();
        }

        private void response(Response response) throws IOException {
            this.out.writeObject(response);
            this.out.flush();
        }

        private String getLanguage (Language languageType) {
            String language = null;
            switch (languageType) {
                case JAVA:
                    language = "java";
                    break;
                case CPP:
                    language = "cpp";
                    break;
                case PYTHON:
                    language = "python";
                    break;
                case CSHARP:
                    language = "csharp";
                    break;
                default:
                    break;
            }
            return language;
        }

        @Override
        public void run () {
            try {
            while (true) {
                Object input = in.readObject();

                if(ObjectUtils.isNotEmpty(input)) {
                    Request request = null;
                    if (clientSecretKey != null) {
                        SealedObject sealedObject = (SealedObject) input;
                        request = (Request) sealedObject.getObject(clientSecretKey);
                    } else {
                        request = (Request) input;
                    }
                    System.out.println("[Client "+uid+"] Request to "+request.getAction().name());
                    switch (request.getAction()) {
                        case DISCONNECT:
                            clientHandlers.remove(this.getUid());
                            break;
                        case SEND_MESSAGE:
                            String message = ((MessageRequest) request).getMessage();
                            System.out.println("Send by Client ["+uid+"] ==> "+ message);
                            this.response(MessageResponse.builder()
                                    .message("Hello client")
                                    .statusCode(StatusCode.OK)
                                    .build());
                            break;
                        case COMPILE_CODE:
                            CompileRequest compileRequest = (CompileRequest) request;
                            String code = compileRequest.getCode();
                            String response;
                            String language = getLanguage(compileRequest.getLanguage());

                            if(code != null || language != null ) {
                                APIRequest apiRequest = new APIRequest();
                                String formattedCode = apiRequest.formatCode(code, language);
                                String outputCompile = apiRequest.compileCode(formattedCode, language);
                                this.response(CompileResponse.builder()
                                        .code(formattedCode)
                                        .output(outputCompile)
                                        .statusCode(StatusCode.OK)
                                        .build());
                            }
                            break;
                        case FORMAT_CODE:
                            CompileRequest formatRequest = (CompileRequest) request;
                            String dataCode = formatRequest.getCode();
                            String responseCode;
                            String languageCode = getLanguage(formatRequest.getLanguage());

                            if(dataCode != null || languageCode != null ) {
                                APIRequest apiRequest = new APIRequest();
                                String formattedCode = apiRequest.formatCode(dataCode, languageCode);
                                this.response(CompileResponse.builder()
                                        .code(formattedCode)
                                        .output("Format Code Success")
                                        .statusCode(StatusCode.OK)
                                        .build());
                            }
                            break;

                        case REQUEST_SERVER_PUBLIC_KEY: {
                            this.out.writeObject(RequestPublicKeyResponse.builder()
                                    .publicKeyBytes(publicKey.getEncoded())
                                    .statusCode(StatusCode.OK)
                                    .build());
                            this.out.flush();
                            break;
                        }

                        case SEND_SECRET_KEY_TO_SERVER: {
                            SendClientSecretKeyRequest sendClientSecretKeyRequest =
                                    (SendClientSecretKeyRequest) request;
                            String encryptedKey = sendClientSecretKeyRequest.getClientSecretKey();

                            try {
                                this.clientSecretKey = decryptSecretKeyFromClient(encryptedKey);
                            } catch (InvalidKeyException e) {
                                e.printStackTrace();
                            } catch (NoSuchPaddingException e) {
                                e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (BadPaddingException e) {
                                e.printStackTrace();
                            } catch (IllegalBlockSizeException e) {
                                e.printStackTrace();
                            }
                            break;
                        }

                        default:
                            break;
                    }
                }
            }
            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                    System.out.println("Disconnected to Client ["+uid+"]");
                } catch (Exception e) {

                }
            }
        }

        public String decryptUsingServerPublicKey(String encryptedText) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] byteDecrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            String decryptedText = new String(byteDecrypted);

            return decryptedText;
        }

        public Key decryptSecretKeyFromClient(String encryptedKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, IOException, ClassNotFoundException {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] byteDecrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedKey));
            SecretKey originalKey = new SecretKeySpec(byteDecrypted, 0, byteDecrypted.length, "AES");

            return originalKey;
        }
    }
}

