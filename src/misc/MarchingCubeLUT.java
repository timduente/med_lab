package misc;

import java.util.Hashtable;

import javax.vecmath.Point3f;

public class MarchingCubeLUT {
	public Hashtable<Byte, Point3f[]> McLut = new Hashtable<Byte, Point3f[]>();

	Point3f p01 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p12 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p23 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p03 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p45 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p56 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p67 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p47 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p04 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p15 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p26 = new Point3f(0.0f, -0.5f, -0.5f);
	Point3f p37 = new Point3f(0.0f, -0.5f, -0.5f);

	Point3f pxx = new Point3f(0.0f, 0.0f, 0.0f);

	/**
	 * Edges are labeled the way on the excercise sheet. Bottom left 0, bottom front 1, up front 2, up left 3, bottom back 4, bottom right 5, up right 6, up back 7. A cube has the following coordinate system: (0,0,0) is the origin with x increasing to point 1/5/2/6. Y increasing to 4/5/7/6 and z
	 * increasing to up.
	 * 
	 * Therefor there are the following centers between points: 0 1 : 0 , -.5, -.5 1 5 : .5, 0, -.5 4 5 :
	 * 
	 */

	public MarchingCubeLUT() {
		/** Case 0 **/
		// McLut.put((byte) 0x00000000, new float[] { 0, 0, 0 });
		add(0x00000000, pxx, pxx, pxx);
		// Case 0 Inverse
		add(0x11111111, pxx, pxx, pxx);

		/** Case 1 and Inverse **/ 
		add(0x01111111, p01, p03, p04);
		add(0x10000000, p01, p03, p04);
		// Case 1 Rotation and Inverse
		// 1
		add(0x10111111, p01, p12, p15);
		add(0x01000000, p01, p12, p15);
		// 2
		add(0x11011111, p12, p23, p26);
		add(0x00100000, p12, p23, p26);
		// 3
		add(0x11101111, p03, p23, p37);
		add(0x00010000, p03, p23, p37);
		// 4
		add(0x11110111, p04, p45, p47);
		add(0x00001000, p04, p45, p47);
		// 5
		add(0x11111011, p15, p45, p56);
		add(0x00000100, p15, p45, p56);
		// 6
		add(0x11111101, p26, p56, p67);
		add(0x00000010, p26, p56, p67);
		// 7
		add(0x11111110, p37, p47, p67);
		add(0x00000001, p37, p47, p67);
		
		/** Case 2 **/
		

	}

	private void add(int value, Point3f a, Point3f b, Point3f c) {
		McLut.put((byte) value, new Point3f[] { a, b, c });
	}

}
