package editortrees;

import java.util.ArrayList;


import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import editortrees.Node.Code;
import editortrees.Node.NodeContainer;
/**
 * A height-balanced binary tree with rank that could be the basis for a text
 * editor.
 * 
 * @author Medhansh Khattar
 * TODO: Acknowledge anyone else you got help
 *         from here, along with the help they provided:
 * 
 * 
 * 
 */
public class EditTree {

	private static final Node NULL_NODE = null;
	Node root;
	private int size;
	public static int totalRotations;
	private DisplayableBinaryTree display;

	/**
	 * MILESTONE 1 Construct an empty tree
	 */
	 public EditTree() {
	        root = null;
	        size = 0;
	        this.totalRotations=0;
	    }

	/**
	 * MILESTONE 1 Construct a single-node tree whose element is ch
	 * 
	 * @param ch
	 */
	 public EditTree(char ch) {
	        this.root = new Node(ch);
	        this.size = 1;
	        this.totalRotations=0;
	    }

	/**
	 * MILESTONE 2 Make this tree be a copy of e, with all new nodes, but the same
	 * shape and contents. You can write this one recursively, but you may not want
	 * your helper to be in the Node class.
	 * 
	 * @param e
	 */
	 public EditTree(EditTree e) {
		 this.root = this.copyTrees(e.root);
		 this.totalRotations = 0; // rotations never occur when copying a tree
	 }
	
	private Node copyTrees(Node nodeToCopy) {
		if(nodeToCopy == Node.NULL_NODE || nodeToCopy == null) {
			return nodeToCopy;
		} 
		Node copy = new Node(nodeToCopy.data, nodeToCopy.rank, nodeToCopy.balance);
		this.size++;
		copy.left = copyTrees(nodeToCopy.left);
		copy.right = copyTrees(nodeToCopy.right);
		return copy;
	}

	/**
	 * MILESTONE 3 Create an EditTree whose toString is s. This can be done in O(N)
	 * time, where N is the size of the tree (note that repeatedly calling insert()
	 * would be O(N log N), so you need to find a more efficient way to do this.
	 * 
	 * @param s
	 */
	public EditTree(String s) {
		if(s.length() == 0) {
			this.root = NULL_NODE;
		}
		this.root = createTreeFromString(s, s.length() / 2);
	}

	private Node createTreeFromString(String s, int stringIndex) {
		if(this.invalidIndex(s, stringIndex)) {
			return NULL_NODE;
		}
		Node newNode = new Node(s.charAt(stringIndex));
		this.add(newNode.data);
		newNode.left = this.createTreeFromString(s, stringIndex - 1);
		if(stringIndex == 0 || stringIndex == s.length() - 1) {
			return newNode;
		}
		if(newNode.left != NULL_NODE) {
			newNode.rank++;
		}
		newNode.right = this.createTreeFromString(s, stringIndex + 1);
		return newNode;
	}

	private boolean invalidIndex(String s, int stringIndex) {
		return stringIndex < 0 || stringIndex >= s.length();
	}

	/**
	 * MILESTONE 1 return the string produced by an in-order traversal of this tree
	 */
	@Override
	public String toString() {
		if(this.size == 0) {
			return "";
		}
//		return toStringHelper(this.root);
		return this.root.toStringHelper();
	}

//	private String toStringHelper(Node node) {
//		if(node == Node.NULL_NODE || node == null) {
//			return "";
//		}
//		return toStringHelper(node.left) + String.valueOf(node.data) + toStringHelper(node.right);
//	}

	/**
	 * MILESTONE 1 Just modify the value of this.size whenever adding or removing a
	 * node. This is O(1).
	 * 
	 * @return the number of nodes in this tree, not counting the NULL_NODE if you
	 *         have one.
	 */
	public int size() {
		return this.size; // nothing else to do here.
	}

	/**
	 * MILESTONE 1
	 * 
	 * @param ch character to add to the end of this tree.
	 */
	public void add(char ch) {
		if(this.size == 0) { // adding to an empty tree.
			this.root = new Node(ch); // simply make the root have the value ch
			this.root.balance = Code.SAME;
		} else {
			NodeContainer nc = new NodeContainer();
			root = root.addSimple(ch, nc);
		} // calls recursive method that recurses to the end of the tree
		this.size++;
	}

	/**
	 * MILESTONE 1
	 * 
	 * @param ch  character to add
	 * 
	 * @param pos character added in this in-order position Valid positions range
	 *            from 0 to the size of the tree, inclusive (if called with size, it
	 *            will append the character to the end of the tree).
	 * @throws IndexOutOfBoundsException if pos is negative or too large for this
	 *                                   tree.
	 */
	public void add(char ch, int pos) throws IndexOutOfBoundsException {
		// You can use your O(1) size field/method to determine if the index is valid.
		if(pos < 0 || pos > this.size) { // checks for a valid index
			throw new IndexOutOfBoundsException();
		} else if(this.root == null) { // checks if we are adding to an empty tree
			this.root = new Node(ch);
			this.root.balance = Code.SAME;
		} else { // calls recursive Node helper method
			NodeContainer nc = new NodeContainer();
			root = root.add(ch, pos, nc);
		}
		this.size++;
	} // commit

	/**
	 * MILESTONE 1 This one asks for more info from each node. You can write it
	 * similar to the arraylist-based toString() method from the BinarySearchTree
	 * assignment. However, the output isn't just the elements, but the elements AND
	 * ranks. Former students recommended that this method, while making it a little
	 * harder to pass tests initially, saves them time later since it catches weird
	 * errors that occur when you don't update ranks correctly. For the tree with
	 * root b and children a and c, it should return the string: [b1, a0, c0] There
	 * are many more examples in the unit tests.
	 * 
	 * @return The string of elements and ranks, given in an PRE-ORDER traversal of
	 *         the tree
	 */
	public String toRankString() {
		return toArrayList().toString();
	}

	private ArrayList<String> toArrayList() {
		ArrayList<String> rankArrayList = new ArrayList<String>();
		root.rankArrayListHelper(rankArrayList);
		return rankArrayList;
	}
	
	

	/**
	 * MILESTONE 1
	 * 
	 * @param pos position in the tree
	 * @return the character at that position
	 * @throws IndexOutOfBoundsException if pos is negative or too big. Note that
	 *                                   the pos is now EXclusive of the size of the
	 *                                   tree, since there is no character there.
	 *                                   But you can still use your size
	 *                                   field/method to determine this.
	 */
	public char get(int pos) throws IndexOutOfBoundsException {
		if(!(pos >= 0 && pos <= this.size - 1) || this.size == 0) {
			throw new IndexOutOfBoundsException();
		}
		if(this.root == Node.NULL_NODE) {
			return '\0';
		}
		return this.root.getHelper(pos);
	}

	// MILESTONE 1: They next two "slow" methods are useful for testing, debugging 
	// and the graphical debugger. They are each O(n) and don't make use of rank or 
	// size. In fact, they are the same as you used in an earlier assignment, so we 
	// are providing them for you.
	// Please do not modify them or their recursive helpers in the Node class.
	public int slowHeight() {
		if(this.root == NULL_NODE) {
			return -1;
		}
		return root.slowHeight();
	}

	public int slowSize() {
		return root.slowSize();
	}

	/**
	 * MILESTONE 1 Returns true iff (read as "if and only if") for every node in the
	 * tree, the node's rank equals the size of the left subtree. This will be used
	 * to check that your ranks are being updated correctly. So when you get a
	 * subtree's size, you should NOT refer to rank but find it brute-force, similar
	 * to slowSize(), and actually calling slowSize() might be a good first-pass.
	 * 
	 * For full credit, then refactor it to make it more efficient: do this in O(n)
	 * time, so in a single pass through the tree, and with only O(1) extra storage
	 * (so no temp collections).
	 * 
	 * Instead of using slowSize(), use the same pattern as the sum of heights
	 * problem in HW5. We put our helper class inside the Node class, but you can
	 * put it anywhere it's convenient.
	 * 
	 * PLEASE feel free to call this method (or its recursive helper) in your code
	 * while you are writing your add() method if rank isn't working correctly. You
	 * may also modify it to print WHERE it is failing. It may be most important to
	 * use in Milestone 2, when you are updating ranks during rotations. (We added
	 * some commented-out calls to this method there so show you how it can be
	 * used.)
	 * 
	 * @return True iff each node's rank correctly equals its left subtree's size.
	 */
	/**
	 * Returns true iff for every node in the tree, the node's rank equals the size
	 * of the left subtree.
	 * 
	 * @return True iff each node's rank correctly equals its left subtree's size.
	 */
	public boolean ranksMatchLeftSubtreeSize() {
		BooleanContainer bc = new BooleanContainer(true);
		root.checkRanks(bc);
		return bc.check;
	}

	class BooleanContainer {
		public boolean check;
		
		public BooleanContainer(boolean value) {
			this.check = value;
		}
	}
	
	/**
	 * MILESTONE 2 Similar to toRankString(), but adding in balance codes too.
	 * 
	 * For the tree with root b and a left child a, it should return the string:
	 * [b1/, a0=] There are many more examples in the unit tests.
	 * 
	 * @return The string of elements and ranks, given in an pre-order traversal of
	 *         the tree.
	 */
	
	public String toDebugString() {
		if(this.root == Node.NULL_NODE || this.root == null) {
			return "[]";
		}
		return toArrayListDebug().toString();
	}

	private ArrayList<String> toArrayListDebug() {
		ArrayList<String> debugArrayList = new ArrayList<String>();
		root.rankArrayListDebugHelper(debugArrayList);
		return debugArrayList;
	}

	/**
	 * MILESTONE 2 returns the total number of rotations done in this tree since it
	 * was created. A double rotation counts as two.
	 *
	 * @return number of rotations since this tree was created.
	 */
	public int totalRotationCount() {
		return this.totalRotations;
	}

	/**
	 * MILESTONE 2 Returns true iff (read as "if and only if") for every node in the
	 * tree, the node's balance code is correct based on its childrens' heights.
	 * Like ranksMatchLeftSubtreeSize() above, you'll need to compare your balance
	 * code to the actual brute-force height calculation. You may start with calling
	 * slowHeight(). But then, for full credit, do this in O(n) time, so in a single
	 * pass through the tree, and with only O(1) extra storage (so no temp
	 * collections). Instead of slowHeight(), use the same pattern as the sum of
	 * heights problem in HW5. We put our helper class inside the Node class, but
	 * you can put it anywhere it's convenient.
	 * 
	 * The notes for ranksMatchLeftSubtreeSize() above apply here - this method is
	 * to help YOU as the developer.
	 * 
	 * @return True iff each node's balance code is correct.
	 */
	public boolean balanceCodesAreCorrect() {
		BooleanContainer bc = new BooleanContainer(true);
		root.checkBalance(bc);
		return bc.check;
	}

	/**
	 * MILESTONE 2 Only write this one once your balance codes are correct. It will
	 * rely on correct balance codes to find the height of the tree in O(log n)
	 * time.
	 * 
	 * @return the height of this tree
	 */
	public int fastHeight() 
	{
//	    return fastHeightHelper(root);
		return this.root.fastHeightHelper();
	}

//	private int fastHeightHelper(Node node) {
//	    if (node == null) {
//	        return -1;
//	    }
//	    int leftHeight = fastHeightHelper(node.left);
//	    int rightHeight = fastHeightHelper(node.right);
//	    return Math.max(leftHeight, rightHeight) + 1;
//	}

	/**
	 * MILESTONE 3
	 * 
	 * @param pos position of character to delete from this tree
	 * @return the character that is deleted
	 * @throws IndexOutOfBoundsException
	 */
	public char delete(int pos) throws IndexOutOfBoundsException {
		// Implementation requirement:
		// When deleting a node with two children, you normally replace the
		// node to be deleted with either its in-order successor or predecessor.
		// The tests assume assume that you will replace it with the
		// *successor*.
		if(!(pos >= 0 && pos <= this.size - 1) || this.size == 0) {
			throw new IndexOutOfBoundsException();
		} else if(this.root == Node.NULL_NODE) {
			return '\0';
		}
		NodeContainer nc = new NodeContainer();
		this.root = this.root.deleteHelper(pos, nc);
//		Code checkOtherSubtree = nc.traversalDirection.getOppositeCode();
//		if(checkOtherSubtree.equals(Code.LEFT)) {
//			this.root.left = this.root.left.deleteHelper(-1, nc); // purpose of using -1 as pos is to indicate that we are checking the subtree where deletion did not occur
//		}
		
//		this.root = this.root.checkOtherSubtreeHelper(nc);
		this.size--;
		return nc.dataToReturn;
	}

	/**
	 * MILESTONE 3 This method operates in O(length), where length is the
	 * parameter provided. The way to do this is to recurse/iterate only
	 * over the nodes of the tree (and possibly their children) that
	 * contribute to the output string.
	 * 
	 * @param pos    location of the beginning of the string to retrieve
	 * @param length length of the string to retrieve
	 * @return string of length that starts in position pos
	 * @throws IndexOutOfBoundsException unless both pos and pos+length-1 are
	 *                                   legitimate indexes within this tree.
	 */
	
	

	public String get(int pos, int length) throws IndexOutOfBoundsException {
		if(pos < 0 || length < 0 || pos + length > this.size()) {
			throw new IndexOutOfBoundsException();
		}
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < length; i++) {
			builder.append(this.get(i + pos));
		}
		return builder.toString();
	}

	// Feel free to add whatever other methods and helpers you need,
	// like for the graphical debugger.
	
	public void show() {
		if (this.display == null) {
			this.display = new DisplayableBinaryTree(this, 960, 1080, true);
		} else {
			this.display.show(true);
		}
	}
	
}