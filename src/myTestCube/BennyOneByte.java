package myTestCube;

public class BennyOneByte {
	byte stored = 0;

	public BennyOneByte(int intToOneByte) {
		stored = (byte) (intToOneByte & 0xff);
	}

	public BennyOneByte(byte byteToStore) {
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

}
