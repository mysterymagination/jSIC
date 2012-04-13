/*
 * This class reads in the SIC source code a second time now with knowledge of optab/symtab and translates it to object code
 */
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.math.*;


public class ReadIF {
	private  FileReader mySource;// = new FileReader(new File("assets/example.txt"));
	private Scanner filescan;// = new Scanner(fr);
    private Scanner linescan;
	private InputStream is;
	private Reader r;
	private Optab optab;
	private Symtab symtab;
	private Double locctr;
	private Double tempLocctr;
	private String sLocctr;
	private Double DDnval;
	private double startAddress;
	private Double startAddress2;
	private WriteToIF writer;
	private int InstructionsRead = 0;
	private boolean startSpecified = false;
	private Double programLength2;
	private String sProgramLength;
	private StreamTokenizer st;
	private boolean moveOn = true;
	private Key myKey;
	private boolean opcodeDone = false;
	private boolean labelDone = false;
	private boolean instFound = false;
	private boolean operandFound = false;
	private BufferedWriter myWriter;
	private String name;
	private String ATW; //address to write-- needs to be continually reset to null if used in a loop
	private String operandAddress; //saved for writing when operand processed
	private String hexOpcode; //saved for writing when the opcode is processed
	private boolean jumpT = false;
	private int tp = 0; //token pairs (6 hex digit inst/operand pair) processed into object code.  Normally 10 per Text record.
	private int trc = 0; //text record count
	private ArrayList<String> trLengthTrack;  
	private boolean allZeroes = false;
	private boolean constantCaseW = false;
	private boolean constantCaseB = false;
	private int hexDigitCount = 0;
	private String theByte;
	private String theBytes;
	private boolean xfound = false;
	private boolean cfound = false;
	private boolean reswFound = false;
	private boolean resbFound = false;
	private int resCount = 0;
	private int resLoc = 0; //locctr before the reserve statement
	private int resLock = 0; //the locked down locctr value before the FIRST reserve statement, if there is a chain
	private String resLocs = "";
	private int resBuffer = 0; //this will track the total space to be reserved, as specified by the operands after a reserve directive.
	private Key thisKey;
	private String properKeyName;
	private String thisKeyName;
	private String thisKeyValue;
	private String thisVal;
	private int srcLineNumber = 0;
	private int resAcc = 0;
	private BufferedWriter myWriter2;
	private FileReader read2; 
    private StreamTokenizer st2; 
    private String count5;
    private boolean useTemp = false;
    private int lineNumber = -1;
    private String userArg;
	
	
	public ReadIF(Optab optab, Symtab symtab,Double startAddr, Double programLength, String name, ArrayList<String> lineLength,String arg){
		this.userArg = arg;
		this.trLengthTrack = lineLength;
		this.optab = optab;
		this.symtab = symtab;
		this.programLength2 = programLength;
		this.startAddress2 = startAddr;
		this.sProgramLength = Integer.toHexString(programLength.intValue());
		this.name = name;
		
		try {
			myWriter = new BufferedWriter(new FileWriter("c:/obj.txt") );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			parseSrc2();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void parseSrc2() throws Exception{
		
		 // r = new FileReader("assets/example.txt"); //need to be able to specify files via command line args
		r = new FileReader(userArg);    
		st = new StreamTokenizer(r);
		    st.eolIsSignificant(true);
		    st.commentChar(46);
		    st.whitespaceChars(9, 9);
		    st.whitespaceChars(32, 32);
		    st.wordChars(39, 39);
		    st.wordChars(44, 44);
		    //st.ordinaryChar(44); //this is the first step in dealing with the "symbol,X" operands....
		    
		   
		   //first we start the H record with the program name
		   myWriter.write("H");
		   myWriter.write(name); //the name of the program
		    	
		    
		    
		    //write the rest of the H record
		    ATW = Integer.toHexString(startAddress2.intValue());
		    
		    for (int i = 0; i<(6-ATW.length()); i++){
		    	myWriter.write("0");
		    }
		    myWriter.write(ATW);
		    sProgramLength = Integer.toHexString(programLength2.intValue());
		    for (int i = 0; i<(6-sProgramLength.length()); i++){
		    	myWriter.write("0");
		    }
		    myWriter.write(sProgramLength);
		    
		    /*
		    myWriter.newLine();//end of H record.  Time to move on to T records...
		    myWriter.write("T");//very first T record started here explicitly for simplicity
		    */
		    //init locctr
		    locctr = startAddress2;
		    newT();
		    //Now we move on to the main parsing loop
		    while (st.ttype != StreamTokenizer.TT_EOF) { //starts parsing again to end of file
		    	//System.out.println("Stuck in eof");
		    	//iLocctr = locctr.intValue();
		    	srcLineNumber++;
		    	instFound = false;
		    	operandFound = false;
		    	opcodeDone = false;
		    	labelDone = false;
		    	moveOn = true;
		    	constantCaseW = false;
		    	constantCaseB = false;
		    	reswFound = false;
		    	resbFound = false;
		    	allZeroes = false;
		    	xfound = false;
		    	cfound = false;
		    	useTemp = false;
		    	
		    	
		    	/*not this simple; there should be a new Text record every 10 processed tokens unless there is a jump/reserve directive which causes a new text record right away...
		    	if (!jumpT){//if this isn't an odd case with a reserve directive etc., we should be writing a new T record?? 
		    	  myWriter.write("T");
		    	}
		    	*/
		    	InstructionsRead = 0; //init InsRead to 0 at the start of each line
		    	
		    	hexOpcode = "00";
		    	operandAddress = "0000";
		    	
		    	/*
		    	hexOpcode = "";
		    	operandAddress = "";
		    	*/
		    	System.out.println("new line");
		    	System.out.println("Ins read: " + InstructionsRead);
		    	sLocctr = Integer.toHexString(locctr.intValue());
	    		if (sLocctr.length() == 1) {
	    			System.out.println("locctr: 000" + Integer.toHexString(locctr.intValue()));
	    		}
	    		else if (sLocctr.length() == 2) {
	    			System.out.println("locctr: 00" + Integer.toHexString(locctr.intValue()));
	    		}
	    		else if (sLocctr.length() == 3 ) {
	    			System.out.println("locctr: 0" + Integer.toHexString(locctr.intValue()));
	    		}
	    		else {
	    			System.out.println("locctr: " + Integer.toHexString(locctr.intValue()));
	    		}
		    	if (foundEnd()) {
		    		break;
		    	}
		    	while (st.ttype != StreamTokenizer.TT_EOL) {//breaks up parsing by lines so that InstructionsRead will be a useful count
		    		useTemp = false;
		    		moveOn = true;
		    		if (st.ttype == StreamTokenizer.TT_WORD) { 
		    			InstructionsRead++;
		    			System.out.println(st.sval);
		    			System.out.println("Instructions Read: " + InstructionsRead);
		    			if (foundEnd()) {
		    				break;
		    			}
		    			
		    			if (instFound) { //if instFound is true, this must be the operand field
		    				operandFound = true;
		    				processOperand(st);
		    			}
		    			if (!instFound) {//this is either label or instruction field
		    				//before anything, search optab to see if this might be the opcode
		    				//if it is an opcode, send it to the opcode processing function
		    				if (optab.searchOpcode(st.sval)) {//if true this is the instruction
		    					if (resCount > 0){
		    						resAcc = resAcc/2;
		    						resAcc += resLock;
		    						//resAcc = resAcc/2;
		    						resLocs = Integer.toHexString(resAcc);
		    						for (int i = 0; i<(6-resLocs.length()); i++){
		    					    	myWriter.write("0");
		    					    }
		    						myWriter.write(resLocs);
		    						lineNumber++;
		    						myWriter.write(trLengthTrack.get(lineNumber));
		    						//myWriter.write("??" + " ");
		    						resCount = 0;
		    						resAcc = 0;
		    					}
		    					//resCount = 0;
		    					instFound = true;
		    					//System.out.println("WRREC address: " + symtab.getValueByKeyName("WRREC"));
		    					//System.out.println("WRREC in symtab: " + symtab.searchLabel("WRREC"));
		    					System.out.println("DEBUG!!!!!" + st.sval + " " + optab.getOpcode(st.sval) + " " + optab.getOpcode("STL") + " " + optab.getOpcode("ADDR") + " " + optab.getOpcode("HIO") + "/DEBUG!!!!!!!");
		    					hexOpcode = Integer.toHexString(Integer.parseInt(optab.getOpcode(st.sval), 2));
		    					if (optab.getOpcode(st.sval).equals("00000000")){
		    						hexOpcode += "0";
		    					}
		    					if (optab.getOpcode(st.sval).substring(0, 4).equals("0000") && !optab.getOpcode(st.sval).equals("00000000")){
		    						hexOpcode = "0" + hexOpcode;
		    					}
		    					processOpcode(st);
		    				}
		    				else if (st.sval.equals("WORD") || st.sval.equals("BYTE")){//these are the constant directives...
		    					if (resCount > 0){
		    						resAcc = resAcc/2;
		    						resAcc += resLock;
		    						//resAcc = resAcc/2;
		    						resLocs = Integer.toHexString(resAcc);
		    						for (int i = 0; i<(6-resLocs.length()); i++){
		    					    	myWriter.write("0");
		    					    }
		    						myWriter.write(resLocs);
		    						lineNumber++;
		    						myWriter.write(trLengthTrack.get(lineNumber));
		    						//myWriter.write("??" + " ");
		    						resCount = 0;
		    						resAcc = 0;
		    					}
		    					//resCount = 0;
		    					instFound = true;
		    					processOpcode(st);
		    				}
		    				else if (st.sval.equals("RESW") || st.sval.equals("RESB")){//these are the reserve directives... not technically instructions, but they go in the instruction field
		    					instFound = true;
		    					hexOpcode = "";
		    					operandAddress = "";
		    					resLoc = locctr.intValue();
		    					processOpcode(st);
		    				}
		    				
		    				else {//otherwise this is the label
		    					//labels are all declared in symtab, so no more processLabel function
		    					//processLabel(st);
		    					
		    				}
		    				
		    				
		    				
		    			}
		    			
		    			
		    		}
		    		
		    		if (st.ttype == StreamTokenizer.TT_NUMBER) {
		    			InstructionsRead++;
		    			System.out.println(st.nval);
		    			System.out.println("Instructions Read: " + InstructionsRead);
		    			if (foundEnd()) {
		    				break;
		    			}
		    			
		    			if (instFound) { //if instFound is true, this must be the operand field
		    				operandFound = true;
		    				processOperandN(st);
		    			}
		    			if (!instFound) {//this is either label or instruction field
		    				//before anything, search optab to see if this might be the opcode
		    				//if it is an opcode, send it to the opcode processing function
		    				if (optab.searchOpcode("" + st.nval)) {//if true this is the instruction
		    					if (resCount > 0){
		    						resAcc = resAcc/2;
		    						resAcc += resLock;
		    						//resAcc = resAcc/2;
		    						resLocs = Integer.toHexString(resAcc);
		    						for (int i = 0; i<(6-resLocs.length()); i++){
		    					    	myWriter.write("0");
		    					    }
		    						myWriter.write(resLocs);
		    						lineNumber++;
		    						myWriter.write(trLengthTrack.get(lineNumber));
		    						//myWriter.write("??" + " ");
		    						resCount = 0;
		    						resAcc = 0;
		    					}
		    					//resCount = 0;
		    					instFound = true;
		    					hexOpcode = Integer.toHexString(Integer.parseInt(optab.getOpcode("" + st.nval), 2));
		    					processOpcodeN(st);
		    				}
		    				else if (NtoString(st.nval).equals("WORD") || NtoString(st.nval).equals("BYTE")){//these are the constant directives...
		    					if (resCount > 0){
		    						resAcc = resAcc/2;
		    						resAcc += resLock;
		    						//resAcc = resAcc/2;
		    						resLocs = Integer.toHexString(resAcc);
		    						for (int i = 0; i<(6-resLocs.length()); i++){
		    					    	myWriter.write("0");
		    					    }
		    						myWriter.write(resLocs);
		    						lineNumber++;
		    						myWriter.write(trLengthTrack.get(lineNumber));
		    						//myWriter.write("??" + " ");
		    						resCount = 0;
		    						resAcc = 0;
		    					}
		    					//resCount = 0;
		    					instFound = true;
		    					processOpcodeN(st);
		    				}
		    				else if (NtoString(st.nval).equals("RESW") || NtoString(st.nval).equals("RESB")){//these are the reserve directives... not technically instructions, but they go in the instruction field
		    					instFound = true;
		    					hexOpcode = "";
		    					operandAddress = "";
		    					resLoc = locctr.intValue();
		    					processOpcodeN(st);
		    					
		    					//instFound = true;
		    					//processOpcodeN(st);
		    				}
		    				
		    				else {//otherwise this is the label
		    					//labels are all declared in symtab, so no more processLabel function
		    					//processLabel(st);
		    				}
		    				
		    				
		    				
		    			}
		    			
		    			
		    		}
		    		
		    	if (moveOn){
		    	st.nextToken(); //read next token in current line
		    	}
		    	if (st.ttype == StreamTokenizer.TT_EOL && !operandFound && srcLineNumber > 1){
		    		/*
		    		if (hexDigitCount + 6 > 60){ //perhaps revisit this when 24 zeroes problem solved....
		    			newT();
		    		}
		    		*/
		    		writeTP();
		    	}
		    	}
		    	////write line just finished to IF eventually
		     if (moveOn){
		     st.nextToken(); //read first token of next line	
		     }
		    }
		    
		    r.close();
		    newE();
		    //lastStep();
		    myWriter.close();
		    //for (int i = 0; i<trLengthTrack.size(); i++){
		    	//System.out.println(trLengthTrack.get(i));
		   // }
		   
	        //printSymTab();
		

	}
	
	public void processOpcode(StreamTokenizer st) throws IOException {
		//if (tp == 9){ //one case in which a text record is completed and a new text record is needed
		if (hexDigitCount == 60){	
			///trLengthTrack.add(Integer.toHexString(trc)); pretty sure we want the tp value here in hex...
			///trLengthTrack.add("1E"); //in this 'normal' case, it is 30 bytes so 1E in hex.
			newT();
		}
		if (optab.searchOpcode(st.sval)) {
			locctr += 3;
		}
		else if (st.sval.equals("WORD")) {
			locctr += 3;
			constantCaseW = true;
			hexOpcode = "";
		}
		else if (st.sval.equals("RESW")) {
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_NUMBER){
				locctr += (3 * st.nval);
			}
			if (st.ttype == StreamTokenizer.TT_WORD){
				locctr += (3 * Integer.parseInt(symtab.getValueByKeyName(st.sval).trim()));
			}
			
			reswFound = true;
			//resCount++;
			hexOpcode = "";
			moveOn = false; //makes the next call to nextToken moot.  Must be reset to true 
			
			
		}
		else if (st.sval.equals("RESB")) {
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_NUMBER){
				locctr += (st.nval);
			}
			if (st.ttype == StreamTokenizer.TT_WORD){
				locctr += (Integer.parseInt(symtab.getValueByKeyName(st.sval).trim()));
			}
			resbFound = true;
			//resCount++;
			hexOpcode = "";
			moveOn = false; //makes the next call to nextToken moot.  Must be reset to true someplace
			
		}
		else if (st.sval.equals("BYTE")) {
			//length of constant in bytes
			st.nextToken();
			if (st.sval.substring(0,1).equals("C")) {
				cfound = true;
				System.out.println("" + (st.sval.length() - 3));
				locctr += (st.sval.length() - 3); //this is an ASCII character constant, each char is 1 byte
				theByte = st.sval.substring(2, (st.sval.length()-1));
				theBytes = "";
				for (int i = 0; i<theByte.length(); i++){
					theBytes = theBytes + Integer.toHexString(theByte.charAt(i));
				}
			}
			if (st.sval.substring(0,1).equals("X")) {
				xfound = true;
				System.out.println("" + Math.ceil(((st.sval.length() - 3)/2)));
				locctr += Math.ceil(((st.sval.length() - 3)/2)); //this is a hex constant, each char is 1/2 bytes 
				theByte = st.sval.substring(2, (st.sval.length()-1));
			}
			moveOn = false;
			constantCaseB = true;
			hexOpcode = "";
		}
		else {
			System.out.println("Invalid operation code"); //eventually we'll add possibility for this to move on to processing the value as an operand if it meets certain conditions
		}
		opcodeDone = true;
	
		//write opcode to object code
		////myWriter.write(hexOpcode);
		
		
	}
	public void processOperand(StreamTokenizer st) throws IOException {
		//process the operand here according to algorithm in book
		
		//if the operand is a symbol...
		//if (symtab.searchLabel(st.sval)){
		//Here's the second part of dealing with the symbol,X issue...
		if (st.sval.length() >= 3 && symtab.getValueByKeyName(st.sval.substring(0,st.sval.length()-2)) != null && st.sval.substring(st.sval.length()-2, st.sval.length()).equals(",X")){
			//so if we have BUFFER,X the comma should be ignored by the st, and the condition should identify the X at the end of a legit symbol 
			operandAddress = symtab.getValueByKeyName(st.sval.substring(0, st.sval.length()-2));
			System.out.println("crazy comma xs thing: " + symtab.getValueByKeyName(st.sval.substring(0, st.sval.length()-2)));
			operandAddress = Integer.toHexString(Integer.parseInt(operandAddress, 16) + 32768);
		}
		else if (symtab.getValueByKeyName(st.sval) != null){
			operandAddress = symtab.getValueByKeyName(st.sval); //this should give us the address of the label in the operand field
		}
		//if the operand is not a symbol (assuming no errors is source code for this project)...
		else if (symtab.getValueByKeyName(st.sval) == null && !constantCaseW && !constantCaseB && !reswFound && !resbFound){
			operandAddress = "0000";
		}
		//If we are dealing with directive WORD....
		else if (constantCaseW){
			operandAddress = "";			
			for (int i = 0; i<6-st.sval.length(); i++){
				operandAddress = operandAddress + "0";
			}
			operandAddress = operandAddress + Integer.toHexString(Integer.parseInt(st.sval)); //the numberformat exception problem here I believe has to do with thre fatc that the sval of the current token is null, since it is a number.  You can't call parseInt on a null value string... hopefully this will be solved when we copy over these processes to the TTYPE_NUMBER handler section.  If not, we may need to remove it to please the compiler, as the operand should be a true number here.  We'll see...
			
			if (hexDigitCount + (operandAddress.length()) > 60){ //this is when the constant overflows the current text record and a new one must be made
				newT();
			}
		}
		//If we are dealing with directive BYTE...
		else if (constantCaseB){
			if (cfound){
				operandAddress = "";			
				for (int i = 0; i<6-st.sval.length(); i++){
					operandAddress = operandAddress + "0";
				}
				operandAddress = theBytes;
				if (hexDigitCount + (operandAddress.length()) > 60){ //this is when the constant overflows the current text record and a new one must be made
					newT();
				}
			}
			else if (xfound){
				operandAddress = "";			
				for (int i = 0; i<6-st.sval.length(); i++){
					operandAddress = operandAddress + "0";
				}
				operandAddress = theByte;
				if (hexDigitCount + (operandAddress.length()) > 60){ //this is when the constant overflows the current text record and a new one must be made
					newT();
				}
			}
		}
		//the case for resw...
		else if (reswFound){
			resBuffer = Integer.parseInt(st.sval) * 3; //reserve buffer size in bytes
			operandAddress = "";
			resCount++;
			newTSpecial();
			
		}
		//the case for resb...
		else if (resbFound){
			resBuffer = Integer.parseInt(st.sval);
			operandAddress = "";
			resCount++;
			newTSpecial();
			
		}
		//if no operand or whatever...
		//else if (allZeroes){
			//operandAddress = "0000";
		//}
		
		writeTP();
		
		//now we increment tp, whether there was an operand token or not (if not, we use the allZeroes thing above.  If there was one follow the books instruction...)
		////tp++; we'll do this in the writeTP method
	}
	
	
	
	public void processOpcodeN(StreamTokenizer st) throws IOException {
		//if (tp == 9){ //one case in which a text record is completed and a new text record is needed
		if (hexDigitCount == 60){	
		///trLengthTrack.add(Integer.toHexString(trc)); pretty sure we want the tp value here in hex...
			//trLengthTrack.add("1E"); //in this 'normal' case, it is 30 bytes so 1E in hex.
			newT();
		}
		if (optab.searchOpcode("" + st.nval)) {
			locctr += 3;
		}
		else if (NtoString(st.nval).equals("WORD")) {
			locctr += 3;
			constantCaseW = true;
			hexOpcode = "";
		}
		else if (NtoString(st.nval).equals("RESW")) {
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_NUMBER){
				locctr += (3 * st.nval);
			}
			if (st.ttype == StreamTokenizer.TT_WORD){
				locctr += (3 * Integer.parseInt(symtab.getValueByKeyName(st.sval).trim()));
			}
			reswFound = true;
			//resCount++;
			hexOpcode = "";
			moveOn = false; //makes the next call to nextToken moot.  Must be reset to true 
			
			
		}
		else if (NtoString(st.nval).equals("RESB")) {
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_NUMBER){
				locctr += (st.nval);
			}
			if (st.ttype == StreamTokenizer.TT_WORD){
				locctr += (Integer.parseInt(symtab.getValueByKeyName(st.sval).trim()));
			}
			resbFound = true;
			//resCount++;
			hexOpcode = "";
			moveOn = false; //makes the next call to nextToken moot.  Must be reset to true someplace
		}
		else if (NtoString(st.nval).equals("BYTE")) {
			//length of constant in bytes
			st.nextToken();
			if (st.sval.substring(0,1).equals("C")) {
				cfound = true;
				System.out.println("" + (st.sval.length() - 3));
				locctr += (st.sval.length() - 3); //this is an ASCII character constant, each char is 1 byte
				theByte = st.sval.substring(2, (st.sval.length()-1));
				theBytes = "";
				for (int i = 0; i<theByte.length(); i++){
					theBytes = theBytes + Integer.toHexString(theByte.charAt(i));
				}
			}
			if (st.sval.substring(0,1).equals("X")) {
				xfound = true;
				System.out.println("" + Math.ceil(((st.sval.length() - 3)/2)));
				locctr += Math.ceil(((st.sval.length() - 3)/2)); //this is a hex constant, each char is 1/2 bytes 
				theByte = st.sval.substring(2, (st.sval.length()-1));
			}
			moveOn = false;
			constantCaseB = true;
			hexOpcode = "";
			
		}
		else {
			System.out.println("Invalid operation code"); //eventually we'll add possibility for this to move on to processing the value as an operand if it meets certain conditions
		}
		opcodeDone = true;
	
		//write opcode to object code
		////myWriter.write(hexOpcode);
		
		
	}
	public void processOperandN(StreamTokenizer st) throws IOException {
		//process the operand here according to algorithm in book
		System.out.println("!!!!!!!!!!!!!!! " + st.nval + " !!!!!!!!!!!");
		
		if (NtoString(st.nval).length() >= 3 && symtab.getValueByKeyName(NtoString(st.nval).substring(0, NtoString(st.nval).length()-2)) != null && NtoString(st.nval).substring(NtoString(st.nval).length()-2, NtoString(st.nval).length()).equals(",X")){
			//so if we have BUFFER,X the comma should be ignored by the st, and the condition should identify the X at the end of a legit symbol 
			operandAddress = symtab.getValueByKeyName(NtoString(st.nval).substring(0, NtoString(st.nval).length()-2));
			System.out.println("crazy comma xn thing: " + symtab.getValueByKeyName(NtoString(st.nval).substring(0, NtoString(st.nval).length()-2)));
			operandAddress = Integer.toHexString(Integer.parseInt(operandAddress, 16) + 32768);
		}
		
		//if the operand is a symbol...
		else if (symtab.getValueByKeyName(NtoString(st.nval)) != null){
			operandAddress = symtab.getValueByKeyName("" + st.nval); //this should give us the address of the label in the operand field
		}
		//if the operand is not a symbol (assuming no errors is source code for this project)...
		else if (symtab.getValueByKeyName(NtoString(st.nval)) == null && !constantCaseW && !constantCaseB && !reswFound && !resbFound){
			operandAddress = "0000";
		}
		//If we are dealing with directive WORD....
		else if (constantCaseW){
			operandAddress = "";			
			for (int i = 0; i<6-NtoString(st.nval).length(); i++){
				operandAddress = operandAddress + "0";
				
			}
			System.out.println("!!!!!!!!!!!!!!! " + st.nval + " !!!!!!!!!!!");
			Double nval = st.nval;
			//operandAddress = operandAddress + Integer.toHexString(Integer.parseInt(NtoString(st.nval))); //the numberformat exception problem here I believe has to do with thre fatc that the sval of the current token is null, since it is a number.  You can't call parseInt on a null value string... hopefully this will be solved when we copy over these processes to the TTYPE_NUMBER handler section.  If not, we may need to remove it to please the compiler, as the operand should be a true number here.  We'll see...
			operandAddress = operandAddress + Integer.toHexString(nval.intValue());
			System.out.println("!!!!!!!!!!!!!!! " + st.nval + " !!!!!!!!!!!");
			if (hexDigitCount + (operandAddress.length()) > 60){ //this is when the constant overflows the current text record and a new one must be made
				newT();
			}
		}
		//If we are dealing with directive BYTE...
		else if (constantCaseB){
			if (cfound){
				operandAddress = "";			
				for (int i = 0; i<6-NtoString(st.nval).length(); i++){
					operandAddress = operandAddress + "0";
				}
				operandAddress = theBytes;
				if (hexDigitCount + (operandAddress.length()) > 60){ //this is when the constant overflows the current text record and a new one must be made
					newT();
				}
			}
			else if (xfound){
				operandAddress = "";			
				for (int i = 0; i<6-NtoString(st.nval).length(); i++){
					operandAddress = operandAddress + "0";
				}
				operandAddress = theByte;
				if (hexDigitCount + (operandAddress.length()) > 60){ //this is when the constant overflows the current text record and a new one must be made
					newT();
				}
			}
		}
		//the case for resw...
		else if (reswFound){
			resBuffer = Integer.parseInt(NtoString(st.nval)) * 3; //reserve buffer size in bytes
			operandAddress = "";
			resCount++;
			newTSpecial();
		}
		//the case for resb...
		else if (resbFound){
			resBuffer = Integer.parseInt(NtoString(st.nval));
			operandAddress = "";
			resCount++;
			newTSpecial();
		}
		//if no operand or whatever...
		//else if (allZeroes){
			//operandAddress = "0000";
		//}
		
		writeTP();
	}
	
	public boolean foundEnd() {
		if(st.ttype == StreamTokenizer.TT_WORD) {
			if(st.sval.equals("END")) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	public String NtoString(double dub){
		Double x = dub;
		return "" + x.intValue();
	}
	
	
	public void newT(){ //for a new text record after a normally completed one of length 30 bytes
		/*
		if (hexDigitCount != 0){
			if (Integer.toHexString(hexDigitCount/2).length() == 1){
				trLengthTrack.add("0" + Integer.toHexString(hexDigitCount/2));
				System.out.println("Foo 0" + Integer.toHexString(hexDigitCount/2));
			}
			else{
			    trLengthTrack.add(Integer.toHexString(hexDigitCount/2)); //records line length before making a new line
			    System.out.println("Foo " + Integer.toHexString(hexDigitCount/2));
			}
			
			}
		*/
		tp = 0;
		try {
			//myWriter.write('\n');
			myWriter.newLine();
			myWriter.write("T");
			//next record the starting address...
			if (useTemp){
				sLocctr = Integer.toHexString(tempLocctr.intValue());
			}
			if (!useTemp){
			    sLocctr = Integer.toHexString(locctr.intValue());
			}
			for (int i = 0; i<(6-sLocctr.length()); i++){
		    	myWriter.write("0");
		    }
		    myWriter.write(sLocctr);
    		//then the record length in two hex digits, which for now are unknown
		    lineNumber++;
			myWriter.write(trLengthTrack.get(lineNumber));
		    //myWriter.write("??" + " ");
    		hexDigitCount = 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//if there is a reserve or a chain of reserves in a row, ONE new Text record is started, 
	//with its start address equal to the sum of the last locctr value before the jump and the 
	//length of the jump in hex digit bytes
	public void newTSpecial(){ //for a text record beginning after a jump/reserve directive, which requires special startaddress handling in accordance with the specific directive used
		/*
		if (hexDigitCount != 0){
			if (Integer.toHexString(hexDigitCount/2).length() == 1){
				trLengthTrack.add("0" + Integer.toHexString(hexDigitCount/2));
				System.out.println("Foo 0" + Integer.toHexString(hexDigitCount/2));
			}
			else{
			    trLengthTrack.add(Integer.toHexString(hexDigitCount/2)); //records line length before making a new line
			    System.out.println("Foo " + Integer.toHexString(hexDigitCount/2));
			}
			
			}
		*/
		//tp = 0;
		try {
			if (resCount < 2){
			  resLock = resLoc; 
			  //myWriter.write('\n');
			  myWriter.newLine();
			  myWriter.write("T");
			  //myWriter.write("" + resLock);
			  tp = 0;
			  hexDigitCount = 0;
			}
			//the below three lines work in the instance of a single reserve directive, but what about chains...?
			System.out.println("resAcc + " + resBuffer);
			resAcc += resBuffer;
			
			//myWriter.write(resLocs);
			//myWriter.write("??");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void newE(){
		/*
		if (hexDigitCount != 0){
			if (Integer.toHexString(hexDigitCount/2).length() == 1){
				trLengthTrack.add("0" + Integer.toHexString(hexDigitCount/2));
				System.out.println("Foo 0" + Integer.toHexString(hexDigitCount/2));
			}
			else{
			    trLengthTrack.add(Integer.toHexString(hexDigitCount/2)); //records line length before making a new line
			    System.out.println("Foo " + Integer.toHexString(hexDigitCount/2));
			}
			
			}
		*/
		tp = 0;
		try {
			//myWriter.write('\n');
			myWriter.newLine();
			myWriter.write("E");
			//the first executable address...
			for (int i = 0; i<(6-Integer.toHexString(startAddress2.intValue()).length()); i++){
		    	myWriter.write("0");
		    }
		    myWriter.write(Integer.toHexString(startAddress2.intValue()));
    		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void writeTP() throws IOException{
		if (hexOpcode.length() == 1){
			hexOpcode = "0" + hexOpcode;
		}
		if (operandAddress.length() != 0 && operandAddress.length() < 4){
			for (int i = 0; i<4-operandAddress.length(); i++){
				operandAddress = "0" + operandAddress;
			}
		}
		
		if (hexOpcode.length() + operandAddress.length() + hexDigitCount <= 60){
			//if (!hexOpcode.equals(null) && !operandAddress.equals(null)){
			if (hexOpcode.length() != 0 && operandAddress.length() != 0){
			if(hexOpcode.equals("00")){
				hexOpcode = "00";
			}
			else if (hexOpcode.substring(0,1).equals("0")){
				hexOpcode = hexOpcode.substring(0,1) + hexOpcode.substring(1,2);
			}
			if (operandAddress.equals("0000")){
				operandAddress = "0000";
			}
			else if (operandAddress.substring(0,3).equals("000")){
				operandAddress = "000" + operandAddress.substring(3,4);
			}
			else if (operandAddress.substring(0,2).equals("00")){
				operandAddress = "00" + operandAddress.substring(2,4);
			}
			else if (operandAddress.substring(0,1).equals("0")){
				operandAddress = "0" + operandAddress.substring(1,4);
			}
			}
		  myWriter.write(hexOpcode + operandAddress); //unless set to something else explicitly, these two values become "00" and "0000" respectively at the start of each source code line processed
		
		//tp++;
		}
		else{
			useTemp = true;
			tempLocctr = locctr - 3;
			newT();
			//if (!hexOpcode.equals(null) && !operandAddress.equals(null)){
			if (hexOpcode.length() != 0 && operandAddress.length() != 0){
			if(hexOpcode.equals("00")){
				hexOpcode = "00";
			}
			else if (hexOpcode.substring(0,1).equals("0")){
				hexOpcode = hexOpcode.substring(0,1) + hexOpcode.substring(1,2);
			}
			if (operandAddress.equals("0000")){
				operandAddress = "0000";
			}
			else if (operandAddress.substring(0,3).equals("000")){
				operandAddress = "000" + operandAddress.substring(3,4);
			}
			else if (operandAddress.substring(0,2).equals("00")){
				operandAddress = "00" + operandAddress.substring(2,4);
			}
			else if (operandAddress.substring(0,1).equals("0")){
				operandAddress = "0" + operandAddress.substring(1,4);
			}
			}
			myWriter.write(hexOpcode + operandAddress);
		}
		
		//if (resCount == 0){
		 // tp++;
		 // hexDigitCount += 6;
		//}
		if (resCount == 0){
		count5 = hexOpcode + operandAddress;
		hexDigitCount += count5.length();
		}
		
	}
	public void fillBlanks(){
		//iterate through object code and replace the ?? symbols in the text-record-length digits of
		//each text record
		
	}
	public void printSymTab() {
		//iterate through hashtable indexes and print k/v's. 
		//if an index is found with a linked list with length greater than 1, iterate through that list as well 
		//before moving on to next hashtable index
		
		Enumeration<String> keys = this.symtab.symtable.keys();
		System.out.println("!!!!22222222CRAZY SYMTAB CONTENT: /CRAZY2222222");
		
		for (int i = 0; i<this.symtab.symtable.size(); i++) {
			 	thisKeyName = keys.nextElement();
			 	thisKey = this.symtab.symtable.get(thisKeyName);
			 	thisKeyValue = thisKey.getValue();
			   if (thisKey.getClist().size() > 1){
				   //if the linked list has other elements besides the head, must iterate through
				   //them before moving on to next hashtable index
				   for (int x = 0; x<thisKey.getClist().size(); x++){
					   System.out.println(thisKey.getClist().get(x).getName() + " " + thisKey.getClist().get(x).getValue());
					   
				   }
				   
			   }
			   else{
				   System.out.println(thisKeyName + " " + thisKeyValue); 
			   }
		}
	}
	public void lastStep() throws IOException{//seems replace isn't enough; we will need a new filewriter here...
		//myWriter2 = new FileWriter("assets/obj.txt");
		
		//what we need to do is read from obj.txt one character at a time and place directly into objectcode.txt
		//until ? is reached, at which point we place the first hex digit of the two digit line size.  We'll need 
		//something tracking line numbers also [every odd numbered time you hit a question mark, linenumber++] so that
		//we'll know what index of the trLengthTrack array to put down in place of the ??.  We'll also need another hexdigit
		//counter which tracks the position in a line, and knows to create a new line after Integer.parseint(trLengthTrack(linenumber), 16) + 8 
		//chars processed
		
		//Scanner scan = new Scanner(new File("assets/obj.txt")); //need to be able to specify files via command line args
	    read2 = new FileReader("assets/obj.txt");
		st2 = new StreamTokenizer(read2);
		st2.wordChars(63, 63);
		st2.wordChars(33,126);
		st2.wordChars(48, 48);
		//String holder = "";
	    //st2.parseNumbers();
		myWriter2 = new BufferedWriter(new FileWriter("assets/objectcode") );
		int lineNumber = -1;
		
		while(st2.ttype != StreamTokenizer.TT_EOF){
			if (st2.ttype == StreamTokenizer.TT_WORD){
				if (st2.sval.equals("T") || st2.sval.equals("E")){
					myWriter2.newLine();
					myWriter2.write(st2.sval);
					lineNumber++;
				}
				else if (st2.sval.equals("??")){
					myWriter2.write(trLengthTrack.get(lineNumber));
				}
				/*
				else if (st.sval.length() >= 6 && st.sval.substring(0,6).equals("000000")){
					myWriter2.write("000000");
					myWriter2.write(st2.sval);
				}
				else if (st.sval.length() >= 5 && st.sval.substring(0,5).equals("00000")){
					myWriter2.write("00000");
					myWriter2.write(st2.sval);
				}
				else if (st.sval.length() >= 4 && st.sval.substring(0,4).equals("0000")){
					myWriter2.write("0000");
					myWriter2.write(st2.sval);
				}
				else if (st.sval.length() >= 3 && st.sval.substring(0,3).equals("000")){
					myWriter2.write("000");
					myWriter2.write(st2.sval);
				}
				else if (st.sval.length() >= 2 && st.sval.substring(0,2).equals("00")){
					myWriter2.write("00");
					myWriter2.write(st2.sval);
				}
				else if (st.sval.length() >= 1 && st.sval.substring(0,1).equals("0")){
					myWriter2.write("0");
					myWriter2.write(st2.sval);
				}
				*/
				else{
			     myWriter2.write(st2.sval);
				}
			}
			
			
			if (st2.ttype == StreamTokenizer.TT_NUMBER){
				/*
				if (NtoString(st2.nval).length() >= 6 && NtoString(st2.nval).substring(0,6).equals("000000")){
					myWriter2.write("000000");
					myWriter2.write(NtoString(st2.nval));
				}
				else if (NtoString(st2.nval).length() >= 5 && NtoString(st2.nval).substring(0,5).equals("00000")){
					myWriter2.write("00000");
					myWriter2.write(NtoString(st2.nval));
				}
				else if (NtoString(st2.nval).length() >= 4 && NtoString(st2.nval).substring(0,4).equals("0000")){
					myWriter2.write("0000");
					myWriter2.write(NtoString(st2.nval));
				}
				else if (NtoString(st2.nval).length() >= 3 && NtoString(st2.nval).substring(0,3).equals("000")){
					myWriter2.write("000");
					myWriter2.write(NtoString(st2.nval));
				}
				else if (NtoString(st2.nval).length() >= 2 && NtoString(st2.nval).substring(0,2).equals("00")){
					myWriter2.write("00");
					myWriter2.write(NtoString(st2.nval));
				}
				else if (NtoString(st2.nval).length() >= 1 && NtoString(st2.nval).substring(0,1).equals("0")){
					myWriter2.write("0");
					myWriter2.write(NtoString(st2.nval));
				}
				else{
					*/
			     myWriter2.write(NtoString(st2.nval));
				//}
				
		}
			st2.nextToken();
		}
			
		
	    System.out.println("scancloser");
	    read2.close();
	  
	    myWriter2.close();
	    
	    
	}
	
}
