package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import edu.mit.csail.sdg.alloy4.Pair;
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



	public SizeProperty createSizeInstance(final Class<? extends SizeProperty> clazz, final Locality local, final Emptiness empty) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);


		return (SizeProperty) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName,
				local, empty);
	}


	public Locality createLocalityInstance(final Class<? extends Locality> clazz, final Side side) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constructor for "+clazz);




		return (Locality) constructors[0].newInstance(RName, SName, SNext, SFirst,
				MiddleName, EndName, RConcreteName,
				SConcreteName, SConcreteNext, SConcreteFirst,
				MConcreteName, EConcreteName, side,
				"", "");
	}


	public Side createSideInstance(final Class<Side> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);


		return (Side) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName,
				EndNext, EndFirst, MiddleNext, MiddleFirst,
				EndConcreteNext, EndConcreteFirst,
				MiddleConcreteNext, MiddleConcreteFirst);

	}

	public Emptiness createEmptinessInstance(final Class<Emptiness> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);


		return (Emptiness) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName);
	}

	public Order createOrderInstance(final Class<? extends Order> clazz, final SizeProperty size) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);

		return (Order) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName, size);
	}


	public Map<String, Pair<String,String>> getAllProperties(){

		final TriplePorpertiesIterators iterators = new TriplePorpertiesIterators(this);


		//A map from each call to the actual pred
		Map<String, Pair<String, String>> preds = new TreeMap<>();

		for(Side side: iterators. new SideIterator(this)){
			for(Locality local: iterators. new LocalityIterator(this, side)){
				for(Emptiness empty: iterators. new EmptinessIterator(this)){
					for(SizeProperty size: iterators. new SizeIterator(this, local, empty)){
						if(!size.isConsistent()) continue;
						preds.put(size.genPredName(), new Pair(size.genPredCall(),  size.generateProp()));
						for(Order order: iterators. new OrderIterator(this, size)){
							if(!order.isConsistent()) continue;
							preds.put(order.genPredName(), new Pair(order.genPredCall(),  order.generateProp()));
						}
						
					}
				}
			}
		}

		return Collections.unmodifiableMap(preds);
	}

}
