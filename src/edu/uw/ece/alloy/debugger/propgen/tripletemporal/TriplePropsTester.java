package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParamLazy;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.Dependency;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.IffPropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.TemporalPropertiesGenerator;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.VacPropertyToAlloyCode;
import edu.uw.ece.alloy.util.Utils;

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

		builder = new TripleBuilder("r", "s", "s_next", "s_first", "m", "m_next",
				"m_first", "e", "e_next", "e_first", "r", "S", "so/next", "so/first",
				"M", "E", "eo/next", "eo/first", "mo/next", "mo/first");
		iterators = new TriplePorpertiesIterators(builder);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {

		// A map from each call to the actual pred
		Map<String, String> preds = new TreeMap<>();

		for (Sd side : iterators.new SideIterator(builder)) {
			// System.out.println(side);
			for (Lclty local : iterators.new LocalityIterator(builder, side)) {
				// System.out.println(local);
				for (Emptnes empty : iterators.new EmptinessIterator(builder)) {
					// System.out.println(empty);
					for (SzPrpty size : iterators.new SizeIterator(builder, local,
							empty)) {
						if (!size.isConsistent())
							continue;
						preds.put(size.genPredCall(), size.generateProp());

						// System.out.println(size.genPredCall());
						// System.out.println(size.generateProp());

						for (Ord order : iterators.new OrderIterator(builder, size)) {
							if (!order.isConsistent())
								continue;
							preds.put(order.genPredCall(), order.generateProp());

							// System.out.println(order.genPredCall());
							// System.out.println(order.generateProp());

							// Composite structures for two size and orders
							for (SzPrpty size2 : iterators.new SizeIterator(builder, local,
									empty)) {
								if (!size2.isConsistent())
									continue;

								for (CmpstSz compositeSizes : iterators.new CompositeSizesIterator(
										builder, size, size2)) {
									if (!compositeSizes.isConsistent())
										continue;
									// Add to the list here
								}

								for (Ord order2 : iterators.new OrderIterator(builder, size2)) {
									if (!order2.isConsistent())
										continue;

									for (CmpstOrds compositeOrders : iterators.new CompositeOrdersIterator(
											builder, order, order2)) {
										if (!compositeOrders.isConsistent())
											continue;
										// Add to the list here
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
	public void test_CompositeOrders()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Sd side = builder.createSideInstance(SdMdl.class);
		Emptnes empty = builder.createEmptinessInstance(EmptNon.class);

		Lclty local = builder.createLocalityInstance(Glbl.class, side);
		SzPrpty size1 = builder.createSizeInstance(SzGrwt.class, local, empty);
		SzPrpty size2 = builder.createSizeInstance(SzShrnk.class, local, empty);

		Ord order1 = builder.createOrderInstance(OrdDcrs.class, size1);
		Ord order2 = builder.createOrderInstance(OrdDcrs.class, size2);

		CmpstOrds compositeOrder = builder
				.createCompositeOrdersInstance(CmpstOrdOR.class, order1, order2);
		compositeOrder.genBody();

		CmpstSz compositeSize = builder
				.createCompositeSizesInstance(CmpstSzOR.class, size1, size2);
		compositeSize.genBody();
		compositeSize.isConsistent();

		System.out.println(compositeSize.genPredName());

	}

	@Test
	public void test_CompositeOrders_Consistency_SameSizes()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Sd side = builder.createSideInstance(SdMdl.class);
		Emptnes empty = builder.createEmptinessInstance(EmptNon.class);

		Lclty local = builder.createLocalityInstance(Glbl.class, side);
		SzPrpty size1 = builder.createSizeInstance(SzGrwt.class, local, empty);
		SzPrpty size2 = builder.createSizeInstance(SzShrnk.class, local, empty);

		Ord order1 = builder.createOrderInstance(OrdDcrs.class, size1);
		Ord order2 = builder.createOrderInstance(OrdDcrs.class, size1);

		CmpstOrds compositeOrder = builder
				.createCompositeOrdersInstance(CmpstOrdOR.class, order1, order2);
		assertFalse(compositeOrder.isConsistent());

		order2 = builder.createOrderInstance(OrdDcrs.class, size2);
		compositeOrder = builder.createCompositeOrdersInstance(CmpstOrdOR.class,
				order1, order2);
		assertTrue(compositeOrder.isConsistent());

		CmpstSz compositeSizes = builder
				.createCompositeSizesInstance(CmpstSzOR.class, size1, size2);
		assertTrue(compositeSizes.isConsistent());

		compositeSizes = builder.createCompositeSizesInstance(CmpstSzOR.class,
				size1, size1);
		assertFalse(compositeSizes.isConsistent());

	}

	@Test
	public void test_generate_papers_properties()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Sd side = builder.createSideInstance(SdEnd.class);
		Emptnes empty = builder.createEmptinessInstance(EmptNon.class);

		Lclty local = builder.createLocalityInstance(Lcl.class, side);
		SzPrpty size1 = builder.createSizeInstance(SzGrwt.class, local, empty);

		Ord order1 = builder.createOrderInstance(OrdIncrs.class, size1);

		System.out.println(order1.genBody());

		empty = builder.createEmptinessInstance(EmptEnd.class);

		size1 = builder.createSizeInstance(SzShrnkStrc.class, local, empty);

		order1 = builder.createOrderInstance(OrdDcrsStrc.class, size1);

		System.out.println(order1.genBody());

		side = builder.createSideInstance(SdMdl.class);
		local = builder.createLocalityInstance(Glbl.class, side);
		size1 = builder.createSizeInstance(SzShrnkStrc.class, local, empty);

		System.out.println(size1.genBody());

		size1 = builder.createSizeInstance(SzNChng.class, local, empty);
		order1 = builder.createOrderInstance(OrdIncrs.class, size1);

		System.out.println(order1.genBody());
	}

	@Test
	public void test_generate_all_properties()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		System.out.println(builder.getAllProperties().size());
	}

	/**
	 * The following 'test_tobe_excluded_' set of test cases are developed to
	 * check whether the properties are inconsistent internally. If so, the the
	 * property generator has to excluded them.
	 * 
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * 
	 */
	@Test
	public void test_tobe_excluded_OrdIncrs_SzNChng_Lcl_SdMdl_EmptNon__VAC_OrdIncrs_SzNChng_Lcl_SdMdl_EmptNon_()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Sd side = builder.createSideInstance(SdMdl.class);
		Lclty local = builder.createLocalityInstance(Lcl.class, side);
		Emptnes empty = builder.createEmptinessInstance(EmptNon.class);
		SzPrpty size = builder.createSizeInstance(SzNChng.class, local, empty);
		Ord order = builder.createOrderInstance(OrdIncrs.class, size);

		String predBodyA = order.generateProp();
		String predBodyB = order.generateProp();

		String predCallA = order.genPredCall();
		String predCallB = order.genPredCall();

		String predNameA = order.genPredName();
		String predNameB = order.genPredName();

		File tempDirectory4Test = new File("tmp/testing");

		final List<Dependency> dependencies = new LinkedList<Dependency>();
		dependencies.add(Dependency.EMPTY_DEPENDENCY.createIt(
				new File(
						TemporalPropertiesGenerator.relationalPropModuleOriginal.getName()),
				Utils.readFile(TemporalPropertiesGenerator.relationalPropModuleOriginal
						.getAbsolutePath())));

		final String SigDecl = "sig M,E{}\nsig S{r:M->E}";
		final String ModuleS = "open util/ordering [S] as so";
		final String ModuleM = "open util/ordering [M] as mo";
		final String ModuleE = "open util/ordering [E] as eo";
		final String RelationProps = "open relational_properties";
		final String header = ModuleS + '\n' + ModuleM + '\n' + ModuleE + '\n'
				+ RelationProps + '\n' + SigDecl + '\n';
		final String scope = " for 5";
		final String field = "r";

		AlloyProcessingParam paramCreator = AlloyProcessingParamLazy.EMPTY_PARAM;

		PropertyToAlloyCode creator = VacPropertyToAlloyCode.EMPTY_CONVERTOR
				.createIt(predBodyA, predBodyB, predCallA, predCallB, predNameA,
						predNameB, dependencies, paramCreator, header, scope, field// [tmpDirectory],
																																			 // tempDirectory4Test
		);

		AlloyProcessingParam param = creator.generate();

		try {
			param.changeTmpDirectory(tempDirectory4Test).dumpAll();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void test_generate_all_properties_csv()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, Err {

		System.out.println(builder.getAllPropertiesCSV());
		String csvNameProps = builder.getAllPropertiesCSV().stream()
				.collect(Collectors.joining("\n"));
		csvNameProps = csvNameProps.replaceAll("Ord", "Order");
		csvNameProps = csvNameProps.replaceAll("Sz", "Size");
		csvNameProps = csvNameProps.replaceAll("Glbl", "Global");
		csvNameProps = csvNameProps.replaceAll("Sd", "Side");
		csvNameProps = csvNameProps.replaceAll("Empt", "Empty");
		csvNameProps = csvNameProps.replaceAll("Shrnk", "Shrink");
		csvNameProps = csvNameProps.replaceAll("Grwt", "Growth");
		csvNameProps = csvNameProps.replaceAll("Lcl", "Local");
		csvNameProps = csvNameProps.replaceAll("Mdl", "Middle");
		csvNameProps = csvNameProps.replaceAll("Non", "None");
		csvNameProps = csvNameProps.replaceAll("Strt", "Start");
		csvNameProps = csvNameProps.replaceAll("Strc", "Strict");
		csvNameProps = csvNameProps.replaceAll("Incrs", "Increase");
		csvNameProps = csvNameProps.replaceAll("Dcrs", "Decrease");
		csvNameProps = csvNameProps.replaceAll("NChng", "NoChange");

		Util.writeAll(
				"/Users/vajih/Documents/Papers/papers/debugger/reviews/data/relational_analyzer/csv/props_detail.csv",
				csvNameProps);

	}

	// Example in the models paper
	@Test
	public void test_ExpandHeadOfRight_MiddleStatic()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Sd side = builder.createSideInstance(SdEnd.class);
		Lclty local = builder.createLocalityInstance(Lcl.class, side);
		Emptnes empty = builder.createEmptinessInstance(EmptNon.class);
		SzPrpty size = builder.createSizeInstance(SzGrwt.class, local, empty);
		Ord order = builder.createOrderInstance(OrdIncrs.class, size);

		System.out.println(order.generateProp());
		System.out.println(order.genParametesCall());

	}

	// Example in the models paper
	@Test
	public void test_ContracttTaillOfRight_MiddleStatic()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Sd side = builder.createSideInstance(SdEnd.class);
		Lclty local = builder.createLocalityInstance(Lcl.class, side);
		Emptnes empty = builder.createEmptinessInstance(EmptNon.class);
		SzPrpty size = builder.createSizeInstance(SzShrnkStrc.class, local, empty);
		Ord order = builder.createOrderInstance(OrdDcrs.class, size);

		System.out.println(order.generateProp());

		System.out.println(order.genParametesCall());

	}

	// Example in the models paper
	@Test
	public void test_ContracttMiddle_LastLeftEmpty()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Sd side = builder.createSideInstance(SdMdl.class);
		Lclty local = builder.createLocalityInstance(Glbl.class, side);
		Emptnes empty = builder.createEmptinessInstance(EmptEnd.class);
		SzPrpty size = builder.createSizeInstance(SzShrnkStrc.class, local, empty);

		System.out.println(size.generateProp());
		System.out.println(size.genParametesCall());

	}

	// Example in the models paper
	@Test
	public void test_MutateHeadOfMiddle_RightStatic()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// OrderDecease_SizeNoChange_Global_SideEnd_EmptyNone_
		Sd side = builder.createSideInstance(SdEnd.class);
		Lclty local = builder.createLocalityInstance(Glbl.class, side);
		Emptnes empty = builder.createEmptinessInstance(EmptNon.class);
		SzPrpty size = builder.createSizeInstance(SzNChng.class, local, empty);
		Ord order = builder.createOrderInstance(OrdDcrs.class, size);

		System.out.println(order.generateProp());
		System.out.println(order.genParametesCall());

	}

	// Example in the models paper
	@Test
	public void test_OrdIncrsStrc_SzNChng_Lcl_SdEnd_EmptNon_IFF_OrdDcrsStrc_SzNChng_Lcl_SdEnd_EmptNon_()
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// OrderDecease_SizeNoChange_Global_SideEnd_EmptyNone_
		Sd side = builder.createSideInstance(SdEnd.class);
		Lclty local = builder.createLocalityInstance(Lcl.class, side);
		Emptnes empty = builder.createEmptinessInstance(EmptNon.class);
		SzPrpty size = builder.createSizeInstance(SzNChng.class, local, empty);
		Ord order1 = builder.createOrderInstance(OrdDcrsStrc.class, size);

		Ord order2 = builder.createOrderInstance(OrdIncrsStrc.class, size);

		String predBodyA = order1.generateProp();
		String predBodyB = order2.generateProp();

		String predCallA = order1.genPredCall();
		String predCallB = order2.genPredCall();

		String predNameA = order1.genPredName();
		String predNameB = order2.genPredName();

		File tempDirectory4Test = new File("tmp/testing");

		final List<Dependency> dependencies = new LinkedList<Dependency>();
		dependencies.add(Dependency.EMPTY_DEPENDENCY.createIt(
				new File(
						TemporalPropertiesGenerator.relationalPropModuleOriginal.getName()),
				Utils.readFile(TemporalPropertiesGenerator.relationalPropModuleOriginal
						.getAbsolutePath())));

		final String SigDecl = "sig M,E{}\nsig S{r:M->E}";
		final String ModuleS = "open util/ordering [S] as so";
		final String ModuleM = "open util/ordering [M] as mo";
		final String ModuleE = "open util/ordering [E] as eo";
		final String RelationProps = "open relational_properties";
		final String header = ModuleS + '\n' + ModuleM + '\n' + ModuleE + '\n'
				+ RelationProps + '\n' + SigDecl + '\n';
		final String scope = " for 5";
		final String field = "r";

		AlloyProcessingParam paramCreator = AlloyProcessingParamLazy.EMPTY_PARAM;

		PropertyToAlloyCode creator = IffPropertyToAlloyCode.EMPTY_CONVERTOR
				.createIt(predBodyA, predBodyB, predCallA, predCallB, predNameA,
						predNameB, dependencies, paramCreator, header, scope, field// [tmpDirectory],
																																			 // tempDirectory4Test
		);

		AlloyProcessingParam param = creator.generate();

		try {
			param.changeTmpDirectory(tempDirectory4Test).dumpAll();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
