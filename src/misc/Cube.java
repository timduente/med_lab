package misc;

import java.util.BitSet;
import java.util.LinkedList;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;

import misc.MarchingCubeLUT.points;

public class Cube {
	public byte corner;
	// This list contains the enum points as an array of 3 each.
	// Each plane/triangle is defined by eg. p45, 67, p56 => This way an order is introduced and it can be rotated
	public LinkedList<points[]> lPlanes;
	public TriangleArray[] planes;

	public Cube(byte corner, LinkedList<points[]> lPlanes) {
		this.corner = corner;
		this.lPlanes = lPlanes;
		// TODO: Translate lPlanes to planes

	}

	private void populateTriangleArray() {
		int index = 0; 
		for (points[] pt : lPlanes) {
			Point3f[] single = new Point3f[3];
			for (int i = 0; i < 3; i++) {
				switch (pt[i]) {
				case p01: 
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p12: //TODO INDIZES!
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p23:
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p03:
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p45:
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p56:
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p67:
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p47:
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p04:
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p15:
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p26:
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				case p37:
					single[i] = new Point3f(0.0f, -0.5f, -0.5f);
					break;
				}
			}
			planes[index] = new TriangleArray(3, TriangleArray.COORDINATES);
			planes[index].setCoordinates(3, single);
			index++; 
		}
	}

	public Cube getNewFromRotXAxes(int rot) {
		BitSet newCor = BitSet.valueOf(new byte[] { corner });
		LinkedList<points[]> newPlanes = new LinkedList<points[]>();

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

			// Do something with rotation planes

			for (points[] pt : lPlanes) {
				points[] pointsRotated = new points[3];
				for (int index = 0; index < 3; index++) {
					switch (pt[index]) {
					case p01:
						pointsRotated[index] = points.p45;
						break;
					case p12:
						pointsRotated[index] = points.p15;
						break;
					case p23:
						pointsRotated[index] = points.p01;
						break;
					case p03:
						pointsRotated[index] = points.p04;
						break;
					case p45:
						pointsRotated[index] = points.p67;
						break;
					case p56:
						pointsRotated[index] = points.p26;
						break;
					case p67:
						pointsRotated[index] = points.p23;
						break;
					case p47:
						pointsRotated[index] = points.p37;
						break;
					case p04:
						pointsRotated[index] = points.p47;
						break;
					case p15:
						pointsRotated[index] = points.p56;
						break;
					case p26:
						pointsRotated[index] = points.p12;
						break;
					case p37:
						pointsRotated[index] = points.p03;
						break;
					default:
						System.out.println("Error rotating cubes on y-axis");
					}
				}
				newPlanes.add(pointsRotated);
			}
		}
		return new Cube(newCor.toByteArray()[0], newPlanes);
	}

	public Cube getNewFromRotYAxes(int rot) {
		BitSet newCor = BitSet.valueOf(new byte[] { corner });
		LinkedList<points[]> newPlanes = new LinkedList<points[]>();

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

			// Do something with rotation planes

			for (points[] pt : lPlanes) {
				points[] pointsRotated = new points[3];
				for (int index = 0; index < 3; index++) {
					switch (pt[index]) {
					case p01:
						pointsRotated[index] = points.p03;
						break;
					case p12:
						pointsRotated[index] = points.p01;
						break;
					case p23:
						pointsRotated[index] = points.p12;
						break;
					case p03:
						pointsRotated[index] = points.p23;
						break;
					case p45:
						pointsRotated[index] = points.p47;
						break;
					case p56:
						pointsRotated[index] = points.p45;
						break;
					case p67:
						pointsRotated[index] = points.p56;
						break;
					case p47:
						pointsRotated[index] = points.p67;
						break;
					case p04:
						pointsRotated[index] = points.p37;
						break;
					case p15:
						pointsRotated[index] = points.p04;
						break;
					case p26:
						pointsRotated[index] = points.p15;
						break;
					case p37:
						pointsRotated[index] = points.p26;
						break;
					default:
						System.out.println("Error rotating cubes on y-axis");
					}
				}
				newPlanes.add(pointsRotated);
			}
		}
		return new Cube(newCor.toByteArray()[0], newPlanes);
	}

	public Cube getNewFromRotZAxes(int rot) {
		BitSet newCor = BitSet.valueOf(new byte[] { corner });
		LinkedList<points[]> newPlanes = new LinkedList<points[]>();

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

			// Do something with rotation planes

			for (points[] pt : lPlanes) {
				points[] pointsRotated = new points[3];
				for (int index = 0; index < 3; index++) {
					switch (pt[index]) {
					case p01:
						pointsRotated[index] = points.p15;
						break;
					case p12:
						pointsRotated[index] = points.p56;
						break;
					case p23:
						pointsRotated[index] = points.p26;
						break;
					case p03:
						pointsRotated[index] = points.p12;
						break;
					case p45:
						pointsRotated[index] = points.p04;
						break;
					case p56:
						pointsRotated[index] = points.p47;
						break;
					case p67:
						pointsRotated[index] = points.p37;
						break;
					case p47:
						pointsRotated[index] = points.p03;
						break;
					case p04:
						pointsRotated[index] = points.p01;
						break;
					case p15:
						pointsRotated[index] = points.p45;
						break;
					case p26:
						pointsRotated[index] = points.p67;
						break;
					case p37:
						pointsRotated[index] = points.p23;
						break;
					default:
						System.out.println("Error rotating cubes on z-axis");
					}
				}
				newPlanes.add(pointsRotated);
			}
		}
		return new Cube(newCor.toByteArray()[0], newPlanes);
	}

}
