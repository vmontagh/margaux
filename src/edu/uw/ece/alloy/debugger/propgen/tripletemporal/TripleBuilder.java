package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.CompositeOrdersIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.CompositeSizesIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.EmptinessIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.LocalityIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.OrderIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.SideIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.SizeIterator;

public class TripleBuilder {

	public final String RName;
	public final String SName, SNext, SFirst;
	public final String MiddleName,MiddleNext, MiddleFirst;
	public final String EndName, EndNext, EndFirst;

	public final String RConcreteName;
	public final String SConcreteName;
	public final String SConcreteNext;
	public final String SConcreteFirst;
	public final String MConcreteName;
	public final String EConcreteName;

	public final String EndConcreteNext, EndConcreteFirst;  
	public final String MiddleConcreteNext, MiddleConcreteFirst; 




	public TripleBuilder(String rName, String sName, String sNext,
			String sFirst, String middleName, String middleNext,
			String middleFirst, String endName, String endNext,
			String endFirst, String rConcreteName, String sConcreteName,
			String sConcreteNext, String sConcreteFirst, String mConcreteName,
			String eConcreteName, String endConcreteNext,
			String endConcreteFirst, String middleConcreteNext,
			String middleConcreteFirst) {
		super();
		RName = rName;
		SName = sName;
		SNext = sNext;
		SFirst = sFirst;
		MiddleName = middleName;
		MiddleNext = middleNext;
		MiddleFirst = middleFirst;
		EndName = endName;
		EndNext = endNext;
		EndFirst = endFirst;
		RConcreteName = rConcreteName;
		SConcreteName = sConcreteName;
		SConcreteNext = sConcreteNext;
		SConcreteFirst = sConcreteFirst;
		MConcreteName = mConcreteName;
		EConcreteName = eConcreteName;
		EndConcreteNext = endConcreteNext;
		EndConcreteFirst = endConcreteFirst;
		MiddleConcreteNext = middleConcreteNext;
		MiddleConcreteFirst = middleConcreteFirst;
	}

	public TripleBuilder(String rConcreteName, String sConcreteName,
			String sConcreteNext, String sConcreteFirst, String mConcreteName,
			String eConcreteName, String endConcreteNext,
			String endConcreteFirst, String middleConcreteNext,
			String middleConcreteFirst) {
		this(
				"r", "s", "s_next", "s_first",
				"m", "m_next", "m_first", 
				"e", "e_next", "e_first",
				rConcreteName,
				sConcreteName,
				sConcreteNext,
				sConcreteFirst,
				mConcreteName,
				eConcreteName,
				endConcreteNext,
				endConcreteFirst,
				middleConcreteNext,
				middleConcreteFirst);
	}



	public SzPrpty createSizeInstance(final Class<? extends SzPrpty> clazz, final Lclty local, final Emptnes empty) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);


		return (SzPrpty) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName,
				local, empty);
	}


	public Lclty createLocalityInstance(final Class<? extends Lclty> clazz, final Sd side) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constructor for "+clazz);




		return (Lclty) constructors[0].newInstance(RName, SName, SNext, SFirst,
				MiddleName, EndName, RConcreteName,
				SConcreteName, SConcreteNext, SConcreteFirst,
				MConcreteName, EConcreteName, side,
				"", "");
	}


	public Sd createSideInstance(final Class<? extends Sd> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);


		return (Sd) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName,
				EndNext, EndFirst, MiddleNext, MiddleFirst,
				EndConcreteNext, EndConcreteFirst,
				MiddleConcreteNext, MiddleConcreteFirst);

	}

	public Emptnes createEmptinessInstance(final Class<? extends Emptnes> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);


		return (Emptnes) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName);
	}

	public Ord createOrderInstance(final Class<? extends Ord> clazz, final SzPrpty size) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);

		return (Ord) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName, size);
	}

	public CmpstOrds createCompositeOrdersInstance(final Class<? extends CmpstOrds> clazz, final Ord order1, final Ord order2) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);

		return (CmpstOrds) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName, order1, order2);
	}
	
	public CmpstSz createCompositeSizesInstance(final Class<? extends CmpstSz> clazz, final SzPrpty size1, final SzPrpty size2) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);

		return (CmpstSz) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName, size1, size2);
	}

	
	
	/**
	 * 
	 * @return output is the list of generated predicates. (predicateName->(PredicateCAll, PredicateFulBody))
	 */
	public Map<String, Pair<String,String>> getAllProperties(){

		final TriplePorpertiesIterators iterators = new TriplePorpertiesIterators(this);


		//A map from each call to the actual pred
		Map<String, Pair<String, String>> preds = new TreeMap<>();
		Set<String> revComposite = new HashSet<String>();

		for(Sd side: iterators. new SideIterator(this)){
			for(Lclty local: iterators. new LocalityIterator(this, side)){
				for(Emptnes empty: iterators. new EmptinessIterator(this)){
					for(SzPrpty size: iterators. new SizeIterator(this, local, empty)){
						if(!size.isConsistent()) continue;
						preds.put(size.genPredName(), new Pair(size.genPredCall(),  size.generateProp()));
						
						for(Ord order: iterators. new OrderIterator(this, size)){
							if(!order.isConsistent()) continue;
							preds.put(order.genPredName(), new Pair(order.genPredCall(),  order.generateProp()));
							
							//Composite structures for two size and orders
							for(SzPrpty size2: iterators. new SizeIterator(this, local, empty)){
								if(!size2.isConsistent()) continue;
								
								//record the reverse in advance
								for(CmpstSz compositeSizes: iterators. new CompositeSizesIterator(this, size2, size)){
									if(!compositeSizes.isConsistent()) continue;
									//Add to the list here
									revComposite.add(compositeSizes.genPredName());
								}
								for(CmpstSz compositeSizes: iterators. new CompositeSizesIterator(this, size, size2)){
									if(!compositeSizes.isConsistent()) continue;
									if(revComposite.contains(compositeSizes.genPredName())) break;
									//Add to the list here
									preds.put(compositeSizes.genPredName(), new Pair(compositeSizes.genPredCall(),  compositeSizes.generateProp()));
								}

								for(Ord order2: iterators. new OrderIterator(this, size2)){
									if(!order2.isConsistent()) continue;
									
									//record the reverse in advance
									for(CmpstOrds compositeOrders: iterators. new CompositeOrdersIterator(this, order2, order)){
										if(!compositeOrders.isConsistent()) continue;
										//Add to the list here
										revComposite.add(compositeOrders.genPredName());
									}
									
									for(CmpstOrds compositeOrders: iterators. new CompositeOrdersIterator(this, order, order2)){
										if(!compositeOrders.isConsistent()) continue;
										if(revComposite.contains(compositeOrders.genPredName())) break;
										//Add to the list here
										preds.put(compositeOrders.genPredName(), new Pair(compositeOrders.genPredCall(),  compositeOrders.generateProp()));
									}
									
								}
								
							}
							
						}
						//break;
						
					}
					//break;
				}
				//break;
			}
			//break;
		}

		//System.out.println(preds.size());
		
		//System.exit(-1);
		
		return Collections.unmodifiableMap(preds);
	}

}
