package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.LinkedList;
import java.util.List;

public abstract class AlloyCodeBuilder {

	final List<PropertyToAlloyCode> PropertyToAlloyCodeObjects;

	public AlloyCodeBuilder() {
		PropertyToAlloyCodeObjects = new LinkedList<PropertyToAlloyCode>();
	}

	public AlloyCodeBuilder(final List<PropertyToAlloyCode> PropertyToAlloyCodeObjects) {
		this();
		for(final PropertyToAlloyCode prop: PropertyToAlloyCodeObjects){
			this.PropertyToAlloyCodeObjects.add(prop.createItself());
		}
	}
	
	public void registerPropertyToAlloyCode(PropertyToAlloyCode propertyToAlloyCode){
		PropertyToAlloyCodeObjects.add(propertyToAlloyCode);
	}
	
}
