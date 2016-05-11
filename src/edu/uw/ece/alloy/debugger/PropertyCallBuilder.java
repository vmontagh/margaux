package edu.uw.ece.alloy.debugger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule.Open;

public class PropertyCallBuilder {

	final List<PropertyDeclaration> properties = new LinkedList<PropertyDeclaration>();
	public final static String PROPERTY_CALL_FORMAT = "%s[%s]";

	final static Logger logger = Logger
			.getLogger(PropertyCallBuilder.class.getName() + "--"
					+ Thread.currentThread().getName());

	public PropertyCallBuilder() {

	}

	public void addPropertyDeclration(final Func function)
			throws IllegalArgumentException {

		PropertyDeclaration pd = new PropertyDeclaration(function);

		if (!pd.isAPropertyDefinition()) {
			throw new IllegalArgumentException(
					"The passed func/pred is not a property declration: " + pd);
		} else {
			properties.add(pd);
		}
	}

	/**
	 * Given a field, all applicable properties are made.
	 * 
	 * @param field
	 * @return
	 * @throws IllegalArgumentException
	 */
	public List<String> makeAllBinaryProperties(final Field field)
			throws IllegalArgumentException {
		if (2 != field.type().arity()) {
			throw new IllegalArgumentException("Field is not Binary");
		}

		final List<String> result = new LinkedList<String>();

		final String relationName = field.label;
		final String leftName = field.type().extract(2).fold().get(0).get(0).label;
		final String rightName = field.type().extract(2).fold().get(0).get(1).label;

		// System.out.println(getAllBinaryPropertyDeclrations());

		for (PropertyDeclaration pd : getAllBinaryPropertyDeclrations()) {
			final String propertyName = pd.getPropertyName();
			String params = relationName.replace("this/", "");

			if (pd.hasLeft()) {
				params += ", " + leftName.replace("this/", "");
			}
			if (pd.hasRight()) {
				params += ", " + rightName.replace("this/", "");
			}
			result.add(String.format(PROPERTY_CALL_FORMAT, propertyName, params));
		}
		return Collections.unmodifiableList(result);
	}

	public List<PropertyDeclaration> getAllNaryPropertyDeclrations(int n) {
		return properties.stream().filter(p -> p.getRelationArity() == n)
				.collect(Collectors.toList());
	}

	public List<PropertyDeclaration> getAllBinaryPropertyDeclrations() {
		return getAllNaryPropertyDeclrations(2);
	}

	public List<PropertyDeclaration> getAllTernaryPropertyDeclrations() {
		return getAllNaryPropertyDeclrations(3);
	}

	/**
	 * Given a field, all applicable properties are made.
	 * 
	 * @param field
	 * @return
	 * @throws IllegalArgumentException
	 */
	public List<String> makeAllTernaryProperties(final Field field,
			final List<Open> opens) throws IllegalArgumentException {
		if (3 != field.type().arity()) {
			throw new IllegalArgumentException("Field is not Ternary");
		}

		final List<String> result = new LinkedList<String>();

		for (PropertyDeclaration pd : getAllTernaryPropertyDeclrations()) {
			final String propertyName = pd.getPropertyName();
			String params = field.label.replace("this/", "");

			// Signature names
			for (int i = 0; i < 3; ++i) {
				if (pd.hasLabel(i)) {
					params += "," + PropertyDeclaration.findSigInField(field, i);
				}
			}

			boolean notOrdered = false;
			// Orders of the Signatures in the field
			for (int i = 0; i < 3; ++i) {
				if (pd.hasOrder(i)) {
					final String orderName = PropertyDeclaration.findOrderingName(field,
							i, opens);
					if (orderName.isEmpty()) {
						notOrdered = true;
						break;
					}
					if (orderName.contains("$")) {
						params += ", first";
						params += ", next";
					} else {
						params += "," + orderName + "/first";
						params += "," + orderName + "/next";
					}
				}
			}
			if (!notOrdered)
				result.add(String.format(PROPERTY_CALL_FORMAT, propertyName, params));
		}
		return Collections.unmodifiableList(result);
	}

}