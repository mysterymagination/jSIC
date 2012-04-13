/*
 * This is the Main driver class which will control the others.  
 */
/*
 * ISSUE TRACKER
 * 
 * --2/20/2011 Need to be able to specify input/output file(s) via command line args.
 *   $see the project version  we released to prof for this implementation
 * 2/22/2011 Need an arraylist to hold Key objects as they are created in setKeyVal.  These must be associated with
 *           their corresponding keyNames so that one can search the HashTable for the keyName string fast,
 *           and then find out its corresponding Key object if necessary to access Key properties like the collision list.
 *           
 *           ALTERNATIVELY, could make a second hashtable keysTable within Symtab which would assoicate Key keyNames as keys 
 *           with Key keyID numbers as values.  This would improve the search efficiency in getKeyByName, as the values would
 *           be hashed; wouldn't need the sequential list iteration it does now.
 *           
 *           $$$BETTER YET, we could alter the existing hash table so that itm holds String value keyNames as keys and Key objects as
 *           values, and those key objects have a keyValue property which can be accessed immediately after the
 *           Key object is found.  This way we only have one hash table for symtab, and it should be able to do everything
 *           we need regarding address value reporting and linked list handling for collisions.
 */
public class Pass1Main {
	private static ReadSRC read;// = new ReadSRC();
	private static Optab optab;
	private static Symtab symtab;
	//private static WriteToIF writer;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		optab = new Optab();
		System.out.println("opcodearray length " + Optab.countE(optab.opcodeArray));
		System.out.println("instructionsarray length " + Optab.countE(optab.instructionArray));
		symtab = new Symtab();
		read = new ReadSRC(optab,symtab,args[0]);
		//writer = new WriteToIF();
		optab.genOptable();
		try {
			read.parseSrc();
		} catch (Exception e) {
			System.out.println("Couldn't read file");
			e.printStackTrace();
		}
		
		symtab.printSymTab();
		
		

	}

}
