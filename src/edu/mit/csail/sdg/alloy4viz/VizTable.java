package edu.mit.csail.sdg.alloy4viz;
import java.awt.Dimension;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class VizTable{	
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
	 * - Use JDom to create XML instance
	 * @throws IOException */
	
	public Document createXML (VizState myState) throws IOException{
		Element Model = new Element("Model");
		Document doc = new Document(Model);
		doc.setRootElement(Model);
		
		
		
		for(AlloyAtom t_x: myState.getOriginalInstance().getAllAtoms()){
			if(t_x.getType().equals(X) || myState.getCurrentModel().getSubTypes(X).contains(t_x.getType())){
				Element X_name = new Element(t_x.toString());
				for(AlloyAtom t_y: myState.getOriginalInstance().getAllAtoms()){					
					if(t_y.getType().equals(Y) || myState.getCurrentModel().getSubTypes(Y).contains(t_y.getType())){
						Element Y_name = new Element(t_y.toString());
						for(AlloyRelation r: alloyRelations){
							if(isConnected(t_x, t_y, myState, r)){
								if(r.getArity()< 3){
//									System.out.println(r.getName() + ", "+t_x+", "+t_y);
									Y_name.addContent(r.getName());
								}else if(r.getArity()==3){
									for(AlloyTuple t: myState.getOriginalInstance().relation2tuples(r)){
										if(t.getAtoms().contains(t_x) && t.getAtoms().contains(t_y)){
											for(AlloyAtom a: t.getAtoms()){
												if(!a.equals(t_x) && !a.equals(t_y)){
//													System.out.println(t_x+", "+t_y+", "+a);
													Y_name.addContent(t_y.toString());
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
						X_name.addContent(Y_name);
						}
					
					}
				doc.getRootElement().addContent(X_name);
				}
			}
		
		XMLOutputter xml_table = new XMLOutputter();
		xml_table.setFormat(Format.getPrettyFormat());
		xml_table.output(doc, new FileWriter("table.xml"));
		
		return doc;
	}
	
	
	/** HELPER function for CreateXML to check if Atoms x2 and y2 are connected by relation r.*/
	private boolean isConnected(AlloyAtom x2, AlloyAtom y2, VizState myState,
			AlloyRelation r2) {
			for(AlloyTuple tu: myState.getOriginalInstance().relation2tuples(r2)){
				if(tu.getAtoms().contains(x2) && tu.getAtoms().contains(y2)){
					return true;
				}
			}
			return false;
			
		}

	public JScrollPane getVizTable(VizState myState) {
		List<AlloyAtom> rowNames = new ArrayList<AlloyAtom>();
		Set<String> columnNames = new TreeSet<String>();
		List<ArrayList<String>> tempBig = new ArrayList<ArrayList<String>>();
 		
		columnNames.add("");
		for(AlloyAtom t_x: myState.getOriginalInstance().getAllAtoms()){
			if(t_x.getType().equals(X) || myState.getCurrentModel().getSubTypes(X).contains(t_x.getType())){
				rowNames.add(t_x);
				ArrayList<String> tempSmall = new ArrayList<String>();
				tempSmall.add(t_x.toString());
				for(AlloyAtom t_y: myState.getOriginalInstance().getAllAtoms()){					
					if(t_y.getType().equals(Y) || myState.getCurrentModel().getSubTypes(Y).contains(t_y.getType())){
						columnNames.add(t_y.toString());
						for(AlloyRelation r: alloyRelations){
							if(isConnected(t_x, t_y, myState, r)){
								if(r.getArity()< 3){
//									System.out.println(r.getName() + ", "+t_x+", "+t_y);
									tempSmall.add(r.getName());
									
								}else if(r.getArity()==3){
									for(AlloyTuple t: myState.getOriginalInstance().relation2tuples(r)){
										if(t.getAtoms().contains(t_x) && t.getAtoms().contains(t_y)){
											for(AlloyAtom a: t.getAtoms()){
												if(!a.equals(t_x) && !a.equals(t_y)){
//													System.out.println(t_x+", "+t_y+", "+a);
													tempSmall.add(a.toString());
												}
											}
										}
									}
								}else{
									//TODO:// VIZTABLE: Should throw Exception
										return null;
								}
							}else {
								
							}
						}		
					}
				}
				tempBig.add(tempSmall);
					
				}
			}
		
//		System.out.println(rowNames);
//		System.out.println(columnNames);
		
		String[][] table = new String[rowNames.size()][columnNames.size()];
		for(int i=0; i<rowNames.size() ; i++){
			for(int j=0; j< columnNames.size(); j++){
				if(table[i][j]==null){
					if(tempBig.get(i).get(j) != null){
						table[i][j] = tempBig.get(i).get(j);
					}else{
						table[i][j] = " ";
					}
				}
				else{
					table[i][j].concat(", "+tempBig.get(i).get(j));
				}
			}
		}
		
		JTable viz = new JTable(table, columnNames.toArray());
		JScrollPane vizScroll = new JScrollPane(viz);
		
		return vizScroll;
	}


}


