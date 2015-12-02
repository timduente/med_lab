package main;

import java.util.Observable;

public class Voxel extends Observable{
	
	public int x, y, z;
	public static Voxel vox = new Voxel(0,0,0);
	
	public Voxel(int _x, int _y, int _z){
		x = _x; 
		y = _y;
		z = _z;
	}
	
	public void setXYZ(int _x, int _y, int _z){
		x = _x;
		y = _y;
		z = _z;
		
		System.out.println("new Voxel: " + toString());
		
		setChanged();
		notifyObservers(new Message(Message.M_REGION_GROW_NEW_SEED));
	}
	
	public String toString(){
		return "Vox( " + x + ", " +y + ", " + z + " )";
	}
}
