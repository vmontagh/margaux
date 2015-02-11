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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.util.SymmetricPair;
import edu.uw.ece.alloy.util.Utils;


public class SpecToApproxGenerator extends PropertyCheckingSource {

	public static enum Operator {
		Consistent("AND"),
		Inconsistent("NOT"),
		CompleteHas("IMPLIES"), 
		PartialHas("REVIMPLIES"),
		; 

		private Operator(String value) {
		}};

		public final static Map<Operator, Map<String, Set<String>>> commandHeaderToProperties = new HashMap<>();
		public final static Map<Operator, Map<String, String>> analyzedProperties;


		private final static Map<Operator, File> mapFiles = new HashMap<SpecToApproxGenerator.Operator, File>(); 
		private final static File workingDirectory= new File("/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/dataset1");

		static{

			mapFiles.put(Operator.Consistent, new File(workingDirectory, "map_and.csv"));
			mapFiles.put(Operator.Inconsistent, new File(workingDirectory, "map_and.csv"));
			mapFiles.put(Operator.CompleteHas, new File(workingDirectory, "map_imply.csv"));
			mapFiles.put(Operator.PartialHas, new File(workingDirectory, "map_rev.csv"));

			final Map<Operator, Map<String, String>> analyzedMap = new HashMap<>();

			for(Operator operator: Operator.values()){
				Map<String,String> propMap = new HashMap<String, String>();
				try (BufferedReader reader = new BufferedReader(new FileReader(mapFiles.get(operator)))) {
					reader.lines()
					.substream(1)
					.map(line -> Arrays.asList(line.split(",")))
					.forEach( list-> propMap.put(list.get(0).trim(), list.get(1).trim()) );
					analyzedMap.put(operator, Collections.unmodifiableMap(propMap) );
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			analyzedProperties = Collections.unmodifiableMap(analyzedMap);
		}

		public SpecToApproxGenerator(File sourceFile_, String property_,
				String fieldName_, Set<String> binaryProperties_,
				Set<String> ternaryProperties_, String sigs_, String openModule_,
				String openStatements_, String functions_, String commandHeader_,
				String formula_, String commandScope_) {
			super(sourceFile_, property_, fieldName_, binaryProperties_,
					ternaryProperties_, sigs_, openModule_, openStatements_,
					functions_, commandHeader_, formula_, commandScope_);


			for( Operator operator: Operator.values() ){

				final String key = String.format("%s_%s_%s",propertyName, propertyFieldName.replaceAll("(\\)|\\()",""), commandHeader );

				final Map<String, Set<String>> map = 
						commandHeaderToProperties.containsKey(operator) ? 
								commandHeaderToProperties.get(operator) : 
									new HashMap(); 
								final Map<String,String> properties = analyzedProperties.get(operator);
								assert properties != null;

								if( properties.containsKey(key) && properties.get(key).trim() != "0" ){
									final Set<String> value = map.containsKey(commandHeader) ? map.get(commandHeader) : new HashSet<String>();

									if(properties.get(key).trim().equals("-1") && operator.equals(Operator.Inconsistent) ){
										//final String prop = "(not "+ property + ")";
										//value.add( prop );
										//Add the negation to the imply. A ^ B is unsat -> A => not B
										final Map<String, Set<String>> inconsistent = 
												commandHeaderToProperties.containsKey(Operator.Inconsistent) ? 
														commandHeaderToProperties.get(Operator.Inconsistent) : 
															new HashMap(); 
														final Set<String> implySet = inconsistent.containsKey(commandHeader) ? inconsistent.get(commandHeader) : new HashSet<String>() ;
														implySet.add( property );
														inconsistent.put(commandHeader, implySet);

														commandHeaderToProperties.put(Operator.Inconsistent, inconsistent);	
									}
									if(properties.get(key).trim().equals("1") && !operator.equals(Operator.Inconsistent) )
										value.add( property );
									map.put(commandHeader, value);

								}

								commandHeaderToProperties.put(operator, map);
			}
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

			for(Operator operator: Operator.values()){
				if( !commandHeaderToProperties.containsKey(operator) ) continue;
				Map<String, Set<String>> map = commandHeaderToProperties.get(operator);
				for(String commandHeader: map.keySet()){
					final String predName = "approx_"+operator+"_"+commandHeader;
					final String body = map.get(commandHeader).stream().collect(Collectors.joining(" \n "));
					newAlloySpec.append(String.format("pred %s{%n %s %n}%n", predName, body)).append("\n");
				}
			}

			return newAlloySpec.toString();
		}

		/**
		 * This function takes a property and returns a standard copy of it that could be compared with
		 * consistency map.
		 * @param props
		 * @return
		 */
		private final static String cleanProperty(final String property){

			final String propName = propertyNameExtractorFromProperty(property);
			String propField = fieldExtractorFromProperty(property);
			final boolean hasNot = property.trim().contains("not ");

			//(State<:holds).Mutex -> (State<:holds).C
			propField = propField.replaceAll("\\.[^\\(].*", ".C");
			//State.(State<:holds) -> A.(State<:holds)
			propField = propField.replaceAll("[^\\.]+\\.\\(", "A.(");
			//State.(State<:holds) -> State.r
			propField = propField.replaceAll("\\([^\\)]+\\)", "r");
			//Remove extra characters.
			propField = propField.replaceAll("\\)", "");

			return String.format("%s%s[%s]",hasNot? "not " : "",propName,propField).trim();

		}


		/**
		 * This function takes two sets of properties, and returns the consistent properties between two sets A and B
		 * 
		 */
		public final static Set<String> findConsistency(Set<String> A, Set<String> B){

			final Set<String> ConsistentProps = new HashSet<String>(); 

			for(String propA : A){
				final String cleanedPropA = cleanProperty(propA);
				//A property of A is consitent to itself
				//ConsistentProps.add(propA);
				for(String propB: B){
					final String cleanedPropB = cleanProperty(propB);

					if( PropertiesConsistencyChecking.consistentProps.get(cleanedPropA).contains(cleanedPropB) ){
						ConsistentProps.add(propB);
					}
				}
			}
			return ConsistentProps;
		}

		final public static Map<Pair<String,String>, Set<String>> findAllConsistencies(){
			final Map<Pair<String, String>, Set<String>> result = new HashMap<Pair<String,String>, Set<String>>(); 
			final Map<String, Set<String>> map = commandHeaderToProperties.get(Operator.Consistent);

			for(String commandName1: map.keySet()){
				for(String commandName2: map.keySet()){
					final Pair<String, String> cPair = new SymmetricPair<String, String>(commandName1, commandName2);
					if( cPair.a.equals(cPair.b) ) continue;
					if( result.containsKey(cPair) ) continue;
					result.put(cPair, findConsistency(map.get(commandName1),map.get(commandName2)));
				}
			}
			System.out.println("\n\tProperties\n");
			map.keySet().stream().forEach(a->System.out.printf("%s\t%d\t%s%n",a,map.get(a).size(),map.get(a)));


			System.out.println("\n\tConsistencies\n");

			result.keySet().stream().forEach(a->System.out.printf("%s\t%d\t%s%n",a,result.get(a).size(),result.get(a)));

			return result;
		}

		/**
		 * From two predicates, A and U, P_A is the properties consistent with A, Q_U is the properties implied from U.
		 * return is the properties from P_A that is consistent with all properties in Q_U
		 */
		final private static Set<String> findConsistentWithImply(final String predA, final String predU){
			final Set<String> result = new HashSet<String>();

			final Map<String, Set<String>> propsMap = commandHeaderToProperties.get(Operator.Consistent);
			final Map<String, Set<String>> ImplyMap = commandHeaderToProperties.get(Operator.CompleteHas);

			final Map<String, Set<String>> consistencyMap = PropertiesConsistencyChecking.consistentProps;

			final Set<String> propsA = propsMap.get(predA);
			final Set<String> propsImpliedU = ImplyMap.get(predU);

			for(final String propA: propsA){
				final String fldPropA = fieldExtractorFromProperty(propA);
				final String cleandFldPropA = cleanProperty(propA);


				final Set<String> propsUWithfldPropA = propsImpliedU.stream()
						.filter(a->a.contains(fldPropA))
						.map(a->cleanProperty(a))
						.collect(Collectors.toSet());
				final Set<String> consistentToPropA = consistencyMap.get(cleandFldPropA);

				//if all props from U is consistent with propA 
				boolean isConsistent = true;
				for(final String propU: propsUWithfldPropA){

					if( !consistentToPropA.contains(propU) ){
						isConsistent = false;
						break;
					}
				}
				if( isConsistent ) result.add(propA);
			}

			return Collections.unmodifiableSet(result);
		}

		final public static Map<Pair<String,String>, Set<Pair<String,String>>> findAllInconsistencies(){


			System.out.printf("%d\t%s%n",commandHeaderToProperties.get(Operator.Consistent).get("C").size(), commandHeaderToProperties.get(Operator.Consistent).get("C") );
			final Set<String> set1= findConsistentWithImply("C","U");
			System.out.printf("%d\t%s%n",set1.size(), set1 );


			final Map<String, Set<String>> ands = commandHeaderToProperties.get(Operator.Consistent);

			ands.keySet()
			.stream()
			.forEach(
					a->System.out.printf("%s\t%d\t%s\n",a,ands.get(a).size(),
							ands.get(a)));

			System.out.println("Imply\n");
			final Map<String, Set<String>> impls = commandHeaderToProperties.get(Operator.CompleteHas);
			impls.keySet()
			.stream()
			.forEach(
					a->System.out.printf("%s\t%d\t%s\n",a,impls.get(a).size(),
							impls.get(a)));

			System.out.println("Not Imply\n");
			final Map<String, Set<String>> incon = commandHeaderToProperties.get(Operator.Inconsistent);
			incon.keySet()
			.stream()
			.forEach(
					a->System.out.printf("%s\t%d\t%s\n",a,incon.get(a).size(),
							incon.get(a)));


			//System.exit(-10);

			final Map<Pair<String, String>, Set<Pair<String,String>>> result = new HashMap<Pair<String,String>, Set<Pair<String,String>>>();

			final Map<String, Set<String>> propsMap = commandHeaderToProperties.get(Operator.Consistent);
			final Map<String, Set<String>> inconsistencyMap = PropertiesConsistencyChecking.inconsistentProps;

			for(final String predA: propsMap.keySet()){

				for(final String predB: propsMap.keySet()){
					if(predA.equals(predB)) continue;

					for( final String propA: findConsistentWithImply(predA,"U")
							//		propsMap.get(predA) 
							){

						final String fldPropA = fieldExtractorFromProperty(propA);
						final String cleandFldPropA = cleanProperty(propA);

						final Set<String> propsBWithfldPropA = propsMap.get(predB)
								.stream()
								.filter(a->a.contains(fldPropA))
								.map(a->cleanProperty(a))
								.collect(Collectors.toSet());
						final Set<String> inconsistentToPropA = inconsistencyMap.get(cleandFldPropA);

						for(final String propB: propsBWithfldPropA){


							if(inconsistentToPropA.contains(propB)){

								/*if("GrabbedInOrder".equals(predA) && "GrabOrRelease".equals(predB)  )	
									System.out.println("hi");*/

								final Pair<String,String> pairKey = new Pair<String, String>(predA, predB);
								final Set<Pair<String,String>> set = result.containsKey(pairKey) ? result.get(pairKey) : new HashSet();
								final Pair<String,String> pairProps = new Pair<String, String>(propA, propB); 
								set.add(pairProps);
								result.put(pairKey,  set);
							}
						}

					}

				}

			}

			System.out.println("\n\tInconsistencies\n");
			result.keySet().stream().forEach(a->System.out.printf("(%s)\t%d\t(%s)%n%n",a,result.get(a).size(),result.get(a).toString().replaceAll(", ","\t") ));


			return result;
		}

		final public static Map<Pair<String,String>, Set<String>> findAllPropertiesDifference(Operator operator){

			final Map<Pair<String, String>, Set<String>> result = new HashMap<Pair<String,String>, Set<String>>();
			final Map<String, Set<String>> map = commandHeaderToProperties.get(operator);

			for(String commandHeaderA: map.keySet()){
				for(String commandHeaderB: map.keySet()){
					if( commandHeaderA.equals(commandHeaderB) ) continue;

					final Set<String> inconsistencyPropAPropB = new HashSet( map.get(commandHeaderA) );
					inconsistencyPropAPropB.removeAll( map.get(commandHeaderB) );
					result.put(new Pair(commandHeaderA ,commandHeaderB), inconsistencyPropAPropB); 
				}
			}
			System.out.println("\n\tDifferences "+operator +"\n");
			result.keySet().stream().forEach(a->System.out.printf("%s\t%d\t%s%n",a,result.get(a).size(),result.get(a)));

			return Collections.unmodifiableMap( result );
		}

		final public static Map<Pair<String,String>, Set<String>> findAllPropertiesDifferenceConsistency(){
			return findAllPropertiesDifference(Operator.Consistent);
		}

		final public static Map<Pair<String,String>, Set<String>> findAllPropertiesDifferenceImply(){

			for(Operator op :commandHeaderToProperties.keySet()){
				System.out.println( op );
				commandHeaderToProperties.get(op).keySet().forEach(
						a->System.out.printf("%s\t%d\t%s%n",a,commandHeaderToProperties.get(op).get(a).size(),
								commandHeaderToProperties.get(op).get(a)));
			}

			return findAllPropertiesDifference(Operator.CompleteHas);
		}


		/**
		 * Assuming the predicate A has property over relation r, i.e. A=> p(r).
		 * Then findInconsistencybyImply returns all the predicates has inconsistent
		 * property with A. B=>q(r) then p(r) and q(r) is UnSAT
		 * @return
		 */
		final public static Map<Pair<String,String>, Set<String>> findInconsistencybyImply(){

			final Map<String, Set<String>> map = commandHeaderToProperties.get(Operator.CompleteHas);
			final Map<Pair<String, String>, Set<String>> result = new HashMap<Pair<String,String>, Set<String>>(); 
			final Map<String, Set<String>> inconsistencyMap = PropertiesConsistencyChecking.inconsistentProps;


			for(String predA: map.keySet()){
				for(String propA: map.get(predA)){
					final String cleanedPropA = cleanProperty(propA);
					final Set<String> inconsistentToPropA =  inconsistencyMap.get(cleanedPropA) ;
					assert inconsistentToPropA != null;
					for(String predB: map.keySet()){
						if( predA.equals(predB) ) continue;
						final Set<String> propsB = map.get(predB).stream().map(a->cleanProperty(a)).collect(Collectors.toSet());
						final Set<String> intersectionInconsistentToPropAPropsB = new HashSet<String>(inconsistentToPropA);

						//System.out.printf("%s\t%s%n%s\t%s%n%s%n%n", predA, predB, cleanedPropA, inconsistentToPropA, propsB);

						for(String tmpiA: inconsistentToPropA){
							if(propsB.contains(tmpiA)){

								//System.out.println(tmpiA+" "+propsB);

								final Pair<String, String> pair = new Pair<String, String>(predA,predB);
								final Set<String> set = result.containsKey(pair) ? result.get(pair) : new HashSet<String>();
								set.add(tmpiA);
								result.put(pair,set);
							}
						}

						/*if(intersectionInconsistentToPropAPropsB.retainAll(propsB) && intersectionInconsistentToPropAPropsB.size() > 0){
							result.put(new Pair<String, String>(predA,predB), 
									intersectionInconsistentToPropAPropsB);
						}*/

					}
				}
			}

			result.keySet().stream().forEach(a->System.out.printf("%s\t%s\t%d\t%s%n",a.a,a.b,result.get(a).size(), result.get(a) ));

			return Collections.unmodifiableMap(result);
		}

		/**
		 * For the predicate in the form of A&B=>U, first find all properties of A and B that
		 * are consistent with U.
		 * Then find the inconsistencies within  
		 * @return
		 */
		public static Map<Pair<String,String>, Set<String>> findInconsistecyByConsistency(){
			final Map<String, Set<String>> map = commandHeaderToProperties.get(Operator.CompleteHas);
			final Map<Pair<String, String>, Set<String>> result = new HashMap<Pair<String,String>, Set<String>>();



			return Collections.unmodifiableMap(result);
		}
		
		
		static Predicate<String> isA = (String a)->{
			return a.equals("A");
		};
		static Predicate<String> isB = (String a)->{
			return a.equals("B");
		};
		static Predicate<String> isC = (String a)->{
			return a.equals("C");
		};
		
		/**
		 * See the documents.
		 * @return
		 */
		public static Map<Pair<String,String>, Pair<String,String>> find_I_a(){
			final Map<Pair<String,String>, Pair<String,String>> result = new HashMap<Pair<String,String>, Pair<String,String>>();
			
			final Map<String, Set<String>> conMap = commandHeaderToProperties.get(Operator.Consistent);
			final Map<String, Set<String>> inconMap = commandHeaderToProperties.get(Operator.Inconsistent);
			
			assert  conMap.keySet().equals(inconMap.keySet()); 

			final Set<String> preds = conMap.keySet().stream().filter(isA.or(isB).or(isC)).collect(Collectors.toSet());
			
			for( final String predAi :  preds){
				for( final String predAj :  preds){
					if( predAi.equals(predAj) ) continue;
					
					final Set<String> predAiConss  = conMap.get(predAi);
					final Set<String> predAjIncons = inconMap.get(predAj);
					
					//look for common properties.
					for(final String propAi: predAiConss){
						if( predAjIncons.contains(propAi) )
							result.put(new Pair<String,String>(predAi,predAj), new Pair<String,String>(propAi,propAi));
					}
					
				}
				
			}

			result.keySet().stream().forEach(a->System.out.printf("%s\t%s%n", a, result.get(a)));
			
			return Collections.unmodifiableMap(result);
		}


}
