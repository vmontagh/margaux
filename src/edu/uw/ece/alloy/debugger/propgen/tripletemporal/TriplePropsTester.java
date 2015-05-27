package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

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

		System.exit(-10);
		
		//A map from each call to the actual pred
		Map<String, String> preds = new TreeMap<>();
		
		for(Side side: iterators. new SideIterator(builder)){
			//System.out.println(side);
			for(Locality local: iterators. new LocalityIterator(builder, side)){
				//System.out.println(local);
				for(Emptiness empty: iterators. new EmptinessIterator(builder)){
					//System.out.println(empty);
					for(SizeProperty size: iterators. new SizeIterator(builder, local, empty)){
						if(!size.isConsistent()) continue;
						preds.put(size.genPredCall(), size.generateProp());
						
						//System.out.println(size.genPredCall());
						//System.out.println(size.generateProp());
						
						for(Order order: iterators. new OrderIterator(builder, size)){
							if(!order.isConsistent()) continue;
							preds.put(order.genPredCall(), order.generateProp());
							
							//System.out.println(order.genPredCall());
							//System.out.println(order.generateProp());
							
							
							//Composite structures for two size and orders
							for(SizeProperty size2: iterators. new SizeIterator(builder, local, empty)){
								if(!size2.isConsistent()) continue;
								
								for(CompositeSizes compositeSizes: iterators. new CompositeSizesIterator(builder, size, size2)){
									if(!compositeSizes.isConsistent()) continue;
									//Add to the list here
								}

								for(Order order2: iterators. new OrderIterator(builder, size2)){
									if(!order2.isConsistent()) continue;
									
									for(CompositeOrders compositeOrders: iterators. new CompositeOrdersIterator(builder, order, order2)){
										if(!compositeOrders.isConsistent()) continue;
										//Add to the list here
									}
									
								}
								
							}
							
							
							
						}
					}
				}
			}
		}
		assertTrue(true);
	}
	
	@Test
	public void test_CompositeOrders() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		Side side = builder.createSideInstance(SideMiddle.class);
		Emptiness empty = builder.createEmptinessInstance(EmptyNone.class);
		
		Locality local = builder.createLocalityInstance(Global.class, side);
		SizeProperty size1 = builder.createSizeInstance(SizeGrowth.class, local, empty);
		SizeProperty size2 = builder.createSizeInstance(SizeShrink.class, local, empty);
		
		Order order1 = builder.createOrderInstance(OrderDecrease.class, size1);
		Order order2 = builder.createOrderInstance(OrderDecrease.class, size2);
		
		CompositeOrders compositeOrder = builder.createCompositeOrdersInstance(CompositeOrdersOR.class, order1, order2);
		compositeOrder.genBody();
		
		CompositeSizes compositeSize = builder.createCompositeSizesInstance(CompositeSizesOR.class, size1, size2);
		compositeSize.genBody();
		compositeSize.isConsistent();
		
		System.out.println(compositeSize.genPredName());
		
		
	}
	

	@Test
	public void test_CompositeOrders_Consistency_SameSizes() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		Side side = builder.createSideInstance(SideMiddle.class);
		Emptiness empty = builder.createEmptinessInstance(EmptyNone.class);
		
		Locality local = builder.createLocalityInstance(Global.class, side);
		SizeProperty size1 = builder.createSizeInstance(SizeGrowth.class, local, empty);
		SizeProperty size2 = builder.createSizeInstance(SizeShrink.class, local, empty);
		
		Order order1 = builder.createOrderInstance(OrderDecrease.class, size1);
		Order order2 = builder.createOrderInstance(OrderDecrease.class, size1);
		
		CompositeOrders compositeOrder = builder.createCompositeOrdersInstance(CompositeOrdersOR.class, order1, order2);
		assertFalse(compositeOrder.isConsistent() );
		
		order2 = builder.createOrderInstance(OrderDecrease.class, size2);
		compositeOrder = builder.createCompositeOrdersInstance(CompositeOrdersOR.class, order1, order2);
		assertTrue(compositeOrder.isConsistent() );
	}
	
	
	
}
