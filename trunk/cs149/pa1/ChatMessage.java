public class ChatMessage {
  private String msg;
  private String room;

  public ChatMessage (final String room, final String msg) {
    this.msg = msg;
    this.room = room;
  }

  public String getRoom () {
    return this.room;
  }

  public String getMsg () {
    return this.msg;
  }
}
