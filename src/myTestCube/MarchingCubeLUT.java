package myTestCube;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

public class MarchingCubeLUT {
	public Hashtable<Integer, Cube> McLut = new Hashtable<Integer, Cube>();

	// public enum points {
	// p01, p12, p23, p03, p45, p56, p67, p47, p04, p15, p26, p37;
	// }
	/** COPIED INTO CUBE.JAVA - DO NOT CHANGE **/
	private final int p01 = 0;
	private final int p12 = 1;
	private final int p23 = 2;
	private final int p03 = 3;
	private final int p45 = 4;
	private final int p56 = 5;
	private final int p67 = 6;
	private final int p47 = 7;
	private final int p04 = 8;
	private final int p15 = 9;
	private final int p26 = 10;
	private final int p37 = 11;

	/** STAY AWAY **/

	/**
	 * Edges are labeled the way on the excercise sheet. Bottom left 0, bottom front 1, up front 2, up left 3, bottom back 4, bottom right 5, up right 6, up back 7. A cube has the following coordinate system: (0,0,0) is the origin with x increasing to point 1/5/2/6. Y increasing to 4/5/7/6 and z
	 * increasing to up.
	 * 
	 * Therefor there are the following centers between points: 0 1 : 0 , -.5, -.5 1 5 : .5, 0, -.5 4 5 :
	 * 
	 */

	public MarchingCubeLUT() {

		/** Case 0 **/
		add0(0b00000000, new LinkedList<int[]>());
		/** Case 1 **/
		LinkedList<int[]> link1 = new LinkedList<int[]>();
		link1.add(new int[] { p01, p03, p04 });
		addAll(0b10000000, link1);
		add1(0b10000000, link1);
		/** Case 2 **/
		LinkedList<int[]> link2 = new LinkedList<int[]>();
		link2.add(new int[] { p04, p03, p12 });
		link2.add(new int[] { p15, p12, p04 });
		addAll(0b11000000, link2);
		/** Case 3 **/
		LinkedList<int[]> link3 = new LinkedList<int[]>();
		link3.add(new int[] { p04, p03, p01 });
		link3.add(new int[] { p23, p12, p26 });
		addAll(0b10100000, link3);
		/** Case 4 **/
		LinkedList<int[]> link4 = new LinkedList<int[]>();
		link4.add(new int[] { p04, p03, p01 });
		link4.add(new int[] { p56, p26, p67 });
		addAll(0b10000010, link4);
		/** Case 5 **/
		LinkedList<int[]> link5 = new LinkedList<int[]>();
		link5.add(new int[] { p56, p12, p47 });
		link5.add(new int[] { p12, p47, p01 });
		link5.add(new int[] { p01, p47, p04 });
		addAll(0b01001100, link5);
		/** Case 6 **/
		LinkedList<int[]> link6 = new LinkedList<int[]>();
		link6.add(new int[] { p56, p67, p26 });
		link6.add(new int[] { p15, p12, p04 });
		link6.add(new int[] { p03, p04, p12 });
		addAll(0b11000010, link6);
		/** Case 7 **/
		LinkedList<int[]> link7 = new LinkedList<int[]>();
		link7.add(new int[] { p01, p12, p15 });
		link7.add(new int[] { p03, p37, p23 });
		link7.add(new int[] { p26, p67, p56 });
		addAll(0b01010010, link7);
		/** Case 8 **/
		LinkedList<int[]> link8 = new LinkedList<int[]>();
		link8.add(new int[] { p03, p47, p56 });
		link8.add(new int[] { p03, p12, p56 });
		addAll(0b11001100, link8);
		/** Case 9 **/
		LinkedList<int[]> link9 = new LinkedList<int[]>();
		link9.add(new int[] { p03, p37, p67 });
		link9.add(new int[] { p03, p67, p01 });
		link9.add(new int[] { p01, p67, p56 });
		link9.add(new int[] { p01, p15, p56 });
		addAll(0b10001101, link9);
		/** Case 10 **/
		LinkedList<int[]> link10 = new LinkedList<int[]>();
		link10.add(new int[] { p67, p26, p45 });
		link10.add(new int[] { p45, p26, p15 });
		link10.add(new int[] { p04, p01, p23 });
		link10.add(new int[] { p23, p37, p04 });
		addAll(0b10010110, link10);
		/** Case 11 **/
		// TODO TODO TODO TODO TODO CANT SEE TODO TODO TODO
		LinkedList<int[]> link11 = new LinkedList<int[]>();
		link11.add(new int[] { p03, p47, p01 });
		link11.add(new int[] { p67, p26, p47 });
		link11.add(new int[] { p01, p47, p26 });
		link11.add(new int[] { p01, p15, p26 });
		addAll(0b10001110, link11);
		/** Case 12 **/
		LinkedList<int[]> link12 = new LinkedList<int[]>();
		link12.add(new int[] { p37, p03, p23 });
		link12.add(new int[] { p56, p47, p12 });
		link12.add(new int[] { p01, p12, p47 });
		link12.add(new int[] { p01, p47, p04 });
		addAll(0b01011100, link12);
		/** Case 13 **/
		LinkedList<int[]> link13 = new LinkedList<int[]>();
		link13.add(new int[] { p01, p04, p03 });
		link13.add(new int[] { p15, p56, p45 });
		link13.add(new int[] { p12, p23, p26 });
		link13.add(new int[] { p37, p47, p67 });
		addAll(0b10100101, link13);
		/** Case 14 **/
		LinkedList<int[]> link14 = new LinkedList<int[]>();
		link14.add(new int[] { p67, p56, p37 });
		link14.add(new int[] { p56, p37, p01 });
		link14.add(new int[] { p01, p12, p56 });
		link14.add(new int[] { p04, p37, p01 });
		addAll(0b01001101, link14);

		System.out.println("Marching Cube LUT Size: " + McLut.size());
		// this.debug_LUT();
	}

	private void addAll(int value, LinkedList<int[]> planes) {
		Cube newCube = new Cube(value, planes);
		McLut.put(value, newCube);

		Cube invertCube = newCube.invert();
		McLut.put(invertCube.corner, invertCube);

		// Brute every orientation
		if (true) {
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

							if (!McLut.containsKey(cZ.corner)) {
								McLut.put(cZ.corner, cZ);
							}
							Cube inverted = cZ.invert();
							if (!McLut.containsKey(inverted.corner)) {
								McLut.put(inverted.corner, inverted);
							}
							if (Integer.compare(cZ.corner, 110100) == 0 || Integer.compare(inverted.corner, 110100) == 0) {
								System.out.println(rotX + " " + rotY + " " + rotZ);
							}
						}
					}
				}
			}
		}
	}

	private void add0(int value, LinkedList<int[]> planes) {
		// New cube
		Cube newCube = new Cube(value, planes);
		McLut.put(value, newCube);
		// Inverted Cube
		Cube invertCube = newCube.invert();
		McLut.put(invertCube.corner, invertCube);
	}

	private void add1(int value, LinkedList<int[]> planes) {
		// New cube
		Cube newCube = new Cube(value, planes);
		McLut.put(value, newCube);
		// Inverted Cube
		Cube invertCube = newCube.invert();
		McLut.put(invertCube.corner, invertCube);
		
		/** Test 1: Multiple rotation around 1 axes : OKAY **/
//		 Cube cX = null, cY = null, cZ = null;
//		System.out.println("Rot0: " + 0 + ": " + newCube.toStringLess());
//		 for (int rotX = 1; rotX < 40; rotX++) {
//		 cX = newCube.getNewFromRotXAxes(rotX);
//		 System.out.println("RotX: " + rotX + ": " + cX.toStringLess());
//		 if(rotX % 4 ==3)  System.out.println();
//		 }
		// for (int rotY = 1; rotY < 4; rotY++) {
		// cY = newCube.getNewFromRotYAxes(rotY);
		// System.out.println("RotY: " + rotY + ": " + cY.toString());
		// }
		// for (int rotZ = 1; rotZ < 4; rotZ++) {
		// cZ = newCube.getNewFromRotZAxes(rotZ);
		// System.out.println("RotZ: " + rotZ + ": " + cZ.toString());
		// }
		
		/** Test 2: rotation around every axes 0 to 3 times : NOT OKAY **/
//		for (int rotX = 0; rotX < 4; rotX++) {
//			for (int rotY = 0; rotY < 4; rotY++) {
//				for (int rotZ = 0; rotZ < 4; rotZ++) {
//					if (rotX != 0 || rotY != 0 || rotZ != 0) {
//						Cube cX;
//						if (rotX != 0) {
//							cX = newCube.getNewFromRotXAxes(rotX);
////							System.out.println("RotX: " + rotX + ": " + cX.toStringLess());
//						} else {
//							cX = newCube;
//						}
//						Cube cY;
//						if (rotY != 0) {
//							cY = cX.getNewFromRotYAxes(rotY);
////							System.out.println("RotX: " + rotX + ": " + "RotY: " + rotY + ": " + cY.toStringLess());
//						} else {
//							cY = cX;
//						}
//						Cube cZ;
//						if (rotZ != 0) {
//							cZ = cY.getNewFromRotZAxes(rotZ);
////							System.out.println("RotX: " + rotX + ": " + "RotY: " + rotY + ": " + "RotZ: " + rotZ + ": " + cZ.toStringLess());
//						} else {
//							cZ = cY;
//						}
//						System.out.println("RotX: " + rotX + ": " + "RotY: " + rotY + ": " + "RotZ: " + rotZ + ": " + cZ.toStringLess());
//					}
//				}
//			}
//		}
		
		/** Test 3:Selected NOT OKAY rotations**/
		 Cube cX = null, cY = null, cZ = null;
		System.out.println("Rot0: " + 0 + ": " + newCube.toStringLess());
		Cube xrot = newCube.getNewFromRotXAxes(1);
		Cube yrot = newCube.getNewFromRotYAxes(1);
		Cube x0y1z3 = yrot.getNewFromRotZAxes(3);
		Cube x1y0z3 = xrot.getNewFromRotZAxes(3);
		
		System.out.println("x: " + xrot.toStringLess());
		System.out.println("y: "+yrot.toStringLess());
		System.out.println(x0y1z3.toStringLess());
		System.out.println(x1y0z3.toStringLess());
		for (int rotX = 1; rotX < 40; rotX++) {
			cX = newCube.getNewFromRotXAxes(rotX);
			System.out.println("RotX: " + rotX + ": " + cX.toStringLess());
			if (rotX % 4 == 3)
				System.out.println();
		}
//		for (int rotY = 1; rotY < 13; rotY++) {
//			cY = newCube.getNewFromRotYAxes(rotY);
//			System.out.println("RotY: " + rotY + ": " + cY.toStringLess());
//		}
//		for (int rotZ = 1; rotZ < 13; rotZ++) {
//			cZ = newCube.getNewFromRotZAxes(rotZ);
//			System.out.println("RotZ: " + rotZ + ": " + cZ.toStringLess());
//		}
		
	}

	private void debug_LUT() {
		System.out.println("S************DEBUG LUT*****************");
		Enumeration<Integer> enumKey = McLut.keys();
		while (enumKey.hasMoreElements()) {
			int key = enumKey.nextElement();
			Cube val = McLut.get(key);
			System.out.println(val.toString());
		}
		System.out.println("E************DEBUG LUT*****************");
	}
}
