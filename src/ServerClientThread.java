import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.io.*;

public class ServerClientThread implements Runnable {
  private final Boolean ANALYSIS_TIME_STRING = false;

  private final DataInputStream DIS;
  private final DataOutputStream DOS;
  private final CharFreqFinder CHAR_FINDER;
  private static Clients_Info clientsInfo;

  private char answer;

  // constructor//
  public ServerClientThread(final DataInputStream DIS, final DataOutputStream DOS, final CharFreqFinder FINDER,
      Clients_Info clientsInfo) {
    this.DIS = DIS;
    this.DOS = DOS;
    this.CHAR_FINDER = FINDER;
    ServerClientThread.clientsInfo = clientsInfo;
  }

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
   * process exit for client
   * 
   * @param TOKENIZED_MSG the tokensized message from client
   */
  private void processExit(final String[] TOKENIZED_MSG) {
    try {
      if (TOKENIZED_MSG.length > 1) {
        DOS.writeUTF("Server >> Please enter properly if you want to exit");
      } else {
        DOS.writeUTF("Server >> Your connection has been closed!");
        DOS.close();
        DIS.close();
      }
    } catch (Exception e) {
      println(e.getMessage());
    }
  }

  /**
   * startegy 1 for client's request
   * 
   * @param REQUEST the request
   * @return answer
   */
  private char strategyOne(final String REQUEST) {
    return this.CHAR_FINDER.frequentCharacterFinder(REQUEST);
  }

  /**
   * process the status request
   * 
   * @param REQUEST the status request
   */
  private void processStatusRequest(final String REQUEST) {
    final String REQUESTED_PASSCODE = REQUEST;

    try {
      if (!Pattern.matches("[0-9]+", REQUESTED_PASSCODE)) {
        DOS.writeUTF("Server >> Please enter a positive number only for passcode.");

      } else if (Integer.parseInt(REQUESTED_PASSCODE) > clientsInfo.getPrevAnswersSize() - 1) {
        DOS.writeUTF("Server >> Invalid passcode. Please try again.");

      } else {
        handleStatusRequest(Integer.parseInt(REQUESTED_PASSCODE));
      }
    } catch (Exception e) {
      println(e.getMessage());
    }
  }

  /**
   * handle the status request via passcode
   * 
   * @param PASSCODE the passcode
   */
  private void handleStatusRequest(final int PASSCODE) {
    answer = clientsInfo.getPrevAnswers(PASSCODE);
    try {
      DOS.writeUTF("Server >> " + answer);
    } catch (Exception e) {
      println(e.getMessage());
    }
  }

  /**
   * handle the new request according to its length (strings)
   */
  private void handleNewRequest() {
    final String REQUEST = clientsInfo.getNewRequest();
    final int LENGTH = REQUEST.length();

    if (LENGTH <= 3) {
      clientsInfo.setStrategy(1);
      answer = strategyOne(REQUEST);
      clientsInfo.setPrevAnswers(answer);
    } else if (LENGTH <= 6) {
      clientsInfo.setHasJob(true);
      clientsInfo.setStrategy(2);
    } else {
      clientsInfo.setHasJob(true);
      clientsInfo.setStrategy(3);
    }
  }

  /**
   * process the new request
   * 
   * @param REQUEST the new request
   */
  private void processNewRequest(final String REQUEST) {
    try {
      if (!Pattern.matches("^[a-zA-Z ]*$", REQUEST)) {
        DOS.writeUTF("Server >> Please enter alphabets only for input string.");
      } else {
        clientsInfo.setNewRequest(REQUEST);
        handleNewRequest();
        DOS.writeUTF("Server >> Passcode: " + clientsInfo.getPasscode() + " (Request has been accepted)");
        clientsInfo.increamentPasscode(1);
      }
    } catch (Exception e) {
      println(e.getMessage());
    }
  }

  /**
   * the menu message, showing available options
   * 
   * @return the message of menu
   */
  private static String menuMessage() {
    final String MESSAGE = "-----------------------------\n" + "Welcome to CharFreqServer\n"
        + "Type any of the following options:\n" + "NewRequest <INPUTSRTING>\n" + "StatusRequest <passcode>\n"
        + "Exit\n";
    return MESSAGE;
  }

  /**
   * the menu for clients
   */
  private void menu() {
    try {
      DOS.writeUTF(menuMessage());

      do {
        final String INCOMING_MSG = DIS.readUTF().trim().toLowerCase();
        final String[] TOKENIZED_MSG = INCOMING_MSG.split("\\s+", 2);
        final String MSG_COMMAND = TOKENIZED_MSG[0];
        String MSG_REQUEST = "";
        if (TOKENIZED_MSG.length > 1)
          MSG_REQUEST = TOKENIZED_MSG[1];

        if (INCOMING_MSG.isEmpty()) {
          DOS.writeUTF("Server >> Please enter an option.");
        } else {
          switch (MSG_COMMAND) {
            case "newrequest":
              processNewRequest(MSG_REQUEST);
              break;

            case "statusrequest":
              processStatusRequest(MSG_REQUEST);
              break;

            case "exit":
              processExit(TOKENIZED_MSG);
              break;
            default:
              DOS.writeUTF("Server >> INVALID OPTION!! PLEASE SPECIFY OPTION AGAIN.");
              break;
          }
        }
      } while (true);
    } catch (final EOFException EOF) {
      if (EOF.getMessage() == null) {
        println("client quited");
      } else {
        println(EOF.getMessage());
      }
    } catch (Exception e) {
      println(e.getMessage());
    }

  }

  /**
   * For Testing ONLY
   * Recieve string and process depends on strategy
   * @throws IOException
   */
  private void TEST_LENGTH_STRING_REQ() throws IOException {
    final Boolean ANALYZE_STRATEGY1 = true;
    final Boolean ANALYZE_STRATEGY2 = false;
    final Boolean ANALYZE_STRATEGY3 = false;

    final String REQUEST = DIS.readUTF().trim().toLowerCase().split("\\s+", 2)[1];
    if (ANALYZE_STRATEGY1) {
      clientsInfo.setStrategy(1);
      char answer = strategyOne(REQUEST);
      clientsInfo.setPrevAnswers(answer);

      DOS.writeUTF("Server >> " + answer);
      clientsInfo.increamentPasscode(1);

    } else if (ANALYZE_STRATEGY2 || ANALYZE_STRATEGY3) {
      clientsInfo.setHasJob(true);
      clientsInfo.setNewRequest(REQUEST);

      if(ANALYZE_STRATEGY2) clientsInfo.setStrategy(2);
      else if(ANALYZE_STRATEGY3)clientsInfo.setStrategy(3);

      try {
        TimeUnit.MILLISECONDS.sleep(3);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      handleStatusRequest(clientsInfo.getPasscode());
      clientsInfo.increamentPasscode(1);

    } 
  }

  /**
   * communicate client with a menu
   */
  private void TALK_TO_CLIENT() {
    menu();
  }

  public void run() {
    try {
      if (ANALYSIS_TIME_STRING) {
        TEST_LENGTH_STRING_REQ();
      }else{
        TALK_TO_CLIENT();
      }
      
    }catch (Exception e) {
      println(e.getMessage());
      // EX.printStackTrace();
    }
  }
}