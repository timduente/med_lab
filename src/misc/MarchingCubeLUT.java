package misc;

import java.util.Hashtable;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.QuadArray;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;

public class MarchingCubeLUT {
	public Hashtable<Byte, GeometryArray[]> McLut = new Hashtable<Byte, GeometryArray[]>();

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
		add(0x00000000);
		// Case 0 Inverse
		add(0x11111111);

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

		/** Case 2 an Inverse **/
		// 0 1
		add(0x00111111, p03, p15, p12, p04);
		add(0x11000000, p03, p15, p12, p04);
		// 1 5
		add(0x10111011, p12, p56, p45, p01);
		add(0x01000100, p12, p56, p45, p01);
		// 4 5
		add(0x11110011, p04, p15, p56, p47);
		add(0x00001100, p04, p15, p56, p47);
		// 0 4
		add(0x01110111, p01, p45, p47, p03); 
		add(0x10001000, p01, p45, p47, p03);
		// 2 3
		add(0x11001111, p03, p12, p37, p26); 
		add(0x00110000, p03, p12, p37, p26);
		// 2 6 
		add(0x11011101, p12, p56, p67, p23); 
		add(0x00100010, p12, p56, p67, p23); 
		//6 7 
		add(0x11111100, p26, p56, p47, p37); 
		add(0x00000011, p26, p56, p47, p37); 
		//0 3
		add(0x01101111, p01, p23, p37, p04);
		add(0x10010000, p01, p23, p37, p04);
		/// 1 2 
		add(0x10011111, p01, p23, p15, p26);
		add(0x01100000, p01, p23, p15, p26); 
		//5 6 
		add(0x11111001, p15, p26, p45, p67); 
		add(0x00000110, p15, p26, p45, p67); 
		//47
		add(0x11110110, p04, p37, p67, p45); 
		add(0x00001001, p04, p37, p67, p45); 
		
		/** Case 3 and Inverse **/
	}

	// No planes
	private void add(int value) {
		McLut.put((byte) value, new GeometryArray[] {});
	}

	// 1 Triangle
	private void add(int value, Point3f tri_a, Point3f tri_b, Point3f tri_c) {
		TriangleArray triangle = new TriangleArray(3, TriangleArray.COORDINATES);
		triangle.setCoordinates(3, new Point3f[] { tri_a, tri_b, tri_c });
		McLut.put((byte) value, new GeometryArray[] { triangle });
	}

	// 1 Quadliteral
	private void add(int value, Point3f quad_a, Point3f quad_b, Point3f quad_c, Point3f quad_d) {
		QuadArray quad = new QuadArray(3, QuadArray.COORDINATES);
		quad.setCoordinates(4, new Point3f[] { quad_a, quad_b, quad_c, quad_d });
		McLut.put((byte) value, new GeometryArray[] { quad });
	}

	// 1 Quadliteral & 1 Triangle
	private void add(int value, Point3f tri_a, Point3f tri_b, Point3f tri_c, Point3f quad_a, Point3f quad_b, Point3f quad_c, Point3f quad_d) {
		this.add(value, tri_a, tri_b, tri_c);
		this.add(value, quad_a, quad_b, quad_c);
	}

	// 2 Triangle
	private void add(int value, Point3f tri_1_a, Point3f tri_1_b, Point3f tri_1_c, Point3f tri_2_a, Point3f tri_2_b, Point3f tri_2_c) {
		this.add(value, tri_1_a, tri_1_b, tri_1_c);
		this.add(value, tri_2_a, tri_2_b, tri_2_c);
	}

	// 2 Quadliteral
	private void add(int value, Point3f quad_1_a, Point3f quad_1_b, Point3f quad_1_c, Point3f quad_1_d, Point3f quad_2_a, Point3f quad_2_b, Point3f quad_2_c, Point3f quad_2_d) {
		this.add(value, quad_1_a, quad_1_b, quad_1_c, quad_1_d);
		this.add(value, quad_2_a, quad_2_b, quad_2_c, quad_2_d);
	}
}
