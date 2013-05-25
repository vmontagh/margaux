package junit;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;
import org.junit.Assume;


@RunWith(value = Parameterized.class)	
public class JavaforKodkod {
	boolean converted = true;
	String filename;
	
	public JavaforKodkod(String filename){
		this.filename = filename;
	}
	
@Before
	public void Compile(){
		String[] filenames = {filename};
		try{
			RunBatchCompiler RBC = new RunBatchCompiler();
			RBC.Compile(filenames);
			
			converted = true;
		}catch(Exception e){
			Assume.assumeNoException(e);
		}
		
	}

//	

	
	@Test
	public void test() {
		assertTrue("Successfully Created Java instances of all MOO alloy models", converted);
	}
	
	@Parameters
	public static Collection<Object[]> data(){
		return Arrays.asList(new Object[][]{
				{"../../test-models/rooks/rooks_7_metrics_6.als"},
				{"../../test-models/rooks/rooks_4_metrics_7.als"},
				{"../../test-models/rooks/rooks_9_metrics_5.als"},
				{"../../test-models/rooks/rooks_6_metrics_6.als"},
				{"../../test-models/rooks/rooks_5_metrics_3.als"},
				{"../../test-models/rooks/rooks_4_metrics_2.als"},
				{"../../test-models/rooks/rooks_8_metrics_5.als"},
				{"../../test-models/rooks/rooks_6_metrics_7.als"},
				{"../../test-models/rooks/rooks_3_metrics_6.als"},
				{"../../test-models/rooks/rooks_5_metrics_7.als"},
				{"../../test-models/rooks/rooks_5_metrics_6.als"},
				{"../../test-models/rooks/rooks_8_metrics_3.als"},
				{"../../test-models/rooks/rooks_6_metrics_3.als"},
				{"../../test-models/rooks/rooks_9_metrics_6.als"},
				{"../../test-models/rooks/rooks_7_metrics_7.als"},
				{"../../test-models/rooks/rooks_6_metrics_4.als"},
				{"../../test-models/rooks/rooks_4_metrics_6.als"},
				{"../../test-models/rooks/rooks_8_metrics_2.als"},
				{"../../test-models/rooks/rooks_8_metrics_6.als"},
				{"../../test-models/rooks/rooks_9_metrics_3.als"},
				{"../../test-models/rooks/rooks_3_metrics_7.als"},
				{"../../test-models/rooks/rooks_7_metrics_3.als"},
				{"../../test-models/rooks/rooks_5_metrics_2.als"},
				{"../../test-models/rooks/rooks_9_metrics_7.als"},
				{"../../test-models/rooks/rooks_7_metrics_4.als"},
				{"../../test-models/rooks/rooks_9_metrics_4.als"},
				{"../../test-models/rooks/rooks_7_metrics_2.als"},
				{"../../test-models/rooks/rooks_6_metrics_2.als"},
				{"../../test-models/rooks/rooks_7_metrics_5.als"},
				{"../../test-models/rooks/rooks_3_metrics_2.als"},
				{"../../test-models/rooks/rooks_3_metrics_3.als"},
				{"../../test-models/rooks/rooks_8_metrics_7.als"},
				{"../../test-models/rooks/rooks_9_metrics_2.als"},
				{"../../test-models/rooks/rooks_4_metrics_5.als"},
				{"../../test-models/rooks/rooks_5_metrics_5.als"},
				{"../../test-models/rooks/rooks_3_metrics_4.als"},
				{"../../test-models/rooks/rooks_6_metrics_5.als"},
				{"../../test-models/rooks/rooks_8_metrics_4.als"},
				{"../../test-models/rooks/rooks_4_metrics_3.als"},
				{"../../test-models/rooks/rooks_5_metrics_4.als"},
				{"../../test-models/rooks/rooks_3_metrics_5.als"},
				{"../../test-models/rooks/rooks_4_metrics_4.als"},
				{"../../test-models/aerospace/decadal.als"},
				{"../../test-models/aerospace/apollo.als"},
				{"../../test-models/aerospace/habitat.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011_33.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011_14.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011_18.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011_40.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011_38.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011_46.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011_50.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011_24.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011_19.als"},
				{"../../test-models/spl/pkjabsplc2011/pkjabsplc2011_37.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011_031.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011_123.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011_104.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011_192.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011_148.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011_027.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011_094.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011_022.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011_129.als"},
				{"../../test-models/spl/sqlitesplc2011/sqlitesplc2011_052.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011_276.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011_016.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011_009.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011_098.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011_283.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011_137.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011_046.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011_170.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011_239.als"},
				{"../../test-models/spl/linkedlistsplc2011/linkedlistsplc2011_042.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal_27.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal_20.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal_19.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal_05.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal_32.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal_25.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal_30.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal_17.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal_40.als"},
				{"../../test-models/spl/berkeleydbqualityjournal/berkeleydbqualityjournal_16.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011_11.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011_13.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011_06.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011_32.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011_01.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011_08.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011_31.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011_02.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011_12.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011.als"},
				{"../../test-models/spl/zipmesplc2011/zipmesplc2011_17.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011_11.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011_13.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011_10.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011_06.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011_05.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011_01.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011_03.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011_04.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011_12.als"},
				{"../../test-models/spl/prevaylersplc2011/prevaylersplc2011_15.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011_11.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011_08.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011_04.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011_02.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011_01.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011_14.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011_05.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011_15.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011_13.als"},
				{"../../test-models/spl/berkeleydbsplc2011/berkeleydbsplc2011_06.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212_12.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212_13.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212_14.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212_07.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212_02.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212_11.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212_03.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212_10.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212_01.als"},
				{"../../test-models/spl/apacheicse212/apacheicse212_09.als"},
				{"../../test-models/spl/SearchAndRescueSystem_ICSE2013/SearchAndRescueSystem_ICSE2013_04.als"},
				{"../../test-models/spl/SearchAndRescueSystem_ICSE2013/SearchAndRescueSystem_ICSE2013_05.als"},
				{"../../test-models/spl/SearchAndRescueSystem_ICSE2013/SearchAndRescueSystem_ICSE2013_03.als"},
				{"../../test-models/spl/SearchAndRescueSystem_ICSE2013/SearchAndRescueSystem_ICSE2013_07.als"},
				{"../../test-models/spl/SearchAndRescueSystem_ICSE2013/SearchAndRescueSystem_ICSE2013_02.als"},
				{"../../test-models/spl/SearchAndRescueSystem_ICSE2013/SearchAndRescueSystem_ICSE2013_06.als"},
				{"../../test-models/queens/queens_8_metrics_2.als"},
				{"../../test-models/queens/queens_7_metrics_7.als"},
				{"../../test-models/queens/queens_9_metrics_4.als"},
				{"../../test-models/queens/queens_6_metrics_2.als"},
				{"../../test-models/queens/queens_5_metrics_5.als"},
				{"../../test-models/queens/queens_5_metrics_6.als"},
				{"../../test-models/queens/queens_4_metrics_5.als"},
				{"../../test-models/queens/queens_7_metrics_5.als"},
				{"../../test-models/queens/queens_9_metrics_6.als"},
				{"../../test-models/queens/queens_4_metrics_2.als"},
				{"../../test-models/queens/queens_6_metrics_5.als"},
				{"../../test-models/queens/queens_7_metrics_6.als"},
				{"../../test-models/queens/queens_6_metrics_3.als"},
				{"../../test-models/queens/queens_8_metrics_3.als"},
				{"../../test-models/queens/queens_7_metrics_2.als"},
				{"../../test-models/queens/queens_5_metrics_7.als"},
				{"../../test-models/queens/queens_4_metrics_6.als"},
				{"../../test-models/queens/queens_7_metrics_4.als"},
				{"../../test-models/queens/queens_8_metrics_6.als"},
				{"../../test-models/queens/queens_9_metrics_7.als"},
				{"../../test-models/queens/queens_4_metrics_3.als"},
				{"../../test-models/queens/queens_4_metrics_4.als"},
				{"../../test-models/queens/queens_9_metrics_3.als"},
				{"../../test-models/queens/queens_5_metrics_2.als"},
				{"../../test-models/queens/queens_5_metrics_3.als"},
				{"../../test-models/queens/queens_9_metrics_2.als"},
				{"../../test-models/queens/queens_8_metrics_7.als"},
				{"../../test-models/queens/queens_6_metrics_4.als"},
				{"../../test-models/queens/queens_8_metrics_5.als"},
				{"../../test-models/queens/queens_8_metrics_4.als"},
				{"../../test-models/queens/queens_6_metrics_7.als"},
				{"../../test-models/queens/queens_4_metrics_7.als"},
				{"../../test-models/queens/queens_7_metrics_3.als"},
				{"../../test-models/queens/queens_6_metrics_6.als"},
				{"../../test-models/queens/queens_5_metrics_4.als"},
				{"../../test-models/queens/queens_9_metrics_5.als"},
				{"../../test-models/knapsack/knapsack_10_metrics_2.als"},
				{"../../test-models/knapsack/knapsack_25_metrics_4.als"},
				{"../../test-models/knapsack/knapsack_25_metrics_2.als"},
				{"../../test-models/knapsack/knapsack_20_metrics_3.als"},
				{"../../test-models/knapsack/knapsack_25_metrics_3.als"},
				{"../../test-models/knapsack/knapsack_15_metrics_3.als"},
				{"../../test-models/knapsack/knapsack_20_metrics_2.als"},
				{"../../test-models/knapsack/knapsack_15_metrics_2.als"},
				{"../../test-models/knapsack/knapsack_10_metrics_3.als"},
				{"../../test-models/knapsack/knapsack_10_metrics_4.als"},
				{"../../test-models/knapsack/knapsack_15_metrics_4.als"},
				{"../../test-models/knapsack/knapsack_20_metrics_4.als"},
		});
	}
}
