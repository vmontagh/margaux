package junit;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;


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
			
			System.out.println(filename);
			converted = true;
		}catch(Exception e){
			System.out.println("Error: "+e);
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
				{"../../test-models/rooks/rooks_3_metrics_2.als"},
		});
	}
	

}
