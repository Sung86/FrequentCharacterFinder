import java.util.regex.Pattern;
import java.net.*;
import java.io.*;
public class MyCharFreqServer {
  private final static Boolean ANALYSIS_TIME_CONN = false;
  private static Clients_Info clientsInfo = new Clients_Info();
  private final static CharFreqFinder CHAR_FINDER = new CharFreqFinder();

  /**
   * print message using System.out.println
   * @param VALUE // the message to be printed
   */
  private static void println(final Object VALUE){
    System.out.println(VALUE);
  }

  /**
   * print message using System.out.print
   * @param VALUE // the message to be printed
   */
  private static void print(final Object VALUE){
    System.out.print(VALUE);
  }

  /**
   * Handle client's connection
   * @param DIS the data input stream of the client
   * @param DOS the data output stream of the client
   */
  private static void handleClient(final DataInputStream DIS, final DataOutputStream DOS){ 
    final ServerClientThread SCT = new ServerClientThread(DIS, DOS, CHAR_FINDER, clientsInfo);
    final Thread CLIENT_THREAD = new Thread(SCT);
    CLIENT_THREAD.start();
  }

  /**
   * Handle worker's connection
   * @param DIS the data input stream of the worker
   * @param DOS the data output stream of the worker
   * @param NUM_WORKERS the number of workers 
   * @param SOCKET the socket connection of the worker
   */
  private static void handleWorker(final DataInputStream DIS, final DataOutputStream DOS,
                                  final int NUM_WORKERS, final Socket SOCKET)  {
    final ServerWorkerThread SWT = new ServerWorkerThread(DIS, DOS, CHAR_FINDER, 
                                                          clientsInfo, NUM_WORKERS, 
                                                          SOCKET);
    final Thread WORKER_THREAD = new Thread(SWT);
    WORKER_THREAD.start();
    
  }
  
  /**
   * Handle any inbound traffic 
   * @param NUM_WORKERS the number of workers 
   * @param SOCKET the socket connection of the connection
   */
  public static void handleInboundTraffic(final int NUM_WORKERS, final Socket SOCKET){
    try {
      final DataInputStream DIS = new DataInputStream(SOCKET.getInputStream());
      final DataOutputStream DOS = new DataOutputStream(SOCKET.getOutputStream());;
    
      switch (DIS.readUTF()) {
        case "client":
          handleClient(DIS, DOS);
          break;
        
        default:
          handleWorker(DIS, DOS, NUM_WORKERS, SOCKET);
          break;
      }
    } catch (Exception e) {
      println(e.getMessage());
    }                                        
  
  }
  
  /**
   * check or validate the initial args from console
   * @param args //initial args
   */
  private static void checkArgs(final String[] args){
    if(args.length != 2){
      println("Need Exactly Two Arguements - Port Number, and Number of Workers\n");
      System.exit(0);
    }
    else if(args.length == 2){
      String regex = "^[0-9]*$";
      String port = String.valueOf(args[0]);
      String numWorkers = String.valueOf(args[1]);
      
      if(!(Pattern.matches(regex, String.valueOf(port)) &&  
          Pattern.matches(regex, String.valueOf(numWorkers)))){

        println("All arguments need to be positive integer\n");
        System.exit(1);
      }else{
        int portNum = Integer.parseInt(port);

        if(portNum != 0 && !(portNum >= 1024 && portNum <= 65535)){
          println("Inavlid Port Number. Valid Port Numbers are 0 or 1024 - 65535\n");
          System.exit(2);
        }
      }
    }
  }
  /**
   * For Testing ONLY
   * test maximum connection to that can be accepted
   * @param PORT the port number
   */
  private static void TEST_MAX_CLIENT_CONN(final int PORT){
    int i = 1;
    try {
      final ServerSocket SERVER_SOCKET = new ServerSocket(PORT);
      while (true) {
        final Socket SOCKET = SERVER_SOCKET.accept();
        DataOutputStream dos = new DataOutputStream(SOCKET.getOutputStream());
        dos.writeUTF("server "+ i);
        i++;
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(3);
    }
  }
  public static void main(final String[] args){
    checkArgs(args);
    final int PORT= Integer.parseInt(args[0]);
    final int NUM_WORKERS = Integer.parseInt(args[1]);

    if(ANALYSIS_TIME_CONN){
      TEST_MAX_CLIENT_CONN(PORT);

    }else{
      try {
        final ServerSocket SERVER_SOCKET = new ServerSocket(PORT);
        println("Server Started ....");
        while (true) {
          final Socket SOCKET = SERVER_SOCKET.accept();
          handleInboundTraffic(NUM_WORKERS, SOCKET);
        }
      } catch (Exception e) {
        println(e.getMessage());
      }
    }
  }
}

