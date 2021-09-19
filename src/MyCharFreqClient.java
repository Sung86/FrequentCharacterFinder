import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.net.*;
import java.io.*;
import java.time.*;

public class MyCharFreqClient {
  private final static Boolean ANALYSIS_TIME_CONN= false;
  private final static Boolean ANALYSIS_TIME_STRING= false;
  private static long total_res_time = 0;
  /**
   * print message using System.out.println
   * 
   * @param VALUE // the message to be printed
   */
  private static void println(final Object VALUE) {
    System.out.println(VALUE);
  }

  /**
   * print message using System.out.print
   * 
   * @param VALUE // the message to be printed
   */
  private static void print(final Object VALUE) {
    System.out.print(VALUE);
  }

  /**
   * communicate with the server
   * 
   * @param DIS //DataInputStream to read from server
   * @param DOS //DataOutputStream to write to server
   * @param BR  //BufferReader used for reading user's message (on the console)
   */
  private static void TALK_TO_SERVER(final DataInputStream DIS, final DataOutputStream DOS, final BufferedReader BR) {
    try {
      DOS.writeUTF("client"); // tell server this is client connection
      String menu = DIS.readUTF();
      println(menu);
      String clientRequest = "";
      String serverReply = "";

      do {
        print("Client >> ");
        clientRequest = BR.readLine();
        DOS.writeUTF(clientRequest);
        serverReply = DIS.readUTF();
        println(serverReply);
      } while (!clientRequest.toLowerCase().equals("exit"));

    } catch (Exception e) {
      println(e.getMessage());
    }
  }

  /**
   * check or validate the initial args from console
   * @param args //initial args
   */
  private static void checkArgs(final String[] args) {
    if (args.length != 2) {
      println("Need Exactly Two Arguements -  ServerIP , and Port Number");
      System.exit(0);
    } else if (args.length == 2) {
      String regex = "^[0-9]*$";
      String port = String.valueOf(args[1]);
      String serverIp = String.valueOf(args[0]);

      if (!(serverIp.equals("localhost") || serverIp.equals("127.0.0.1"))) {
        println("Server IP must be localhost or 127.0.0.1\n");
        System.exit(1);

      } else if (!Pattern.matches(regex, String.valueOf(port))) {
        println("Port number need to be positive integer\n");
        System.exit(2);
      } else {
        int portNum = Integer.parseInt(port);

        if (portNum != 0 && !(portNum >= 1024 && portNum <= 65535)) {
          println("Inavlid Port Number. Valid Port Numbers are 0 or 1024 - 65535\n");
          System.exit(3);
        }
      }
    }
  }

  /**
   * For Testing
   * Test maximum connection to server
   * @param SERVER_IP the server ip
   * @param PORT the port number
   */
  private static void TEST_MAX_SERVER_CONN(final String SERVER_IP, final int PORT) {
    ArrayList<Long> times = new ArrayList<Long>();

    for(int i= 0;i< 4088; i++) {

      Thread t = new Thread(new Runnable(){
        private ArrayList<Long> time;{
          this.time = times;
          
        }
        public void run() {
          try {
            final Socket SOCKET = new Socket(SERVER_IP, PORT);
            DataOutputStream dos = new DataOutputStream(SOCKET.getOutputStream());
            DataInputStream dis = new DataInputStream(SOCKET.getInputStream());
            
            final Instant STOPWATCH_START = Instant.now();
            dos.writeUTF("client");
            final Instant STOPWATCH_END = Instant.now();
            final long WAITED_DUR = Duration.between(STOPWATCH_START, STOPWATCH_END).toNanos();
            println(dis.readUTF() + ".\twaited: " + WAITED_DUR);
            this.time.add(WAITED_DUR);
          }
          catch (Exception e) {
            println(e.getMessage());
            System.exit(0);
          }
        }
      });

      t.start();
      try {
        TimeUnit.MILLISECONDS.sleep(3);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    }

    long totaltime =times.stream().mapToLong(Long::longValue).sum();
    long average = totaltime/4088;
    println("totaltime: " + totaltime + " ns\taverage: " + average+" ns");
  }

  /**
   * For Testing
   * Test the response time with increasing string size
   * @param SERVER_IP // server ip 
   * @param PORT // port number 
   * @param str_len // length of string
   */
  private static void TEST_LENGTH_STRING_REQ(final String SERVER_IP, final int PORT,
                                              int str_len) {
    try {
      final Socket SOCKET = new Socket(SERVER_IP, PORT); // connect to serrver
      final DataOutputStream DOS = new DataOutputStream(SOCKET.getOutputStream());
      final DataInputStream DIS = new DataInputStream(SOCKET.getInputStream());

      DOS.writeUTF("client"); // tell server this is client connection
      String reqString = "";
      final int STR_LEN = str_len;

      for (int i = 0; i <= STR_LEN; i++) {
        int asciiCode =(int)(Math.random()*(((int)'z' - (int)'a') + 1))  + (int)'a';
        reqString += (char)asciiCode;
      }
      String reqCommand = "newrequest";
      String request = reqCommand +" "+ reqString;
      
      println("Length: " + reqString.length());

      final Instant STOPWATCH_START = Instant.now();
      DOS.writeUTF(request);
      String serverReply = DIS.readUTF();

      final Instant STOPWATCH_END = Instant.now();
      final long WAITED_DUR = Duration.between(STOPWATCH_START, STOPWATCH_END).toNanos();
      total_res_time += WAITED_DUR;

      println(serverReply + "\twaited: " + WAITED_DUR+" ns");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(2);
    }
  }
  public static void main(String[] args) {
    checkArgs(args);
    final String SERVER_IP = args[0];
    final int PORT_NUM = Integer.parseInt(args[1]);

    //For Testing Performance//
    if(ANALYSIS_TIME_CONN || ANALYSIS_TIME_STRING){
      if(ANALYSIS_TIME_CONN){
        TEST_MAX_SERVER_CONN(SERVER_IP, PORT_NUM);
      }
      else if(ANALYSIS_TIME_STRING){
        final int NUM_TEST = 10;
        final int MIN_STR_LEN = 100;
        final int MAX_STR_LEN = 1000;
        final int STR_LEN_INCREMENT = 100;
        ArrayList<String> test_result = new ArrayList<String>();

        //Change to true if it is strategy 1//
        final Boolean STRATEGY1 = true;
        for(int str_len =MIN_STR_LEN ; str_len <= MAX_STR_LEN; str_len += STR_LEN_INCREMENT){
          for (int i = 0; i < NUM_TEST; i++) {
            TEST_LENGTH_STRING_REQ (SERVER_IP, PORT_NUM, str_len);
          }  
          
          long average_res_time = STRATEGY1
                                ? (total_res_time / NUM_TEST)
                                :(total_res_time / NUM_TEST)-3000000;

          test_result.add("\nString Size: "+ str_len +"\tResponse Time: " + average_res_time + " ns\n");
        }
        for(int i = 0; i < test_result.size();i++){
          println(test_result.get(i));
        }
        println(test_result.size());
      }
    }
    ////
    else{
      try {
        final Socket SOCKET = new Socket(SERVER_IP, PORT_NUM); // connect to serrver
        final BufferedReader BR_CLIENT = new BufferedReader(new InputStreamReader(System.in));
        final DataOutputStream DOS_SERVER = new DataOutputStream(SOCKET.getOutputStream());
        final DataInputStream DIS_SERVER = new DataInputStream(SOCKET.getInputStream());
        TALK_TO_SERVER(DIS_SERVER, DOS_SERVER, BR_CLIENT);
        SOCKET.close();
      } catch (Exception e) {
        println(e.getMessage());
      }
    }
  }
}
