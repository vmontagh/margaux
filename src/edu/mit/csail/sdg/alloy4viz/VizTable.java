package edu.mit.csail.sdg.alloy4viz;
import java.awt.Dimension;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JTable;

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
	
	/** Constructor for a new vizTable with SigX, SigX and Relations */
	public VizTable(AlloyType X, AlloyType Y, AlloyRelation ...alloyRelations){
		this.X = X;
		this.Y = Y;
		this.alloyRelations = alloyRelations;
	}
	
	/** Creates and return an XML instance of the table using the following algorithm:
	 * - For the given Sigs in Row and Column
	 * - Select Relation and
	 * 		- if arity < 2 -> Data value = relation.name
	 * 		- else if arity = 3 -> Data value =  Third Sig of relation
	 * 		- else throw exception. (This case should never arise, due to filtering in the GUI)
	 * - Use JDom to create XML instance*/
	
	public Document createXML (VizState myState){
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
								if(r.getArity()< 3){
									System.out.println(r.getName() + ", "+t_x+", "+t_y);
								}else if(r.getArity()==3){
									for(AlloyTuple t: myState.getOriginalInstance().relation2tuples(r)){
										if(t.getAtoms().contains(t_x) && t.getAtoms().contains(t_y)){
											for(AlloyAtom a: t.getAtoms()){
												if(!a.equals(t_x) && !a.equals(t_y)){
													System.out.println(a+", "+t_x+", "+t_y);
													//TODO: VIZTABLE: USE jDOM API to create the XML lines and save in doc under Model
												}
											}
										}
									}
								}else{
									//TODO:// VIZTABLE: Should throw Exception
										return null;
									}
								}
							}
						}
					}
				}
			}
		

		return doc;
	}
	
	/** Creates a JTable using the XML Document instance*/
	public JTable createTable(Document doc) {
		// TODO VIZTABLE: createTable
		
		// Test Table. To be replaced with code to create table out of doc.{
		String[] columnNames = {"First Name",
                "Last Name",
                "Sport",
                "# of Years",
                "Vegetarian"};

			Object[][] data = {
			{"Kathy", "Smith",
			"Snowboarding", new Integer(5), new Boolean(false)},
			{"John", "Doe",
			"Rowing", new Integer(3), new Boolean(true)},
			{"Sue", "Black",
			"Knitting", new Integer(2), new Boolean(false)},
			{"Jane", "White",
			"Speed reading", new Integer(20), new Boolean(true)},
			{"Joe", "Brown",
			"Pool", new Integer(10), new Boolean(false)}
			};
			
			final JTable table = new JTable(data, columnNames);
	        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
	        table.setFillsViewportHeight(true);
		
		return table;
		//}
	}
	
	/** HELPER function for CreateXML to check if Atoms x2 and y2 are connected by relation r.*/
	private boolean isConnected(AlloyAtom x2, AlloyAtom y2, VizState myState,
			AlloyRelation r) {
		for(AlloyTuple tu: myState.getOriginalInstance().relation2tuples(r)){
			if(tu.getAtoms().contains(x2) && tu.getAtoms().contains(y2)){
				return true;
			}
		}
		return false;
	}

	/** Draws a Table in JScrollPane in the Alloyviz GUI using the JTable instance*/
	public void drawTable(JTable table) {
		// TODO VIZTABLE: drawTable
		
	}


}


