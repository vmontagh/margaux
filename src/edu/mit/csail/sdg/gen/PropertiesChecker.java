package edu.mit.csail.sdg.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;

/**
 * This class is used to check what relational properties are satisfiable. 
 * Also we will see how tight are they to each other. The tightness is gauged
 * by counting the number of generated instances and relations.
 * @author vajih
 *
 */
public class PropertiesChecker {
	
	private  final String INCLUDE  = "open relation_checking";
	private  final String SIG_DECL = "sig s{ r: set s}";
	private  final Byte   S_COUNT  = 4;
	private  final String PRED     = "pred p[]{:1 and :2 and gte[#s,:3] and gte[#r,:4]}";
	private  final String RUN_CMD  = "run p for i";
	private  final String INST_BLK = "inst i {0, :5 s}";
	
	private  final String CODE     = INCLUDE  + System.getProperty( "line.separator" )+
					                 SIG_DECL + System.getProperty( "line.separator" )+
					                 PRED     + System.getProperty( "line.separator" )+
					                 INST_BLK + System.getProperty( "line.separator" )+
					                 RUN_CMD  + System.getProperty( "line.separator" );
	
	private  final String OUT_PATH = "models/debugger/tmp_props"; 	
	
	//20 binary relational properties that are listed in relation_checking.als
	private  final String[] BIN_PROPS   = new String[]{ 
		"acyclic[r, s]"    , "antisymmetric[r]"       , "complete[r,s]"  , "equivalence"     ,
		"function[r,s]"    , "functional[r, s]"       , "injective[r, s]", "irreflexive[r]"  , 
		"partialOrder[r,s]", "preorder[r,s]"          , "reflexive[r,s]" , "rootedAll[r ,s]" , 
		"rootedOne[r, s]"  , "stronglyConnected[r, s]", "surjective[r,s]", "symmetric[r]"    ,
		"total[r, s]"      , "totalOrder[r,s]"        , "transitive[r]"  , "weaklyConnected[r,s]"};

	private final static PropertiesChecker  myself = new PropertiesChecker();
	
	protected PropertiesChecker(){}
	
	public static final PropertiesChecker getInstance(){
		return myself;
	}
	
	private String makeNewDSfile(final String fileName, final String src, final String dest) throws FileNotFoundException, IOException, Err{
		final String filePath = dest + File.separator + fileName;
		Util.writeAll(filePath, src);
		return filePath;
	}
	
	private String extractName(String name){		  
		return name.split("\\[")[0];
	}
	
	private void checkProperties() throws FileNotFoundException, IOException, Err, InterruptedException{
		
		Collection<Object[]> tcs = new ArrayList();
		
		for( int i = 0; i < BIN_PROPS.length; ++i ){
			for( int j = 0; j < BIN_PROPS.length; ++j ){
				if( i == j)
					continue;
				for( int k = 1; k < S_COUNT; ++k){
					for( int l = 0; l < ( (S_COUNT << 1) + 1 ); ++l){
						final String fileName = extractName( BIN_PROPS[i] ) + "_" +
													extractName( BIN_PROPS[j] ) + "_" +
														k + "_" + l + ".als";
						final String src      = CODE.replace( ":1" , BIN_PROPS[i] ).replace(":2", BIN_PROPS[j] )
													.replace(":3", String.valueOf(k)).replace(":4", String.valueOf(l))
													.replace(":5", String.valueOf(S_COUNT));
						Object[] f = new Object[1];
						f[0]       = makeNewDSfile( fileName, src, OUT_PATH ); 
						tcs.add( f );
						
					}
				}
			}
		}
		
		BenchmarkRunner.getInstance().doTest("old",1, 1, tcs);
		
	}
	
	
	public static void main(String...args){
		
		try {
			PropertiesChecker.getInstance().checkProperties();
		} catch (IOException | Err | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
