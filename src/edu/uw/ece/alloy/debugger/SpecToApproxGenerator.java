package edu.uw.ece.alloy.debugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.debugger.PropertySet.Field;
import edu.uw.ece.alloy.debugger.PropertySet.Operator;
import edu.uw.ece.alloy.debugger.PropertySet.Property;
import edu.uw.ece.alloy.debugger.PropertySet.PropertyRelation;
import edu.uw.ece.alloy.debugger.PropertySet.Statement;
import edu.uw.ece.alloy.util.SymmetricPair;
import edu.uw.ece.alloy.util.Utils;


public class SpecToApproxGenerator extends PropertyCheckingSource {

	public SpecToApproxGenerator(File sourceFile_, String property_,
			String fieldName_, Set<String> binaryProperties_,
			Set<String> ternaryProperties_, String sigs_, String openModule_,
			String openStatements_, String functions_, String commandHeader_,
			String formula_, String commandScope_, String fact_) {
		super(sourceFile_, property_, fieldName_, binaryProperties_,
				ternaryProperties_, sigs_, openModule_, openStatements_,
				functions_, commandHeader_, formula_, commandScope_,fact_);


	}

	@Override
	protected String makeNewFileName() {
		return "props.als";
	}

	@Override
	protected String getNewStatement() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toAlloy(){

		final StringBuilder newAlloySpec = new StringBuilder();

		/*for(Operator operator: Operator.values()){
			if( !commandHeaderToProperties.containsKey(operator) ) continue;
			Map<String, Set<String>> map = commandHeaderToProperties.get(operator);
			for(String commandHeader: map.keySet()){
				final String predName = "approx_"+operator+"_"+commandHeader;
				final String body = map.get(commandHeader).stream().collect(Collectors.joining(" \n "));
				newAlloySpec.append(String.format("pred %s{%n %s %n}%n", predName, body)).append("\n");
			}
		}*/

		return "";
	}

	private static Set<PropertyRelation> findAllProperties(Operator op){
		final Set<PropertyRelation> result = new HashSet<>();

		final Set<Statement> stmts = PropertySet.getAllStatements();
		final Set<Field> flds = PropertySet.getAllFields();

		for(final Statement stmt: stmts){
			for(final Field fld: flds){
				for(Property p: PropertySet.getProperties(op, stmt,fld)){
					result.add(  new PropertyRelation(p , stmt, fld) );
				}
			}
		}

		return Collections.unmodifiableSet(result);
	}

	public static Set<PropertyRelation> findAllImplications(){
		return findAllProperties(Operator.CompleteHas);
	}

	public static Set<PropertyRelation> findAllConsistents(){
		return findAllProperties(Operator.Consistent);
	}

	public static Set<PropertyRelation> findAllInconsistents(){
		return findAllProperties(Operator.Inconsistent);
	}

	/**
	 * General Properties
	 * @return
	 */
	public static Set<Pair<PropertyRelation,PropertyRelation>> findProps(final Operator operatorI, final Operator operatorJ, final BiPredicate<Property, Property> checker){

		final Set<Pair<PropertyRelation,PropertyRelation>> result = new HashSet<>();

		final Set<Statement> stmts = PropertySet.getAllStatements();
		final Set<Field> flds = PropertySet.getAllFields();


		//Lets order the stmts in order to have Ai,Aj instead of Ai,Aj and AjAi 

		for(final Field fld: flds){
			for(final Statement stmtp: stmts){
				for(final Statement stmtq: stmts){

					if(stmtp.compareTo(stmtq) == 0 ) continue;

					Set<Property> Ps = PropertySet.getProperties(operatorI, stmtp, fld);
					Set<Property> Qs = PropertySet.getProperties(operatorJ, stmtq, fld);

					for(Property p: Ps){
						for(Property q: Qs){

							if( checker.test(p, q)){
								final PropertyRelation prP = new PropertyRelation(p, stmtp, fld);
								final PropertyRelation prQ = new PropertyRelation(q, stmtq, fld);
								final Pair<PropertyRelation,PropertyRelation> pair = 
										new Pair<>(prP, prQ);
								result.add(pair);
							}
						}
					}
				}
			}
		}

		return Collections.unmodifiableSet(result);
	}


	public static Set<Pair<PropertyRelation,PropertyRelation>> findI_a(){
		return findProps(Operator.Consistent, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isEqual);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findI_b(){
		return findProps(Operator.Consistent, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findI_c(){
		return findProps(Operator.Consistent, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isConsistent);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findI_d(){
		return findProps(Operator.Consistent, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isRevImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findI_e(){
		return findProps(Operator.Consistent, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isInconsistent);
	}



	public static Set<Pair<PropertyRelation,PropertyRelation>> findII_a(){
		return findProps(Operator.CompleteHas, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isEqual);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findII_b(){
		return findProps(Operator.CompleteHas, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findII_c(){
		return findProps(Operator.CompleteHas, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isConsistent);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findII_d(){
		return findProps(Operator.CompleteHas, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isRevImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findII_e(){
		return findProps(Operator.CompleteHas, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isInconsistent);
	}	


	public static Set<Pair<PropertyRelation,PropertyRelation>> findIII_a(){
		return findProps(Operator.CompleteHas, Operator.Consistent, PrecomputedProperties.INSTANCE.isEqual);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findIII_b(){
		return findProps(Operator.CompleteHas, Operator.Consistent, PrecomputedProperties.INSTANCE.isImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findIII_c(){
		return findProps(Operator.CompleteHas, Operator.Consistent, PrecomputedProperties.INSTANCE.isConsistent);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findIII_d(){
		return findProps(Operator.CompleteHas, Operator.Consistent, PrecomputedProperties.INSTANCE.isRevImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findIII_e(){
		return findProps(Operator.CompleteHas, Operator.Consistent, PrecomputedProperties.INSTANCE.isInconsistent);
	}	


	public static Set<Pair<PropertyRelation,PropertyRelation>> findIV_a(){
		return findProps(Operator.Consistent, Operator.Consistent, PrecomputedProperties.INSTANCE.isEqual);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findIV_b(){
		return findProps(Operator.Consistent, Operator.Consistent, PrecomputedProperties.INSTANCE.isImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findIV_c(){
		return findProps(Operator.Consistent, Operator.Consistent, PrecomputedProperties.INSTANCE.isConsistent);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findIV_d(){
		return findProps(Operator.Consistent, Operator.Consistent, PrecomputedProperties.INSTANCE.isRevImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findIV_e(){
		return findProps(Operator.Consistent, Operator.Consistent, PrecomputedProperties.INSTANCE.isInconsistent);
	}



	public static Set<Pair<PropertyRelation,PropertyRelation>> findV_a(){
		return findProps(Operator.CompleteHas, Operator.CompleteHas, PrecomputedProperties.INSTANCE.isEqual);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findV_b(){
		return findProps(Operator.CompleteHas, Operator.CompleteHas, PrecomputedProperties.INSTANCE.isImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findV_c(){
		return findProps(Operator.CompleteHas, Operator.CompleteHas, PrecomputedProperties.INSTANCE.isConsistent);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findV_d(){
		return findProps(Operator.CompleteHas, Operator.CompleteHas, PrecomputedProperties.INSTANCE.isRevImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findV_e(){
		return findProps(Operator.CompleteHas, Operator.CompleteHas, PrecomputedProperties.INSTANCE.isInconsistent);
	}



	public static Set<Pair<PropertyRelation,PropertyRelation>> findVI_a(){
		return findProps(Operator.Inconsistent, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isEqual);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findVI_b(){
		return findProps(Operator.Inconsistent, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findVI_c(){
		return findProps(Operator.Inconsistent, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isConsistent);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findVI_d(){
		return findProps(Operator.Inconsistent, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isRevImplied);
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> findVI_e(){
		return findProps(Operator.Inconsistent, Operator.Inconsistent, PrecomputedProperties.INSTANCE.isInconsistent);
	}

	final static Set<Statement> included = new HashSet<>();
	static{
		/*included.add(new Statement("A"));
		included.add(new Statement("B")); 
		included.add(new Statement("C"));
		included.add(new Statement("CF1"));
		included.add(new Statement("CF2"));
		included.add(new Statement("CF3"));*/
		included.add(new Statement("CF4"));
		included.add(new Statement("CF5"));
		included.add(new Statement("U"));
		included.add(new Statement("CF4NOTU"));
		included.add(new Statement("EXCF4Y"));
		included.add(new Statement("CF4U"));
		included.add(new Statement("CF4ANT"));
		included.add(new Statement("EXCF4N"));		
		//included.add(new Statement("State<:holds"));
	}

	public static Set<Pair<PropertyRelation,PropertyRelation>> filterMap(final Set<Pair<PropertyRelation,PropertyRelation>> map){
		return map.stream()

				.filter(a-> (
						included.contains(a.a.stmt) && included.contains(a.b.stmt) 
						//&& included.contains(a.a.fld.value)

						)).collect(Collectors.toSet());
	}

	public static void printMap(final Set<Pair<PropertyRelation,PropertyRelation>> map){
		filterMap(map).stream()

		//.filter(a-> (included.contains(a.a.stmt) && included.contains(a.b.stmt)))
		//.forEach(a->System.out.printf("%s %s %s %s %n", a.a.value, included.contains(a.a) ,  map.get(a).a.value, included.contains(map.get(a).a)) );
		//.filter(a-> included.contains(a.a.value) && included.contains(map.get(a).b.a.value))
		//.forEach(System.out::println);
		.forEach(k -> System.out.printf("%s - %s %n",k.a, k.b));
	}

	public static void printSet(final Set<PropertyRelation> set){
		set.stream()

		.filter(a-> (included.contains(a.stmt) && included.contains(a.stmt)))

		.sorted(new Comparator<PropertyRelation>() {
			@Override
			public int compare(PropertyRelation o1, PropertyRelation o2) {
				 
						if (o1.stmt.compareTo(o2.stmt)!= 0)
							return o1.stmt.compareTo(o2.stmt);
						else if(o1.fld.compareTo(o2.fld)!= 0)
							return o1.fld.compareTo(o2.fld);
						else
							return o1.prop.compareTo(o2.prop);
			}
		})
		

				//.forEach(a->System.out.printf("%s %s %s %s %n", a.a.value, included.contains(a.a) ,  map.get(a).a.value, included.contains(map.get(a).a)) );
				//.filter(a-> included.contains(a.a.value) && included.contains(map.get(a).b.a.value))
				//.forEach(System.out::println);
				.forEach(k -> System.out.printf("%s %n",k));
	}

	/**
	 * In the given set we have ([a,b],[p,q]) in state statement S over the field r and we know that a=>p, then the output is ([a,b], [a,q])
	 * @param set
	 * @return
	 */
	public static Set<Pair<PropertyRelation,PropertyRelation>> filterbyImplicationRightside(final Set<Pair<PropertyRelation,PropertyRelation>> set){

		final Set<Pair<PropertyRelation,PropertyRelation>> result = 
				set.stream().filter(a-> (included.contains(a.a.stmt) && included.contains(a.b.stmt))).collect(Collectors.toSet());

		Set<Pair<PropertyRelation,PropertyRelation>> initResult;

		do{
			//what was the initial result. f it is not changed then get out.
			initResult = new HashSet<Pair<PropertyRelation,PropertyRelation>>(result);

			//needed to find the concrete parents.
			final Set<Property> propsFrom = result.stream().map(a->a.a.prop ).collect(Collectors.toSet());
			//needed to find the concrete parents.
			final Set<PropertyRelation> rightPropRelations = result.stream().map(a->a.a ).collect(Collectors.toSet());
			//for concurrency modification.
			final Set<Pair<PropertyRelation,PropertyRelation>> pairs =  new HashSet<Pair<PropertyRelation,PropertyRelation>>(result);

			//Read from pairs, but remove from the result.
			for(final Pair<PropertyRelation,PropertyRelation> pair: pairs){
				final PropertyRelation prFrom = pair.a;
				final Property pFrom = prFrom.prop;

				//find all parents of right of the the current pair
				final List<Property> parentsPFrom = PrecomputedProperties.INSTANCE.getImplicationParents(pFrom, propsFrom);
				boolean shrunk = false;
				for(Property parent: parentsPFrom){
					//There is a pair containing the parent?
					PropertyRelation tmpParent = new PropertyRelation(parent, pair.a.stmt, pair.a.fld);

					if(rightPropRelations.contains(tmpParent)){
						//pair is [p,q], a(i.e. tmpParent) is the parent p(i.e. pair.a), i.e. a=>p OR pair.a => tmpParent
						//remove the pair, i.e. [p,q]
						result.remove(pair);
						//add [a,q]
						result.add(new Pair<PropertyRelation, PropertyRelation>(tmpParent, pair.b) );
						shrunk = true;
						break;

					}
				}
				if(shrunk) break;
			}
			//System.out.printf("result size: %d -> %s %n", result.size(), result);
		}while(!result.equals(initResult));


		return Collections.unmodifiableSet(result);
	}

	/**
	 * In the given set we have ([a,b],[p,q]) in state statement S over the field r and we know that b=>q, then the output is ([a,q], [p,q])
	 * @param set
	 * @return
	 */
	public static Set<Pair<PropertyRelation,PropertyRelation>> 
	filterbyImplicationLeftside(final Set<Pair<PropertyRelation,PropertyRelation>> set){

		final Set<Pair<PropertyRelation,PropertyRelation>> result = 
				set.stream().filter(a-> (included.contains(a.a.stmt) && included.contains(a.b.stmt))).collect(Collectors.toSet());

		Set<Pair<PropertyRelation,PropertyRelation>> initResult;

		do{
			//what was the initial result. f it is not changed then get out.
			initResult = new HashSet<Pair<PropertyRelation,PropertyRelation>>(result);

			//needed to find the concrete parents.
			final Set<Property> propsTo = result.stream().map(a->a.b.prop ).collect(Collectors.toSet());
			//needed to find the concrete parents.
			final Set<PropertyRelation> leftPropRelations = result.stream().map(a->a.b ).collect(Collectors.toSet());
			//for concurrency modification.
			final Set<Pair<PropertyRelation,PropertyRelation>> pairs =  new HashSet<Pair<PropertyRelation,PropertyRelation>>(result);

			//Read from pairs, but remove from the result.
			for(final Pair<PropertyRelation,PropertyRelation> pair: pairs){
				final PropertyRelation prTo = pair.b;
				final Property pTo = prTo.prop;

				//find all parents of right of the the current pair
				final List<Property> childrenPTo = PrecomputedProperties.INSTANCE.getImplicationChildren(pTo, propsTo);
				boolean shrunk = false;
				for(Property child: childrenPTo){
					//There is a pair containing the child?
					PropertyRelation tmpChild = new PropertyRelation(child, pair.b.stmt, pair.b.fld);

					if(leftPropRelations.contains(tmpChild)){
						//pair is [a,b], q(i.e. tmpChild) is the child b(i.e. pair.b), i.e. b=>q OR pair.b => tmpChild
						//remove the pair, i.e. [p,q]
						result.remove(pair);
						//add [a,q]
						result.add(new Pair<PropertyRelation, PropertyRelation>(pair.a, tmpChild) );
						shrunk = true;
						break;

					}
				}
				if(shrunk) break;
			}
			//System.out.printf("result size: %d -> %s %n", result.size(), result);
		}while(!result.equals(initResult));


		return Collections.unmodifiableSet(result);
	}

}
