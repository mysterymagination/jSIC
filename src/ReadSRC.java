/*
 * This class reads in the SIC source code and translates it to machine code which is passed on to Optab/Symtab.
 * It will null-out the property holding the resultant translation after passing it to Optab or Symtab in preparation
 * for the next instruction to translate.
 */
import java.io.*;
import java.util.*;
import java.math.*;


public class ReadSRC {
	private  FileReader mySource;// = new FileReader(new File("assets/example.txt"));
	private Scanner filescan;// = new Scanner(fr);
    private Scanner linescan;
	private InputStream is;
	private Reader r;
	private Optab optab;
	private Symtab symtab;
	private Double locctr;
	private String sLocctr;
	private Double DDnval;
	private double startAddress;
	private Double startAddress2;
	private WriteToIF writer;
	private int InstructionsRead = 0;
	private boolean startSpecified = false;
	private double programLength;
	private Double programLength2;
	private String sProgramLength;
	private StreamTokenizer st;
	private boolean moveOn = true;
	private Key myKey;
	private boolean opcodeDone = false;
	private boolean labelDone = false;
	private boolean instFound = false;
	private BufferedWriter myWriter;
	private String name;
	private String ATW; //address to write-- needs to be continually reset to null if used in a loop
	private ArrayList<String> lineLength = new ArrayList<String>();
	private int hexDigitCount = 0;
	private int hol = 0;//hexOpcodeLength
	private int oal = 0;//operandAddressLength
	private boolean constantCaseB = false;
	private boolean cfound = false;
	private boolean xfound = false;
	private int resCount = 0;
	private String userArg;
	
	
	public ReadSRC(Optab optab, Symtab symtab, String arg){
		this.optab = optab;
		this.symtab = symtab;
		this.userArg = arg;
		/*
		try {
			myWriter = new BufferedWriter(new FileWriter("assets/IF.txt") );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*this will occur in class ReadIF... need to have Pass1 completed before can begin Pass2
		try {
			myWriter2 = new BufferedWriter(new FileWriter("assets/IF.txt") );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		//writer = new WriteToIF();
	}
	
	public String parseSrc() throws Exception{
		 // r = new FileReader("assets/example.txt"); //need to be able to specify files via command line args
		r = new FileReader(userArg);    
		st = new StreamTokenizer(r);
		    st.eolIsSignificant(true);
		    st.commentChar(46);
		    st.whitespaceChars(9, 9);
		    st.whitespaceChars(32, 32);
		    st.wordChars(39, 39);
		    st.wordChars(44, 44);
		   
		    //first we check for a specified start address
		    while (InstructionsRead < 2) {
		    	if (st.sval != null) {
		    		InstructionsRead++;
		    		if (st.sval.equals("START")) {
		    		while(st.ttype != StreamTokenizer.TT_NUMBER) {
		    			st.nextToken();
		    		}
		    		DDnval = st.nval;
		    		startAddress = Integer.parseInt("" + DDnval.intValue(), 16);
		    		locctr = startAddress;
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
		    		//writer.write("" + locctr); //should write to IF, not working yet 2/15/2011
		    		startSpecified = true;
		    		}
		    		else if (!st.sval.equals("START") && InstructionsRead < 2) { //this should be the program name, also could allow for label here potentially...
		    			//this is the name of the program
		    			//search SYMTAB for label etc. 
		    			name = st.sval;
		    		}
		    	}
		     st.nextToken();
		    }
		    if (!startSpecified) {
		    	startAddress = 0;
		    	locctr = startAddress;
		    }
		    startAddress2 = startAddress;
		    /*
		    ATW = Integer.toHexString(startAddress2.intValue());
		    
		    for (int i = 0; i<(6-ATW.length()); i++){
		    	myWriter.write("0");
		    }
		    myWriter.write(ATW);
		    myWriter.newLine();//end of line for now; in ReadIF when this line is read and translated to object code, the program length will be tacked on the end
		    */
		    
		    //Now that startAddress has been established, we move on to the main parsing loop
		    while (st.ttype != StreamTokenizer.TT_EOF) { //starts parsing to end of file
		    	//System.out.println("Stuck in eof");
		    	//iLocctr = locctr.intValue();
		    	instFound = false;
		    	opcodeDone = false;
		    	labelDone = false;
		    	moveOn = true;
		    	//constantCaseW = false;
		    	constantCaseB = false;
		    	xfound = false;
		    	cfound = false;
		    	
		    	InstructionsRead = 0; //init InsRead to 0 at the start of each line
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
		    		
		    		moveOn = true;
		    		if (st.ttype == StreamTokenizer.TT_WORD) { 
		    			InstructionsRead++;
		    			System.out.println(st.sval);
		    			System.out.println("Instructions Read: " + InstructionsRead);
		    			if (foundEnd()) {
		    				break;
		    			}
		    			/*
		    			 * The whole instructinsread control architecture doesn't quite work, because
		    			 * the ST doesn't count the whitespace it reads in.  This is by design because
		    			 * the prof specified that whitespace is non-regulated and thus we cannot
		    			 * predict exactly how many instances of char dec value 32 (space) will appear
		    			 * before and/or between the label/instruction/operand 'fields'.  
		    			 * 
		    			 * What we could try alternatively is to structure the control around'
		    			 * the optab, since it it static and populated pre-runtime.  The schema might
		    			 * go something like this: first convert whatever the sval or nval is to string.
		    			 * Then call the optab.searchOpcode method with the resultant string as the input
		    			 * parameter.  If the string is in optab then it is an instruction and the ST is in the
		    			 * instruction 'field' and boolean foundInst is set to true.  If it is not in optab AND a boolean variable foundInst which resets to
		    			 * false at the beginning of each new line is still false, then it is a label being declared in the label 'field'
		    			 * If it is not in the optab AND foundInst is true, then the ST is at the operand 'field'.
		    			 * This should work even if the prof has a crappy line with just a label declaration and no instruction or operand, because there
		    			 * definitely cannot be an operand without an instruction...
		    			 */
		    			if (instFound){
		    				processOperand(st);
		    			}
		    			if (!instFound) {//this is either label or instruction field
		    				//before anything, search optab to see if this might be the opcode
		    				//if it is an opcode, send it to the opcode processing function
		    				if (st.sval.equals("WORD") || st.sval.equals("BYTE")){//these are the directives... not technically instructions, but they go in the instruction field
		    					resCount = 0;
		    					if (st.sval.equals("WORD")){
		    						if (hexDigitCount >= 55){
		    							newTHandle();
		    						}
		    						hexDigitCount += 6;
		    					}
		    					if (st.sval.equals("BYTE")){
		    						constantCaseB = true;
		    					}
		    					instFound = true;
		    					processOpcode(st);
		    				}
		    				else if (st.sval.equals("RESW") || st.sval.equals("RESB")){//these are the directives... not technically instructions, but they go in the instruction field
		    					resCount++;
		    					newTHandle();
		    					instFound = true;
		    					processOpcode(st);
		    				}
		    				else if (optab.searchOpcode(st.sval)) {//if true this is the instruction
		    					resCount = 0;
		    					if (hexDigitCount >= 55){
	    							newTHandle();
	    						}
		    					hexDigitCount += 6;
		    					instFound = true;
		    					processOpcode(st);
		    					//InstructionsRead++;
		    				}
		    				else {//otherwise this is the label
		    					processLabel(st);
		    				}
		    				
		    				
		    				
		    			}
		    			//else{ //if instFound is true, this must be the operand field
		    				
		    				//processOperand(st);
		    			//}
		    			//if (InstructionsRead == 3) {//this is the operand field
		    				//processOperand();
		    				
		    			//}
		    		}
		    		
		    		if (st.ttype == StreamTokenizer.TT_NUMBER) {
		    			InstructionsRead++;
		    			if (!instFound) {//this is either label or instruction field
		    				//before anything, search optab to see if this might be the opcode
		    				//if it is an opcode, send it to the opcode processing function
		    				if (NtoString(st.nval).equals("WORD") || NtoString(st.nval).equals("BYTE")){//these are the directives... not technically instructions, but they go in the instruction field
		    					resCount = 0;
		    					if (NtoString(st.nval).equals("WORD")){
		    						if (hexDigitCount >= 55){
		    							newTHandle();
		    						}
		    						hexDigitCount += 6;
		    					}
		    					if (NtoString(st.nval).equals("BYTE")){
		    						constantCaseB = true;
		    					}
		    					instFound = true;
		    					processOpcodeN(st);
		    				}
		    				if (NtoString(st.nval).equals("RESW") || NtoString(st.nval).equals("RESB")){//these are the directives... not technically instructions, but they go in the instruction field
		    					resCount++;
		    					newTHandle();
		    					instFound = true;
		    					processOpcodeN(st);
		    				}
		    				else if (optab.searchOpcode("" + st.nval)) {
		    					resCount = 0;
		    					if (hexDigitCount >= 55){
	    							newTHandle();
	    						}
		    					hexDigitCount += 6;
		    					instFound = true;
		    					processOpcodeN(st);
		    					//InstructionsRead++;
		    				}
		    				else {
		    					processLabelN(st);
		    				}
		    				
		    			}
		    			else{ //this is the operand field
		    				processOperandN(st);
		    			}
		    		}
		    		
		    	if (moveOn){
		    	st.nextToken(); //read next token in current line
		    	}
		    	}
		    	////write line just finished to IF eventually
		     if (moveOn){
		     st.nextToken(); //read first token of next line	
		     }
		    }
		    programLength = (locctr - startAddress); 
		    programLength2 = programLength;
		    System.out.println(" !!prgmlngth2:" + Integer.toHexString(programLength2.intValue()));
		    /*
		    sProgramLength = Integer.toHexString(programLength2.intValue());
		    for (int i = 0; i<(6-sProgramLength.length()); i++){
		    	myWriter.write("0");
		    }
		    myWriter.write(sProgramLength);
		    myWriter.close();
		    ////myWriter.close();
		     
		    */
		    r.close();
		    System.out.println("?????!?!?!?!?ALPHA?!?!?!?!?!??!?!?!");
		    if (hexDigitCount/2 < 16){
		    	lineLength.add("0" + Integer.toHexString(hexDigitCount/2));
		    }
		    else{
		    lineLength.add(Integer.toHexString(hexDigitCount/2));
		    }
		    for (int i = 0; i<lineLength.size(); i++){
		    System.out.println(lineLength.get(i));
		    }
		   // System.out.println(hexDigitCount);
		    ReadIF pass2 = new ReadIF(this.optab,this.symtab,this.startAddress2,this.programLength2,this.name,this.lineLength,this.userArg);
		    return st.sval;
		    
		

	}
	public void processLabel(StreamTokenizer st) {
		//Search symtab for the current st.sval
		//and if the sval is found, raise an error flag for duplicate symbol declaration
		if(symtab.searchLabel(st.sval)) {
		
			System.out.println(st.sval);
			System.out.println("Error: Duplicate Label declaration");
		}
		//otherwise, insert <st.sval,locctr> into symtab.
		else {
			sLocctr = Integer.toHexString(locctr.intValue());
    		if (sLocctr.length() == 1) {
    			
    			symtab.setKeyVal(new Key(st.sval,"000" + Integer.toHexString(locctr.intValue()),symtab), "000" + Integer.toHexString(locctr.intValue()));
    			
    		}
    		else if (sLocctr.length() == 2) {
    			symtab.setKeyVal(new Key(st.sval,"00" + Integer.toHexString(locctr.intValue()),symtab), "00" + Integer.toHexString(locctr.intValue()));
    		}
    		else if (sLocctr.length() == 3 ) {
    			symtab.setKeyVal(new Key(st.sval,"0" + Integer.toHexString(locctr.intValue()),symtab), "0" + Integer.toHexString(locctr.intValue()));
    		}
    		else {
    			symtab.setKeyVal(new Key(st.sval,"" + Integer.toHexString(locctr.intValue()),symtab), "" + Integer.toHexString(locctr.intValue()));
    		}
		}
		/*
		try {
			myWriter.write(st.sval);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		labelDone = true;
	}
	public void processOpcode(StreamTokenizer st) throws IOException {
		if (optab.searchOpcode(st.sval)) {
			locctr += 3;
			System.out.println("opcode Prcoessed: " + st.sval);
		}
		else if (st.sval.equals("WORD")) {
			locctr += 3;
		}
		else if (st.sval.equals("RESW")) {
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_NUMBER){
				locctr += (3 * st.nval);
			}
			if (st.ttype == StreamTokenizer.TT_WORD){
				locctr += (3 * Integer.parseInt(symtab.getValueByKeyName(st.sval).trim()));
			}
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
			moveOn = false; //makes the next call to nextToken moot.  Must be reset to true someplace
		}
		else if (st.sval.equals("BYTE")) {
			//length of constant in bytes
			st.nextToken();
			if (st.sval.substring(0,1).equals("C")) {
				cfound = true;
				System.out.println("" + (st.sval.length() - 3));
				locctr += (st.sval.length() - 3); //this is an ASCII character constant, each char is 1 byte
			}
			if (st.sval.substring(0,1).equals("X")) {
				xfound = true;
				System.out.println("" + Math.ceil(((st.sval.length() - 3)/2)));
				locctr += Math.ceil(((st.sval.length() - 3)/2)); //this is a hex constant, each char is 1/2 bytes 
			}
			moveOn = false;
		}
		else {
			System.out.println("Invalid operation code"); //eventually we'll add possibility for this to move on to processing the value as an operand if it meets certain conditions
		}
		opcodeDone = true;
	
	}
	public void processOperand(StreamTokenizer st) {
		//process the operand here according to algorithm in book
		if (constantCaseB){
			if (cfound){
				if (hexDigitCount == 60 || hexDigitCount >= (61 - (st.sval.substring(2, st.sval.length()-1).length() * 2))){
					newTHandle();
				}
				hexDigitCount += (st.sval.substring(2, st.sval.length()-1).length() * 2);
			}
			else if (xfound){
				if (hexDigitCount == 60 || hexDigitCount >= (61 - st.sval.substring(2, st.sval.length()-1).length())){
					newTHandle();
				}
				hexDigitCount += (st.sval.substring(2, st.sval.length()-1).length());
			}
		}
	}
	
	public void processLabelN(StreamTokenizer st) {
		//Search symtab for the current st.nval
		//and if the nval is found, raise an error flag for duplicate symbol declaration
		if(symtab.searchLabel("" + st.nval)) {
		
			System.out.println("" + st.nval);
			System.out.println("Error: Duplicate Label declaration");
		}
		//otherwise, insert <"" + st.nval,locctr> into symtab.
		else {
			sLocctr = Integer.toHexString(locctr.intValue());
    		if (sLocctr.length() == 1) {
    			
    			symtab.setKeyVal(new Key("" + st.nval,"000" + Integer.toHexString(locctr.intValue()),symtab), "000" + Integer.toHexString(locctr.intValue()));
    		}
    		else if (sLocctr.length() == 2) {
    			symtab.setKeyVal(new Key("" + st.nval,"00" + Integer.toHexString(locctr.intValue()),symtab), "00" + Integer.toHexString(locctr.intValue()));
    		}
    		else if (sLocctr.length() == 3 ) {
    			symtab.setKeyVal(new Key("" + st.nval,"0" + Integer.toHexString(locctr.intValue()),symtab), "0" + Integer.toHexString(locctr.intValue()));
    		}
    		else {
    			symtab.setKeyVal(new Key("" + st.nval,"" + Integer.toHexString(locctr.intValue()),symtab), "" + Integer.toHexString(locctr.intValue()));
    		}
		}
		labelDone = true;
	}
	public void processOpcodeN(StreamTokenizer st) throws IOException {
		if (optab.searchOpcode("" + st.nval)) {
			locctr += 3;
		}
		
		else if (NtoString(st.nval).equals("WORD")) {
			locctr += 3;
		}
		else if (NtoString(st.nval).equals("RESW")) {
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_NUMBER){
				locctr += (3 * st.nval);
			}
			if (st.ttype == StreamTokenizer.TT_WORD){
				locctr += (3 * Integer.parseInt(symtab.getValueByKeyName(st.sval).trim()));
			}
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
			moveOn = false; //makes the next call to nextToken moot.  Must be reset to true someplace
		}
		else if (NtoString(st.nval).equals("BYTE")) {
			//length of constant in bytes
			st.nextToken();
			if (st.sval.substring(0,1).equals("C")) {
				System.out.println("" + (st.sval.length() - 3));
				locctr += (st.sval.length() - 3); //this is an ASCII character constant, each char is 1 byte
			}
			if (st.sval.substring(0,1).equals("X")) {
				System.out.println("" + Math.ceil(((st.sval.length() - 3)/2)));
				locctr += Math.ceil(((st.sval.length() - 3)/2)); //this is a hex constant, each char is 1/2 bytes 
			}
			moveOn = false;
		}
		
		else {
			System.out.println("Invalid operation code"); //eventually we'll add possibility for this to move on to processing the value as an operand if it meets certain conditions
		}
		opcodeDone = true;
	
	}
	public void processOperandN(StreamTokenizer st) {
		//process the operand here according to algorithm in book
		if (constantCaseB){
			if (cfound){
				hexDigitCount += (NtoString(st.nval).substring(2, NtoString(st.nval).length()-1).length() * 2);
			}
			else if (xfound){
				hexDigitCount += (NtoString(st.nval).substring(2, NtoString(st.nval).length()-1).length());
			}
		}
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
		return "" + dub;
	}
	public void newTHandle(){
		//if (hexDigitCount > 0){
		if (resCount < 2){
			if (hexDigitCount/2 < 16){
		    	lineLength.add("0" + Integer.toHexString(hexDigitCount/2));
		    }
		    else{
		    lineLength.add(Integer.toHexString(hexDigitCount/2));
		    }
		}
		//}
	    hexDigitCount = 0;
	}
	
}
