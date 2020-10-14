package BUS;

import com.complier.socket.commons.enums.Action;
import com.complier.socket.commons.enums.Language;
import com.complier.socket.commons.request.CompileRequest;
import com.complier.socket.commons.request.InformationRequest;
import com.complier.socket.commons.request.MessageRequest;
import com.complier.socket.commons.request.Request;
import com.complier.socket.commons.response.CompileResponse;
import com.complier.socket.commons.response.MessageResponse;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client_CMD {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Scanner scanner;

    public Client_CMD() {}

    private void sendRequest (Request req) throws IOException {
        this.out.writeObject(req);
        this.out.flush();
    }

    public void startConnection(String ip, int port) {
        try {
//          Create Client socket
            clientSocket = new Socket(ip, port);
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            scanner = new Scanner(System.in);
//            new ResponseProcess().start();
            System.out.println("Chose your options");
            System.out.println("1: SEND MESSAGE ");
            System.out.println("2: FORMAT CODE ");
            System.out.println("-1: ESC");
            while(true) {
                System.out.print("[Enter your option]: ");
                String ch = scanner.nextLine();
                switch (ch) {
                    case "1": {
                        System.out.print("Send your message: ");
                        String message = scanner.nextLine();
                        sendRequest(MessageRequest.builder().action(Action.SEND_MESSAGE).message(message).build());
                        break;
                    }
                    case "2": {
                        Language language = null;
                        System.out.print( "Select your language: \n"+
                                            "1. Java\n"+
                                            "2. C++\n"+
                                            "3. Python\n"+
                                            "[Choice your language]: ");
                        LOOP:
                        while(true){
                            String input = scanner.nextLine();
                            language = getLanguage(input);
                            if(language != null) {
                                break LOOP;
                            }
                            System.out.println("[Choice your language]: ");
                        }
                        System.out.println("Send your code: ");
//                        scanner.nextLine();
                        String code = scanner.nextLine();
                        sendRequest(CompileRequest
                                    .builder()
                                    .action(Action.COMPILE_CODE)
                                    .code(code)
                                    .language(language)
                                    .build());
                        break;
                    }
                    case "-1": {
                        sendRequest(InformationRequest.builder().action(Action.DISCONNECT).build());
                        close();
                        return;
                    }
                }
                listenResponse();
            }
        } catch (UnknownHostException e ) {
            e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Language getLanguage (String language) {
        switch (language) {
            case "1":
                return Language.JAVA;
            case "2":
                return Language.CPP;
            case "3":
                return Language.PYTHON;
            default:
                break;
        }
        return null;
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

    public void listenResponse () throws IOException, ClassNotFoundException {
        Object object = in.readObject();
        if (ObjectUtils.isEmpty(object)) {
            return;
        }
        if (object instanceof MessageResponse) {
            MessageResponse messageResponse = (MessageResponse) object;
            System.out.println("Message from server " + messageResponse.getMessage());
        } else if (object instanceof CompileResponse) {
            CompileResponse compileResponse = (CompileResponse) object;
            System.out.println( "After Formatter \n" +
                                "=============== \n"+
                                compileResponse.getCode()+"\n"+
                                "===== [OUTPUT] =====\n"+
                                compileResponse.getOutput()
                                );
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
                        System.out.println( "After Formatter \n" +
                                            "=========================== \n"
                                            +compileResponse.getCode());
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
