package edu.uw.ece.alloy.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AlloyProcessed;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.IamAlive;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessReady;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.events.*;

public abstract class ServerSocketInterface implements Runnable, ThreadToBeMonitored {
    
    protected final static Logger logger = Logger.getLogger(ServerSocketInterface.class.getName() + "--" + Thread.currentThread().getName());
    
    protected final Thread listeningThread;
    protected final InetSocketAddress hostAddress;
    protected final InetSocketAddress remoteAddress;
    protected final BlockingQueue<RemoteCommand> queue;
    
    protected volatile AtomicInteger livenessFailed = new AtomicInteger(0);
    
    private int maxRetryAttempts;
    
    public ServerSocketInterface(final int hostPort, final int remotePort) {
        this(new InetSocketAddress(hostPort), new InetSocketAddress(remotePort));
    }
    
    public ServerSocketInterface(final String hostName, final int hostPort, final String remoteName, final int remotePort) {
        this(new InetSocketAddress(hostName, hostPort), new InetSocketAddress(remoteName, remotePort));
    }
    
    public ServerSocketInterface(final InetSocketAddress hostAddress, final InetSocketAddress remoteAddress) {
        this.hostAddress = hostAddress;
        this.remoteAddress = remoteAddress;
        this.listeningThread = new Thread(this);
        this.queue = new LinkedBlockingQueue<>();
        this.maxRetryAttempts = 1;
        
        this.ListeningStarted = new Event<>();
        this.CommandSent = new Event<>();
        this.CommandAttempt = new Event<>();
        this.CommandFailed = new Event<>();
        this.CommandReceived = new Event<>();
    }
    
    public final Event<EventArgs> ListeningStarted;
    public final Event<CommandSentEventArgs> CommandSent;
    public final Event<CommandSentEventArgs> CommandAttempt;
    public final Event<CommandSentEventArgs> CommandFailed;
    public final Event<CommandReceivedEventArgs> CommandReceived;
    
    public InetSocketAddress getHostAddress() {
        
        return this.hostAddress;
    }
    
    public InetSocketAddress getRemoteAddress() {
        
        return this.remoteAddress;
    }
    
    public int getLivenessFailed() {
        
        return this.livenessFailed.get();
    }
    
    public void setMaxRetryAttempts(int maxRetryAttempts) {
        
        this.maxRetryAttempts = maxRetryAttempts;
    }
    
    public void startThread() {
        
        getThread().start();
    }
    
    @Override
    public void run() {
        
        startListening();
    }
    
    @Override
    public void cancelThread() {
        
        this.getThread().interrupt();
    }
    
    @Override
    public void actionOnNotStuck() {
        
        this.sendLivenessMessage();
        this.haltIfCantProceed(this.maxRetryAttempts);
    }
    
    public void haltIfCantProceed(final int maxRetryAttepmpts) {
        
        // Recovery was not enough, the whole processes has to be shut-down
        if (livenessFailed.get() > maxRetryAttepmpts) {
            logger.severe(Utils.threadName() + livenessFailed + " liveness message attempts does not prceeed, So the process is exited.");
            Runtime.getRuntime().halt(0);
        }
    }
    
    public void sendMessage(RemoteCommand command) {
        
        this.sendMessage(command, this.getRemoteAddress());
    }
    
    public void sendMessage(RemoteCommand command, InetSocketAddress address) {
        
        if (command == null) {
            return;
        }
        
        address = address == null ? this.getRemoteAddress() : address;
        CommandSentEventArgs eventArgs = new CommandSentEventArgs(command, address);
        try {
            
            this.onCommandAttempt(eventArgs);
            command.sendMe(address);
            
            this.onCommandSent(eventArgs);
        }
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, Utils.threadName() + "Failed to send command: " + command, e);
            this.onCommandFailed(eventArgs);
        }
        
    }
    
    public void sendLivenessMessage() {
        
        this.sendLivenessMessage(-1, -1);
    }
    
    public void sendLivenessMessage(int processed, int toBeProcessed) {
        
        try {
            
            IamAlive iamAlive = new IamAlive(this.getHostAddress(), System.currentTimeMillis(), processed, toBeProcessed);
            iamAlive.sendMe(this.getRemoteAddress());
            livenessFailed.set(0);
            
            if (Configuration.IsInDeubbungMode)
                logger.info(Utils.threadName() + "A live message" + iamAlive);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, Utils.threadName() + "Failed to send a live signal. " + livenessFailed + " attempts made.", e);
            livenessFailed.incrementAndGet();
        }
    }
    
    public void sendReadynessMessage() {
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "Sending a readyness message pId: " + hostAddress.getPort());
            
        ProcessReady command = new ProcessReady(hostAddress);
        try {
            command.sendMe(remoteAddress);
        }
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, Utils.threadName() + "Failed to send Ready signal.", e);
        }
    }
    
    protected final Thread getThread() {
        
        return this.listeningThread;
    }
    
    protected final void startListening() {
        
        AsynchronousServerSocketChannel serverSocketChannel = null;
        
        try {
            if (Configuration.IsInDeubbungMode) {
                logger.info(Utils.threadName() + "Starting listening fornt runner for pId: " + hostAddress.getPort());
            }
            
            // Open socket for listening
            serverSocketChannel = AsynchronousServerSocketChannel.open().bind(hostAddress);
            
            this.onListeningStarted();
            
            Future<AsynchronousSocketChannel> serverFuture = null;
            AsynchronousSocketChannel clientSocket = null;
            InputStream connectionInputStream = null;
            ObjectInputStream ois = null;
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    
                    if (Configuration.IsInDeubbungMode) {
                        logger.info(Utils.threadName() + "waiting for a request: " + hostAddress.getPort());
                    }
                    
                    serverFuture = serverSocketChannel.accept();
                    
                    if (Configuration.IsInDeubbungMode) {
                        logger.info(Utils.threadName() + "a message is received: " + hostAddress.getPort());
                    }
                    
                    clientSocket = serverFuture.get();
                    
                    if ((clientSocket != null) && (clientSocket.isOpen())) {
                        
                        connectionInputStream = Channels.newInputStream(clientSocket);
                        ois = new ObjectInputStream(connectionInputStream);
                        onReceivedMessage((RemoteCommand) ois.readObject());
                    }
                    
                }
                catch (IOException | InterruptedException | ExecutionException | ClassNotFoundException e) {
                    logger.log(Level.SEVERE, Utils.threadName() + "Error while listening for request: ", e);
                }
                finally {
                    
                    if (ois != null) {
                        try {
                            ois.close();
                        }
                        catch (IOException e) {
                            logger.log(Level.SEVERE, Utils.threadName() + "Error while closing InputOutputstream: ", e);
                        }
                    }
                    
                    if (connectionInputStream != null) {
                        try {
                            connectionInputStream.close();
                        }
                        catch (IOException e) {
                            logger.log(Level.SEVERE, Utils.threadName() + "Error while closing Connection Inputputstream: ", e);
                        }
                    }
                    
                    if (clientSocket != null && clientSocket.isOpen()) {
                        try {
                            clientSocket.close();
                        }
                        catch (IOException e) {
                            logger.log(Level.SEVERE, Utils.threadName() + "Error while closing Client socket: ", e);
                        }
                    }
                }
            }
            
        }
        catch (Throwable t) {
            logger.log(Level.SEVERE, Utils.threadName() + "A serious error breaks the Front Processor listener: ", t);
        }
        finally {
            
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                try {
                    serverSocketChannel.close();
                }
                catch (IOException e) {
                    logger.log(Level.SEVERE, Utils.threadName() + "Error while closing AsynchronousServerSocketChannel socket: ", e);
                }
            }
        }
        
    }
    
    protected void onReceivedMessage(final RemoteCommand command) {
        
        // Supposed to be a registering call;
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "Recieved message: " + command);
            
    }
    
    protected void onListeningStarted() {
        
        Event<EventArgs> event = this.ListeningStarted;
        if (event.hasListeners()) {
            event.invokeListeners(this, EventArgs.empty());
        }
    }
    
    protected void onCommandSent(CommandSentEventArgs e) {
        
        Event<CommandSentEventArgs> event = this.CommandSent;
        if (event.hasListeners()) {
            event.invokeListeners(this, e);
        }
    }
    
    protected void onCommandAttempt(CommandSentEventArgs e) {
        
        Event<CommandSentEventArgs> event = this.CommandAttempt;
        if (event.hasListeners()) {
            event.invokeListeners(this, e);
        }
    }
    
    protected void onCommandFailed(CommandSentEventArgs e) {
        
        Event<CommandSentEventArgs> event = this.CommandFailed;
        if (event.hasListeners()) {
            event.invokeListeners(this, e);
        }
    }
    
    protected void onCommandReceived(CommandReceivedEventArgs e) {
        
        Event<CommandReceivedEventArgs> event = this.CommandReceived;
        if (event.hasListeners()) {
            event.invokeListeners(this, e);
        }
    }
}
