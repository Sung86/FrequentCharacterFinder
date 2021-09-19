import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerWorkerThread implements Runnable {

  private DataInputStream dis;
  private DataOutputStream dos;
  private final CharFreqFinder CHAR_FINDER;
  private static Clients_Info clientsInfo;
  private static int numWorkers;
  private static ArrayList<Socket> workersSocket;
  private static int workerPos = 0;
  // constructor//
  public ServerWorkerThread(final DataInputStream DIS, final DataOutputStream DOS, final CharFreqFinder FINDER,
      Clients_Info clients_Info, final int NUM_WORKERS, final Socket SOCKET) {
    this.dis = DIS;
    this.dos = DOS;
    this.CHAR_FINDER = FINDER;
    ServerWorkerThread.clientsInfo = clients_Info;
    ServerWorkerThread.numWorkers = NUM_WORKERS;
    workersSocket = new ArrayList<Socket>();
  }

  /**
   * print message using System.out.println
   * @param VALUE // the message to be printed
   */
  private static void println(final Object VALUE){
    System.out.println(VALUE);
  }
  /**
   * create workers
   */
  private void createWorkers(){
    if (numWorkers != 0) {
      try {
        Socket socket = new Socket("localhost", 8888);// 1
        // workersSocket.add(socket);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeInt(numWorkers);

        dis = new DataInputStream(socket.getInputStream());
        for (int i = 0; i < (numWorkers); i++) {
          int port = dis.readInt();
          socket = new Socket("localhost", port);
          workersSocket.add(socket);
        }
      } catch (Exception e) {
        println(e.getMessage());
      }
    }
  }

  /**
   * strategy2
   */
  private void strategy2() {
    try {
      DataOutputStream dos = new DataOutputStream(workersSocket.get(workerPos).getOutputStream());
      DataInputStream dis = new DataInputStream(workersSocket.get(workerPos).getInputStream());
  
      dos.writeUTF(clientsInfo.getNewRequest());
      final char ANSWER = dis.readChar();
      clientsInfo.setPrevAnswers(ANSWER);
      workerPos++;
  
      if (workerPos == workersSocket.size()) {
        workerPos = 0;
      }
    } catch (Exception e) {
      println(e.getMessage());
    }
  }

  /**
   * strategy 3
   */
  private void strategy3() {
    int workerSize = workersSocket.size();
    final String REQUEST = clientsInfo.getNewRequest();
    final int STR_LENGTH = REQUEST.length();
    // make sure the task is equally shared to all workers
    if (workerSize > STR_LENGTH) {
      int diff = workerSize - STR_LENGTH;
      workerSize -= diff;
    }
    int step = STR_LENGTH / workerSize;
    final String FULL_STRING = REQUEST;

    // sends task to each worker
    int workerIndex = 0;
    try {
      for (int i = 0, numDivision = 0; (i + step) < STR_LENGTH; i += step) {
        DataOutputStream dos = new DataOutputStream(workersSocket.get(workerIndex).getOutputStream());
        dos.writeUTF(FULL_STRING.substring(i, i + step));
        numDivision++;
        workerIndex++;
  
        /**
         * the possibility: all workers handle equally but there's still string left
         * solution: the other worker handle equally, let the last worker handles the
         * rest of the task (won't cause much differ in length)
         */
        if (numDivision == workerSize - 1) {
          dos = new DataOutputStream(workersSocket.get(workerIndex).getOutputStream());
          dos.writeUTF(FULL_STRING.substring(i + step));
          numDivision++;
          break;
        }
      }
  
      // receive computed answer from each worker
      String computedStr = "";
      for (int i = 0; i < workerSize; i++) {
        DataInputStream dis = new DataInputStream(workersSocket.get(i).getInputStream());
        char computedchar = dis.readChar();
        computedStr += computedchar;
      }
      // compute the aggregated answers from workers
      final char ANSWER = this.CHAR_FINDER.frequentCharacterFinder(computedStr);
      clientsInfo.setPrevAnswers(ANSWER);
    } catch (Exception e) {
      println(e.getMessage());
    }
  }

  /**
   * communicate with the worker according to the strategy
   */
  private void TALK_TO_WORKER() {
    try {
      do {
        DataOutputStream dos = new DataOutputStream(workersSocket.get(0).getOutputStream());
        if (clientsInfo.getHasJob()) {
          clientsInfo.setHasJob(false);
          switch (clientsInfo.getStrategy()) {
            case 2:
              dos.writeUTF("strategy2");
              strategy2();
              break;
            case 3:
              dos.writeUTF("strategy3");
              strategy3();
              break;
          }
        }
      } while (true);
    } catch (Exception e) {
      println(e.getMessage());
    }
  
  }

  public void run() {
    try {
      createWorkers();
      TALK_TO_WORKER();
    } catch (Exception e) {
      println(e.getMessage());
    }
  }
}