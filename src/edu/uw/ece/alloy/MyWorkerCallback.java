package edu.uw.ece.alloy;

import edu.mit.csail.sdg.alloy4.WorkerEngine.WorkerCallback;

public class MyWorkerCallback implements WorkerCallback {

	public final String fileName;
	
	public MyWorkerCallback(String fileName){
		this.fileName = fileName;
	}
	
	public void callback(Object msg) {
		// TODO Auto-generated method stub
		System.out.println("a callBack:"+msg);
	}

	public void done() {
		// TODO Auto-generated method stub
		System.out.println("done");
	}

	public void fail() {
		// TODO Auto-generated method stub
		System.out.println("Failed");
	}

}
