// ChatServer
//package cs149.chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;

public class ChatServer {
    private static final Charset utf8 = Charset.forName("UTF-8");

    private static final String OK = "200 OK";
    private static final String NOT_FOUND = "404 NOT FOUND";
    private static final String HTML = "text/html";
    private static final String TEXT = "text/plain";

    private static final Pattern PAGE_REQUEST
	= Pattern.compile("GET /([^ /]+)/chat\\.html HTTP.*");
    private static final Pattern PULL_REQUEST
	= Pattern.compile("POST /([^ /]+)/pull\\?last=([0-9]+) HTTP.*");
    private static final Pattern PUSH_REQUEST
	= Pattern.compile("POST /([^ /]+)/push\\?msg=([^ ]*) HTTP.*");

    private static final String CHAT_HTML;
    static {
	try {
	    CHAT_HTML = getResourceAsString("chat.html");
	} catch (final IOException xx) {
	    throw new Error("unable to start server", xx);
	}
    }

    private final int port;
    private final Map<String,ChatState> stateByName = new HashMap<String,ChatState>();
    private final LinkedList<ChatMessage> worklist = new LinkedList<ChatMessage>();

    private int numThreads;
    private WorkerThreads[] threads;

    private ChatState getState(final String room) {
	ChatState state = stateByName.get(room);
	if (state == null) {
	    state = new ChatState(room);
	    stateByName.put(room, state);
	}
	return state;
    }



    public void WorkInit (int numThreads)
    {
	this.numThreads = numThreads;
	threads = new WorkerThreads[numThreads];

	for (int i=0; i<numThreads; i++)
	{
	    threads[i] = new WorkerThreads();
	    threads[i].start();
	}
    }

    private class WorkerThreads extends Thread 
    {
	public void run()
	{
	    ChatMessage newWork;
	    while(true)
	    {
		synchronized(worklist)
		{
		    while(worklist.isEmpty())
		    {
			try {
			    worklist.wait();
			}

			catch (InterruptedException i)
			{
			}
		    }
		    newWork = worklist.removeLast();	
		}

		final Socket connection = newWork.getConnection();
		try
		{
		    //  private void handle(final Socket connection) 
		    final BufferedReader xi
			= new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    final OutputStream xo = connection.getOutputStream();

		    final String request = xi.readLine();
		    System.out.println(Thread.currentThread() + ": " + request);

		    Matcher m;
		    if (PAGE_REQUEST.matcher(request).matches()) {
			sendResponse(xo, OK, HTML, CHAT_HTML);
		    }
		    else if ((m = PULL_REQUEST.matcher(request)).matches()) {
			final String room = m.group(1);
			final long last = Long.valueOf(m.group(2));
                        ChatState state;
                        synchronized (ChatServer.this.stateByName){
                            state = ChatServer.this.getState(room);
                        }
			sendResponse(xo, OK, TEXT, state.recentMessages(last));
		    }
		    else if ((m = PUSH_REQUEST.matcher(request)).matches()) {
			final String room = m.group(1);
			final String msg = m.group(2);
                        synchronized (ChatServer.this.stateByName){
			  ChatServer.this.getState("ALL").addMessage(room+"--"+msg);

                          if (room.equals("ALL")){
 		             Iterator hashIterator = stateByName.keySet().iterator();
                             while(hashIterator.hasNext())
                             {
                                String newroom = (String)hashIterator.next();
                                if (! newroom.equals ("ALL")){
			          ChatServer.this.getState(newroom).addMessage(room+"--"+msg);
                                }
                             }
                          } else {
			    ChatServer.this.getState(room).addMessage(msg);
                          }
                        }


			sendResponse(xo, OK, TEXT, "ack");
		    }
		    else {
			sendResponse(xo, NOT_FOUND, TEXT, "Nobody here with that name.");
		    }

		    connection.close();
		}
		catch (final Exception xx) {
		    xx.printStackTrace();
		    try {
			connection.close();
		    }
		    catch (final Exception yy) {
			// silently discard any exceptions here
		    }
		}

	    }	
	}
    }	

    /** Constructs a new <code>ChatServer</code> that will service requests on
     *  the specified <code>port</code>.  <code>state</code> will be used to
     *  hold the current state of the chat.
     */
    public ChatServer(final int port) throws IOException {
	this.port = port;
    }

    public void runForever() throws Exception {
	final ServerSocket server = new ServerSocket(port);
	WorkInit(8);
	while (true) {
	    final Socket connection = server.accept();
	    //handle(connection);
	    ChatMessage chatmsg = new ChatMessage (connection);
	    synchronized(worklist)
	    {
		worklist.addFirst(chatmsg);
		worklist.notifyAll();	
	    }		
	}
    }

/*    private void handle(final Socket connection) {
	try {
	    final BufferedReader xi
		= new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    final OutputStream xo = connection.getOutputStream();

	    final String request = xi.readLine();
	    System.out.println(Thread.currentThread() + ": " + request);

	    Matcher m;
	    if (PAGE_REQUEST.matcher(request).matches()) {
		sendResponse(xo, OK, HTML, CHAT_HTML);
	    }
	    else if ((m = PULL_REQUEST.matcher(request)).matches()) {
		final String room = m.group(1);
		final long last = Long.valueOf(m.group(2));
		sendResponse(xo, OK, TEXT, getState(room).recentMessages(last));
	    }
	    else if ((m = PUSH_REQUEST.matcher(request)).matches()) {
		final String room = m.group(1);
		final String msg = m.group(2);
		//Create a new chat message (constructor creates ID based on timestamp)
		//ChatMessage chatmsg = new ChatMessage(room,msg);
		//Add msg to worklist
		//synchronized (worklist) {
		//worklist.addFirst(chatmsg);
		//Notify
		//worklist.notify();
		//}

		//TODO need to remove this
		//getState(room).addMessage(msg);


		//sendResponse(xo, OK, TEXT, "ack");
	    }
	    else {
		sendResponse(xo, NOT_FOUND, TEXT, "Nobody here with that name.");
	    }

	    connection.close();
	}
	catch (final Exception xx) {
	    xx.printStackTrace();
	    try {
		connection.close();
	    }
	    catch (final Exception yy) {
		// silently discard any exceptions here
	    }
	}
    }
*/
    /** Writes a minimal-but-valid HTML response to <code>output</code>. */
    private static void sendResponse(final OutputStream output,
	    final String status,
	    final String contentType,
	    final String content) throws IOException {
	final byte[] data = content.getBytes(utf8);
	final String headers =
	    "HTTP/1.0 " + status + "\n" +
	    "Content-Type: " + contentType + "; charset=utf-8\n" +
	    "Content-Length: " + data.length + "\n\n";

	final BufferedOutputStream xo = new BufferedOutputStream(output);
	xo.write(headers.getBytes(utf8));
	xo.write(data);
	xo.flush();

	System.out.println(Thread.currentThread() + ": replied with " + data.length + " bytes");
    }

    /** Reads the resource with the specified name as a string, and then
     *  returns the string.  Resource files are searched for using the same
     *  classpath mechanism used for .class files, so they can either be in the
     *  same directory as bare .class files or included in the .jar file.
     */
    private static String getResourceAsString(final String name) throws IOException {
	final Reader xi = new InputStreamReader(
		ChatServer.class.getClassLoader().getResourceAsStream(name));
	try {
	    final StringBuffer result = new StringBuffer();
	    final char[] buf = new char[8192];
	    int n;
	    while ((n = xi.read(buf)) > 0) {
		result.append(buf, 0, n);
	    }
	    return result.toString();
	} finally {
	    try {
		xi.close();
	    } catch (final IOException xx) {
		// discard
	    }
	}
    }



    /** Runs a chat server, with a default port of 8080. */
    public static void main(final String[] args) throws Exception {
	final int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
	new ChatServer(port).runForever();
    }
}
