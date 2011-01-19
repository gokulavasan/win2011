/*public class ChatMessage {
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
*/

import java.net.Socket;
import java.net.ServerSocket;

public class ChatMessage {
	private final Socket connection;
	//private final int = timestamp;
	
	public ChatMessage (final Socket connection) {
	this.connection = connection;
	}
	
	public Socket getConnection () {
		return this.connection;
	}
}
