package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.knowledgebase.BinaryImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.TernaryImplicationLattic;

public class PropertyToAlloyCode implements Serializable {

	private static final long serialVersionUID = -7891570520910464309L;

	final public static PropertyToAlloyCode EMPTY_CONVERTOR = new PropertyToAlloyCode();
	final public static String COMMAND_BLOCK_NAME = "check_or_run_assert_or_preicate_name";

	// Sources are generated here.

	// template names
	final static String Scope = "for 5";

	final public String predBodyA, predBodyB, predCallA, predCallB, predNameA,
			predNameB, header, scope, field;

	public final List<Dependency> dependencies;

	final transient List<ImplicationLattic> implications;

	protected PropertyToAlloyCode(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, // AlloyProcessingParam paramCreator,
			String header, String scope,
			String field) {
		super();
		this.predBodyA = predBodyA;
		this.predBodyB = predBodyB;
		this.predCallA = predCallA;
		this.predCallB = predCallB;
		this.predNameA = predNameA;
		this.predNameB = predNameB;
		this.dependencies = new LinkedList<Dependency>();
		dependencies.forEach(p -> this.dependencies.add(p.createItsef()));
		this.header = header;
		this.scope = scope;
		this.field = field;
		implications = new LinkedList<>();
		/* The BinaryImplicationLattic and TernaryImplicationLAttice are not
		* connected to the given relational and temporal patterns stored in a request message
		*/
		implications.add(new BinaryImplicationLattic());
		implications.add(new TernaryImplicationLattic());
	}

	public PropertyToAlloyCode() {
		this(Compressor.EMPTY_STRING, Compressor.EMPTY_STRING,
				Compressor.EMPTY_STRING, Compressor.EMPTY_STRING,
				Compressor.EMPTY_STRING, Compressor.EMPTY_STRING, Compressor.EMPTY_LIST,
				Compressor.EMPTY_STRING, Compressor.EMPTY_STRING,
				Compressor.EMPTY_STRING);
	}

	public PropertyToAlloyCode createIt(String predBodyA, String predBodyB,
			String predCallA, String predCallB, String predNameA, String predNameB,
			List<Dependency> dependencies, // AlloyProcessingParam paramCreator,
			String header, String scope, String field) {
		throw new RuntimeException("Invalid call!");
	}

	public PropertyToAlloyCode createItself() {
		PropertyToAlloyCode result = createIt(this.predBodyA, this.predBodyB,
				this.predCallA, this.predCallB, this.predNameA, this.predNameB,
				this.dependencies, this.header, this.scope, this.field);
		return result;
	}

	protected String generateAlloyCode() {

		String source = "";

		source += generatePrepend();
		source += '\n' + generatePredicateBody(predBodyA);
		source += '\n' + generatePredicateBody(predBodyB);
		source += '\n' + commandStatement(predCallA, predCallB);
		source += " " + scope;

		return source;
	}

	protected String generatePrepend() {

		return header;
	}

	protected String generatePredicateBody(final String preProcessedBody) {
		// No process Now.
		return preProcessedBody;
	}

	String commandKeyword() {
		throw new RuntimeException("Invalid call!");
	}

	String commandKeyWordBody() {
		// It could be 'assert' or 'pred'
		throw new RuntimeException("Invalid call!");
	}

	public String commandOperator() {
		throw new RuntimeException("Invalid call!");
	}

	String commandStatement(final String predCallA, final String predCallB) {

		final String block = commandKeyWordBody() + " " + COMMAND_BLOCK_NAME
				+ " {\n" + " (" + predCallA + " " + commandOperator() + " " + predCallB
				+ ")" + "\n}\n";

		return block + commandKeyword() + " " + COMMAND_BLOCK_NAME;
	}

	public String srcPath() {
		return srcName();
	}

	public String destPath() {
		return destName();
	}

	public String srcName() {
		return predNameA + srcNameOperator() + predNameB + "_" + field + ".als";
	}

	public String srcNameOperator() {
		throw new RuntimeException("Invalid call!");
	}

	public String destName() {
		return srcName() + ".out.txt";
	}

	public boolean isSymmetric() {
		throw new RuntimeException("Invalid call!");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		if (dependencies != null) {
			for (Dependency dependency : dependencies) {
				result = prime * result
						+ ((dependency == null) ? 0 : dependency.hashCode());
			}
		}

		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result
				+ ((predBodyA == null) ? 0 : predBodyA.hashCode());
		result = prime * result + ((predBodyB == null) ? 0 : predBodyB.hashCode());
		result = prime * result + ((predCallA == null) ? 0 : predCallA.hashCode());
		result = prime * result + ((predCallB == null) ? 0 : predCallB.hashCode());
		result = prime * result + ((predNameA == null) ? 0 : predNameA.hashCode());
		result = prime * result + ((predNameB == null) ? 0 : predNameB.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((field == null) ? 0 : field.hashCode());

	  return result;
	}

	protected boolean isEqual(PropertyToAlloyCode other) {
		if (dependencies == null) {
			if (other.dependencies != null)
				return false;
		} else {
			if (dependencies.size() != other.dependencies.size())
				return false;
			for (int i = 0; i < dependencies.size(); ++i) {
				if (!dependencies.get(i).equals(other.dependencies.get(i)))
					return false;
			}
		}

		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		if (predBodyA == null) {
			if (other.predBodyA != null)
				return false;
		} else if (!predBodyA.equals(other.predBodyA))
			return false;
		if (predBodyB == null) {
			if (other.predBodyB != null)
				return false;
		} else if (!predBodyB.equals(other.predBodyB))
			return false;
		if (predCallA == null) {
			if (other.predCallA != null)
				return false;
		} else if (!predCallA.equals(other.predCallA))
			return false;
		if (predCallB == null) {
			if (other.predCallB != null)
				return false;
		} else if (!predCallB.equals(other.predCallB))
			return false;
		if (predNameA == null) {
			if (other.predNameA != null)
				return false;
		} else if (!predNameA.equals(other.predNameA))
			return false;
		if (predNameB == null) {
			if (other.predNameB != null)
				return false;
		} else if (!predNameB.equals(other.predNameB))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;

		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyToAlloyCode other = (PropertyToAlloyCode) obj;
		return isEqual(other);
	}

	protected Optional<List<ImplicationLattic>> getImplicationLattices() {
		return Optional.ofNullable(implications);
	}

	/**
	 * After checking a=>b, if a=>b is true, means the check is unSAT (No
	 * Counter-example): if a=E and b=Prop then allImpliedProperties Of b also has
	 * to be returned if a=prop and b=E then allRevImpliedProperties of a has to
	 * returned. The return type is false. Means stop any furtherAnaylsis and take
	 * the result as the inferred propertied
	 */
	public List<String> getInferedProperties(int sat) {
		throw new RuntimeException("Invalid call!");
	}

	public List<PropertyToAlloyCode> getInferedPropertiesCoder(int sat) {
		throw new RuntimeException("Invalid call!");
	}

	/**
	 * if sat, there is a counterexample if a=E and b=Prop then next properties
	 * implied from Prop has to be evaluated if a=Prop and b=E then next
	 * properties that implying Prop has to be evaluated
	 * 
	 * @param sat
	 * @return
	 */
	public List<String> getToBeCheckedProperties(int sat) {
		throw new RuntimeException("Invalid call!");
	}

	public List<String> getInitialProperties() {
		throw new RuntimeException("Invalid call!");
	}

	/**
	 * return a predicated name: predA operator predB This function is used to see
	 * whether a check is done regardless it inferred or ran.
	 * 
	 * @return
	 */
	public String getPredName() {
		return predNameA + commandOperator() + predNameB;
	}

	public List<Dependency> getDependencies() {
		return Collections.unmodifiableList(dependencies);
	}

	/**
	 * What would be the desired sat answer for the property to be checked. sat ==
	 * 1 SAT sat == -1 UnSAT sat == 0 Unknown
	 * 
	 * @return
	 */
	public boolean isDesiredSAT(int sat) {
		throw new RuntimeException("Invalid call!");
	}
}
