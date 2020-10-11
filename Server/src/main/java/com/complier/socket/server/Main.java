package com.complier.socket.server;

public class Main {
    public static void main (String args[]) {
        Server server = new Server();
        server.start(5000);
//        String tmp = "public class MyClass {\n" +
//                "    public static void main(String args[]) {\n" +
//                "        int x = 10;\n" +
//                "        int y = 25;\n" +
//                "        int z = x + y;\n" +
//                "        System.out.println(\"Sum of x+y = \" + z);\n" +
//                "    }\n" +
//                "}\n" +
//                "{\"clientId\":\"c445a61b53d2c866dbfaf2ab56753eba\",\"clientSecret\":\"bc09fac63c383c707fe78b72a487a3192a923630baf073154f1ef4ee3155389a\",\"script\":\"public class MyClass {\\n    public static void main(String args[]) {\\n        int x = 10;\\n        int y = 25;\\n        int z = x + y;\\n        System.out.println(\"Sum of x+y = \" + z);\\n    }\\n}\",\"language\":\"java\",\"versionIndex\":\"3\"}\n" +
//                "[Content-Type: text/plain; charset=ISO-8859-1,Content-Length: 372,Chunked: false]\n" +
//                "{\"error\":\"Invalid Request\",\"statusCode\":400}\n";
//        System.out.println(tmp.replace("\n", "\\n").replace("\"","\\\"" ));
    }
}
