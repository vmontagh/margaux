package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.FilenameFilter;

import edu.mit.csail.sdg.gen.alloy.Configuration;

/**
 * A simple class that poll the files in the output directory to see the rate of processing 
 * @author vajih
 *
 */
public class ProgressReporter {

	final static File path = new File(Configuration.getProp("log_out_directory"));
	final static int refreshRate = Integer.valueOf(Configuration.getProp("reporter_refresh_rate"));
	
	private File[] findAllFiles(String pattern){
		FilenameFilter textFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(pattern)) {
					return true;
				} else {
					return false;
				}
			}
		};
		
		return path.listFiles(textFilter);
	}
	
	public int filesCount(){
		return findAllFiles(".als.out.txt").length;
	}
	
	public ProgressReporter() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws InterruptedException {

		ProgressReporter pr = new ProgressReporter();
		
		int oldC = pr.filesCount();
		long deltaT = System.currentTimeMillis();
		long rate = 0;
		long totalRate = 0;
		long count = 0;
		
		while(true){
			
			int newC = pr.filesCount();
			deltaT =  System.currentTimeMillis() - deltaT;
			rate = (newC - oldC)*1000/deltaT;
			
			if(newC == 0 )
				count = 0;
			else
				count++;
			if(newC > 0){
				totalRate += rate;
			}else{
				totalRate = 0;
			}
			
			System.out.printf("Rate,%s,Total,%s,TotalRate,%s\n", rate, newC, count!=0? totalRate/count:0 );
			
			deltaT = System.currentTimeMillis();
			oldC = newC ;
			

			
			Thread.sleep(refreshRate);
		}
		
	}

}
