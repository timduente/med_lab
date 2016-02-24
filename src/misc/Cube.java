package misc;

import java.util.BitSet;
import javax.media.j3d.TriangleArray;

public class Cube	{
	public byte corner; 
	public TriangleArray[] planes;
	public Cube(byte corner, TriangleArray[] plane)	{
		this.corner = corner; 
		planes = plane; 
	}
	
//	public Cube getNewFromRotX(int rot)	{
//		byte newCor = corner; 
//		TriangleArray[] newPlan = planes.clone(); 
//		
//		BitSet bits = BitSet.valueOf(new byte[]{newCor});
//		//Change bit value and points of plane
//		for(int i = 1; i <= rot; i++)	{
//			boolean helper = bits.get(0);
//			bits.set(0, bits.get(1));
//			bits.set(1, bits.get(5));
//			bits.set(5, bits.get(4));
//			bits.set(4, helper);
//			helper = bits.get(3); 
//			bits.set(3, bits.get(2));
//			bits.set(2, bits.get(6));
//			bits.set(6, bits.get(7));
//			bits.set(7, helper);
//			for(TriangleArray tri : newPlan)	 {
//				
//			}
//			
//		}
//	}
//	
}
