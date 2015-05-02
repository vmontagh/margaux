package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TriplePropsTester {

	TripleBuilder builder;
	TriplePorpertiesIterators iterators;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		builder = new TripleBuilder( "r", "s", "s_next",
				"s_first", "m", "m_next",
				"m_first", "e", "e_next",
				"e_first", "R", "S",
				"so/next", "so/first", "M",
				"E", "eo/next", "eo/first",
				"mo/next", "mo/first");
		iterators = new TriplePorpertiesIterators(builder);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {

		//A map from each call to the actual pred
		Map<String, String> preds = new TreeMap<>();
		
		for(Side side: iterators. new SideIterator(builder)){
			System.out.println(side);
			for(Locality local: iterators. new LocalityIterator(builder, side)){
				System.out.println(local);
				for(Emptiness empty: iterators. new EmptinessIterator(builder)){
					System.out.println(empty);
					for(SizeProperty size: iterators. new SizeIterator(builder, local, empty)){
						if(!size.isConsistent()) continue;
						preds.put(size.genPredCall(), size.generateProp());
						
						System.out.println(size.genPredCall());
						System.out.println(size.generateProp());
						
						for(Order order: iterators. new OrderIterator(builder, size)){
							if(!order.isConsistent()) continue;
							preds.put(order.genPredCall(), order.generateProp());
							
							System.out.println(order.genPredCall());
							System.out.println(order.generateProp());
							
						}
					}
				}
			}
		}



		assertTrue(true);
	}

}
