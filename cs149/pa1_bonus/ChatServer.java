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
import java.util.Collection;
import java.util.Iterator;
import java.util.*;
import java.util.concurrent.*;
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
    private AllThread athread;

    private ChatState getState(final String room) {
	ChatState state = stateByName.get(room);
	if (state == null) {
	    state = new ChatState(room);
	    stateByName.put(room, state);
	}
	return state;
    }
/*
    class Semaphore {
	private int count;
	public Semaphore (int n) {
		this.count = n;
	}
        public void reinit(int n) {
             this.count = n;
        }
	public synchronized void decrement () {
		count = -8;
		notifyAll();
		try {
			wait();	
		} catch (InterruptedException e)
		{}
	
	}
	public synchronized void increment() {
		while (count <= 0)
		{
			count++;
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
			
	}
    }	
*/		
    private Semaphore waitForAllSema;	
    private Semaphore waitOnAllSema;	

    public void WorkInit (int numThreads)
    {
	this.numThreads = numThreads;
	threads = new WorkerThreads[numThreads];
	athread = new AllThread();
        waitForAllSema = new Semaphore (0,true);
        waitOnAllSema = new Semaphore (0,true);
	for (int i=0; i<numThreads; i++)
	{
	    threads[i] = new WorkerThreads();
	    threads[i].start();
	}
	athread.start();
    }

    private class AllThread extends Thread
    {
       public void run()
       {
	while(true)
        {
       		ChatMessage newWork;
		synchronized (worklist)
		{
			if (worklist.isEmpty())
			{
				System.out.println("\nERROR: All thread invoked but no all-work job! \n");
				break;
			}
			else
			{
				newWork = worklist.removeFirst();
			}
		}
		
		final Socket connection = newWork.getConnection();
	        try
		{
		    //  private void handle(final Socket connection) 
		    final BufferedReader xi
			= new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    final OutputStream xo = connection.getOutputStream();
		    String allroom = "all";
		    final String request = xi.readLine();
		    System.out.println(Thread.currentThread() + ": " + request);

		    Matcher m;
		    if (PAGE_REQUEST.matcher(request).matches()) {
		        System.out.println("\nERROR: Chat Html request in all thread\n");	
			sendResponse(xo, OK, HTML, CHAT_HTML);
		    }
		    else if ((m = PULL_REQUEST.matcher(request)).matches()) {
			final String room = m.group(1);
			final long last = Long.valueOf(m.group(2));
			if (room.equals("all"))	
			{
			 //sendResponse(xo, OK, TEXT, ChatServer.this.getState(room).recentMessages(last));
			  sendResponse(xo, OK, TEXT, "");
			}
			else
			{
			 System.out.println("\nERROR: All thread but room not equal to all\n");
                         waitOnAllSema.release(numThreads);
                         waitForAllSema.acquire(numThreads);
			 //allsema.increment();
			}
		    }
		    else if ((m = PUSH_REQUEST.matcher(request)).matches()) {
			final String room = m.group(1);
			final String msg = m.group(2);
			
			//ChatServer.this.getState(room).addMessage(msg);
			Iterator hashIterator = stateByName.keySet().iterator();
			while(hashIterator.hasNext())
			{
				String roomname = (String) hashIterator.next();
				ChatServer.this.getState(roomname).addMessage(msg);
			}			
			if (room.equals("all"))
			{	
				sendResponse(xo, OK, TEXT, "ack");
			}
			else
			{
				System.out.println("\nERROR: All thread but room not equal to all\n");

                         waitOnAllSema.release(numThreads);
                         waitForAllSema.acquire(numThreads);
				//allsema.decrement();
			}
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
		//allsema.decrement();
         }
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
		    newWork = worklist.removeFirst();	
		}

		final Socket connection = newWork.getConnection();
		try
		{
		    //  private void handle(final Socket connection) 
		    final BufferedReader xi
			= new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    final OutputStream xo = connection.getOutputStream();
		    String allroom = "all";
		    final String request = xi.readLine();
		    System.out.println(Thread.currentThread() + ": " + request);

		    Matcher m;
		    if (PAGE_REQUEST.matcher(request).matches()) {
			sendResponse(xo, OK, HTML, CHAT_HTML);
		    }
		    else if ((m = PULL_REQUEST.matcher(request)).matches()) {
			final String room = m.group(1);
			final long last = Long.valueOf(m.group(2));
			if (room.equals(allroom))	
			{
                               waitForAllSema.release(1);
                               waitOnAllSema.acquire(1);
			}
			else
			{
				sendResponse(xo, OK, TEXT, ChatServer.this.getState(room).recentMessages(last));
			}
		    }
		    else if ((m = PUSH_REQUEST.matcher(request)).matches()) {
			final String room = m.group(1);
			final String msg = m.group(2);

			if (room.equals(allroom))
			{
                               waitForAllSema.release(1);
                               waitOnAllSema.acquire(1);
                        }
			else
                        {
				ChatServer.this.getState(room).addMessage(msg);
				sendResponse(xo, OK, TEXT, "ack");
			}
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
        //allsema = new Semaphore(-7);
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
		System.out.println("\nI am facing a problem here!\n");
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
