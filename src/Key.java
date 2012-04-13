/*
 * The Key class replaces simply using Strings for keys in the symtab hashtable.  It exists so that
 * we could add a linked list property to it and thereby have a nice distinct linked list
 * assoicated with each distinct Key object...
 */
import java.util.*;//contains linked list classes

public class Key  {
	private LinkedList<Key> collisionList = new LinkedList<Key>();
	private String keyName;
	private String keyVal;
	private Symtab symtable;
	
	public Key(String name, String val, Symtab symtab){
		
		symtable = symtab;
		keyName = name;
		keyVal = val;
		this.collisionList.addFirst(this);
		
	}
	
	public void bucketCheck(Hashtable<String,Key> symtable, int capacity, Key key, String val){
		if (symtable.size() >= capacity) {//this is a collision event because there are no more free buckets-- add this k/v combo to the linked list at current index
			this.collisionList.add(key);
			
		}
		else {
			symtable.put(key.keyName, key);
	    }
	}
	
	public String getName(){
		return this.keyName;
	}
	public String getValue(){
		return this.keyVal;
	}
	public Key getKey(String str){
		if (this.keyName.equals(str)){
			return this;
		}
		else{
			return null;
		}
	}
	
	public String toString(Key key){
		return "" + key;
	}
	public LinkedList<Key> getClist(){
		return this.collisionList;
	}
	
	
	

}
