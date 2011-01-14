public class ChatMessage {
  private long ID;
  private String msg;
  public ChatMessage (final String msg) {
    this.ID = System.currentTimeMillis();
    this.msg = msg;
  }
  public long getID () {
    return this.ID;
  }
  public String getMsg () {
    return this.msg;
  }
}
