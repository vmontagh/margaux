package edu.uw.ece.alloy.debugger.onborder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import kodkod.ast.Formula;

public class TestConstraintMapper {
    
    @Test
    public void test() throws Err {
        
        String alloy4Home = "/home/fikayo/Documents/Engineering/Alloy/alloy";
        String fileName = "linked_list_sigs.als";
        System.out.println("Running file: " + fileName);
        
        String[] files = { alloy4Home + "/models/debugger/min_dist/" + fileName };
        // alloy4Home + "/models/examples/toys/birthday.als" };
        Map<Command, A4Solution> map = A4CommandExecuter.getInstance().runThenGetAnswers(files, A4Reporter.NOP);
        
        System.out.println("Listing commands and solutions:");
        
        for (Entry<Command, A4Solution> entry : map.entrySet()) {
            
            System.out.println("==========================================");
            
            Command cmd = entry.getKey();
            A4Solution soln = entry.getValue();
            System.out.println(cmd.toString());
            System.out.println(soln.toString());
            
            Map<Field, ?> constrMap = Field2ConstraintMapper.mapFields(soln);
            System.out.println("Constraint Map:");
            for (Field f : constrMap.keySet()) {
                System.out.println();
                System.out.println(f.toString());
                System.out.println(constrMap.get(f).toString());
            }
        }
    }
    
    @Test
    public void testSigExtraction() throws Err {
        
        String alloy4Home = "/home/fikayo/Documents/Engineering/Alloy/alloy";
        
        String fileName = "linked_list.als";
        String directory = alloy4Home + "/models/debugger/min_dist/";
        // fileName = "birthday.als";
        // directory = alloy4Home + "/models/examples/toys/";
        String file = directory + fileName;
        System.out.println("Running file: " + fileName);
        
        String sigs = getSigString(file);
        System.out.println(sigs);
        String sigFile = directory + fileName.substring(0, fileName.lastIndexOf(".")) + "_sigs.als";
        Util.writeAll(sigFile, sigs);
        System.out.println("Sigs writted to: " + sigFile);
        
        List<Formula> formulas = A4CommandExecuter.getInstance().translateAlloy2KK(sigFile, A4Reporter.NOP, "p");
        
        System.out.println("\nFormulas: ");
        String regex = "this\\/([^\\s])*\\."; // Remove all this/*.
        for (Formula f : formulas) {
            String structuralConstraint = f.toString().replaceAll(regex, "").replace("this/", "");
            System.out.println(structuralConstraint);
        }
        
    }
    
    @Test
    public void testCodeGenerator() throws Err {
        
        String alloy4Home = "/home/fikayo/Documents/Engineering/Alloy/alloy";
        
        String fileName = "linked_list.als";
        String directory = alloy4Home + "/models/debugger/min_dist/";
        fileName = "birthday.als";
        fileName = "ceilingsAndFloors.als";
        fileName = "railway.als";
        directory = alloy4Home + "/models/examples/toys/";
        
        String file = directory + fileName;
        System.out.println("Testing Code Generation on file: " + fileName);
        
        String newName = fileName.substring(0, fileName.lastIndexOf(".")) + "_mod.als";        
        PrintWriter writer = null;
        try {
            File newFile = new File(directory + newName);
            newFile.getParentFile().mkdirs();
            writer = new PrintWriter(newFile);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        OnBorderCodeGenerator generator = new OnBorderCodeGenerator(file, writer);
        generator.run();
        
        System.out.println("Done");
    }
    
    private String getSigString(String file) throws Err {
        
        Module module = A4CommandExecuter.getInstance().parse(file, A4Reporter.NOP);
        
        String run = "\npred p[] {}\nrun p";
        String sigs = Field2ConstraintMapper.getSigDeclationViaPos(module);
        sigs += run;
        return sigs;
    }
}
