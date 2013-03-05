package edu.mit.csail.sdg.alloy4viz;
import java.io.FileWriter;
import java.io.IOException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class VizTable {	
	/**VizTable Constructor */
	final AlloyType X;
	final AlloyType Y;
	final AlloyRelation[] alloyRelations;
	
	public VizTable(AlloyType X, AlloyType Y, AlloyRelation ...alloyRelations){
		this.X = X;
		this.Y = Y;
		this.alloyRelations = alloyRelations;
	}
	
	public Document createXML(VizState myState){
		Element Model = new Element("Model");
		Document doc = new Document(Model);
		
		Element X_name = new Element(X.toString());
		for(AlloyAtom t_x: myState.getOriginalInstance().getAllAtoms()){
			if(t_x.getType().equals(X) || myState.getCurrentModel().getSubTypes(X).contains(t_x.getType())){
				Element Y_name = new Element(Y.toString());
				for(AlloyAtom t_y: myState.getOriginalInstance().getAllAtoms()){
					if(t_y.getType().equals(Y) || myState.getCurrentModel().getSubTypes(Y).contains(t_y.getType())){
						for(AlloyRelation r: alloyRelations){
							if(isConnected(t_x, t_y, myState, r)){
								System.out.println(r + ": "+myState.getOriginalInstance().relation2tuples(r));
								//TODO: USE jDOM API to create the XML lines
							}
						}
					}
				}
			}
		}

		return doc;
	}
	
	
	
	private boolean isConnected(AlloyAtom x2, AlloyAtom y2, VizState myState,
			AlloyRelation r) {
		for(AlloyTuple tu: myState.getOriginalInstance().relation2tuples(r)){
			if(tu.getAtoms().contains(x2) && tu.getAtoms().contains(y2)){
				return true;
			}
		}
		return false;
	}
}


