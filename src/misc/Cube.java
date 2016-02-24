package misc;

import java.util.BitSet;
import javax.media.j3d.TriangleArray;

public class Cube	{
	public byte corner; 
	public BitSet bPlane;
	public TriangleArray[] planes;
	public Cube(byte corner, BitSet bPlane)	{
		this.corner = corner; 
		this.bPlane = bPlane; 
	}
	
	public Cube getNewFromRotX(int rot)	{
//		byte newCor = corner; 
//		byte newbPlane = bPlane; 
		
		BitSet newCor = BitSet.valueOf(new byte[]{corner});
		BitSet newPlane = (BitSet) bPlane.clone();
		
		//Change bit value and points of plane
		for(int i = 1; i <= rot; i++)	{
			boolean helper = newCor.get(0);
			newCor.set(0, newCor.get(1));
			newCor.set(1, newCor.get(5));
			newCor.set(5, newCor.get(4));
			newCor.set(4, helper);
			helper = newCor.get(3); 
			newCor.set(3, newCor.get(2));
			newCor.set(2, newCor.get(6));
			newCor.set(6, newCor.get(7));
			newCor.set(7, helper);

			//Do something with rotation planes
			
		}
		return new Cube(newCor.toByteArray()[0], newPlane);
	}
	
}
