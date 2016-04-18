/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.net.InetSocketAddress;

/**
 * The class represents the Remote process ID. 
 * It is immutable 
 * @author vajih
 *
 */
public class RemoteProcess {
	final InetSocketAddress address;

	public RemoteProcess(InetSocketAddress address){
		this.address  = address;
	}
	
	public InetSocketAddress getAddress(){
		return address;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RemoteProcess other = (RemoteProcess) obj;
		if (address == null) {
			if (other.address != null) {
				return false;
			}
		} else if (!address.equals(other.address)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RemoteProcess [address=" + address + "]";
	}
	
	
	
}
