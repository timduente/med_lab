package lab_med;

import java.io.File;

import static org.junit.Assert.*;
import main.ImageStack;
import misc.DiFile;

import org.junit.Before;
import org.junit.Test;


public class DicomParserTest {
	ImageStack imageStack = ImageStack.getInstance();
	
	@Before
	public void prepare(){
		
	}

	@Test
	public void testParserExplizit() throws Exception{	
		
		DiFile file  = new DiFile();
		file.initFromFile("ct_head_ex/CTHd001");
	}
	
	@Test
	public void testParserImplizit() throws Exception{
		DiFile file  = new DiFile();
		file.initFromFile("ct_head_im/CTHd001");
	}
	
	@Test
	public void byteToIntTest(){
		int a = 0;
		byte b = 12;
		byte c = -2;
		a = (( b & 0xFF) << 8) | c & 0xFF;
		assertEquals(12 * (int)Math.pow(2, 8), a);
	}
	
	
}
