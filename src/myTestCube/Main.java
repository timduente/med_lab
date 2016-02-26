package myTestCube;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.QuadArray;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;

import myTestCube.Cube;

;

public class Main {
	public Hashtable<Integer, Cube> McLut = new Hashtable<Integer, Cube>();

	public enum points {
		p01, p12, p23, p03, p45, p56, p67, p47, p04, p15, p26, p37;
	}

	// TODO: Coordinates not set!!

	// public Point3f p01 = new Point3f(0.0f, -0.5f, -0.5f); // byte 0
	// public Point3f p12 = new Point3f(0.0f, -0.5f, -0.5f);// byte 1
	// public Point3f p23 = new Point3f(0.0f, -0.5f, -0.5f);// byte 2
	// public Point3f p03 = new Point3f(0.0f, -0.5f, -0.5f);// byte 3
	// public Point3f p45 = new Point3f(0.0f, -0.5f, -0.5f);// byte 4
	// public Point3f p56 = new Point3f(0.0f, -0.5f, -0.5f);// byte5
	// public Point3f p67 = new Point3f(0.0f, -0.5f, -0.5f);// byte 6
	// public Point3f p47 = new Point3f(0.0f, -0.5f, -0.5f);// byte 7
	// public Point3f p04 = new Point3f(0.0f, -0.5f, -0.5f);// byte 8
	// public Point3f p15 = new Point3f(0.0f, -0.5f, -0.5f);// byte 9
	// public Point3f p26 = new Point3f(0.0f, -0.5f, -0.5f);// byte 10
	// public Point3f p37 = new Point3f(0.0f, -0.5f, -0.5f);// byte 11
	//
	// Point3f pxx = new Point3f(0.0f, 0.0f, 0.0f);

	/**
	 * Edges are labeled the way on the excercise sheet. Bottom left 0, bottom front 1, up front 2, up left 3, bottom back 4, bottom right 5, up right 6, up back 7. A cube has the following coordinate system: (0,0,0) is the origin with x increasing to point 1/5/2/6. Y increasing to 4/5/7/6 and z
	 * increasing to up.
	 * 
	 * Therefor there are the following centers between points: 0 1 : 0 , -.5, -.5 1 5 : .5, 0, -.5 4 5 :
	 * 
	 */

	public static void main(String[] args) {
		new Main();
	}

	public Main() {

		/** Case 0 **/
		add0(0b00000000, new LinkedList<points[]>());
		/** Case 1 **/
		LinkedList<points[]> link1 = new LinkedList<points[]>();
		link1.add(new points[] { points.p01, points.p03, points.p04 });
		addAll(0b10000000, link1);
		/** Case 2 **/
		LinkedList<points[]> link2 = new LinkedList<points[]>();
		link2.add(new points[] { points.p04, points.p03, points.p01 });
		link2.add(new points[] { points.p15, points.p12, points.p04 });
		addAll(0b11000000, link2);
		/** Case 3 **/
		LinkedList<points[]> link3 = new LinkedList<points[]>();
		link3.add(new points[] { points.p04, points.p03, points.p01 });
		link3.add(new points[] { points.p23, points.p12, points.p26 });
		addAll(0b10100000, link3);
		/** Case 4 **/
		LinkedList<points[]> link4 = new LinkedList<points[]>();
		link4.add(new points[] { points.p04, points.p03, points.p01 });
		link4.add(new points[] { points.p56, points.p26, points.p67 });
		addAll(0b10000010, link4);
		/** Case 5 **/
		LinkedList<points[]> link5 = new LinkedList<points[]>();
		link5.add(new points[] { points.p56, points.p12, points.p47 });
		link5.add(new points[] { points.p12, points.p47, points.p01 });
		link5.add(new points[] { points.p01, points.p47, points.p04 });
		addAll(0b01001100, link5);
		/** Case 6 **/
		LinkedList<points[]> link6 = new LinkedList<points[]>();
		link6.add(new points[] { points.p56, points.p67, points.p26 });
		link6.add(new points[] { points.p15, points.p12, points.p04 });
		link6.add(new points[] { points.p03, points.p04, points.p26 });
		addAll(0b11000010, link6);
		/** Case 7 **/
		LinkedList<points[]> link7 = new LinkedList<points[]>();
		link7.add(new points[] { points.p01, points.p12, points.p15 });
		link7.add(new points[] { points.p03, points.p37, points.p23 });
		link7.add(new points[] { points.p26, points.p67, points.p56 });
		addAll(0b01010010, link7);
		/** Case 8 **/
		LinkedList<points[]> link8 = new LinkedList<points[]>();
		link8.add(new points[] { points.p03, points.p47, points.p56 });
		link8.add(new points[] { points.p03, points.p12, points.p56 });
		addAll(0b11001100, link8);
		/** Case 9 **/
		LinkedList<points[]> link9 = new LinkedList<points[]>();
		link9.add(new points[] { points.p03, points.p37, points.p67 });
		link9.add(new points[] { points.p03, points.p67, points.p01 });
		link9.add(new points[] { points.p01, points.p67, points.p56 });
		link9.add(new points[] { points.p01, points.p15, points.p56 });
		addAll(0b10001101, link9);
		/** Case 10 **/
		LinkedList<points[]> link10 = new LinkedList<points[]>();
		link10.add(new points[] { points.p67, points.p26, points.p45 });
		link10.add(new points[] { points.p45, points.p26, points.p15 });
		link10.add(new points[] { points.p04, points.p37, points.p23 });
		link10.add(new points[] { points.p23, points.p37, points.p04 });
		addAll(0b10010110, link10);
		/** Case 11 **/                               //TODO TODO TODO TODO TODO CANT SEE TODO TODO TODO
		LinkedList<points[]> link11 = new LinkedList<points[]>();
//		link11.add(new points[] { points.p, points.p, points.p });
//		link11.add(new points[] { points.p, points.p, points.p });
		addAll(0b10001110, link11);
		/** Case 12 **/
		LinkedList<points[]> link12 = new LinkedList<points[]>();
		link12.add(new points[] { points.p37, points.p03, points.p23 });
		link12.add(new points[] { points.p56, points.p47, points.p12 });
		link12.add(new points[] { points.p01, points.p12, points.p47 });
		link12.add(new points[] { points.p01, points.p47, points.p04 });
		addAll(0b01011100, link12);
		/** Case 13 **/
		LinkedList<points[]> link13 = new LinkedList<points[]>();
		link13.add(new points[] { points.p01, points.p04, points.p03 });
		link13.add(new points[] { points.p15, points.p56, points.p45 });
		link13.add(new points[] { points.p12, points.p23, points.p26 });
		link13.add(new points[] { points.p37, points.p47, points.p67 });
		addAll(0b10100101, link13);
		/** Case 14 **/								//TODO TODO TODO TODO TODO CANT SEE TODO TODO TODO
		LinkedList<points[]> link14 = new LinkedList<points[]>();
//		link14.add(new points[] { points.p, points.p, points.p });
//		link14.add(new points[] { points.p, points.p, points.p });
//		link14.add(new points[] { points.p, points.p, points.p });
//		link14.add(new points[] { points.p, points.p, points.p });
		addAll(0b01001101, link14);

		Enumeration<Integer> enumKey = McLut.keys();
		while (enumKey.hasMoreElements()) {
			Integer key = enumKey.nextElement();
			Cube val = McLut.get(key);
			// System.out.printf("0x%02X" key.toString());
			System.out.println("Bytes-Key: " + Integer.toBinaryString(key));
			// for (points[] pt : val.lPlanes) {
			// System.out.println("Values:" + pt[0] + " " + pt[1] + " " + pt[2] + " ");
			// }

		}
		Cube cubibubi = McLut.get(0b11111111111111111111111111111111);
		if (cubibubi != null) {
			System.out.println(cubibubi.corner);
		} else {
			System.out.println("Fail");
		}
	}

	private void addAll(int value, LinkedList<points[]> planes) {
		Cube newCube = new Cube(value, planes);
		McLut.put(value, newCube);
		Cube invertCube = newCube.invert();
		McLut.put(invertCube.corner, invertCube);
		// Brute every orientation
		for (int rotX = 0; rotX < 4; rotX++) {
			for (int rotY = 0; rotY < 4; rotY++) {
				for (int rotZ = 0; rotZ < 4; rotZ++) {
					if (rotX != 0 || rotY != 0 || rotZ != 0) {
						Cube cX;
						if (rotX != 0) {
							cX = newCube.getNewFromRotXAxes(rotX);
						} else {
							cX = newCube;
						}
						Cube cY;
						if (rotY != 0) {
							cY = cX.getNewFromRotYAxes(rotY);
						} else {
							cY = cX;
						}
						Cube cZ;
						if (rotZ != 0) {
							cZ = cY.getNewFromRotZAxes(rotZ);
						} else {
							cZ = cY;
						}
						if (!McLut.contains(cZ.corner)) {
							McLut.put(cZ.corner, cZ);
						}
						Cube inverted = cZ.invert();
						if (!McLut.contains(inverted.corner)) {
							McLut.put(inverted.corner, inverted);
						}

					}
				}
			}
		}

	}

	private void add0(int value, LinkedList<points[]> planes) {
		// New cube
		Cube newCube = new Cube(value, planes);
		McLut.put(value, newCube);
		// Inverted Cube
		Cube invertCube = newCube.invert();
		McLut.put(invertCube.corner, invertCube);
	}

	private void add1(int value, LinkedList<points[]> planes) {
		// New cube
		Cube newCube = new Cube(value, planes);
		McLut.put(value, newCube);
		// Inverted Cube
		Cube invertCube = newCube.invert();
		McLut.put(invertCube.corner, invertCube);
		System.out.println("invert: " + Integer.toBinaryString(invertCube.corner));
		// Rotate Cube
		Cube bottomCube1 = newCube.getNewFromRotZAxes(1);
		McLut.put(bottomCube1.corner, bottomCube1);
		Cube bottomCube2 = newCube.getNewFromRotZAxes(2);
		McLut.put(bottomCube2.corner, bottomCube2);
		Cube bottomCube3 = newCube.getNewFromRotZAxes(3);
		McLut.put(bottomCube3.corner, bottomCube3);
		// Invert bottom
		Cube invertBotCube1 = bottomCube1.invert();
		McLut.put(invertBotCube1.corner, invertBotCube1);
		Cube invertBotCube2 = bottomCube2.invert();
		McLut.put(invertBotCube2.corner, invertBotCube2);
		Cube invertBotCube3 = bottomCube3.invert();
		McLut.put(invertBotCube3.corner, invertBotCube3);

		Cube topCube0 = newCube.getNewFromRotYAxes(1);
		McLut.put(topCube0.corner, topCube0);
		// System.out.println("top: " +Integer.toBinaryString(topCube0.corner));
		Cube topCube1 = topCube0.getNewFromRotZAxes(1);
		McLut.put(topCube1.corner, topCube1);
		Cube topCube2 = topCube0.getNewFromRotZAxes(2);
		McLut.put(topCube2.corner, topCube2);
		Cube topCube3 = topCube0.getNewFromRotZAxes(3);
		McLut.put(topCube3.corner, topCube3);
	}

	// // No planes
	// private void add(int value) {
	// McLut.put((byte) value, new GeometryArray[] {});
	// }
	//
	// // 1 Triangle
	// private void add(int value, Point3f tri_a, Point3f tri_b, Point3f tri_c) {
	// TriangleArray triangle = new TriangleArray(3, TriangleArray.COORDINATES);
	// triangle.setCoordinates(3, new Point3f[] { tri_a, tri_b, tri_c });
	// McLut.put((byte) value, new GeometryArray[] { triangle });
	// }
	//
	// // 1 Quadliteral
	// private void add(int value, Point3f quad_a, Point3f quad_b, Point3f quad_c, Point3f quad_d) {
	// QuadArray quad = new QuadArray(3, QuadArray.COORDINATES);
	// quad.setCoordinates(4, new Point3f[] { quad_a, quad_b, quad_c, quad_d });
	// McLut.put((byte) value, new GeometryArray[] { quad });
	// }
	//
	// // 1 Quadliteral & 1 Triangle
	// private void add(int value, Point3f tri_a, Point3f tri_b, Point3f tri_c, Point3f quad_a, Point3f quad_b, Point3f quad_c, Point3f quad_d) {
	// this.add(value, tri_a, tri_b, tri_c);
	// this.add(value, quad_a, quad_b, quad_c);
	// }
	//
	// // 2 Triangle
	// private void add(int value, Point3f tri_1_a, Point3f tri_1_b, Point3f tri_1_c, Point3f tri_2_a, Point3f tri_2_b, Point3f tri_2_c) {
	// this.add(value, tri_1_a, tri_1_b, tri_1_c);
	// this.add(value, tri_2_a, tri_2_b, tri_2_c);
	// }
	//
	// // 2 Quadliteral
	// private void add(int value, Point3f quad_1_a, Point3f quad_1_b, Point3f quad_1_c, Point3f quad_1_d, Point3f quad_2_a, Point3f quad_2_b, Point3f quad_2_c, Point3f quad_2_d) {
	// this.add(value, quad_1_a, quad_1_b, quad_1_c, quad_1_d);
	// this.add(value, quad_2_a, quad_2_b, quad_2_c, quad_2_d);
	// }
	//
	// private byte addRotateX(int rot) {
	// return 0;
	//
	// }
	//
	// private byte addRotateY(int rot) {
	// return 0;
	// }
	//
	// // Rotate around the bottom plane
	// // 1 0 0 0 => 0 1 0 0 => 0 0 1 0
	// private byte addRotateZ(int value, Point3f tri_a, Point3f tri_b, Point3f tri_c, int rot) {
	// BitSet bits = new BitSet(8);
	// boolean bs[] = new boolean[8];
	// byte bVal = (byte) value;
	// bs[0] = (bVal & 0x00) != 0;
	// bs[1] = (bVal & 0x01) != 0;
	// bs[2] = (bVal & 0x02) != 0;
	// bs[3] = (bVal & 0x03) != 0;
	// bs[4] = (bVal & 0x04) != 0;
	// bs[5] = (bVal & 0x05) != 0;
	// bs[6] = (bVal & 0x06) != 0;
	// bs[7] = (bVal & 0x07) != 0;
	//
	// for (int i = 1; i <= rot; i++) {
	// boolean helper = bs[0];
	// bs[0] = bs[1];
	// bs[1] = bs[5];
	// bs[5] = bs[4];
	// bs[4] = helper;
	// helper = bs[3];
	// bs[3] = bs[2];
	// bs[2] = bs[6];
	// bs[6] = bs[7];
	// bs[7] = helper;
	//
	// }
	// for (int i = 0; i < 8; i++) {
	// if (bs[i] == true) {
	// bits.set(i);
	// }
	// }
	// return bits.toByteArray()[0];
	// // int retVal = bits.toLongArray();
	// }

	/** Case 0 **/
	// // McLut.put((byte) 0x00000000, new float[] { 0, 0, 0 });
	// add(0b00000000);
	// // Case 0 Inverse
	// add(0b11111111);
	//
	// /** Case 1 and Inverse **/
	// add(0b01111111, p01, p03, p04);
	// add(0b10000000, p01, p03, p04);
	// // Case 1 Rotation and Inverse
	// // 1
	// add(0b10111111, p01, p12, p15);
	// add(0b01000000, p01, p12, p15);
	// // 2
	// add(0b11011111, p12, p23, p26);
	// add(0b00100000, p12, p23, p26);
	// // 3
	// add(0b11101111, p03, p23, p37);
	// add(0b00010000, p03, p23, p37);
	// // 4
	// add(0b11110111, p04, p45, p47);
	// add(0b00001000, p04, p45, p47);
	// // 5
	// add(0b11111011, p15, p45, p56);
	// add(0b00000100, p15, p45, p56);
	// // 6
	// add(0b11111101, p26, p56, p67);
	// add(0b00000010, p26, p56, p67);
	// // 7
	// add(0b11111110, p37, p47, p67);
	// add(0b00000001, p37, p47, p67);
	//
	// /** Case 2 an Inverse **/
	// // 0 1
	// add(0b00111111, p03, p15, p12, p04);
	// add(0b11000000, p03, p15, p12, p04);
	// // 1 5
	// add(0b10111011, p12, p56, p45, p01);
	// add(0b01000100, p12, p56, p45, p01);
	// // 4 5
	// add(0b11110011, p04, p15, p56, p47);
	// add(0b00001100, p04, p15, p56, p47);
	// // 0 4
	// add(0b01110111, p01, p45, p47, p03);
	// add(0b10001000, p01, p45, p47, p03);
	// // 2 3
	// add(0b11001111, p03, p12, p37, p26);
	// add(0b00110000, p03, p12, p37, p26);
	// // 2 6
	// add(0b11011101, p12, p56, p67, p23);
	// add(0b00100010, p12, p56, p67, p23);
	// // 6 7
	// add(0b11111100, p26, p56, p47, p37);
	// add(0b00000011, p26, p56, p47, p37);
	// // 0 3
	// add(0b01101111, p01, p23, p37, p04);
	// add(0b10010000, p01, p23, p37, p04);
	// // / 1 2
	// add(0b10011111, p01, p23, p15, p26);
	// add(0b01100000, p01, p23, p15, p26);
	// // 5 6
	// add(0b11111001, p15, p26, p45, p67);
	// add(0b00000110, p15, p26, p45, p67);
	// // 47
	// add(0b11110110, p04, p37, p67, p45);
	// add(0b00001001, p04, p37, p67, p45);
	//
	// /** Case 3 and Inverse **/
}
