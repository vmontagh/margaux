package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Pair;

public class PropertyToAlloyCodeBuilder extends AlloyCodeBuilder {

	final List<Pair<File, String>> dependencies;
	final String header, scope; 
	final AlloyProcessingParam paramCreator;
	
	final File tmpDirectory;
	
	public PropertyToAlloyCodeBuilder(List<Pair<File, String>> dependencies,
			String header, String scope, AlloyProcessingParam paramCreator,
			File tmpDirectory) {
		super();
		this.dependencies = dependencies;
		this.header = header;
		this.scope = scope;
		this.paramCreator = paramCreator; 
		this.tmpDirectory = tmpDirectory;
	}

	

	public List<PropertyToAlloyCode> createObjects(final String predBodyA, final String predBodyB, 
			final String predCallA, final String predCallB, 
			final String predNameA, final String predNameB, final File tmpDirectory){
		final List<PropertyToAlloyCode> result =  new LinkedList<>();
		
		for(final PropertyToAlloyCode propertyToAlloyCode: PropertyToAlloyCodeObjects){
			result.add( propertyToAlloyCode.createIt(predBodyA, predBodyB, 
					predCallA, predCallB, 
					predNameA, predNameB, 
					dependencies, paramCreator, header, scope, tmpDirectory
					) );
		}
		
		return Collections.unmodifiableList(result);
	}
	
	public PropertyToAlloyCode createReverse(final PropertyToAlloyCode propertyToAlloyCode){
		return propertyToAlloyCode.createIt(propertyToAlloyCode.predBodyB, propertyToAlloyCode.predBodyA, propertyToAlloyCode.predCallB, propertyToAlloyCode.predCallA, propertyToAlloyCode.predNameB, propertyToAlloyCode.predNameA, dependencies, paramCreator, header, scope, propertyToAlloyCode.tmpDirectory);
	}
	
}
