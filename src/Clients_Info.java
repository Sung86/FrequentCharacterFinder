import java.util.ArrayList;

class Clients_Info {
  private static ArrayList<Character> prevAnswers;
  private static int passcode;
  private static String newRequest;
  private static int strategy; 
  private static Boolean hasJob;
  
  //constructor//
  public Clients_Info() {
    prevAnswers= new ArrayList<Character>();
    passcode = 0;
    hasJob = false;
  }
  
  /**
   * @param PASSCODE the index in ArrayList of prevAnswers
   * @return the answer
   */
  public synchronized char getPrevAnswers(final int PASSCODE)  {
    return prevAnswers.get(PASSCODE);
  }
  
  /**
   * @param prevRequests the answer to set
   */
  public synchronized void setPrevAnswers(final char ANSWER) {
    Clients_Info.prevAnswers.add(ANSWER);
  }
  
  /**
   * @return the size of ArrayList preAnswers
   */
  public synchronized int getPrevAnswersSize(){
    return prevAnswers.size();
  }
  
  /**
   * @return the passcode
   */
  public synchronized int getPasscode(){
    return passcode;
  }
  
  /**
   * @param VAL the value to be increament to passcode
   */
  public synchronized void increamentPasscode(final int VAL) {
    Clients_Info.passcode += VAL;
  }
  
  /**
   * @return the new request
   */
  public synchronized String getNewRequest(){
    return Clients_Info.newRequest;
  }
  
  /**
   * @param request the new request to be set to
   */
  public synchronized void setNewRequest(final String REQUEST) {
    Clients_Info.newRequest = REQUEST;
  }
  
  /**
   * @return //the strategy for client's new request
   */
  public synchronized int getStrategy() {
    return strategy;
  }
  
  /**
   * @param request the new request to be set to
   */
  public synchronized void setStrategy(final int STRATEGY) {
    Clients_Info.strategy = STRATEGY;
  }
  
  /**
   * @return //the strategy for client's new request
   */
  public synchronized Boolean getHasJob(){
    return hasJob;
  }
  
  /**
   * @param request the new request to be set to
   */
  public synchronized void setHasJob(final Boolean VAL) {
    Clients_Info.hasJob = VAL;
  }
}
