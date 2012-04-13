/*
 * This class handles the OPTAB generation [as a hash table] and ?binary searching functions?
 * The OPTAB is static, so the entire table will be defined in this class pre-runtime. 
 * 
 * When designing the hashtable for OPTAB, the mnemonic instruction is the key and the opcode is the value
 * The OPTAB hashtable is static, so it will be searched via a binary search data structure whenever it is accessed
 */
import java.util.*; //contains the hashtable and linked list classes

public class Optab {
	private Hashtable<String,String> optable = new Hashtable<String,String>(59);
	
	String[] opcodeArray = {"00011000","01011000","10010000","01000000","10110100","00101000","10001000","1010000","00100100","01100100","10011100","11000100","11000000","11110100","00111100","00110000","00110100","00111000","01001000","00000000","01101000","01010000","01110000","00001000","01101100","01110100","00000100","11010000","00100000","01100000","10011000","11001000","01000100","11011000","10101100","01001100","10100100","10101000","11110000","11101100","00001100","01111000","01010100","10000000","11010100","00010100","01111100","11101000","10000100","00010000","00011100","01011100","10010100","10110000","11100000","11111000","00101100","10111000","11011100"};
	String[] instructionArray = {"ADD","ADDF","ADDR","AND","CLEAR","COMP","COMPF","COMPR","DIV","DIVF","DIVR","FIX","FLOAT","HIO","J","JEQ","JGT","JLT","JSUB","LDA","LDB","LDCH","LDF","LDL","LDS","LDT","LDX","LPS","MUL","MULF","MULR","NORM","OR","RD","RMO","RSUB","SHIFTL","SHIFTR","SIO","SSK","STA","STB","STCH","STF","STI","STL","STS","STSW","STT","STX","SUB","SUBF","SUBR","SVC","TD","TIO","TIX","TIXR","WD"};
	
	//public Optab() {
		//opcodeArray = 
	//}

	public Hashtable<String,String> genOptable() {
		for (int i = 0; i<59; i++) {
			optable.put(instructionArray[i], opcodeArray[i]);
		}
		//String str = optable.get("ADDF");
		//System.out.println(str);
		return optable;
		
	}
	
	public boolean searchOpcode(String str) {
		if(optable.get(str) != null) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	public String getOpcode(String inst){
		return optable.get(inst);
		
	}
	public static int countE(String[] array){
		return array.length;
	}
	
}
