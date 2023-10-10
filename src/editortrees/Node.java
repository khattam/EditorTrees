package editortrees;

import java.util.ArrayList;
import java.util.Stack;

import editortrees.EditTree.BooleanContainer;

/**
 * A node in a height-balanced binary tree with rank. Except for the NULL_NODE,
 * one node cannot belong to two different trees
 * 
 * @author <<You>>
 */
public class Node {

	enum Code {
		SAME, LEFT, RIGHT;

		// Used in the displayer and debug string
		public String toString() {
			switch (this) {
			case LEFT:
				return "/";
			case SAME:
				return "=";
			case RIGHT: 
				return "\\";
			default:
				throw new IllegalStateException();
			}
		}

		boolean isOpposite(Code traversalDirection) {
			return (this.equals(Code.RIGHT) && traversalDirection.equals(Code.LEFT)) || (this.equals(Code.LEFT) && traversalDirection.equals(Code.RIGHT));
		}

		Code getOppositeCode() {
			if(this.equals(Code.LEFT)) {
				return Code.RIGHT;
			}
			return Code.LEFT;
		}
	}


	// The fields would normally be private, but for the purposes of this class,
	// we want to be able to test the results of the algorithms in addition to the
	// "publicly visible" effects

	char data;
	Node left, right; // subtrees
	int rank; // inorder position of this node within its own subtree.
	Code balance;
	DisplayableNodeWrapper displayableNodeWrapper;
	

	// Feel free to add other fields that you find useful.
	// You probably want a NULL_NODE, but you can comment it out if you decide
	// otherwise.
	// The NULL_NODE uses the "null character", \0, as it's data and null children,
	// but they could be anything since you shouldn't ever actually refer to them in
	// your code.
	static final Node NULL_NODE = new Node('\0', null, null);
	// Node parent; You may want parent, but think twice: keeping it up-to-date
	// takes effort too, maybe more than it's worth.

	public Node() {
		this.data = '\0';
		this.left = null;
		this.right = null;
		this.rank = -1;
		this.balance = null;
		displayableNodeWrapper = new DisplayableNodeWrapper(this);
	}
	
	public Node(char data, Node left, Node right) {
		this.data = data;
		this.left = left;
		this.right = right;
		displayableNodeWrapper = new DisplayableNodeWrapper(this);
	}

	public Node(char data) {
		// Make a leaf
		this(data, NULL_NODE, NULL_NODE);
		this.balance = Code.SAME;
		displayableNodeWrapper = new DisplayableNodeWrapper(this);
	}

	public Node(char data2, int rank2) {
		this.data = data2;
		this.left = NULL_NODE;
		this.right =NULL_NODE;
		this.rank = rank2;
		this.balance = Code.SAME;
		displayableNodeWrapper = new DisplayableNodeWrapper(this);
	}
	
	public Node(char data, int rank, Code balanceCode) {
		this.data = data;
		this.rank = rank;
		this.balance = balanceCode;
		this.left = NULL_NODE;
		this.right =NULL_NODE;
		displayableNodeWrapper = new DisplayableNodeWrapper(this);
	}

	public int height() {
		if(this == NULL_NODE) {
			return -1;
		}
		if(this.balance == Code.LEFT) {
			return 1 + this.left.height();
		}
		else {
			return 1 + this.right.height();
		}
	}
	
	int size() {
		if(this == NULL_NODE) {
			return 0;
		}
		Node currentLeft = this;
		Node currentRight = this;
		int rightAdd = 0;
		while (currentLeft.left != NULL_NODE) {
			currentLeft = currentLeft.left;
		}
		while (currentRight.right != NULL_NODE) {
			rightAdd += currentRight.rank + 1;
			currentRight = currentRight.right;
		}
		return 1 + (currentRight.rank + rightAdd) - (currentLeft.rank);
	}
	
	// Provided to you to enable testing, please don't change.
	int slowHeight() {
		if (this == NULL_NODE) {
			return -1;
		}
		return Math.max(left.slowHeight(), right.slowHeight()) + 1;
	}

	// Provided to you to enable testing, please don't change.
	public int slowSize() {
		if (this == NULL_NODE) {
			return 0;
		}
		return left.slowSize() + right.slowSize() + 1;
	}
	
	// You will probably want to add more constructors and many other
	// recursive methods here. I added 47 of them - most were tiny helper methods
	// to make the rest of the code easy to understand. My longest method was
	// delete(): 20 lines of code other than } lines. Other than delete() and one of
	// its helpers, the others were less than 10 lines long. Well-named helper
	// methods are more effective than comments in writing clean code
	public Node addSimple(char ch, NodeContainer nc) {
		if(this == NULL_NODE) {
			Node newNode = new Node(ch);
			newNode.balance = Code.SAME;
			return newNode;
		}
		this.right = this.right.addSimple(ch, nc);
//		return this; // used to make sure M1 still works
		if(nc.isBalanced) {
			return this;
		}
		return this.balanceAfterInsert(Code.RIGHT, nc);
	}

	public Node add(char ch, int pos, NodeContainer nc) {
		if(this == NULL_NODE) {
			Node newNode = new Node(ch);
			newNode.balance = Code.SAME;
			return newNode;
		}
		if(pos <= this.rank) {
			this.rank++;
			this.left = this.left.add(ch, pos, nc);
			if(nc.isBalanced) {
				return this;
			}
			return this.balanceAfterInsert(Code.LEFT, nc);
		} else {
			this.right = this.right.add(ch, pos - (1 + this.rank), nc);
			if(nc.isBalanced) {
				return this;
			}
			return this.balanceAfterInsert(Code.RIGHT, nc);
		}
		// commit
	}
	
	public static class NodeContainer {
		boolean isBalanced;
		char dataToReturn;
		boolean isLeafNode;
		boolean hasOneChild;
		boolean hasTwoChildren;
		Code traversalDirection;
		boolean isDeleted;
		
		public NodeContainer() {
			this.isBalanced = false;
			this.dataToReturn = '\0';
			this.isLeafNode = false;
			this.hasOneChild = false;
			this.hasTwoChildren = false;
			this.traversalDirection = null;
			this.isDeleted = false;
		}

		public void updateParameters(int numChildren, char data) {
			this.dataToReturn = data;
			if(numChildren == 0) {
				this.isLeafNode = true;
			} else if(numChildren == 1) {
				this.hasOneChild = true;
			} else {
				this.hasTwoChildren = true;
			}
		}
		
	}
	
	public Node getChild() {
		if(this.left == NULL_NODE) {
			return this.right;
		} else if(this.right == NULL_NODE) {
			return this.left;
		}
		return NULL_NODE;
	}
	
	private Node getInorderSuccessor() {
		return this.right.getLeftmostChild();
	}

	
	private Node getLeftmostChild() {
		if(this.left == NULL_NODE) {
			return this;
		}
		return this.left.getLeftmostChild();
	}
	
	private Node getSecondLeftmostChild() {
		if(this.left.left == NULL_NODE || this.left == NULL_NODE) {
			return this;
		}
		this.left.rank--;
		return this.left.getSecondLeftmostChild();
	}

	private Node balanceAfterInsert(Code traversalDirection, NodeContainer nc) {
		if(this.balance.equals(Code.SAME)) {
			this.balance = traversalDirection;
		} else if(this.balance.equals(traversalDirection)) {
			nc.isBalanced = true;
			return this.rotation();
		} else { // need to stop recursing upward here!!!
			this.balance = Code.SAME;
			nc.isBalanced = true;
		}
		return this;
	}
	
	
	private Node rotation()  {
		if(this.balance.equals(Code.LEFT)) {
			if(this.balance.equals(this.left.balance)) {
				EditTree.totalRotations++;
				return this.singleRightRotation(this, this.left);
			} else {
				EditTree.totalRotations += 2;
				return this.doubleRightRotation(this, this.left);
			}
		} else {
			if(this.balance.equals(this.right.balance)) {
				EditTree.totalRotations++;
				return this.singleLeftRotation(this, this.right);
			} else {
				EditTree.totalRotations += 2;
				return this.doubleLeftRotation(this, this.right);
			}
		}
		
	}
	
	private Node singleLeftRotation(Node parent, Node child) {
		
		int parentRank = parent.rank;
		int childRank = child.rank;
		parent.right = child.left;
		child.left = parent;
		parent.balance = Code.SAME;
		child.balance = Code.SAME;
		child.rank = childRank + parentRank + 1;
		return child;
		
	}
	
	private Node singleRightRotation(Node parent, Node child) {
		int parentRank = parent.rank;
		int childRank = child.rank;
		parent.left = child.right;
		child.right = parent;
		parent.balance = Code.SAME;
		child.balance = Code.SAME;
		parent.rank = parentRank - childRank - 1;
		child.rank = childRank;
		
		return child;
		
	}

	// right-left rotation
	private Node doubleLeftRotation(Node parent, Node child) {
		Node parentTemp = parent;
		Node childTemp = child;
		Node grandchild = child.left;
		Code grandchildCode = grandchild.balance;
		child = singleRightRotation(child, child.left);
		Node newRoot = singleLeftRotation(parent, child);
		if(grandchildCode.equals(Code.LEFT)) {
			parentTemp.balance = Code.SAME;
			childTemp.balance = Code.RIGHT;
		} else if(grandchildCode.equals(Code.RIGHT)) {
			parentTemp.balance = Code.LEFT;
			childTemp.balance = Code.SAME;
		} else {
			parentTemp.balance = Code.SAME;
			childTemp.balance = Code.SAME;
			grandchild.balance = Code.SAME;
		}
		
		return newRoot;
	}

	// left-right rotation.
	private Node doubleRightRotation(Node parent, Node child) {
		Node parentTemp = parent;
		Node childTemp = child;
		Node grandchild = child.right;
		Code grandchildCode = grandchild.balance;
		child = singleLeftRotation(child, child.right);
		Node newRoot = singleRightRotation(parent, child);
		if(grandchildCode.equals(Code.RIGHT)) {
			parentTemp.balance = Code.SAME;
			childTemp.balance = Code.LEFT;
		} else if(grandchildCode.equals(Code.LEFT)) {
			parentTemp.balance = Code.RIGHT;
			childTemp.balance = Code.SAME;
		} else {
			parentTemp.balance = Code.SAME;
			childTemp.balance = Code.SAME;
			grandchild.balance = Code.SAME;
		}
		
		return newRoot;
	}

	public void rankArrayListHelper(ArrayList<String> rankArrayList) {
		if(this == NULL_NODE) {
			return;
		}
		String currentDataAndRank = String.valueOf(this.data) + this.rank;
		rankArrayList.add(currentDataAndRank);
		this.left.rankArrayListHelper(rankArrayList);
		this.right.rankArrayListHelper(rankArrayList);
	}
	
	public void rankArrayListDebugHelper(ArrayList<String> debugArrayList) {
		if(this == NULL_NODE) {
			return;
		}
		String dataRankCode = String.valueOf(this.data) + this.rank + this.balance;
		debugArrayList.add(dataRankCode);
		this.left.rankArrayListDebugHelper(debugArrayList);
		this.right.rankArrayListDebugHelper(debugArrayList);
	}
	
	public char getHelper(int pos) {
		if(this.rank == pos) {
			return this.data;
		} else if(this.rank < pos) {
			return this.right.getHelper(pos - (1 + this.rank));
		}
		return this.left.getHelper(pos);
	}

	public int checkRanks(BooleanContainer bc) {
		if(this == NULL_NODE) {
			return 0;
		}
		int leftRank = this.left.checkRanks(bc);
		if(leftRank != this.rank) {
			bc.check = false;
		}
		return 1 + this.left.checkRanks(bc) + this.right.checkRanks(bc);
	}
	
	public void checkBalance(BooleanContainer bc) {
		if(this == NULL_NODE) {
			return;
		}
		Code calculatedCode = this.calculateCode();
		if(!(calculatedCode.equals(this.balance))) {
			bc.check = false;
		}
		this.left.checkBalance(bc);
		this.right.checkBalance(bc);
	}
	
	private Code calculateCode() {
		int leftSubtreeHeight = this.left.fastHeightHelper();
		int rightSubtreeHeight = this.right.fastHeightHelper();
		if(leftSubtreeHeight > rightSubtreeHeight) {
			return Code.LEFT;
		} else if(leftSubtreeHeight < rightSubtreeHeight) {
			return Code.RIGHT;
		}
		return Code.SAME;
	}
	
	public int fastHeightHelper() {
		if(this == NULL_NODE) {
			return -1;
		}
		int leftHeight = this.left.fastHeightHelper();
		int rightHeight = this.right.fastHeightHelper();
		return Math.max(leftHeight, rightHeight) + 1;
	}

	public String toString() {
		return Character.toString(this.data);
	}
	
	public void inOrder(int addRank) {
		if (this == NULL_NODE) {
			return;
		}
		if (this.left != NULL_NODE) {
			this.left.inOrder(addRank);
		}
		System.out.print(this.rank + addRank + "'" + this + "' ");
		if (this.right != NULL_NODE) {
			this.right.inOrder(addRank + this.rank + 1);
		}
	}
	
	public String inOrder(int addRank, String result) {
		if (this == NULL_NODE) {
			return "";
		}
		if (this.left != NULL_NODE) {
			result += this.left.inOrder(addRank, result);
		}
		result += this.data;
		if (this.right != NULL_NODE) {
			result += this.right.inOrder(addRank + this.rank + 1, result);
		}
		return result;
	}

	public Node deleteHelper(int pos, NodeContainer nc) {
		if(pos < 0) {
			
		}
		if(this.rank == pos) { // reached node to delete
			nc.updateParameters(this.getNumberOfChildren(), this.data);
			// nc stores properties of node that will be deleted
			return this.updateBalanceAfterDeletion(nc);
		} else if(this.rank < pos) { // traverse right
			this.right = this.right.deleteHelper(pos - (1 + this.rank), nc);
			nc.traversalDirection = Code.RIGHT;
		} else {// traverse left
			this.rank--;
			this.left = this.left.deleteHelper(pos, nc);
			nc.traversalDirection = Code.LEFT;
		}
		return this.updateBalanceAfterDeletion(nc);
		// from how the code works now, it will only balance codes in the subtree where deletion occurs
		// however, we need to check the other subtree as well, since deletion has more cases where rotation is necessary than insertion
//		Node beforeCheckingOtherSubtree = this.updateBalanceAfterDeletion(nc);
//		return beforeCheckingOtherSubtree;
	}
	
	public Node checkOtherSubtreeHelper(NodeContainer nc) {
		Code subtreeToCheck = nc.traversalDirection.getOppositeCode();
		if(subtreeToCheck.equals(Code.LEFT)) {
			return this.updateBalanceAfterDeletion(nc);
		}
		return null;
	}

	// traversal direction indicates the direction the parent node traversed to reach where the
	// deleted node was
	// Ex. in test300, traversalDirection is right since o traversed right to reach u, the node
	// to be deleted
	private Node updateBalanceAfterDeletion(NodeContainer nc) {
		// Consider all cases where rotations are required after a node is deleted
		// The cases where rotations are required is NOT the same as insertion (there are more cases to consider)
		// this = node to be deleted
		if(!nc.isDeleted) { // this is called only when the deletion occurs
			nc.isDeleted = true;
			Node replacement = this.updateNodeAfterDeletion(); 
			if(replacement == NULL_NODE) {
				return NULL_NODE;
			}
			return replacement.balanceAfterDeletion(nc);
		} 
		return this.balanceAfterDeletion(nc);
	}
	
	private Node balanceAfterDeletion(NodeContainer nc) {
		// TODO: one of the most important methods
		int numChildren = this.getNumberOfChildren();
		if(numChildren == 0) {
			this.rank = 0;
			this.balance = Code.SAME;
			return this;
		} else if(numChildren == 1) {
			return this.adjustOneChildBalanceCode();
		} else {
			return this.adjustTwoChildrenBalanceCode();
		}
	}
	
	// covers conditions where rotations may or may not be required for replacement nodes with 1 child
	// TODO: may need to call balanceAfterDeletion in the helper functions multiple times...
	private Node adjustOneChildBalanceCode() {
		if(this.left != NULL_NODE) {
			if(this.left.balance.equals(Code.LEFT)) {
				return this.singleLeftChildLeftBalance();
			} else if(this.left.balance.equals(Code.SAME)) {
				this.singleLeftChildSameBalance();
			} else {
				return this.singleLeftChildRightBalance();
			}
		} else {
			if(this.right.balance.equals(Code.LEFT)) {
				return this.singleRightChildLeftBalance();
			} else if(this.right.balance.equals(Code.SAME)) {
				this.singleRightChildSameBalance();
			} else {
				return this.singleRightChildRightBalance();
			}
		}
		return this;
	}

	// next six methods covers cases where, after deletion, the replacement node has one child
	private Node singleLeftChildLeftBalance() {
		return this.rotation();
	}
	
	private void singleLeftChildSameBalance() {
		this.balance = Code.LEFT;
	}
	
	private Node singleLeftChildRightBalance() {
		return this.rotation();
	}
	
	private Node singleRightChildLeftBalance() {
		return this.rotation();
	}
	
	private void singleRightChildSameBalance() {
		this.balance = Code.RIGHT;
	}

	private Node singleRightChildRightBalance() {
		return this.rotation();
	}

	// covers conditions where rotations may or may not be required for replacement nodes with 2 children
	// TODO: bookmark for handling cases with 2 children, which seem more common
	private Node adjustTwoChildrenBalanceCode() {
		if(this.left.balance.equals(Code.RIGHT)) {
			return this.handleLeftChildRightBalance();
		} else if(this.left.balance.equals(Code.SAME)) {
			return this.handleLeftChildSameBalance();
		} else {
			return this.handleLeftChildLeftBalance();
		}
	}

	private Node handleLeftChildRightBalance() {
		if(this.right.balance.equals(Code.LEFT)) {
			return this.leftChildRightBalanceRightChildLeftBalance();
		} else if(this.right.balance.equals(Code.SAME)) {
			return this.leftChildRightBalanceRightChildSameBalance();
		} else {
			return this.leftChildRightBalanceRightChildRightBalance();
		}
	}

				private Node leftChildRightBalanceRightChildLeftBalance() {
					if(this.left.rank >= this.right.rank + 3) {
						this.balance = Code.LEFT;
						return this.rotation();
					}
					if(this.left.rank + 1 < this.right.rank) {
						this.balance = Code.RIGHT;
						return this;
					} else if(this.left.rank < this.right.rank) {
						this.balance = Code.SAME;
						return this;
					} else {
						this.balance = Code.LEFT;
						return this;
					}
				}
				
				private Node leftChildRightBalanceRightChildSameBalance() {
					if(this.left.rank >= this.right.rank + 2) {
						return this.rotation();
					}
					if(this.left.rank == this.right.rank) {
						this.balance = Code.LEFT;
						return this;
					} else if(this.left.rank + 1 == this.right.rank) {
						this.balance = Code.SAME;
						return this;
					} else {
						this.balance = Code.RIGHT;
						return this;
					}
				}
			
				private Node leftChildRightBalanceRightChildRightBalance() {
					
					if(this.left.rank == this.right.rank) {
						this.balance = Code.SAME;
						return this;
					} else if(this.left.rank < this.right.rank) {
						this.balance = Code.RIGHT;
						return this;
					} else {
						this.balance = Code.LEFT;
						return this;
					}
				}

	private Node handleLeftChildSameBalance() {
		if(this.right.balance.equals(Code.LEFT)) {
			return this.leftChildSameBalanceRightChildLeftBalance();
		} else if(this.right.balance.equals(Code.SAME)) {
			return this.leftChildSameBalanceRightChildSameBalance();
		} else {
			return this.leftChildSameBalanceRightChildRightBalance();
		}
	}

				private Node leftChildSameBalanceRightChildLeftBalance() {
					if(this.left.rank + 3 <= this.right.rank) {
						return this.rotation();
					}
					if(this.left.rank < this.right.rank) {
						this.balance = Code.RIGHT;
						return this;
					} else if(this.left.rank == this.right.rank) {
						this.balance = Code.SAME;
						return this;
					} else {
						this.balance = Code.LEFT;
						return this;
					}
				}
				
				private Node leftChildSameBalanceRightChildSameBalance() {
					if(this.left.rank < this.right.rank && this.left.getNumberOfChildren() < this.right.getNumberOfChildren()) {
						this.balance = Code.RIGHT;
						return this;
					} else if(this.left.rank == this.right.rank + 1 || this.left.rank == this.right.rank || this.left.rank + 1== this.right.rank) {
						this.balance = Code.SAME;
						return this;
					} else {
						this.balance = Code.LEFT;
						return this;
					}
				}
				
				private Node leftChildSameBalanceRightChildRightBalance() {
					if(this.left.rank + 2 <= this.right.rank) {
						return this.rotation();
					}
					if(this.left.rank <= this.right.rank) {
						this.balance = Code.RIGHT;
						return this;
					} else if((this.left.rank > 0) && (this.left.rank >= this.right.rank + 1)) {
						this.balance = Code.SAME;
						return this;
					} else {
						this.balance = Code.LEFT;
						return this;
					}
				}

	private Node handleLeftChildLeftBalance() {
		if(this.right.balance.equals(Code.LEFT)) {
			return this.leftChildLeftBalanceRightChildLeftBalance();
		} else if(this.right.balance.equals(Code.SAME)) {
			return this.leftChildLeftBalanceRightChildSameBalance();
		} else {
			return this.leftChildLeftBalanceRightChildRightBalance();
		}
	}

				private Node leftChildLeftBalanceRightChildLeftBalance() {
					if(this.left.rank < this.right.rank) {
						this.balance = Code.RIGHT;
						return this;
					} else if(this.left.rank == this.right.rank) {
						this.balance = Code.SAME;
						return this;
					} else {
						this.balance = Code.LEFT;
						return this;
					}
				}
				
				private Node leftChildLeftBalanceRightChildSameBalance() {
					if(this.left.rank >= this.right.rank + 3) {
						this.balance = Code.LEFT;
						return this.rotation();
					} else if(this.left.rank < this.right.rank) {
						this.balance = Code.LEFT;
						return this;
					} else {
						this.balance = Code.SAME;
						return this;
					}
				}
				
				private Node leftChildLeftBalanceRightChildRightBalance() {
					if(this.left.rank + 2 > this.right.rank) {
						this.balance = Code.LEFT;
						return this;
					} else if(this.left.rank > this.right.rank) {
						this.balance = Code.SAME;
						return this;
					} else {
						this.balance = Code.RIGHT;
						return this;
					}
				}

	// this method serves to replace the deleted node with its proper successor
	// it will update the balance codes and ranks of all nodes traversed to reach the node to delete
	// except for the node that will replace it
	private Node updateNodeAfterDeletion() {
		// For test306
		// this = X
		Node temp = this.getReplacement(); // a
		if(temp == NULL_NODE) {
			return NULL_NODE;
		}
		Node replacement = new Node(temp.data); // need to deep copy the inorder successor, otherwise a stack overflow occurs
		// can either be NULL_NODE, the current node's child, or the current node's inorder successor
		
		// Attaches the node-to-be-deleted's left subtree to the replacement node
		// Ranks and balance codes will be updated later
		Node oldNodeLeftSubtree = this.left;
		Node oldNodeRightSubtree = this.right; 
		if(replacement.data != oldNodeLeftSubtree.data) {
			replacement.left = oldNodeLeftSubtree;
		}
		// why does this affect oldNodeRightSubtree
		replacement.rank = this.rank;
		replacement.balance = this.balance;
		
		// Attaches the node-to-be-deleted's right subtree to the replacement node
		// This process will also delete the inorder successor (replacement) from
		// its original spot
		if(oldNodeRightSubtree != NULL_NODE) {
			Node replacementRightSubtree = oldNodeRightSubtree.deleteInorderSuccessor();
			replacement.right = replacementRightSubtree;
		} 
		
		return replacement;
	}
	
	private Node deleteInorderSuccessor() {
		NodeContainer ncRightSubtree = new NodeContainer();
		return this.deleteHelper(0, ncRightSubtree);
	}


	// this method serves to get the correct replacement node based on this node's properties
	// if a node has 0 children, it will be replaced by a NULL_NODE
	// if a node has 1 child, it will be replaced by its child
	// if a node has 2 children, it will be replaced by its inorder successor
	private Node getReplacement() {
		int numChildren = this.getNumberOfChildren();
		if(numChildren == 0) {
			return NULL_NODE;
		} else if(numChildren == 1) {
			return this.getChild();
		}
		return this.getInorderSuccessor();
		// the inorder successor can only have at most 1 child; if it had a left child, that would be the inorder successor
	}

	private int getNumberOfChildren() {
		if(this.left == NULL_NODE && this.right == NULL_NODE) { // leaf node
			return 0;
		} else if(this.left == NULL_NODE || this.right == NULL_NODE) { // either left or right null, but not both
			return 1;
		}
		return 2; // has 2 children
	}

	public Node nodeToDelete(int pos) {
		if(this.rank == pos) {
			return this;
		} else if(this.rank < pos) {
			return this.right.nodeToDelete(pos - (1 + this.rank));
		}
		return this.left.nodeToDelete(pos);
	}

	public String toStringHelper() {
		if(this == NULL_NODE) {
			return "";
		}
		String leftData = this.left.toStringHelper();
		String currentData = String.valueOf(this.data);
		String rightData = this.right.toStringHelper(); // fails here
		return leftData + currentData + rightData;
	}
	
	public boolean hasLeft() {
		return this.left != NULL_NODE;
	}

	public boolean hasRight() {
		return this.right != NULL_NODE;
	}

	public boolean hasParent() {
		return false;
	}

	public Node getParent() {
		return NULL_NODE;
	}
}