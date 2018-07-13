import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.util.*;
import java.security.cert.*;
import java.security.*;

class Untitled {
  // HTTP GET request
  private static String sendGet(String url) throws Exception {
//    TrustManager[] trustAllCerts = new TrustManager[] { 
//      new X509TrustManager() {     
//        public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
//          return new X509Certificate[0];
//        } 
//        public void checkClientTrusted( 
//          java.security.cert.X509Certificate[] certs, String authType) {
//          } 
//        public void checkServerTrusted( 
//          java.security.cert.X509Certificate[] certs, String authType) {
//        }
//      } 
//    };
//    
//    try {
//      SSLContext sc = SSLContext.getInstance("SSL"); 
//      sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
//      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//    } catch (GeneralSecurityException e) {
//      e.printStackTrace();
//    }
    
    URL obj = new URL(url);
    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

    // optional default is GET
    con.setRequestMethod("GET");

    //add request header
    int responseCode = con.getResponseCode();
    System.out.println("\nSending 'GET' request to URL : " + url);
    System.out.println("Response Code : " + responseCode);

    BufferedReader in = new BufferedReader(
            new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();

    return response.toString();
  }
  
  // HTTP POST request
  private static String sendPost(String url, String urlParameters) throws Exception {

//    String url = "https://selfsolve.apple.com/wcResults.do";
    URL obj = new URL(url);
    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

    //add reuqest header
    con.setRequestMethod("POST");
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

//    String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
    
    // Send post request
    con.setDoOutput(true);
    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    wr.writeBytes(urlParameters);
    wr.flush();
    wr.close();

    int responseCode = con.getResponseCode();
    System.out.println("\nSending 'POST' request to URL : " + url);
    System.out.println("Post parameters : " + urlParameters);
    System.out.println("Response Code : " + responseCode);

    BufferedReader in = new BufferedReader(
            new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    
    return response.toString();
  }
  
  private static String urlExpectYieldText() {
    String base = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/expectedYield?";
    base = base + "orchardId=" + "-LCEFgdMMO80LR98BzPC";//get correct orchard ID
    double currentTime;
    double divideBy1000Var = 1000.0000000;
    currentTime = (System.currentTimeMillis() / divideBy1000Var);
    base = base + "&date=" + currentTime;
    base = base + "&uid=" + "xFBNcNmiuON8ACbAHzH0diWcFQ43";
    return base;
  }

  public static void getExpectedYield() {
    try {
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            String response = sendGet(urlExpectYieldText());
            System.out.println(" %%%%%%%%%%%%% " + response + " %%%%%%%%%%%%% ");
//            JSONObject obj = new JSONObject(response);
//            final Double expectedYield = obj.getDouble("expected"); // This is the value
//            System.out.println(" $$$$$$$$$$$$$$$$$$$ " + expectedYield + " $$$$$$$$$$$$$$$$$$$ ");
//            runOnUiThread(new Runnable() {
//              public void run() {
//                textView = findViewById(R.id.textView);
//                textView.setText("Expected Yield: " + expectedYield);
//              }
//            });
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });

      thread.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) throws Exception {
//    String res = sendGet("https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/flattendSessions?pageNo=1&pageSize=20&uid=xFBNcNmiuON8ACbAHzH0diWcFQ43");
//    System.out.println(res);
//    String res = sendGet("https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/expectedYield?orchardId=-LCEFgdMMO80LR98BzPC&date=1530633623.78762&uid=xFBNcNmiuON8ACbAHzH0diWcFQ43");
//    System.out.println(res);
  
//   https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/expectedYield?orchardId=-LCEFgdMMO80LR98BzPC&date=1530633623.78762&uid=xFBNcNmiuON8ACbAHzH0diWcFQ43
//  https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/expectedYieldorchardId=-LCEFgdMMO80LR98BzPC&date=1530633.0&uid=xFBNcNmiuON8ACbAHzH0diWcFQ43
  
    getExpectedYield();
  }
}