package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PropertyToAlloyCodeBuilder extends AlloyCodeBuilder {

	final List<Dependency> dependencies;
	String header, scope;
	/*final AlloyProcessingParam paramCreator;*/
	// [tmpDirectory]final File tmpDirectory;

	public PropertyToAlloyCodeBuilder(List<Dependency> dependencies,
			String header, String scope/*, AlloyProcessingParam paramCreator*/
	// ,[tmpDirectory]File tmpDirectory
	) {
		super();
		this.dependencies = dependencies;
		this.header = header;
		this.scope = scope;
		/*this.paramCreator = paramCreator;*/
		// [tmpDirectory]this.tmpDirectory = tmpDirectory;
	}

	public List<PropertyToAlloyCode> createObjects(final String predBodyA,
			final String predBodyB, final String predCallA, final String predCallB,
			final String predNameA, final String predNameB// [tmpDirectory], final
																										// File tmpDirectory
	, final String field) {
		final List<PropertyToAlloyCode> result = new LinkedList<>();

		for (final PropertyToAlloyCode propertyToAlloyCode : PropertyToAlloyCodeObjects) {
			result.add(propertyToAlloyCode.createIt(predBodyA, predBodyB, predCallA,
					predCallB, predNameA, predNameB, dependencies, /*paramCreator,*/ header,
					scope, field));
		}

		return Collections.unmodifiableList(result);
	}

	public PropertyToAlloyCode createReverse(
			final PropertyToAlloyCode propertyToAlloyCode) {
		return propertyToAlloyCode.createIt(propertyToAlloyCode.predBodyB,
				propertyToAlloyCode.predBodyA, propertyToAlloyCode.predCallB,
				propertyToAlloyCode.predCallA, propertyToAlloyCode.predNameB,
				propertyToAlloyCode.predNameA, dependencies, /*paramCreator,*/ header,
				scope, propertyToAlloyCode.field// [tmpDirectory],
																				// propertyToAlloyCode.tmpDirectory
		);
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getHeader() {
		return this.header;
	}

}
