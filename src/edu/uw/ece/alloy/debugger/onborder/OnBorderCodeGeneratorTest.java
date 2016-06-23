package edu.uw.ece.alloy.debugger.onborder;

import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;

public class OnBorderCodeGeneratorTest {

    @Test
    public void testCodeGenerator() throws Err {

        String alloy4Home = "/home/ooodunay/workspace/alloy4";

        String fileName = "linked_list.als";
        String directory = alloy4Home + "/models/debugger/min_dist/";
//      directory = alloy4Home + "/models/examples/toys/";
//        fileName = "birthday.als";
//        fileName = "ceilingsAndFloors.als";
//        fileName = "railway.als";
        fileName = "bare_linked_list.als";
        
        String file = directory + fileName;
        
        OnBorderCodeGenerator generator = new OnBorderCodeGenerator(file);
        generator.run();
        
    }
}
