/*package whatever //do not write package name here */

import java.io.*;
import java.util.*;
import java.text.*;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

class CryptoHelper {

	public String encrypt( String plaintext ) throws Exception {
		return encrypt( generateIV(), plaintext );
	}

	public String encrypt( byte [] iv, String plaintext ) throws Exception {

		byte [] decrypted = plaintext.getBytes();
		byte [] encrypted = encrypt( iv, decrypted );

		StringBuilder ciphertext = new StringBuilder();

		ciphertext.append( Base64.getEncoder().encode( iv ) );
		ciphertext.append( ":" );
		ciphertext.append( Base64.getEncoder().encode( encrypted ) );

		return ciphertext.toString();

	}

	public String decrypt( String ciphertext ) throws Exception {
		String [] parts = ciphertext.split( ":" );
		byte [] iv = Base64.getDecoder().decode( parts[0] );
		byte [] encrypted = Base64.getDecoder().decode( parts[1] );
		byte [] decrypted = decrypt( iv, encrypted );
		return new String( decrypted );
	}

	private Key key;

	public CryptoHelper( Key key ) {
		this.key = key;
	}

	public CryptoHelper() throws Exception {
		this( generateSymmetricKey() );
	}

	public Key getKey() {
		return key;
	}

	public void setKey( Key key ) {
		this.key = key;
	}

	public static byte [] generateIV() {
		SecureRandom random = new SecureRandom();
		byte [] iv = new byte [16];
		random.nextBytes( iv );
		return iv;
	}

	public static Key generateSymmetricKey() throws Exception {
		KeyGenerator generator = KeyGenerator.getInstance( "AES" );
		SecretKey key = generator.generateKey();
		return key;
	}

	public byte [] encrypt( byte [] iv, byte [] plaintext ) throws Exception {
		Cipher cipher = Cipher.getInstance( key.getAlgorithm() + "/CBC/PKCS5Padding" );
		cipher.init( Cipher.ENCRYPT_MODE, key, new IvParameterSpec( iv ) );
		return cipher.doFinal( plaintext );
	}

	public byte [] decrypt( byte [] iv, byte [] ciphertext ) throws Exception {
		Cipher cipher = Cipher.getInstance( key.getAlgorithm() + "/CBC/PKCS5Padding" );
		cipher.init( Cipher.DECRYPT_MODE, key, new IvParameterSpec( iv ) );
		return cipher.doFinal( ciphertext );
	}

}

class Node{
    Date timestamp;
    String data;
    String ownerId;
    float value;
    String ownerName;
    String hashOfData;
    int nodeNumber;
    String nodeId;
    String referenceNodeId;
    List<String> childReferenceNodeId;
    List<Node> children;
    static String genesisReferenceNodeId;
    String HashValue;
    float childSum;
    Node(int nN, String rNI, List<String> cRNI, String oI, float v, String oN, List<Node> ch){
        timestamp = new Date();
        nodeNumber = nN;
        nodeId = Character.toString((char)nN);
        referenceNodeId = rNI;
        childReferenceNodeId = cRNI;
        ownerId = oI;
        value = v;
        ownerName = oN;
        children = ch;
        try{
        CryptoHelper crypto = new CryptoHelper();
        hashOfData = Hashes.hash(ownerId.hashCode(),value,ownerName.hashCode());
        data = crypto.encrypt(ownerId+String.format("%.2f",value)+ownerName+hashOfData);
        childSum=0;
        System.out.println("Node Created. Key to access is " + data);}
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void setGenesis(String gRNI){
        genesisReferenceNodeId = gRNI;
    }
    public void addChild(String childId, float val, Node n){
        childReferenceNodeId.add(childId);
        childSum += val;
        children.add(n);
    }
    public String getID(){
        return nodeId;
    }
    
    public float getValue(){
        return value;
    }
    public float getChildSum(){
        return childSum;
    }
    public boolean checkHash(){
        if(Hashes.hash(ownerId.hashCode(), value, ownerName.hashCode()).equals(hashOfData))
            return true;
        return false;
    }
    public boolean verifyOwner(String key) {
        if(checkHash()){
            try{
            CryptoHelper crypto = new CryptoHelper();}
            catch(Exception e){
                e.printStackTrace();
            }
            if(key.equals(data)){
                return true;
            }
            return false;
        }
        return false;
    }
    public void changeValue(float f){
        if(f<childSum){
            int noOfChildren = children.size();
            float dec = (childSum-f)/noOfChildren;
            //trickling down
            for(int i=0; i<noOfChildren; i++){
                children.get(i).changeValue(children.get(i).getValue()-dec);
            }
        } else {
            value = f;
        }
    }
}

class Hashes{
    public static String hash(int a, float b, int c){
        int result = 101;
        result = 37 * result + (a+Float.floatToIntBits(b)+c);
        return String.format("%d", result);
    }
}


public class GFG {
	public static void main (String[] args) throws Exception {
		int nodes =0;
		nodes++;
	    //1
	    float val = (float)26.5;
		Node GenesisNode = new Node(nodes, null, new ArrayList<String>(), "Owner 1", val, "Ram", new ArrayList<Node>());
		Node.setGenesis(GenesisNode.getID());
		//2
		nodes++;
		float childVal = new Random().nextFloat()*(GenesisNode.getValue()-GenesisNode.getChildSum());
		Node child1 = new Node(nodes, GenesisNode.getID(), new ArrayList<String>(), "Owner 1", childVal, "Ram", new ArrayList<Node>());
		GenesisNode.addChild(child1.getID(), childVal, child1);
		nodes++;
		childVal = new Random().nextFloat()*(GenesisNode.getValue()-GenesisNode.getChildSum());
		Node child2 = new Node(nodes, GenesisNode.getID(), new ArrayList<String>(), "Owner 1", childVal, "Ram", new ArrayList<Node>());
		GenesisNode.addChild(child2.getID(), childVal, child2);
		nodes++;
		childVal = new Random().nextFloat()*(GenesisNode.getValue()-GenesisNode.getChildSum());
		Node child3 = new Node(nodes, GenesisNode.getID(), new ArrayList<String>(), "Owner 1", childVal, "Ram", new ArrayList<Node>());
		GenesisNode.addChild(child3.getID(), childVal, child3);
		//3 Child node originating from child3
		nodes++;
		childVal = new Random().nextFloat()*(child3.getValue()-child3.getChildSum());
		Node childNode = new Node(nodes, child3.getID(), new ArrayList<String>(), "Owner 1", childVal, "Ram", new ArrayList<Node>());
		child3.addChild(childNode.getID(), childVal, childNode);
		//4 Encryption and decryption already in the Node constructor
		//5 Verifying owner by key
		Scanner sc  = new Scanner(System.in);
		System.out.println("Enter key for GenesisNode, ie, the first key : ");
		String key = sc.nextLine();
		if(GenesisNode.verifyOwner(key)){
		    System.out.println("Valid owner");
		} else {
		    System.out.println("INValid owner");
		}
		//6 Editing value of GenesisNode
		System.out.println("Current Value of GenesisNode is "+GenesisNode.getValue());
		System.out.println("Input new Value: ");
		float newVal = sc.nextFloat();
		GenesisNode.changeValue(newVal);
		
	}
}
