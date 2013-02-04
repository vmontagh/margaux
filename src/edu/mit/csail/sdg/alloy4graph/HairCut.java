package edu.mit.csail.sdg.alloy4graph;

import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.math.*;

public enum HairCut {
	/** No Hair*/ 	Bald("Bald") {
		@Override
		public Path2D render(int x1, int y1, int x2) {
			// TODO Auto-generated method stub
			Path2D bald = new Path2D.Double();
			bald.moveTo(x1, y1);
			bald.lineTo(x2, y1);
			return bald;
		}
	},
	/** Sin*/ 		Sin("Sin") {
		@Override
		public  Path2D render(int x1, int y1, int x2) {
			// TODO Auto-generated method stub
			double period = 10;
			double maximum = (x2-x1)/period;
			Path2D sin = new Path2D.Double();
			sin.moveTo(x1, y1);
			for(int i=1;i<maximum+1; i++){
				if(!(i+1>maximum+1)){
					sin.quadTo((x1+(period*(i-1))+(period/4)), y1-period, 
								x1+(period*(i-1)+(period/2)), y1);
					sin.quadTo(x1+(period*(i-1)+(period*(0.75))), y1+period, 
								x1+(period*i), y1);
				}else{
					sin.quadTo((x1+(period*(i-1))+(period/4)), y1-period, 
							x1+(period*(i-1)+(period/2)), y1);
				}
			}
			return sin;
		}
	},
	
	/**Triangle*/ Triangle("Triangle"){
		public  Path2D render(int x1, int y1, int x2){
				// TODO Auto-generated method stub
				double period = 6;
				double maximum = (x2-x1)/period;
				Path2D triangle = new Path2D.Double();
				triangle.moveTo(x1, y1);
				for(int i=1;i<maximum+1; i++){
					if(!(i+1>maximum+1)){
						triangle.lineTo((x1+(period*(i-1))+(period/4)), y1-period);
						triangle.lineTo((x1+(period*(i-1)+(period/2))), y1);
						triangle.lineTo(x1+(period*(i-1)+(period*0.75)), y1+period);
						triangle.lineTo(x1+(period*i), y1);
					} else{
						triangle.lineTo((x1+(period*(i-1))+(period/4)), y1-period);
						triangle.lineTo((x1+(period*(i-1)+(period/2))), y1);
					}
				}
				return triangle;
			}
		},
	
		/**Sawtooth*/ Sawtooth("Sawtooth"){
			public  Path2D render(int x1, int y1, int x2) {
					// TODO Auto-generated method stub
					double period = 8;
					double maximum = (x2-x1)/period;
					Path2D sawtooth = new Path2D.Double();
					sawtooth.moveTo(x1, y1);
					for(int i=1;i<maximum+1; i++){
						sawtooth.lineTo((x1+period*(i)), y1-period);
						sawtooth.lineTo((x1+period*i), y1);
					}
					return sawtooth;
				}
			},
	
			/**Square*/ Square("Square"){
				public  Path2D render(int x1, int y1, int x2) {
						// TODO Auto-generated method stub
						double period = 10;
						double maximum = (x2-x1)/period;
						Path2D square = new Path2D.Double();
						square.moveTo(x1, y1);
						for(int i=1;i<maximum+1; i++){
							if(!(i+1>maximum+1)){
								square.lineTo((x1+(period*(i-1))), y1-period);
								square.lineTo(((x1+(period*(i-1)+(period/2)))), y1-period);
								square.lineTo(((x1+(period*(i-1)+(period/2)))), y1);
								square.lineTo(x1+period*i, y1);
							}else{
								square.lineTo((x1+(period*(i-1))), y1-period);
								square.lineTo(((x1+(period*(i-1)+(period/2)))), y1-period);
								square.lineTo(((x1+(period*(i-1)+(period/2)))), y1);
							}
						}
						return square;
					}
				},

				/** AbsSin*/ 		AbsSin("AbsSin") {
					@Override
					public  Path2D render(int x1, int y1, int x2) {
						// TODO Auto-generated method stub
						double period = 10;
						double maximum = (x2-x1)/period;
						Path2D absSin = new Path2D.Double();
						absSin.moveTo(x1, y1);
						for(int i=1;i<maximum; i++){
							absSin.quadTo((x1+(period*(i-1))+(period/2)), y1-period, 
										x1+(period*(i)), y1);
							
						}
						return absSin;
					}
				},
	
				/** DampedSin*/ 		DampedSin("DampedSin") {
					@Override
					public  Path2D render(int x1, int y1, int x2) {
						// TODO Auto-generated method stub
						double period = 20;
						double maximum = (x2-x1)/period;
						Path2D dampedSin = new Path2D.Double();
						dampedSin.moveTo(x1, y1);
						for(int i=1;i<maximum+1; i++){
							if(!(i+1>maximum+1)){
								dampedSin.quadTo((x1+(period*(i-1))+(period/4)), y1-((period*(i-1))+((i/Math.floor(maximum))*period))/4, 
											 	 x1+(period*(i-1)+(period/2)), y1);
								dampedSin.quadTo(x1+(period*(i-1)+(period*(0.75))), y1+((period*i-1)-(((i)/Math.floor(maximum))*period))/4, 
											 	 x1+(period*i), y1);
							}else{
								dampedSin.quadTo((x1+(period*(i-1))+(period/4)), y1-((period*(i-1))+((i/Math.floor(maximum))*period))/4, 
									 	 		  x1+(period*(i-1)+(period/2)), y1);
							}
						}			
						return dampedSin;
					}
				},
				/** Straight*/ 		Straight("Straight") {
					@Override
					public  Path2D render(int x1, int y1, int x2) {
						// TODO Auto-generated method stub
						double period = 10;
						double maximum = (x2-x1)/period;
						Path2D straight = new Path2D.Double();
						straight.moveTo(x1, y1);
						for(int i=1;i<maximum; i++){
							straight.lineTo((x1+period*i), (y1));
							straight.lineTo((x1+period*i), (y1-period));
							straight.moveTo((x1+period*i), (y1));
						}
						straight.lineTo((x1+period*Math.floor(maximum)), (y1));
						straight.lineTo((x1+period*Math.floor(maximum)), (y1-period));
						
						return straight;
					}
				},
		
				/**Slanted*/ Slanted("Slanted"){
					public  Path2D render(int x1, int y1, int x2) {
							// TODO Auto-generated method stub
							double period = 10;
							double maximum = (x2-x1)/period;
							Path2D slanted = new Path2D.Double();
							slanted.moveTo(x1, y1);
							for(int i=1;i<maximum+1; i++){
								slanted.lineTo((x1+period*(i)), y1-period);
								slanted.moveTo((x1+period*i), y1);
							}
							return slanted;
						}
					},


	;
	
	
	
	private HairCut(String name){
		this.name = name;
	}
	
	private final String name;
	public abstract Path2D render(int x1, int y1, int x2);
	
	public String toString(){return name;}
}
