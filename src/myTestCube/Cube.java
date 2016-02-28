package myTestCube;

import java.util.BitSet;
import java.util.LinkedList;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;

public class Cube {
	public int corner;
	// This list contains the enum points as an array of 3 each.
	// Each plane/triangle is defined by eg. p45, 67, p56 => This way an order
	// is introduced and it can be rotated
	public LinkedList<int[]> lPlanes;
	public TriangleArray[] planes;

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

	public Cube(int corner, LinkedList<int[]> lPlanes) {
		this.corner = corner & 0xFF;
		this.lPlanes = lPlanes;
		// TODO: Translate lPlanes to planes
		planes = new TriangleArray[lPlanes.size()];
		populateTriangleArray();
		// System.out.println("New Cube with : " + Integer.toBinaryString(corner));
	}
	
	public Cube(Cube aCube)	{
		this(aCube.corner, aCube.lPlanes); 
	}

	public Cube invert() {
		BitSet newCor = BitSet.valueOf(new byte[] { (byte) (corner & 0xFF) });
		LinkedList<int[]> newPlanes = new LinkedList<int[]>();

		// Change bit value and points of plane
		for (int i = 0; i < 8; i++) {
			newCor.flip(i);
		}

		// System.out.println("cor1 " + Integer.toBinaryString(corner));
		// System.out.println("cor2 " +
		// Integer.toBinaryString(newCor.toByteArray()[0] & 0xFF));

		for (int[] pt : lPlanes) {
			int[] threePoints = new int[pt.length];
			threePoints[0] = new Integer(pt[2]);
			threePoints[1] = new Integer( pt[1]);
			threePoints[2] = new Integer(pt[0]);

			newPlanes.add(threePoints);
		}
		return new Cube(newCor.toByteArray()[0] & 0xFF, newPlanes);
	}

	private void populateTriangleArray() {
		int index = 0;
		for (int[] pt : lPlanes) {
			Point3f[] single = new Point3f[3];
			for (int i = 0; i < 3; i++) {
				switch (pt[i]) {
				case p01:
					single[i] = new Point3f(0.0f, -1.0f, -1.0f);
					break;
				case p12:
					single[i] = new Point3f(+1.0f, -1.0f, 0.0f);
					break;
				case p23:
					single[i] = new Point3f(0.0f, -1.0f, +1.0f);
					break;
				case p03:
					single[i] = new Point3f(-1.0f, -1.0f, 0.0f);
					break;
				case p45:
					single[i] = new Point3f(0.0f, +1.0f, -1.0f);
					break;
				case p56:
					single[i] = new Point3f(+1.0f, +1.0f, 0.0f);
					break;
				case p67:
					single[i] = new Point3f(0.0f, +1.0f, +1.0f);
					break;
				case p47:
					single[i] = new Point3f(-1.0f, +1.0f, 0.0f);
					break;
				case p04:
					single[i] = new Point3f(-1.0f, 0.0f, -1.0f);
					break;
				case p15:
					single[i] = new Point3f(+1.0f, 0.0f, -1.0f);
					break;
				case p26:
					single[i] = new Point3f(+1.0f, 0.0f, +1.0f);
					break;
				case p37:
					single[i] = new Point3f(-1.0f, 0.0f, +1.0f);
					break;
				}
			}
			planes[index] = new TriangleArray(3, TriangleArray.COORDINATES);
			planes[index].setCoordinates(0, single);
			index++;
		}
	}

	public Cube getNewFromRotXAxes(int rot) {
		BitSet newCor = BitSet.valueOf(new byte[] { (byte) corner });
		LinkedList<int[]> newPlanes = new LinkedList<int[]>();

		// Change bit value and points of plane
		for (int i = 1; i <= rot; i++) {
			boolean helper = newCor.get(0);
			newCor.set(0, newCor.get(3));
			newCor.set(3, newCor.get(7));
			newCor.set(7, newCor.get(4));
			newCor.set(4, helper);
			helper = newCor.get(5);
			newCor.set(5, newCor.get(1));
			newCor.set(1, newCor.get(2));
			newCor.set(2, newCor.get(6));
			newCor.set(6, helper);
		}
		for (int[] pt : lPlanes) {
			int[] ptRot = new int[3];
			ptRot[0] = new Integer(pt[0]);
			ptRot[1] = new Integer(pt[1]);
			ptRot[2] = new Integer(pt[2]);
			for (int i = 1; i <= rot; i++) {
				int[] ptRotTemp = new int[3]; // Needed, otherwise switch case will be changed while inside for loop.
				ptRotTemp[0] = new Integer(ptRot[0]);
				ptRotTemp[1] = new Integer(ptRot[1]);
				ptRotTemp[2] = new Integer(ptRot[2]);
				for (int index = 0; index < 3; index++) {
					switch (ptRotTemp[index]) {
					case p01:
						ptRot[index] = new Integer(p45);
						break;
					case p12:
						ptRot[index] = new Integer(p15);
						break;
					case p23:
						ptRot[index] = new Integer(p01);
						break;
					case p03:
						ptRot[index] = new Integer(p04);
						break;
					case p45:
						ptRot[index] = new Integer(p67);
						break;
					case p56:
						ptRot[index] = new Integer(p26);
						break;
					case p67:
						ptRot[index] = new Integer(p23);
						break;
					case p47:
						ptRot[index] = new Integer(p37);
						break;
					case p04:
						ptRot[index] = new Integer(p47);
						break;
					case p15:
						ptRot[index] = new Integer(p56);
						break;
					case p26:
						ptRot[index] = new Integer(p12);
						break;
					case p37:
						ptRot[index] = new Integer(p03);
						break;
					default:
						System.out.println("Error rotating cubes on y-axis");
					}
				}
			}
			newPlanes.add(ptRot);

		}
		return new Cube(newCor.toByteArray()[0], newPlanes);
	}

	public Cube getNewFromRotYAxes(int rot) {
		BitSet newCor = BitSet.valueOf(new byte[] { (byte) corner });
		LinkedList<int[]> newPlanes = new LinkedList<int[]>();

		// Change bit value and points of plane
		for (int i = 1; i <= rot; i++) {
			boolean helper = newCor.get(0);
			newCor.set(0, newCor.get(3));
			newCor.set(3, newCor.get(2));
			newCor.set(2, newCor.get(1));
			newCor.set(1, helper);
			helper = newCor.get(4);
			newCor.set(4, newCor.get(7));
			newCor.set(7, newCor.get(6));
			newCor.set(6, newCor.get(5));
			newCor.set(5, helper);
		}
		// Rotiere Plane
		// Rotiere x-mal
		for (int[] pt : lPlanes) {
			int[] ptRot = new int[3];
			ptRot[0] = new Integer(pt[0]);
			ptRot[1] = new Integer(pt[1]);
			ptRot[2] = new Integer(pt[2]);
			for (int i = 1; i <= rot; i++) {
				int[] ptRotTemp = new int[3]; // Needed, otherwise switch case will be changed while inside for loop.
				ptRotTemp[0] = new Integer(ptRot[0]);
				ptRotTemp[1] = new Integer(ptRot[1]);
				ptRotTemp[2] = new Integer(ptRot[2]);
				for (int index = 0; index < 3; index++) {
					switch (ptRotTemp[index]) {
					case p01:
						ptRot[index] = new Integer(p03);
						break;
					case p12:
						ptRot[index] = new Integer(p01);
						break;
					case p23:
						ptRot[index] = new Integer(p12);
						break;
					case p03:
						ptRot[index] = new Integer(p23);
						break;
					case p45:
						ptRot[index] = new Integer(p47);
						break;
					case p56:
						ptRot[index] = new Integer(p45);
						break;
					case p67:
						ptRot[index] = new Integer(p56);
						break;
					case p47:
						ptRot[index] = new Integer(p67);
						break;
					case p04:
						ptRot[index] = new Integer(p37);
						break;
					case p15:
						ptRot[index] = new Integer(p04);
						break;
					case p26:
						ptRot[index] = new Integer(p15);
						break;
					case p37:
						ptRot[index] = new Integer(p26);
						break;
					default:
						System.out.println("Error rotating cubes on y-axis");
					}
				}
			}
			newPlanes.add(ptRot);

		}
		return new Cube(newCor.toByteArray()[0], newPlanes);
	}

	public Cube getNewFromRotZAxes(int rot) {
		BitSet newCor = BitSet.valueOf(new byte[] { (byte) corner });
		LinkedList<int[]> newPlanes = new LinkedList<int[]>();

		// Change bit value and points of plane
		for (int i = 1; i <= rot; i++) {
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
		}
		for (int[] pt : lPlanes) {
			int[] ptRot = new int[3];
			ptRot[0] = new Integer(pt[0]);
			ptRot[1] = new Integer(pt[1]);
			ptRot[2] = new Integer(pt[2]);
			for (int i = 1; i <= rot; i++) {
				int[] ptRotTemp = new int[3];
				ptRotTemp[0] = new Integer(ptRot[0]);
				ptRotTemp[1] = new Integer(ptRot[1]);
				ptRotTemp[2] = new Integer(ptRot[2]);
				for (int index = 0; index < 3; index++) {
					switch (ptRotTemp[index]) {
					case p01:
						ptRot[index] = new Integer(p15);
						break;
					case p12:
						ptRot[index] = new Integer(p56);
						break;
					case p23:
						ptRot[index] = new Integer(p26);
						break;
					case p03:
						ptRot[index] = new Integer(p12);
						break;
					case p45:
						ptRot[index] = new Integer(p04);
						break;
					case p56:
						ptRot[index] = new Integer(p47);
						break;
					case p67:
						ptRot[index] = new Integer(p37);
						break;
					case p47:
						ptRot[index] = new Integer(p03);
						break;
					case p04:
						ptRot[index] = new Integer(p01);
						break;
					case p15:
						ptRot[index] = new Integer(p45);
						break;
					case p26:
						ptRot[index] = new Integer(p67);
						break;
					case p37:
						ptRot[index] = new Integer(p23);
						break;
					default:
						System.out.println("Error rotating cubes on z-axis");
					}
				}
			}
			newPlanes.add(ptRot);

		}
		return new Cube(newCor.toByteArray()[0], newPlanes);
	}

	public String toString() {
		String retVal = "Cube: ";
		String binString = Integer.toBinaryString(corner & 0xFF);
		  while (binString.length() < 8) {    //pad with 16 0's
		        binString = "0" + binString;
		  }
		  retVal+= binString + " with lPlanes size: " + this.lPlanes.size() + " & planes length: " + planes.length + "\n";
		for (int[] pt : lPlanes) {
			retVal += "  Points: " + toPt(pt[0]) + " " + toPt(pt[1]) + " " + toPt(pt[2]) + "\n";
		}
		return retVal;

	}
	
	public String toStringLess() {
		String retVal = "Cube: ";
		String binString = Integer.toBinaryString(corner & 0xFF);
		  while (binString.length() < 8) {    //pad with 16 0's
		        binString = "0" + binString;
		  }
		  retVal+= binString;
		for (int[] pt : lPlanes) {
			retVal += "  Points: " + toPt(pt[0]) + " " + toPt(pt[1]) + " " + toPt(pt[2]);
		}
		return retVal;

	}
	
	private String toPt(int pt)	{
		switch (pt) {
		case p01:
			return "p01"; 
		case p12:
			return "p12";
		case p23:
			return "p23";
		case p03:
			return "p03";
		case p45:
			return "p45";
		case p56:
			return "p56";
		case p67:
			return "p67";
		case p47:
			return "p47";
		case p04:
			return "p04";
		case p15:
			return "p15";
		case p26:
			return "p26";
		case p37:
			return "p37";
		default:
			System.out.println("Error rotating cubes on y-axis");
			return "error"; 
		}
	}
	
	public int debug_rotateX(int rot)	{
		BitSet newCor = BitSet.valueOf(new byte[] { (byte) corner });
//		LinkedList<int[]> newPlanes = new LinkedList<int[]>();

		// Change bit value and points of plane
		for (int i = 1; i <= rot; i++) {
			boolean helper = newCor.get(0);
			newCor.set(0, newCor.get(4));
			newCor.set(4, newCor.get(7));
			newCor.set(7, newCor.get(3));
			newCor.set(3, helper);
			helper = newCor.get(5);
			newCor.set(5, newCor.get(6));
			newCor.set(6, newCor.get(2));
			newCor.set(2, newCor.get(1));
			newCor.set(1, helper);
		}
		return newCor.toByteArray()[0]& 0xFF;
	}
	
	public int debug_rotateY(int rot)	{
		BitSet newCor = BitSet.valueOf(new byte[] { (byte) corner });
//		LinkedList<int[]> newPlanes = new LinkedList<int[]>();

		// Change bit value and points of plane
		for (int i = 1; i <= rot; i++) {
			boolean helper = newCor.get(0);
			newCor.set(0, newCor.get(3));
			newCor.set(3, newCor.get(2));
			newCor.set(2, newCor.get(1));
			newCor.set(1, helper);
			helper = newCor.get(4);
			newCor.set(4, newCor.get(7));
			newCor.set(7, newCor.get(6));
			newCor.set(6, newCor.get(5));
			newCor.set(5, helper);
		}
		return newCor.toByteArray()[0]& 0xFF;
	}
	
	public int debug_rotateZ(int rot)	{
		BitSet newCor = BitSet.valueOf(new byte[] { (byte) corner });
//		LinkedList<int[]> newPlanes = new LinkedList<int[]>();

		// Change bit value and points of plane
		for (int i = 1; i <= rot; i++) {
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
		}
		return newCor.toByteArray()[0]& 0xFF;
	}

}
/** OLD DO NOT USE JUST FOR BACKUP STAY AWAY **/
//public Cube getNewFromRotYAxes(int rot) {
//	BitSet newCor = BitSet.valueOf(new byte[] { (byte) corner });
//	LinkedList<int[]> newPlanes = new LinkedList<int[]>();
//
//	// Change bit value and points of plane
//	for (int i = 1; i <= rot; i++) {
//		boolean helper = newCor.get(0);
//		newCor.set(0, newCor.get(3));
//		newCor.set(3, newCor.get(2));
//		newCor.set(2, newCor.get(1));
//		newCor.set(1, helper);
//		helper = newCor.get(4);
//		newCor.set(4, newCor.get(7));
//		newCor.set(7, newCor.get(6));
//		newCor.set(6, newCor.get(5));
//		newCor.set(5, helper);
//	}
//	// Rotiere Plane
//	// Rotiere x-mal
//	for (int[] pt : lPlanes) {
//		int[] ptRot = new int[3];
//		ptRot[0] = new Integer(pt[0]);
//		ptRot[1] = new Integer(pt[1]);
//		ptRot[2] = new Integer(pt[2]);
//		for (int i = 1; i <= rot; i++) {
//			int[] ptRotTemp = new int[3]; // Needed, otherwise switch case will be changed while inside for loop.
//			ptRotTemp[0] = new Integer(ptRot[0]);
//			ptRotTemp[1] = new Integer(ptRot[1]);
//			ptRotTemp[2] = new Integer(ptRot[2]);
//			for (int index = 0; index < 3; index++) {
//				switch (ptRotTemp[index]) {
//				case p01:
//					ptRot[index] = new Integer(p03);
//					break;
//				case p12:
//					ptRot[index] = new Integer(p01);
//					break;
//				case p23:
//					ptRot[index] = new Integer(p12);
//					break;
//				case p03:
//					ptRot[index] = new Integer(p23);
//					break;
//				case p45:
//					ptRot[index] = new Integer(p47);
//					break;
//				case p56:
//					ptRot[index] = new Integer(p45);
//					break;
//				case p67:
//					ptRot[index] = new Integer(p56);
//					break;
//				case p47:
//					ptRot[index] = new Integer(p67);
//					break;
//				case p04:
//					ptRot[index] = new Integer(p37);
//					break;
//				case p15:
//					ptRot[index] = new Integer(p04);
//					break;
//				case p26:
//					ptRot[index] = new Integer(p15);
//					break;
//				case p37:
//					ptRot[index] = new Integer(p26);
//					break;
//				default:
//					System.out.println("Error rotating cubes on y-axis");
//				}
//			}
//		}
//		newPlanes.add(ptRot);
//
//	}
//	return new Cube(newCor.toByteArray()[0], newPlanes);
//}
