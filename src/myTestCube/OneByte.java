package myTestCube;

public class OneByte {
	byte stored = 0;

	public OneByte(int intToOneByte) {
		stored = (byte) (intToOneByte & 0xff);
	}

	public OneByte(byte byteToStore) {
		stored = byteToStore;
	}

	/**
	 * 
	 * @param index
	 *            0 - 7
	 */
	public void set(int index, boolean value) {
		if (index < 0 || index > 7) {
			System.err.println("Wrong index. 0-7 allowed. Was: " + index);
		}
		
		stored = value ? (byte) (stored | (1 << index)) : (byte) (stored & ~(1 << index));
	}

	/**
	 * 
	 * @param index 0-7
	 * @return
	 */
	public boolean get(int index) {
		if (index < 0 || index > 7) {
			System.err.println("Wrong index. 0-7 allowed. Was: " + index);
		}
		return (stored & (1 << index)) != 0;
	}

	public int getAsInt() {
		return stored & 0xff;
	}

	public byte getAsByte() {
		return stored;
	}
	
	public byte invGetByte(){
		return (byte)~stored;
	}
	
	public void inv(){
		stored = (byte)~stored;
	}
	
	public OneByte invGetThis(){
		this.inv();
		return this;
	}
	

}
