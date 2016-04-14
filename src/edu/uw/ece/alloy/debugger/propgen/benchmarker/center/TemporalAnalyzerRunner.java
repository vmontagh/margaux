package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.util.logging.Logger;

public class TemporalAnalyzerRunner extends AnalyzerRunner {

	protected final static Logger logger = Logger.getLogger(TemporalAnalyzerRunner.class.getName()+"--"+Thread.currentThread().getName());

	private final static AnalyzerRunner self = new TemporalAnalyzerRunner();

	private TemporalAnalyzerRunner() {
		super(null, null);
	}

	public final static AnalyzerRunner getInstance() {
		return self;
	}

	public static void main(String[] args) throws Exception {


		TemporalAnalyzerRunner.getInstance().start();		

		//busywait
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		final StringBuilder sb = new StringBuilder();
		while(true){
			Thread.sleep(20000);
			sb.append("["+Thread.currentThread().getName()+"]" + "Main is alive....\n");
			TemporalAnalyzerRunner.getInstance().monitoredThreads.stream().forEach(m->sb.append(m.getStatus()).append("\n"));

			System.out.println(sb);
			logger.warning(sb.toString());
			sb.delete(0, sb.length()-1);

			Thread.currentThread().yield();
			System.gc();
		}
	}

}
