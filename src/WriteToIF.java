/*
 ///This class will write the SYMTAB and OPTAB as they stand after Pass 1 into an Intermediate File to be used for 
  //Pass 2.
   * Well, not anymore.  Now this class will handle the writing of object code to the obj.txt
 */
import java.io.*;

public class WriteToIF {
	private FileWriter IF; 
	
	public WriteToIF() {
		try {
			IF = new FileWriter("assets/obj.txt",true);
		} catch (IOException e) {
			System.out.println("1Could not write to file");
			e.printStackTrace();
		}
	}
	
	public void write(String str) {
		try {
			IF.write(str);
		} catch (IOException e) {
			System.out.println("2Could not write to file");
			e.printStackTrace();
		}
	}

}
