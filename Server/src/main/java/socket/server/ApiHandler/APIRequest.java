package socket.server.ApiHandler;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class APIRequest {

    private String clientId = "c445a61b53d2c866dbfaf2ab56753eba";
    private String clientSecret = "bc09fac63c383c707fe78b72a487a3192a923630baf073154f1ef4ee3155389a";

    public APIRequest() {

    }

    public String formatCode (String code, String language) {
        try {
            String formatLanguage = language;
            switch (language) {
                case "java":
                case "csharp":
                    formatLanguage = "javascript";
                    break;
                case "cpp":
                    formatLanguage = "c";
                    break;
                default:
                    break;
            }

            CloseableHttpClient httpclient = HttpClients.createDefault();
            //Creating a HttpGet object
            HttpPost httpPost = new HttpPost("https://tools.tutorialspoint.com/format_"+formatLanguage+".php");
            //Add form data
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("code", code));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            //Executing the POST request
            HttpResponse httpresponse = httpclient.execute(httpPost);
            Scanner sc = new Scanner(httpresponse.getEntity().getContent());
            String json = null;
            while(sc.hasNext()) {
                json = sc.nextLine();
            }
            Gson gson = new Gson(); // Create Gson
            Formatter formatter = gson.fromJson(json, Formatter.class);
            return formatter.code;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String compileCode (String code, String language) {
        try {
            int version = 3;
            if(language.equals("python")) {
                language = language+"2";
                version = 2;
            }
            CloseableHttpClient httpclient = HttpClients.createDefault();
            System.out.println(code);
            //Creating a HttpGet object
            HttpPost httpPost = new HttpPost("https://api.jdoodle.com/v1/execute");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type", "application/json");
            String postJson =   "{"+
                                "\"clientId\":\""+clientId+"\","+
                                "\"clientSecret\":\""+clientSecret+"\","+
                                "\"script\":\""+code
                                                .replace("\"", "\\\"")
                                                .replace("\n", "\\n")
//                                                .replaceAll("\\n", "\n")
                                                +"\","+
                                "\"language\":\""+language+"\","+
                                "\"versionIndex\":\""+version+"\""+
                                "}";
            StringEntity entity = new StringEntity(postJson);
            System.out.println(postJson);
            httpPost.setEntity(entity);

            System.out.println(httpPost.getEntity());
            HttpResponse httpresponse = httpclient.execute(httpPost);
            Scanner sc = new Scanner(httpresponse.getEntity().getContent());
            String json = null;
            while(sc.hasNext()) {
                json = sc.nextLine();
                System.out.println(json);
            }
            Gson gson = new Gson(); // Create Gson
            Complier complier = gson.fromJson(json, Complier.class);
            return complier.output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public class Formatter
    {
        @SerializedName("code")
        public String code;
    }

    public class Complier
    {
        @SerializedName("output")
        public String output;
        @SerializedName("statusCode")
        public String statusCode;
        @SerializedName("memory")
        public String memory;
        @SerializedName("cpuTime")
        public String cpuTime;
    }
}
