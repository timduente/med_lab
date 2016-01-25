package misc;

import java.util.Hashtable;

public class MarchingCubeLUT {
	public Hashtable<Byte, float[]> McLut = new Hashtable<Byte, float[]>();
	
	
	/**
	 * Edges are labeled the way on the excercise sheet. Bottom left 0, bottom front 1, up front 2, up left 3, bottom back 4, bottom right 5, up right 6, up back 7. 
	 * A cube has the following coordinate system: 
	 * (0,0,0) is the origin with x increasing to point 1/5/2/6. Y increasing to 4/5/7/6 and z increasing to up.
	 * 
	 *  Therefor there are the following centers between points: 
	 *  0 1 : 0 , -.5, -.5
	 *  1 5 : .5, 0, -.5
	 *  4 5 : 
	 * 
	 */
	
	public MarchingCubeLUT()	{
		McLut.put((byte) 0x00000000, new float[]{0,0,0});
		
		
		McLut.put((byte) 0x11111111, new float[]{0,0,0});
	}
	
	
}
