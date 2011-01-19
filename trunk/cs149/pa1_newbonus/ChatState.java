// ChatState
//package cs149.chat;

import java.util.LinkedList;

public class ChatState {
    private static final int MAX_HISTORY = 32;
    private static final long NO_MSG_TIMEOUT = 15000;
    private final String name;
    private final LinkedList<String> history = new LinkedList<String>();
    private long lastID = System.currentTimeMillis();

    public ChatState(final String name) {
        this.name = name;
        history.addLast("Hello " + name + "!");
    }

    public String getName() {
        return name;
    }

    //Synchronize on history so only one thread can add at a time
    //  Need to do this because we check size, remove and add, needs to be atomic
    public void addMessage(final String chatmsg) {
        synchronized (history) {
          if (history.size() == MAX_HISTORY){
            history.removeLast();
          }
          history.addFirst(chatmsg);
          lastID++;
          history.notify();
        }
    }

    /*
      String recentMessages (long)
      This function should determine if any mes-sages are available that have
      an ID newer than the mostRecentSeenID. If messages are available, they 
      should be returned immediately. If not, this function should block for up 
      to 15 seconds awaiting a concurrent call to addMessage(). Once a concurrent 
      call to addMessage() is made, this method should unblock itself. Your 
      implementation should use Object.wait() or Condition.await() to block itself, 
      not Thread.sleep() 
    */
    // TODO don't know if messages are supposed to be returned most recent first or not
    public String recentMessages(long mostRecentSeenID) {
      //  synchronized (history) {
          //With the history locked, check if empty or if we have any new msgs from
          if (history.size() == 0 || lastID <= mostRecentSeenID){
             try {
             history.wait(NO_MSG_TIMEOUT);
             } catch (InterruptedException e){
               throw new Error("ERROR Died on waiting on history linked list", e);
             }
          }
 
          if (history.size() != 0){
             String msg = "";
             for (int i = (int)Math.min(lastID - mostRecentSeenID,history.size())-1 ; i >= 0; i -- ){
                 msg += (lastID-i) + ": " + history.get(i) + "\n";
             }
             return msg;
          }
       //}
        return null;
    }
}
