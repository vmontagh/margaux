package edu.uw.ece.alloy.debugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import edu.uw.ece.alloy.debugger.PropertySet.Property;

public class PrecomputedProperties {

	final public static PrecomputedProperties INSTANCE = new PrecomputedProperties();

	final private static boolean SAMESIGS = false; 

	final public static Map<String, Set<String>> consistentPropsDistinctedType;
	final public static Map<String, Set<String>> inconsistentPropsDistictedType;
	final public static Map<String, Set<String>> consistentPropsSameType;
	final public static Map<String, Set<String>> inconsistentPropsSameType;

	final public static Map<String, Set<String>> ImplicationPropsDistinctedType;
	final public static Map<String, Set<String>> ImplicationPropsSameType;

	//Each property is a string. Each property points to its parents. The parents are stored
	//stored in a set because the parents can be disjoint.
	final public static Map<Property, Set<Property>> backwardImplicationHierarchy = new HashMap<Property, Set<Property>>();
	//For every p, the related set contains all q's that p=>q
	final public static Map<Property, Set<Property>> forwardImplicationHierarchy =  new HashMap<Property, Set<Property>>();

	/**
	 * Reads the file and returns a map based on the keySensitive. For consistency, keySensitive is 1
	 * but for inconsistency keySensitive is -1;  
	 * @param mapFile
	 * @param keySensitive
	 * @return
	 */
	private static Map<String, Set<String>> readFile( final File mapFile, final String keySensitive ){
		final Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		final Consumer<List<String>> toMaps = new Consumer<List<String>>() {

			@Override
			public void accept(List<String> t) {

				assert t.size() == 5 : String.format("File name:%s: %s%n", mapFile.getName(), t) ;
				
				if(!t.get(4).equals(keySensitive)) return;
				
				final String prop = String.format("%s[%s]", t.get(0), t.get(1) );
				Set<String> set = map.containsKey(prop) ? map.get(prop) : new HashSet<String>();
				set.add(String.format("%s[%s]", t.get(2), t.get(3) ));
				map.put(prop, set);
			}
		};
		try (BufferedReader reader = new BufferedReader(new FileReader(mapFile))) {
			reader.lines()
			.substream(1)
			.map(line -> Arrays.asList(line.split(",")))
			//.filter(list -> list.size() == 5)
			.forEach( toMaps);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return  Collections.unmodifiableMap(map);
	}

	static{
		//TODO extract all the files and put in the config file or embed it.
		final File mapConsistnencyDistinctedFile = new File("/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/dataset1/consistency_distincted.csv"); 
		consistentPropsDistinctedType =  readFile(mapConsistnencyDistinctedFile, "1");
		inconsistentPropsDistictedType = readFile(mapConsistnencyDistinctedFile, "-1");;

		final File mapConsistnencySameTypeFile = new File("/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/dataset1/consistency_same.csv_not_implmented"); 
		consistentPropsSameType =  readFile(mapConsistnencySameTypeFile, "1");
		inconsistentPropsSameType = readFile(mapConsistnencySameTypeFile, "-1");;		

		final File mapImplicationDistinctedTypeFile = new File("/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/dataset1/implication_distincted.csv"); 
		ImplicationPropsDistinctedType =  readFile(mapImplicationDistinctedTypeFile, "-1");
		makeImplicationHierarchry();

		final File mapImplicationSameTypeFile = new File("/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/dataset1/implication_same.csv"); 
		ImplicationPropsSameType =  readFile(mapImplicationSameTypeFile, "-1");

	}


	public static void main(String ...str){
		//Set<Property> set = PropertySet.getAllProperties();
		//set.stream().flatMap(a-> set.stream().map(b->String.format("%s => %s = %s%n", a,b,PrecomputedProperties.INSTANCE.isImpliedProps(a,b)))).forEach(System.out::println);
		System.out.println(backwardImplicationHierarchy);
		System.out.println(forwardImplicationHierarchy);
	}


	protected PrecomputedProperties() {

	}



	public static Map<String, Set<String>> getConsistentProps(final boolean isSameType) {
		return isSameType? consistentPropsSameType : consistentPropsDistinctedType;
	}

	public static Map<String, Set<String>> getInconsistentProps(final boolean isSameType) {
		return /*isSameType? inconsistentPropsSameType :*/inconsistentPropsDistictedType;
	}

	public static Map<String, Set<String>> getImplicationProps(final boolean isSameType) {
		return isSameType? ImplicationPropsSameType :ImplicationPropsDistinctedType;
	}

	public static Set<String> sharedProperties(final Map<String, Set<String>> map, final Set<String> propperties, final String originalProperty){
		final Set<String> result = new HashSet<String>();

		final String fldPropA = PropertyCheckingSource.fieldExtractorFromProperty(originalProperty);
		final String cleandFldPropA = PropertyCheckingSource.cleanProperty(originalProperty);

		final Set<String> propsUWithfldPropA = propperties.stream()
				.filter(a->a.contains(fldPropA))
				.map(a->PropertyCheckingSource.cleanProperty(a))
				.collect(Collectors.toSet());

		return Collections.unmodifiableSet(result);
	}

	/**
	 * p <=> q
	 * @param p
	 * @param q
	 * @return
	 */
	public  boolean isEqualProps(final Property p, final Property q){
		return p.equals(q);
	}

	/**
	 * p ? q
	 * @param p
	 * @param q
	 * @return
	 */
	private  boolean isProps(final Property p, final Property q, final Map<String, Set<String>> map ){


		//TODO subtyping is not implemented and needed to be considered.
		if( ! p.fld.equals(q.fld) ){
			//System.err.printf("%s and %s not have same fields: %s %s%n", p,q, p.fld, q.fld);
			return false;
			//throw new RuntimeException(String.format("%s and %s not have same fields.%n", p,q));
		}
		final String cleandP = PropertyCheckingSource.cleanProperty(p);
		final String cleandQ = PropertyCheckingSource.cleanProperty(q);

		return  map.containsKey(cleandP) ? map.get(cleandP).contains(cleandQ) : false;
	}



	/**
	 * p => q
	 * @param p
	 * @param q
	 * @return
	 */
	public  boolean isImpliedProps(final Property p, final Property q){

		return isProps(p, q, getImplicationProps(SAMESIGS));

	}

	/**
	 * p & q is SAT
	 * @param p
	 * @param q
	 * @return
	 */
	public  boolean isConsistentProps(final Property p, final Property q){

		return isProps(p, q, getConsistentProps(SAMESIGS));
	}


	/**
	 * p & q is UnSAT
	 * @param p
	 * @param q
	 * @return
	 */
	public  boolean isInconsistentProps(final Property p, final Property q){
		/*for(Object key: getInconsistentProps(SAMESIGS).keySet()){
			System.out.println(key+" <-> "+getInconsistentProps(SAMESIGS).get(key));
			
		}*/
		
		return isProps(p, q, getInconsistentProps(SAMESIGS));
	}


	public BiPredicate< Property,  Property> isEqual = (final Property p, final Property q)->{
		return isEqualProps(p,q);
	};

	public BiPredicate< Property,  Property> isImplied = (final Property p, final Property q)->{
		return isImpliedProps(p, q);
	};

	public BiPredicate< Property,  Property> isRevImplied = (final Property p, final Property q)->{
		return isImpliedProps(q, p);
	};

	public BiPredicate< Property,  Property> isConsistent = (final Property p, final Property q)->{
		return isConsistentProps(p,q);
	};

	public BiPredicate< Property,  Property> isInconsistent = (final Property p, final Property q)->{
		return isInconsistentProps(p,q);
	};

	/**
	 * q=>p means q is the parent of p. q has to be added to p parents set.
	 * 
	 * @param p
	 * @param q
	 */
	private static void addImplicationParentChildren(final Property p, final Property q){

		if( q.equals(p) ) return;
		//make sure q=>p
		if( !INSTANCE.isImpliedProps(q,p) ) return;

		//breaking the symmetry: If q=>p and p=>q, and p is already in q's parent set, then break the symmetry and return.
		if( INSTANCE.isImpliedProps(p,q) && backwardImplicationHierarchy.containsKey(q) && backwardImplicationHierarchy.get(q).contains(p)) return; 
		
		Set<Property> pSet,qSet;
		if( !backwardImplicationHierarchy.containsKey(p) ){
			pSet = new HashSet<PropertySet.Property>();
		}else{
			pSet = backwardImplicationHierarchy.get(p);
		}
		
		pSet.add(q);
		backwardImplicationHierarchy.put(p, pSet);

		if( !forwardImplicationHierarchy.containsKey(q) ){
			qSet = new HashSet<PropertySet.Property>();
		}else{
			qSet = forwardImplicationHierarchy.get(q);
		}
		qSet.add(p);
		forwardImplicationHierarchy.put(q, qSet);

/*
		final Set<Property> parents = new HashSet<>(backwardImplicationHierarchy.get(p));
		//final Set<Property> newParents = Collections.unmodifiableSet(backwardImplicationHierarchy.get(p));
		boolean addToParents = true;

		for( final Property m: parents ){
			//m=>q
			if( INSTANCE.isImpliedProps(m,q) ){
				//removing m from p parents
				final Set<Property> pSet = backwardImplicationHierarchy.get(p);
				pSet.remove(m);
				backwardImplicationHierarchy.put(p, pSet);
				//add m to parent of q
				addImplicationParent(q,m);
				//add q as parent of p outside of the for loop

				//q=>m
			}else if( INSTANCE.isImpliedProps(q,m) ){
				addImplicationParent(m,q);
				//m is the parent of p, q is the parent of m; So, q should not be added to the parent of p
				addToParents = false;
			}
		}

		if(addToParents){
			final Set<Property> pSet = backwardImplicationHierarchy.get(p);
			pSet.add(q);
			backwardImplicationHierarchy.put(p, pSet);
		}*/
		
		

	}
	
	//calling making forward and backward implication implications
	private static void makeImplicationHierarchry(){


		final List<Property> properties = ImplicationPropsDistinctedType.keySet().stream().
				map(s-> PropertySet.Property.StringPorpertyToProperty(s)).collect(Collectors.toList());

		properties.stream().forEach(p-> properties.stream().forEach(q->addImplicationParentChildren(p,q)));
	}

	/**
	 * if q=>p, m=>p, n=>m, then (n,m,q) are returned. The order matters.
	 * Parents make a DAG, but the maximal is returned first.
	 * If the returned list is empty, the property does not have any parent.
	 * The return is the cleared properties
	 * @param p
	 * @return
	 */
	public List<Property> getImplicationParents(final Property p){

		final Set<Property> set = new HashSet<PropertySet.Property>();
		final Set<Property> queue = new HashSet<PropertySet.Property>();

		final String cleandPStr = PropertyCheckingSource.cleanProperty(p);
		final Property cleanedP = Property.StringPorpertyToProperty(cleandPStr);
		//The given property does not have any parent.
		if(!backwardImplicationHierarchy.containsKey(cleanedP))
			return new LinkedList<>();

		queue.addAll(backwardImplicationHierarchy.get( cleanedP ));

		while(!queue.isEmpty()){
			final Property headP = queue.iterator().next();
			queue.remove(headP);
			//To prevent cycles. Some properties are equivalent  p<=>q. 
			if( set.contains(headP) ) continue;
			set.add(headP);
			if( backwardImplicationHierarchy.containsKey(headP))
				queue.addAll(backwardImplicationHierarchy.get(headP));
		}
		
		return Collections.unmodifiableList(new LinkedList<Property>(set));
	}
	
	public List<Property> getImplicationParents(final Property p, final Set<Property> props){
		return getImplications(p, props, backwardImplicationHierarchy);
	}

	public List<Property> getImplicationChildren(final Property p, final Set<Property> props){
		return getImplications(p, props, forwardImplicationHierarchy);
	}

	
	private List<Property> getImplications(final Property p, final Set<Property> props, final Map<Property,  Set<Property>> map){
		//Cleaned property to concrete property
		final Map<Property, Property> propsMap = new HashMap<PropertySet.Property, PropertySet.Property>();
		//filter the properties that have the same fields
		final Set<Property> filteredProps = props.stream().filter(a->a.fld.equals(p.fld)).collect(Collectors.toSet());
		
		filteredProps.stream().forEach(a-> propsMap.put(Property.StringPorpertyToProperty(PropertyCheckingSource.cleanProperty(a)), a) );
		
		final String cleandPStr = PropertyCheckingSource.cleanProperty(p);
		final Property cleanedP = Property.StringPorpertyToProperty(cleandPStr);
		
		if( ! map.containsKey(cleanedP) ){
			return Collections.EMPTY_LIST;
		}
		
		final List<Property> consList = new LinkedList<Property>(map.get(cleanedP)); //getImplicationParents(p);
		
		final List<Property> concreteParents = new LinkedList<PropertySet.Property>();
		
		consList.stream().filter(k->propsMap.containsKey(k)).forEach(a-> concreteParents.add(propsMap.get(a)));
		
		return Collections.unmodifiableList(concreteParents);
	}
}
 