package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessRunner;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyProcess.Status;
import edu.uw.ece.alloy.util.Utils;

public class ProcessesManager {
    
    protected final static Logger logger = Logger.getLogger(ProcessesManager.class.getName() + "--" + Thread.currentThread().getName());
    
    public final static int MaxFeedThreashold = Integer.valueOf(Configuration.getProp("max_feed_treashold"));
    
    final int ProcessNumbers, newmem, newstack;
    final String jniPath, classPath;
    final InetSocketAddress watchdogAddress;
    
    // TODO change the key type to Process
    final ConcurrentHashMap<InetSocketAddress, AlloyProcess> processes = new ConcurrentHashMap<>();
    // This map stores how many messages are sent to an Alloy processor to be
    // processed. The value is used to stop sent many messages to an Alloy
    // processor.
    // If the value.a is 0, means that the processor is IDLE or INIATED
    // value.b stores all the messages sent
    final ConcurrentHashMap<InetSocketAddress, Pair<AtomicInteger, AtomicInteger>> sentMessagesCounter = new ConcurrentHashMap<>();
    
    // Keeps the number of inferred messages
    final ConcurrentHashMap<InetSocketAddress, AtomicInteger> inferredMessaged = new ConcurrentHashMap<>();
    
    final String processLoggerConfig;
    
    public ProcessesManager(int ProcessNumbers, String classPath, int newMem, int newStack, final String jniPath, final String processLogConfig) {
        this(ProcessNumbers, classPath, newMem, newStack, jniPath, ProcessorUtil.findEmptyLocalSocket(), processLogConfig);
    }
    
    public ProcessesManager(int ProcessNumbers, String classPath, int newMem, int newStack, final String jniPath, final InetSocketAddress watchdogAddress, final String processLogConfig) {
        this.ProcessNumbers = ProcessNumbers;
        this.jniPath = jniPath;
        this.newmem = newMem;
        this.newstack = newStack;
        this.watchdogAddress = watchdogAddress;
        System.out.println("this.watchdogAddress->" + this.watchdogAddress);
        this.processLoggerConfig = processLogConfig;
        if (classPath == null || classPath.length() == 0)
            this.classPath = System.getProperty("java.class.path");
        else
            this.classPath = classPath;
    }
    
    public InetSocketAddress getProcessRemoteMonitorAddress() {
        
        return watchdogAddress;
    }
    
    /**
     * The method is called by feeder to record how many message is sent so far
     * an Alloy Process. This method has to called whenever the message is sent
     * to the processor.
     * 
     * @param pId
     */
    public void recordAMessageSentCounter(InetSocketAddress pId) {
        
        if (!sentMessagesCounter.containsKey(pId)) {
            sentMessagesCounter.put(pId, new Pair<AtomicInteger, AtomicInteger>(new AtomicInteger(1), new AtomicInteger(1)));
        }
        else {
            sentMessagesCounter.get(pId).b.incrementAndGet();
            sentMessagesCounter.get(pId).a.incrementAndGet();
        }
    }
    
    /**
     * This function resets the number of the messages sent the given Alloy
     * processor in this shot. This method should be called whenever the process
     * becomes IDLE or INITIATED
     * 
     * @param pId
     */
    public void resetMessageCounter(InetSocketAddress pId) {
        
        if (!sentMessagesCounter.containsKey(pId)) {
            sentMessagesCounter.put(pId, new Pair<AtomicInteger, AtomicInteger>(new AtomicInteger(0), new AtomicInteger(0)));
        }
        else {
            sentMessagesCounter.get(pId).b.set(0);
        }
    }
    
    /**
     * This method decrements the number of sent messages showing they have
     * already received.
     * 
     * @param pId
     */
    public void decreaseMessageCounter(final InetSocketAddress pId) {
        
        if (!sentMessagesCounter.containsKey(pId)) {
            throw new RuntimeException("The message counter is not in the map.");
        }
        else {
            sentMessagesCounter.get(pId).b.decrementAndGet();
        }
    }
    
    public void increaseInferredMessageCounter(final InetSocketAddress pId) {
        
        if (!inferredMessaged.containsKey(pId)) {
            inferredMessaged.put(pId, new AtomicInteger(1));
        }
        else {
            inferredMessaged.get(pId).incrementAndGet();
        }
    }
    
    public synchronized AlloyProcess createProcess(final InetSocketAddress address) throws IOException {
        
        final Process sub;
        final String java = "java";
        final String debug = Boolean.parseBoolean(System.getProperty("debug")) ? "yes" : "no";
        
        try {
            sub = (jniPath != null && jniPath.length() > 0)
                    ? Utils.createProcess(
                            java,
                            "-Xmx" + newmem + "m",
                            "-Xss" + newstack + "k",
                            "-Djava.util.logging.config.file=" + processLoggerConfig,
                            "-Djava.library.path=" + jniPath,
                            "-Ddebug=" + debug,
                            "-cp",
                            classPath,
                            AlloyProcessRunner.class.getName(),
                            "" + address.getPort(),
                            "" + address.getAddress().getHostAddress(),
                            "" + watchdogAddress.getPort(),
                            "" + watchdogAddress.getAddress().getHostAddress())
                    : Utils.createProcess(
                            java,
                            "-Xmx" + newmem + "m",
                            "-Xss" + newstack + "k",
                            "-Ddebug=" + debug,
                            "-Djava.util.logging.config.file=" + processLoggerConfig,
                            "-cp",
                            classPath,
                            AlloyProcessRunner.class.getName(),
                            "" + address.getPort(),
                            "" + address.getAddress().getHostAddress(),
                            "" + watchdogAddress.getPort(),
                            "" + watchdogAddress.getAddress().getHostAddress());
                            
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, Utils.threadName() + "Not able to create a new process on port: " + address, e);
            throw e;
        }
        
        AlloyProcess result = new AlloyProcess(address, sub);
        return result;
    }
    
    /*
     * public synchronized AlloyProcess createProcessOld(final InetSocketAddress
     * address) throws IOException{
     * 
     * final Process sub; final String java = "java"; final String debug =
     * Boolean.parseBoolean(System.getProperty("debug")) ? "yes" : "no";
     * 
     * try { ProcessBuilder pb = (jniPath!=null && jniPath.length()>0) ? new
     * ProcessBuilder(java, "-Xmx" + newmem + "m", "-Xss" + newstack + "k",
     * "-Djava.util.logging.config.file=" + processLoggerConfig,
     * "-Djava.library.path=" + jniPath, "-Ddebug=" + debug, "-cp", classPath,
     * AlloyProcessRunner.class.getName(), ""+address.getPort(),
     * ""+address.getAddress().getHostAddress(), ""+watchdogAddress.getPort(),
     * ""+watchdogAddress.getAddress().getHostAddress())
     * 
     * 
     * : new ProcessBuilder(java, "-Xmx" + newmem + "m", "-Xss" + newstack +
     * "k", "-Ddebug=" + debug, "-Djava.util.logging.config.file=" +
     * processLoggerConfig, "-cp", classPath,
     * AlloyProcessRunner.class.getName(), ""+address.getPort(),
     * ""+address.getAddress().getHostAddress(), ""+watchdogAddress.getPort(),
     * ""+watchdogAddress.getAddress().getHostAddress());
     * 
     * 
     * pb.redirectOutput(Redirect.INHERIT); pb.redirectError(Redirect.INHERIT);
     * 
     * sub = pb.start();
     * 
     * } catch (IOException e) { logger.log(Level.SEVERE,Utils.threadName()+
     * "Not able to create a new process on port: "+address, e); throw e; }
     * 
     * AlloyProcess result = new AlloyProcess(address, sub); return result; }
     */
    
    /*
     * public void activateProess(final int pId) throws InterruptedException{
     * if(! processes.containsKey(pId)){
     * logger.log(Level.SEVERE,Utils.threadName()+
     * "Pid does not exits to be activated: "+pId); throw new RuntimeException(
     * "Pid does not exits to be activated: "+pId); }else
     * if(processes.get(pId).status != Status.INITIATED){
     * logger.log(Level.SEVERE,Utils.threadName()+
     * "Pid is not in Initial state and does not tobe activated: "+pId); }else{
     * RegisterCallback command = new RegisterCallback(watchdogAddress); try {
     * command.sendMe(processes.get(pId).address); //this look to be
     * inconsistent. The status has to go IDLE to be ready for changeStatus(pId,
     * Status.IDLE); } catch (InterruptedException e) {
     * logger.log(Level.SEVERE,Utils.threadName()+
     * "Command cannot be sent to activate: "+pId, e); throw e; } } }
     * 
     * public void activateAllProesses() { //Not need to be thread safe.
     * for(Integer pId: processes.keySet()){ try{ activateProess(pId);
     * }catch(InterruptedException e) {
     * logger.log(Level.SEVERE,Utils.threadName()+ "Cannot be  activated: "+pId,
     * e);
     * 
     * } } }
     */
    
    /*
     * public AlloyProcess createProcess(final int port) throws IOException{
     * return createProcess(new InetSocketAddress(port)); }
     */
    
    public void addProcess() throws IOException {
        
        InetSocketAddress port = ProcessorUtil.findEmptyLocalSocket();
        processes.putIfAbsent(port, createProcess(port));
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "A process at port '" + port + "' has been added to the process list " + processes);
    }
    
    /**
     * Not thread safe.
     */
    public void addAllProcesses() {
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "Starting to add processes");
        
        // No other thread can add a process to the map.
        synchronized (processes) {
            int i = processes.size();
            int maxAttempts = i + 100;
            while (i < maxAttempts) {
                
                if (processes.size() == ProcessNumbers)
                    break;
                if (i > ProcessNumbers)
                    throw new RuntimeException("Invalid state: i=" + i + " Should not be more than ProcessNumbers=" + ProcessNumbers);
                try {
                    addProcess();
                    ++i;
                    logger.warning(Utils.threadName() + "A process is added to the processes list:" + processes);
                }
                catch (IOException e) {
                    logger.severe(Utils.threadName() + "Processes cannot be created in setUpAllProcesses");
                }
                finally {
                    --maxAttempts;
                }
            }
            
            if (i != ProcessNumbers)
                throw new RuntimeException("Cannot create all processes: " + ProcessNumbers + " after " + maxAttempts + " attempts.");
        }
        
    }
    
    /**
     * Precondition. The process has to be in the Killing state
     * 
     * @param port
     */
    public boolean killProcess(final InetSocketAddress port) {
        
        System.out.println("KillProcess->" + port);
        boolean result = false;
        synchronized (processes) {
            if (!processes.containsKey(port)) {
                logger.warning(Utils.threadName() + "The process is not found: " + port);
            }
            else if (processes.get(port).status != AlloyProcess.Status.KILLING) {
                logger.warning(Utils.threadName() + "The process: " + port + " is not in the killing state and cannot be killed: " + processes.get(port).status);
            }
            else {
                logger.warning(Utils.threadName() + "Killing a process:" + port);
                synchronized (processes) {
                    logger.warning(Utils.threadName() + "Entering a lock for killing a process:" + port);
                    processes.get(port).process.destroyForcibly();
                    processes.remove(port);
                    logger.warning(Utils.threadName() + "A process:" + port + " is killed to the process list " + processes);
                }
                
                result = true;
            }
        }
        
        return result;
    }
    
    /**
     * Not thread safe Precondition. The process has to be in the Killing state
     * 
     * @param port
     */
    public void killAndReplaceProcess(final InetSocketAddress port) {
        
        // synchronized (processes) {
        if (killProcess(port)) {
            addAllProcesses();
        }
        // }
    }
    
    public AlloyProcess getRandomProcess() {
        
        synchronized (processes) {
            @SuppressWarnings("rawtypes")
            List<InetSocketAddress> randomArray = new ArrayList<InetSocketAddress>(processes.keySet());
            final int max = randomArray.size();
            final int randomIndex = (new Random()).nextInt(max);
            return processes.get(randomArray.get(randomIndex));
        }
    }
    
    /**
     * Return the most idle process.
     * 
     * @return
     */
    public AlloyProcess getIdlerProcess() {
        
        // TODO
        throw new RuntimeException("Unimplemented");
    }
    
    private boolean isAccepting(final AlloyProcess process) {
        
        if (!process.isActive())
            return false;
            
        if (process.status.equals(Status.WORKING) &&
            sentMessagesCounter.get(process.address/* getPort is eaual to ID */).b.intValue() > MaxFeedThreashold) {
            return false;
        }
        return true;
    }
    
    public AlloyProcess getActiveRandomeProcess() {
        
        AlloyProcess result;
        int retry = 10;
        int maxRetry = 10000;
        do {
            if (retry > maxRetry) {
                logger.log(Level.SEVERE, Utils.threadName() + "Not abale to find a random working process after atempting: " + retry);
                throw new RuntimeException("Not working process was found.");
            }
            result = getRandomProcess();
            ++retry;
            try {
                Thread.sleep(retry);
            }
            catch (InterruptedException e) {
                logger.log(Level.SEVERE, Utils.threadName() + "Interrupted while waiting for an active process. ");
            }
            
        } while (!isAccepting(result));
        
        return result;
    }
    
    public void changeStatus(final InetSocketAddress pId, Status status) {
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + "Changing the status of PID:" + pId + " to: " + status);
        synchronized (processes) {
            if (processes.containsKey(pId)) {
                processes.replace(pId, processes.get(pId).changeStatus(status));
                if (Configuration.IsInDeubbungMode)
                    logger.info(Utils.threadName() + "The status is chanaged PID:" + pId + " to: " + status);
            }
            else {
                logger.log(Level.SEVERE, Utils.threadName() + "The process is not found to be changed its status. ");
                throw new RuntimeException("The process is not found: " + pId);
            }
        }
    }
    
    private void changeNumber(final InetSocketAddress pId, AlloyProcess newProcess) {
        
        if (processes.containsKey(pId)) {
            processes.replace(pId, newProcess);
        }
        else {
            throw new RuntimeException("The process is not found: " + pId);
        }
    }
    
    public void changeSentTasks(final InetSocketAddress pId, int sentTasks) {
        
        synchronized (processes) {
            changeNumber(pId, processes.get(pId).changeSentTasks(sentTasks));
        }
    }
    
    public void IncreaseSentTasks(final InetSocketAddress pId, int sentTasks) {
        
        synchronized (processes) {
            if (!processes.containsKey(pId)) {
                if (Configuration.IsInDeubbungMode)
                    logger.info(Utils.threadName() + "The process is deleted whiling sending a message to it. PID:" + pId);                    
            }
            else {
                changeNumber(pId, processes.get(pId).changeSentTasks(processes.get(pId).sentTasks + sentTasks));
            }
        }
    }
    
    public void changeDoingTasks(final InetSocketAddress pId, int doingTasks) {
        
        synchronized (processes) {
            changeNumber(pId, processes.get(pId).changeDoingTasks(doingTasks));
        }
    }
    
    public void IncreaseDoingTasks(final InetSocketAddress pId, int doingTasks) {
        
        synchronized (processes) {
            changeNumber(pId, processes.get(pId).changeDoneTasks(processes.get(pId).doingTasks + doingTasks));
        }
    }
    
    public void changeDoneTasks(final InetSocketAddress pId, int doneTasks) {
        
        synchronized (processes) {
            changeNumber(pId, processes.get(pId).changeDoneTasks(doneTasks));
        }
    }
    
    public void IncreaseDoneTasks(final InetSocketAddress pId, int doneTasks) {
        
        synchronized (processes) {
            changeNumber(pId, processes.get(pId).changeDoneTasks(processes.get(pId).doneTasks + doneTasks));
        }
    }
    
    public void changeLastLiveTimeReported(final InetSocketAddress pId, long lastLiveTimeReported) {
        
        synchronized (processes) {
            changeNumber(pId, processes.get(pId).changeLastLiveTimeReported(lastLiveTimeReported));
        }
    }
    
    public void changeLastLiveTimeRecieved(final InetSocketAddress pId, long lastLiveTimeRecieved) {
        
        synchronized (processes) {
            changeNumber(pId, processes.get(pId).changeLastLiveTimeRecieved(lastLiveTimeRecieved));
        }
    }
    
    public AlloyProcess getAlloyProcess(final InetSocketAddress pId) {
        
        if (Configuration.IsInDeubbungMode)
            logger.info(Utils.threadName() + " The Pid: " + pId + " is in ?" + Arrays.asList(processes.keySet()));
        return processes.get(pId);
    }
    
    public Set<InetSocketAddress> getAllRegisteredPIDs() {
        
        return processes.keySet();
    }
    
    /**
     * Find which processors are timed out.
     * 
     * @param threshold
     *            in milliseconds
     * @return
     */
    public List<AlloyProcess> getTimedoutProcess(int threshold) {
        
        List<AlloyProcess> result = Collections.synchronizedList(new LinkedList<>());
        // No need to synchronized. The time is loose.
        for (AlloyProcess ap : processes.values()) {
            if (System.currentTimeMillis() - Math.max(ap.lastLiveTimeReported, ap.lastLiveTimeRecieved) > threshold)
                result.add(ap);
        }
        return Collections.unmodifiableList(result);
    }
    
    public void finalize() {
        
        for (final InetSocketAddress port : processes.keySet())
            killProcess(port);
    }
    
    public Set<InetSocketAddress> getLiveProcessIDs() {
        
        return Collections.unmodifiableSet(processes.keySet());
        
    }
    
    public boolean allProcessesNotWorking() {
        
        synchronized (processes) {
            for (InetSocketAddress pId : processes.keySet()) {
                System.out.println("pid=" + pId + "," + processes.get(pId).status);
                if (processes.get(pId).status.equals(Status.WORKING)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public boolean allProcessesWorking() {
        
        synchronized (processes) {
            for (InetSocketAddress pId : processes.keySet()) {
                System.out.println("pid=" + pId + "," + processes.get(pId).status);
                if (!processes.get(pId).status.equals(Status.WORKING)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public boolean SomeProcessesWorking() {
        
        synchronized (processes) {
            for (InetSocketAddress pId : processes.keySet()) {
                System.out.println("pid=" + pId + "," + processes.get(pId).status);
                if (processes.get(pId).status.equals(Status.WORKING)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    public String getStatus() {
        
        allProcessesNotWorking();// remove it
        final StringBuilder result = new StringBuilder();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        int waiting = 0, done = 0, doneForPId = 0, waitingForPId = 0;
        for (InetSocketAddress pId : processes.keySet()) {
            doneForPId = processes.get(pId).doneTasks;
            done += doneForPId;
            result.append("Current reported Done Meessages for PID=<" + pId + "> is:" + done).append("\n");
            waitingForPId = processes.get(pId).doingTasks;
            waiting += waitingForPId;
            result.append("Current reported Doing Meessages for PID=<" + pId + "> is:" + waitingForPId).append("\n");
            result.append("Current last message was recieved from PID=<" + pId + "> was at:" + sdf.format(processes.get(pId).lastLiveTimeRecieved)).append("\n");
            result.append("Current last message was reported from PID=<" + pId + "> was at:" + sdf.format(processes.get(pId).lastLiveTimeReported)).append("\n");
            result.append("Current reported Sent Meessages for PID=<" + pId + "> is:" + processes.get(pId).sentTasks).append("\n");
            
        }
        
        result.append("The current total waiting: ").append(waiting).append("\n").append("The current total Done: ").append(done).append("\n");
        done = 0;
        waiting = 0;
        for (InetSocketAddress pId : sentMessagesCounter.keySet()) {
            doneForPId = sentMessagesCounter.get(pId).a.intValue();
            done += doneForPId;
            result.append("Total sent Meessages for PID=<" + pId + "> is:").append(doneForPId).append("\n");
            
            waitingForPId = sentMessagesCounter.get(pId).b.intValue();
            waiting += waitingForPId;
            result.append("Send Meessages for PID=<" + pId + "> is:").append(waitingForPId).append("\n");
            System.out.println("pId->" + pId + "   " + inferredMessaged);
            result.append("Inferred Meessages for PID=<" + pId + "> is:").append(inferredMessaged.containsKey(pId) ? inferredMessaged.get(pId).get() : "{}").append("\n");
            
        }
        
        result.append("Total messages are sent: ").append(done).append("\n").append("Message are waiting now: ").append(waiting).append("\n");
        
        return result.toString();
    }
    
}
