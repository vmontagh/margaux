package edu.uw.ece.alloy.debugger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

/**
 * This class provides an Alloy program that checks the relational properties over 
 * Each relation in the given alloy program.
 * 
 * The Alloy program has one or more commands. For every command, a slice of the 
 * program is executed. The relations from that slice is extracted and checked.
 * Assuming the given Alloy program is as `P check{q} for i' then a property 
 * like `prop1'. Then, the generated code would be like:
 * 		`open general_props P check{q=>prop1[r1]}' 
 * 		`open general_props P check{prop1[r1]=>q}'
 * It means both directions are checked.
 * @author vajih
 *
 */
public class RelationalPropertiesChecker {
	
	final private Module world;
	final private String relationalPropertyNameFile;
	
	
	final Function<String, String> extracerFunction = (String s)->{
		String[] r = s.split(",");
		return r.length > 0 ? r[0] : s;
	};
	
	public RelationalPropertiesChecker(Module world_, String relationalPropertyNameFile_){
		this.world = world_;
		this.relationalPropertyNameFile  = relationalPropertyNameFile_;
	}
	
	final private List<Sig.Field> getAllFields(){
		
		final List<Sig.Field> fields = new ArrayList<Sig.Field>();
        
        //what is inside the world? I am looking for fields
        for(Sig sig:world.getAllSigs()){
        	for(Sig.Field field: sig.getFields()){
        		fields.add(field);
        	}
        }
		return fields;
	}
	
	final private List<Sig> getAllSigs(){
		
		List<Sig> sigs = world.getAllSigs().makeConstList();
		return sigs;
	}
	

	
	final private List<String> getAllProperties(Predicate<String> p, Function<String, String> f) throws FileNotFoundException, IOException{
		
		String content = Util.readAll(relationalPropertyNameFile);
		
		return Arrays.asList(content.split("\n")).stream().filter(p).map(f).collect(Collectors.toList());
		
	}
	
	final public List<String> getAllBinaryWithDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException{
		
		return getAllProperties((s)->{return s.contains(",b,d,r");},extracerFunction);
		
	}
	
	final public List<String> getAllBinaryWithDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException{
		
		return getAllProperties((s)->{return s.contains(",b,d,0");},extracerFunction);
		
	}
	
	final public List<String> getAllBinaryWithoutDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException{
		
		return getAllProperties((s)->{return s.contains(",b,0,r");},extracerFunction);
		
	}	

	final public List<String> getAllBinaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException{
		
		return getAllProperties((s)->{return s.contains(",b,0,0");},extracerFunction);
		
	}
	
	final public List<String> getAllTernaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException{
		
		return getAllProperties((s)->{return s.contains(",t,0,0");},extracerFunction);
		
	}	
	
}
