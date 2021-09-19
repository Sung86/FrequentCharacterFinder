import java.util.regex.Pattern;
import java.net.*;
import java.io.*;
import java.util.*;

public class MyCharFreqWorker {
  private final static CharFreqFinder CHAR_FINDER = new CharFreqFinder();
  private static ArrayList<Socket> workersSocket = new ArrayList<Socket>();
  private static int roundRobin = 0;
  
  /**
   * print message using System.out.println
   * @param VALUE // the message to be printed
   */
  private static void println(final Object VALUE){
    System.out.println(VALUE);
  }

  /**
   * create the workers, number of workers is determined from server
   * @param SERVER_SOCKET server socket for accepting connection from the server
   */
  private static void createWorkers(ServerSocket SERVER_SOCKET) {
    try {
      Socket socket  = SERVER_SOCKET.accept();
      DataInputStream dis = new DataInputStream(socket.getInputStream());
      DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
      
      final int NUM_WORKERS = dis.readInt();
      for (int i = 0; i < (NUM_WORKERS); i++) {
        ServerSocket createServerSocket = new ServerSocket(0);
        int port = createServerSocket.getLocalPort();
        dos.writeInt(port);
  
        socket = createServerSocket.accept();
        workersSocket.add(socket);// the rest of the workers
      }
    } catch (Exception e) {
      println(e.getMessage());
    }
  }
  /**
   * algorithm for strategy 2
   */
  private static void strategy2() {
    try {
      DataInputStream dis = new DataInputStream(workersSocket.get(roundRobin).getInputStream());
      final String REQUEST = dis.readUTF();
      WorkerThread wt = new WorkerThread(workersSocket.get(roundRobin),CHAR_FINDER, REQUEST);
      Thread workerThread = new Thread(wt);
      workerThread.start();
    
      roundRobin++;
      if (roundRobin == workersSocket.size()) {
        roundRobin = 0;
      }
    } catch (Exception e) {
      println(e.getMessage());
    }
  }
  /**
   * algorithm for strategy 3
   */
  private static void strategy3(){
    for (int i = 0; i < workersSocket.size(); i++) {
      try {
        DataInputStream dis = new DataInputStream(workersSocket.get(i).getInputStream());
        final String REQUEST = dis.readUTF();
        WorkerThread wt = new WorkerThread(workersSocket.get(i),CHAR_FINDER, REQUEST);
        Thread workerThread = new Thread(wt);
        workerThread.start();
      } catch (Exception e) {
        println(e.getMessage());
      }
    }
  }
  /**
   * read from the server and switch the strategy according the strategy from the server
   */
  private static void READY_TO_WORK(){
    try {
      while (true) {
        DataInputStream dis = new DataInputStream(workersSocket.get(0).getInputStream());
        final String STRATEGY = dis.readUTF();
        switch (STRATEGY) {
          case "strategy2":
            strategy2();
            break;
          case "strategy3":
            strategy3();
            break;
        }
      }
    } catch (Exception e) {
      println(e.getMessage());
    }
  }

  /**
   * tell the server this is the connection from worker
   * @param SOCKET the socket to the server
   */
  private static void TALK_TO_SERVER(final Socket SOCKET) {
    try {
      DataOutputStream dos = new DataOutputStream(SOCKET.getOutputStream());
      dos.writeUTF("worker");
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
      println("Need Exactly Two Arguements -  ServerIP , and Port Number\n");
      System.exit(0);
    }
    else if(args.length == 2){
      String regex = "^[0-9]*$";
      String port = String.valueOf(args[1]);
      String serverIp = String.valueOf(args[0]);
      
      if(!(serverIp.equals("localhost") || serverIp.equals("127.0.0.1"))){
        println("Server IP must be localhost or 127.0.0.1\n");
        System.exit(1);

      }else if(!Pattern.matches(regex, String.valueOf(port))){
        println("Port number need to be positive integer\n");
        System.exit(2);
      }
      else{
        int portNum = Integer.parseInt(port);

        if(portNum != 0 && !(portNum >= 1024 && portNum <= 65535)){
          println("Inavlid Port Number. Valid Port Numbers are 0 or 1024 - 65535\n");
          System.exit(3);
        }
      }
    }
  }
  public static void main(String[] args) {
    checkArgs( args);
    final String SERVER_IP =  args[0]; //ip address
    final int PORT_NUM = Integer.parseInt(args[1]); //port number
    
    try {
      final Socket SOCKET = new Socket(SERVER_IP , PORT_NUM); //connect to serrver
      final ServerSocket SERVER_SOCKET = new ServerSocket(8888);
      TALK_TO_SERVER(SOCKET);
      createWorkers(SERVER_SOCKET);
      // READY_TO_WORK(SERVER_SOCKET);
      READY_TO_WORK();

    } catch (Exception EX) {
      System.out.println(EX.getMessage());
      //TODO: handle exception
      // EX.printStackTrace();
    }
  }

  
}