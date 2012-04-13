/*
 * This class handles the SYMTAB generation [as a hash table], SYMTAB hash index search, and 
 * linked-listing [at collision indexes] functions
 * 
 * When designing the hashtable for SYMTAB, the label is the key and the address is the value
 * The SYMTAB hashtable is dynamic, so it will be searched via general hashtable insertion/retrieval data structures 
 * whenever it is accessed
 */
import java.util.*;//contains the hashtable and linked list classes


public class Symtab {
	 Hashtable<String,Key> symtable = new Hashtable<String,Key>(23);//23 buckets should be an efficient capacity to search
	private int capacity = 23;
	private Key thisKey;
	private String properKeyName;
	private String thisKeyName;
	private String thisKeyValue;
	private String thisVal;
	private int properCount = 1;
	private ArrayList<String> keysArray = new ArrayList<String>();
	private Key properKey = new Key("","",this);
	
	public Hashtable<String,Key> genSymtable() {
		return symtable;
	}
	
	public Hashtable<String,Key> getSymtab(){
		return symtable;
	}
	
	public void setKeyVal(Key key, String val) {
		keysArray.add(key.getName());
		if (symtable.size() > 0){
		for (int i = properCount-1; i<properCount; i++){
			properKeyName = keysArray.get(i);
		}
		properCount++;
		if (properCount == capacity){
			properCount = 1;
		}
		properKey = symtable.get(properKeyName);
		}
		properKey.bucketCheck(symtable, capacity, key, val);
		
	}
	
	public boolean searchLabel(String str) {
		if(symtable.get(str) != null) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	public boolean searchLabelN(String dub) {
		if(symtable.get(dub) != null) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	public void collision() { //if there is a collision, search linked list at given index
		
	}
	public Key getKeyByName(String str){
	  thisKey = symtable.get(str);
		   if (thisKey.getClist().size() > 1){
			   //if the linked list has other elements besides the head, must iterate through
			   //them before moving on to next hashtable index
			   for (int x = 0; x<thisKey.getClist().size(); x++){
				
				   if(thisKey.getClist().get(x).getName().equals(str)){   
					   return thisKey.getClist().get(x);
				   }
				   
				   
			   }
			   return null;
			  
			   
		   }
		   else{
			 
				   return thisKey;
			 
			   
		   }
	    }
		
	public String getValueByKeyName(String str){
		if (searchLabel(str) || searchLabelN(str)){
		thisKey = symtable.get(str);
		   if (thisKey.getClist().size() > 1){
			   //if the linked list has other elements besides the head, must iterate through
			   //them before moving on to next hashtable index
			   for (int x = 0; x<thisKey.getClist().size(); x++){
				
				   if(thisKey.getClist().get(x).getName().equals(str)){   
					   return thisKey.getClist().get(x).getValue();
				   }
				   
				   
			   }
			  return null;
			   
		   }
		   else{
			 
				   return thisKey.getValue();
			  
			   
		   }
		}
		else{
			Enumeration<String> keys = symtable.keys();
			for (int i = 0; i<symtable.size(); i++) {
			 	thisKeyName = keys.nextElement();
			 	thisKey = symtable.get(thisKeyName);
			 	thisKeyValue = thisKey.getValue();
			   if (thisKey.getClist().size() > 1){
				   //if the linked list has other elements besides the head, must iterate through
				   //them before moving on to next hashtable index
				   for (int x = 0; x<thisKey.getClist().size(); x++){
					   if(thisKey.getClist().get(x).getName().equals(str)){
						   return thisKey.getClist().get(x).getValue();
					   }
					   
				   }
				   
			   }
			   else{
				   //return "keyName " + str + " not found.";
			   }
		}
			return null;
		}
	
	}
	public void findValue(String str){
		
	}
	public void printSymTab() {
		//iterate through hashtable indexes and print k/v's. 
		//if an index is found with a linked list with length greater than 1, iterate through that list as well 
		//before moving on to next hashtable index
		
		Enumeration<String> keys = symtable.keys();
		System.out.println("SYMTAB CONTENT: ");
		
		for (int i = 0; i<symtable.size(); i++) {
			 	thisKeyName = keys.nextElement();
			 	thisKey = symtable.get(thisKeyName);
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

}
