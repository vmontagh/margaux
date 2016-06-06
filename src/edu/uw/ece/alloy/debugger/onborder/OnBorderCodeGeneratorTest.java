package edu.uw.ece.alloy.debugger.onborder;

import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;

public class OnBorderCodeGeneratorTest {

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
        
        OnBorderCodeGenerator generator = new OnBorderCodeGenerator(file);
        generator.run();
        
        System.out.println("Done");
    }
}
