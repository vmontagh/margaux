package edu.uw.ece.alloy.debugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This class stores all the information regarding each statements, all
 * properties it has or implied, and relations.
 * 
 * @author vajih
 *
 */
public final class PropertySet {

	public static void main(String... args) {
		System.out.println(props);
	}

	final static public Map<Operator, Map<Statement, Map<Field, Set<Property>>>> props;

	static {
		// TODO remove it or make in a config file
		final Map<Operator, File> mapFiles = new HashMap<Operator, File>();
		final File workingDirectory = new File(
				"models/debugger/temporal_kb/csv/dataset1");

		final Map<Operator, Integer> desiredValue = new HashMap<PropertySet.Operator, Integer>();

		desiredValue.put(Operator.Consistent, 1);
		desiredValue.put(Operator.Inconsistent, -1);
		desiredValue.put(Operator.CompleteHas, -1);
		desiredValue.put(Operator.PartialHas, -1);

		mapFiles.put(Operator.Consistent,
				new File(workingDirectory, "map_and.csv"));
		// map_not is exactly similar to map_and except all 1's are turned into -1
		// and vice versa.
		mapFiles.put(Operator.Inconsistent,
				new File(workingDirectory, "map_and.csv"));
		mapFiles.put(Operator.CompleteHas,
				new File(workingDirectory, "map_imply.csv"));
		mapFiles.put(Operator.PartialHas,
				new File(workingDirectory, "map_rev.csv"));

		Map<Operator, Map<Statement, Map<Field, Set<Property>>>> tmpOps = new HashMap<PropertySet.Operator, Map<Statement, Map<Field, Set<Property>>>>();

		final BiConsumer<Operator, List<String>> insertInMap = (final Operator op,
				final List<String> list) -> {
			assert list.size() == 4 : list + " is not complete\n";
			final Statement stmt = new Statement(list.get(2));
			final Field fld = new Field(list.get(1));
			final Property prop = new Property(list.get(0), fld);
			final int status = Integer.parseInt(list.get(3));

			if (status == desiredValue.get(op)) {
				final Map<Statement, Map<Field, Set<Property>>> tmpStmt = tmpOps
						.containsKey(op) ? tmpOps.get(op) : new HashMap<>();
				final Map<Field, Set<Property>> tmpFld = tmpStmt.containsKey(stmt)
						? tmpStmt.get(stmt) : new HashMap<>();
				final Set<Property> tmpProps = tmpFld.containsKey(fld) ? tmpFld.get(fld)
						: new HashSet<>();
				tmpProps.add(prop);
				tmpFld.put(fld, tmpProps);
				tmpStmt.put(stmt, tmpFld);
				tmpOps.put(op, tmpStmt);
			}

		};

		for (final Operator operator : Operator.values()) {
			try (BufferedReader reader = new BufferedReader(
					new FileReader(mapFiles.get(operator)))) {

				reader.lines().skip(0).map(line -> Arrays.asList(line.split("(,)")))
						// .flatMap(l -> l.stream())
						// .map(line -> Arrays.asList(line.split("_")))
						// .flatMap(l -> l.stream())

						.forEach(s -> insertInMap.accept(operator, s));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		props = Collections.unmodifiableMap(tmpOps);
	}

	public static final Set<Operator> getAllOperators() {
		return props.keySet();
	}

	public static final Set<Statement> getAllStatements() {
		return props.values().stream().map(m -> m.keySet()).flatMap(a -> a.stream())
				.collect(Collectors.toSet());
	}

	public static final Set<Field> getAllFields() {
		return props.values().stream().map(m -> m.values()).flatMap(a -> a.stream())
				.map(n -> n.keySet()).flatMap(b -> b.stream())
				.collect(Collectors.toSet());
	}

	public static final Set<Property> getAllProperties() {
		return props.values().stream().map(m -> m.values()).flatMap(a -> a.stream())
				.map(n -> n.values()).flatMap(b -> b.stream()).flatMap(b -> b.stream())
				.collect(Collectors.toSet());
	}

	public static final Set<Property> getProperties(final Operator op,
			final Statement stmt, final Field fld) {
		try {
			return props.get(op).get(stmt).containsKey(fld)
					? props.get(op).get(stmt).get(fld) : Collections.emptySet();
		} catch (NullPointerException np) {
			return Collections.emptySet();
		}
	}

	public static final Set<Property> getProperties(final Operator op,
			final Statement stmt) {
		try {
			return props.get(op).get(stmt).values().stream().flatMap(b -> b.stream())
					.collect(Collectors.toSet());
		} catch (NullPointerException np) {
			return Collections.emptySet();
		}
	}

	public static final Set<Property> getProperties(final Operator op) {
		try {
			return props.get(op).values().stream().map(a -> a.values())
					.flatMap(b -> b.stream()).flatMap(b -> b.stream())
					.collect(Collectors.toSet());
		} catch (NullPointerException np) {
			return Collections.emptySet();
		}
	}

	final public Operator operator;
	final public Statement stmt;
	final public Field fld;
	final public Set<Property> properties;

	/**
	 * 
	 */
	public PropertySet(final Operator operator, final Statement stmt,
			final Field fld) {

		if (getProperties(operator, stmt, fld).isEmpty())
			throw new RuntimeException("");

		this.operator = operator;
		this.stmt = stmt;
		this.fld = fld;

		this.properties = Collections
				.unmodifiableSet(getProperties(operator, stmt, fld));
	}

	/**
	 * 
	 */
	public PropertySet(final Operator operator, final String stmt,
			final String fld) {
		this.operator = operator;
		this.stmt = new Statement(stmt);
		this.fld = new Field(fld);

		if (getProperties(operator, this.stmt, this.fld).isEmpty())
			throw new RuntimeException("");

		this.properties = Collections
				.unmodifiableSet(getProperties(operator, this.stmt, this.fld));
	}

	public static abstract class Value implements Comparable {

		final public String value;

		public Value(String value) {
			super();
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + ": " + value + "]";
		}

	}

	public static class Statement extends Value {

		@Override
		public boolean equals(Object obj) {
			if (!super.equals(obj))
				return false;

			Statement other = (Statement) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public int compareTo(Object obj) {
			if (this == obj)
				return 0;
			if (obj == null)
				return 1;
			if (getClass() != obj.getClass())
				return 1;
			Statement other = (Statement) obj;
			if (value == null) {
				if (other.value != null)
					return -1;
				else
					return 1;
			} else {

				return value.compareTo(other.value);
			}
		}

		public Statement(String value) {
			super(value);
		}

	}

	public static class Field extends Value {

		@Override
		public boolean equals(Object obj) {
			if (!super.equals(obj))
				return false;

			Field other = (Field) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public int compareTo(Object obj) {
			if (this == obj)
				return 0;
			if (obj == null)
				return 1;
			if (getClass() != obj.getClass())
				return 1;
			Field other = (Field) obj;
			if (value == null) {
				if (other.value != null)
					return -1;
				else
					return 1;
			} else {

				return value.compareTo(other.value);
			}
		}

		public Field(String value) {
			super(value);
		}

	}

	public static class Property extends Value {

		public final Field fld;

		public boolean equals(Object obj) {

			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!this.getClass().equals(obj.getClass()))
				return false;

			Property that = (Property) obj;

			if (that.fld != null && that.value != null)
				return that.fld.equals(this.fld) && that.value.equals(this.value);
			else if (that.fld == null && that.value != null)
				return this.fld == null && that.value.equals(this.value);
			else if (that.fld != null && that.value == null)
				return that.fld.equals(this.fld) && this.value == null;
			else
				return that.fld == null && this.value == null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((fld == null) ? 0 : fld.hashCode());
			return result;
		}

		@Override
		public String toString() {
			final String value = this.value == null ? "" : this.value;
			return fld == null ? value : String.format("%s[%s]", value, fld.value);
		}

		public Property(String value, final Field fld) {
			super(value);
			this.fld = new Field(fld.value);
		}

		public Property(String value) {
			super(value);
			this.fld = null;
		}

		@Override
		public int compareTo(Object obj) {

			if (obj == this)
				return 0;
			if (obj == null)
				return 1;

			if (!this.getClass().equals(obj.getClass()))
				return 1;

			Property that = (Property) obj;

			return this.toString().compareTo(that.toString());
		}

		public Property setField(final Field fld) {
			return new Property(this.value, fld);
		}

		/**
		 * return a new object of property by extracting the property name and field
		 * name fromt he given property string name
		 * 
		 * @param propString
		 * @return
		 */
		public static Property StringPorpertyToProperty(final String propString) {
			final String propName = PropertyCheckingSource
					.propertyNameExtractorFromProperty(propString);
			final String fldName = PropertyCheckingSource
					.fieldExtractorFromProperty(propString);

			return new Property(propName, new Field(fldName));
		}
	}

	public static enum Operator {
		Consistent("AND"), Inconsistent("NOT"), CompleteHas("IMPLIES"), PartialHas(
				"REVIMPLIES"),;

		private Operator(String value) {
		}
	};

	public static class PropertyRelation {
		final Property prop;

		public Property getProp() {
			return prop;
		}

		public Statement getStmt() {
			return stmt;
		}

		public Field getFld() {
			return fld;
		}

		final Statement stmt;
		final Field fld;

		public PropertyRelation(Property prop, Statement stmt, Field fld) {
			super();
			this.prop = prop;
			this.stmt = stmt;
			this.fld = fld;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fld == null) ? 0 : fld.hashCode());
			result = prime * result + ((prop == null) ? 0 : prop.hashCode());
			result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PropertyRelation other = (PropertyRelation) obj;
			if (fld == null) {
				if (other.fld != null)
					return false;
			} else if (!fld.equals(other.fld))
				return false;
			if (prop == null) {
				if (other.prop != null)
					return false;
			} else if (!prop.equals(other.prop))
				return false;
			if (stmt == null) {
				if (other.stmt != null)
					return false;
			} else if (!stmt.equals(other.stmt))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return String.format("%s{%s[%s]}", stmt != null ? stmt.value : "",
					prop != null ? prop.value : "", fld != null ? fld.value : "");
		}

	}

}
