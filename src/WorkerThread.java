
import java.net.*;
import java.io.*;

public class WorkerThread implements Runnable {
  private Socket socket; 
  private CharFreqFinder charFinder;
  private String request;

  //constructor//
  public WorkerThread(final Socket SOCKET, final CharFreqFinder  FINDER, 
                      final String REQUEST) {
    this.socket = SOCKET;
    this.charFinder = FINDER;
    this.request  = REQUEST;
  }

  /**
   * Compute the string received from server
   * and send back to server
   */
  private void compute(){
    try {
      DataOutputStream out=new DataOutputStream(this.socket.getOutputStream());
      final char ANSWER = this.charFinder.frequentCharacterFinder(this.request);
      out.writeChar(ANSWER);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      // e.printStackTrace();
    }
  }

  public void run() {
    try {
      compute();
    } catch (final Exception e) {
      System.out.println(e.getMessage());
      // e.printStackTrace();
    }
  }
}