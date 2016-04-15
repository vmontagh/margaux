package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.TemporalPropertiesGenerator;
import edu.uw.ece.hola.agent.Utils;

public class TemporalAnalyzerRunner extends DistributedRunner {
    
    protected final static Logger logger = Logger.getLogger(TemporalAnalyzerRunner.class.getName() + "--" + Thread.currentThread().getName());
    
    private final static DistributedRunner self = new TemporalAnalyzerRunner();

    private TemporalPropertiesGenerator propGenerator;
    
    private TemporalAnalyzerRunner() {
        super(null, null);
    }
    
    public final static DistributedRunner getInstance() {
        
        return self;
    }
    
    /* (non-Javadoc)
     * @see edu.uw.ece.alloy.debugger.propgen.benchmarker.center.DistributedRunner#start()
     */
    @Override
    public void start() throws Exception {
        
        // TODO Auto-generated method stub
        super.start();
        this.propGenerator = new TemporalPropertiesGenerator(this.feeder);
        this.addThreadToBeMonitored(this.propGenerator);
        this.propGenerator.startThread();
    }
    
    public static void main(String[] args) throws Exception {
        
        TemporalAnalyzerRunner.getInstance().start();
        
        // busy wait
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        final StringBuilder sb = new StringBuilder();
        while (true) {
            Thread.sleep(20000);
            sb.append(Utils.threadName() + "Main is alive....\n");
            TemporalAnalyzerRunner.getInstance().monitoredThreads.stream().forEach(m -> sb.append(m.getStatus()).append("\n"));
            
            System.out.println(sb);
            logger.warning(sb.toString());
            sb.delete(0, sb.length() - 1);
            
            Thread.currentThread().yield();
            System.gc();
        }
    }
    
}
