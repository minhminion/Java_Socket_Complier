package socket.server.ServerHandler;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import socket.commons.enums.Language;
import socket.commons.enums.StatusCode;
import socket.commons.request.CompileRequest;
import socket.commons.request.MessageRequest;
import socket.commons.request.Request;
import socket.commons.request.SendClientSecretKeyRequest;
import socket.commons.response.CompileResponse;
import socket.commons.response.MessageResponse;
import socket.commons.response.RequestPublicKeyResponse;
import socket.commons.response.Response;
import socket.server.ApiHandler.APIRequest;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Getter
@Setter
public class Worker implements Runnable {
    public Socket clientSocket;
    private String uid;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Key clientSecretKey;

    public Worker(Socket clientSocket) throws IOException{
        this.clientSocket = clientSocket;
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
        this.uid = UUID.randomUUID().toString();
    }

    private void response(Response response) throws IOException {
        if (clientSecretKey != null) {
            try {
                Cipher c = null;
                c = Cipher.getInstance("AES");
                c.init(Cipher.ENCRYPT_MODE, clientSecretKey);

                SealedObject so = new SealedObject(response, c);

                this.out.writeObject(so);
                this.out.flush();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        } else {
            this.out.writeObject(response);
            this.out.flush();
        }
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
                            Server.clientHandlers.remove(this.getUid());
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
                                    .publicKeyBytes(Server.publicKey.getEncoded())
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
        cipher.init(Cipher.DECRYPT_MODE, Server.privateKey);

        byte[] byteDecrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        String decryptedText = new String(byteDecrypted);

        return decryptedText;
    }

    public Key decryptSecretKeyFromClient(String encryptedKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, IOException, ClassNotFoundException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, Server.privateKey);

        byte[] byteDecrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedKey));
        SecretKey originalKey = new SecretKeySpec(byteDecrypted, 0, byteDecrypted.length, "AES");

        return originalKey;
    }
}
