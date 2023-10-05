/*
 * Alex Kuehnl
 * UWM CS351-401
 * Discussed this homework with Nathan Edwards, Nathan Stout - did not share code
 */

package edu.uwm.cs351;

import java.awt.Point;
import java.util.function.Consumer;

/**
 * A collection of pixels on the screen, typically arranged 
 * in a rectangular form.
 */


public class DynamicRaster {
	
	private Node head;
	private int manyNodes;
	private Node precursor;
	
	
	/*
	 * Class to create Nodes to be used as elements of a linked list data structure
	 * in the DynamicRaster class.
	 */
	private static class Node {
		Pixel data;
		Node next;
		
		/*
		 * @param Pixel p
		 * Creates a new Node object with a Pixel as its data and null as its next value
		 */
		public Node(Pixel p) {
			this.data = p;
			this.next = null;
		}
		
		
		/*
		 * @param Pixel p, Node n
		 * Creates a new Node object with a Pixel as its data and another Node as its next value
		 */
		public Node(Pixel p, Node n) {
			this.data = p;
			this.next = n;
		}
		
	}
	
	
	
	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);
	
	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	
	/*
	 * Checks if p1's coordinates come before p2's coordinates in column major order.
	 * @return true if p1 comes before p2, false if p1 comes after p2 or if the coordinates
	 * are the same.
	 * @exception - NPE if p1 or p2 are null.
	 */
	private boolean comesBefore(Point p1, Point p2) {
		if (p1.x < p2.x) return true;
		if (p1.x == p2.x && p1.y < p2.y) return true;
		
		return false;
		
	}

	/*
	 * Method checking the invariants for the data structure.
	 */
	private boolean wellFormed() {
		// Check the invariant.
		// 1. The list has no cycles
		// 2. The precursor field is null or points to a node in the list.
		// 3. manyNodes accurately contains the number of nodes in the list (starting at head).
		// 4. No pixel is null
		// 5. No pixel has negative coordinates
		// 6. The pixels are in column major order
		
		
		
		// 1. The list has no cycles.
		// It uses Floyd's Tortoise & Hare algorithm
		if (head != null) {
			Node slow = head;
			Node fast = head.next;
			while (fast != null) {
				if (slow == fast) return report("Invariant 1: Found cycle in list");
				slow = slow.next;
				fast = fast.next;
				if (fast != null) fast = fast.next;
			}
		}
		
		// 2. The precursor field is null or points to a node in the list.
		if (precursor != null) {
			boolean inList = false;
			for (Node n = head; n != null; n = n.next) {
				if (n == precursor) {
					inList = true;
					break;
				}
				
			}
			
			if (!(inList)) return report("Invariant 2: the precursor points to a node not in the list.");
		}
		
		
		//3. manyNodes accurately contains the number of nodes in the list (starting at head).
		int count = 0;
			for (Node n = head; n != null; n = n.next) {
				 count++;
			}
			if (count != manyNodes) return report ("Invariant 3: manyNodes is not equal to the counted number of nodes.");
			
		//4. No pixel is null
		for (Node n = head; n != null; n = n.next) {
			if (n.data == null) return report ("Invariant 4: list contains null pixel");
		}
		
		//5. No pixel has negative coordinates
		for (Node n = head; n != null; n = n.next) {
			if (n.data.loc().x < 0 || n.data.loc().y < 0) return report("Invariant 5: list contains pixel with negative coordinates.");
		}
		
		//6. The pixels are in column major order
		for (Node n = head; n != null; n = n.next) {
			if (n.next != null) {
				if (!comesBefore(n.data.loc(), n.next.data.loc())) return report("Invariant 6");
			}
		}
		
		
		// If no problems discovered, return true
		return true;
	}

	// This is only for testing the invariant.  Do not change!
	private DynamicRaster(boolean testInvariant) { }

	/**
	 * Create an empty raster..
	 */
	public DynamicRaster() {
		head = null;
		manyNodes = 0;
		precursor = null;
		
		
		assert wellFormed() : "broken in constructor.";
	}
	
	@Override // implementation
	public String toString() {
		// don't assert invariant, so we can use this for testing/debugging
		StringBuilder sb = new StringBuilder();
		boolean foundPre = precursor == null;
		Node lag = null;
		Node fast = head;
		sb.append("[");
		for (Node p=head; p != null; p = p.next) {
			if (p == precursor) foundPre = true;
			if (fast != null) fast = fast.next;
			if (p != head) sb.append(", ");
			if (lag == precursor) {
				sb.append("*");
				foundPre = true;
			}
			sb.append(p.data);
			if (p == fast) {
				sb.append(" ???");
				break;
			}
			lag = p;
			if (fast != null) fast = fast.next;
		}
		sb.append("]:" + manyNodes + (foundPre ? "" : "*?"));
		return sb.toString();
	}
		
	/** Get a pixel from the raster
	 * @param x x-coordinate, must not be negative
	 * @param y y-coordinate, must not be negative
	 * @return the pixel at x,y, or null if no pixel.
	 */
	public Pixel getPixel(int x, int y) {
		assert wellFormed() : "invariant broken in getPixel()";
		if (x < 0 || y < 0) throw new IllegalArgumentException("x & y must not be negative");
		for (Node n = head; n != null; n = n.next) {
			if (n.data.loc().x == x && n.data.loc().y > y) return null;
			if (n.data.loc().x == x && n.data.loc().y == y) {
				return n.data;
			}
		}
		
		return null;
	}
	
	/**
	 * Set a pixel in the raster.  Return whether a change was made.
	 * This pixel is now current.
	 * @param p pixel to add, must not be null
	 * @return whether a change was made to a pixel.
	 * But the pixel will be current whether or not it was newly added.
	 */
	public boolean add(Pixel p) {
		assert wellFormed() : "invariant broken in add";
		boolean added = false;
		
		//if p is not legal do not advance
		if (p == null) throw new NullPointerException("cannot add null pixels to list");
		if (p.loc().x < 0 || p.loc().y < 0) throw new IllegalArgumentException("Pixel contained"
				+ "negative coordinates.");
		
		//if list is empty, add at the head
		if (head == null) {
			head = new Node(p, head);
			manyNodes++;
			precursor = null;
			assert wellFormed() : "invariant broken in add (197)";
			return true;
		}
		
		Node lag = null;
		for (Node n = head; n != null; lag = n, n = n.next) {

			if (comesBefore(p.loc(), n.data.loc())) {
				if (lag != null) {
					lag.next = new Node(p, lag.next);
					precursor = lag;
					added = true;
					break;
				} else {
					head = new Node(p, head);
					precursor = null;
					added = true;
					break;
				}

			}
			
			if (p.equals(n.data)) {
				if (lag != null) {
					precursor = lag;
				} else {
					precursor = null;
				}
					
				return false;
			}
			
			if (p.loc().equals(n.data.loc())) {
				n.data = p;
				if (lag != null) {
					precursor = lag;
				} else {
					precursor = null;
				}

				assert wellFormed() : "invariant broken in add (219)";
				return true;
			}
		}
		
		if (!added) {
			lag.next = new Node(p, lag.next);
			added = true;
			precursor = lag;
		}
		
		if (added) manyNodes++;
		assert wellFormed() : "invariant broken by add";
		return added;
	}
	
	/**
	 * Remove the pixel, if any, at the given coordinates.
	 * Returns whether there was a pixel to remove.
	 * @param x x-coordinate, must not be negative
	 * @param y y-coordinate, must not be negative
	 * @return whether anything was removed.
	 */
	public boolean clearAt(int x, int y) {
		assert wellFormed() : "invariant broken in clearAt";
		if (x < 0 || y < 0) throw new IllegalArgumentException("x & y must not be negative");
		
		boolean removed = false;
		boolean setPrecursor = false;
		
		if (precursor != null && (x == precursor.data.loc().x && y == precursor.data.loc().y)) {
			setPrecursor = true;
		}
		
		Node lag = null;
		for (Node n = head; n != null; lag = n, n = n.next) {
			if (x < n.data.loc().x || (x == n.data.loc().x && y < n.data.loc().y)) return false;
			if (n.data.loc().x == x && n.data.loc().y == y){
				if (lag != null) {
					if (setPrecursor) precursor = lag;
					lag.next = n.next;
					removed = true;
					--manyNodes;
					break;
				} else {
					if (setPrecursor) precursor = null;
					head = head.next;
					removed = true;
					--manyNodes;
					break;
				}
			}
		}
		assert wellFormed() : "invariant broken by clearAt";
		return removed;
	}
	
	/**
	 * Return the number of pixels in the raster.
	 * @return number of (non-null) pixels
	 */
	public int size() {
		assert wellFormed() : "invariant broken in size";
		return manyNodes;
	}
	
	//Model field methods
	
	/**
	 * @return Node
	 * Returns the node representing the tail of the linked list (node which next == null)
	 * If there is an empty list, returns null.
	 */
	private Node getTail() {
		assert wellFormed() : "invariant broken in getTail";
		for (Node n = head; n != null; n = n.next) {
			if (n.next == null) return n;
		}
		
		return null;
	}
	
	/*
	 * Returns the node where the cursor is located(Always one node after precursor). If 
	 * precursor is null, the cursor is at the head of the linked list; else it
	 * is one node after the precursor.
	 */
	private Node getCursor() {
		assert wellFormed() : "invariant broken in getCursor()";
		return precursor == null ? head : precursor.next;
	}
	
	/// Cursor methods
	
	/**
	 * Move the cursor to the beginning, first pixel in the raster,
	 * if any.
	 */
	public void start() {
		assert wellFormed() : "invariant broken in start()";
		precursor = null;
		if (head != null) getCursor();
		assert wellFormed() : "invariant broken after start()";
	}
	
	/**
	 * Return whether we have a current pixel
	 * @return whether there is a current pixel.
	 */
	public boolean isCurrent() {
		assert wellFormed() : "invariant broken in isCurrent";
		return getCursor() != null;
	}
	
	/**
	 * Return the current pixel.
	 * @exception IllegalStateException if there is no current pixel
	 * @return the current pixel, never null.
	 */
	public Pixel getCurrent() {
		assert wellFormed() : "invariant broken in getCurrent";
		if (!isCurrent()) throw new IllegalStateException("No current pixel");
		return getCursor().data;
	}
	
	/**
	 * Move on to the next pixel, if any.  The pixels are organized 
	 * left to right and top-to-bottom in each column.
	 * If there are no more pixels, then afterwards, {@link #isCurrent()}
	 * will return false.
	 * @throws IllegalStateException if there is no current pixel before this operation starts
	 */
	public void advance() {
		assert wellFormed() : "invariant broken in advance()";
		if (!isCurrent()) throw new IllegalStateException("there is no current pixel");
		
		if (getCursor() != null)  {
			precursor = getCursor();
		}
		
		assert wellFormed() : "invariant broken in advance()";
	}
	
	/**
	 * Remove the current pixel, advancing the cursor to the next pixel.
	 * @throws IllegalStateException if there is no current pixel.
	 */
	public void removeCurrent() {
		assert wellFormed() : "invariant broken in removeCurrent()";
		if (!isCurrent()) throw new IllegalStateException("there is no current pixel");
		
		if (precursor != null) {
			precursor.next = getCursor().next;
			--manyNodes;
		} else {
			head = head.next;
			--manyNodes;
		}
		
		assert wellFormed() : "invariant broken in removeCurrent()";
		}
	
	
	/**
	 * Class for internal testing.  Do not modify.
	 * Do not use in client/application code
	 */
	public static class Spy {
		/**
		 * A public version of the data structure's internal node class.
		 * This class is only used for testing.
		 */
		public static class Node extends DynamicRaster.Node {
			/**
			 * Create a node with null data and null next fields.
			 */
			public Node() {
				this(null, null);
			}
			/**
			 * Create a node with the given values
			 * @param p data for new node, may be null
			 * @param n next for new node, may be null
			 */
			public Node(Pixel p, Node n) {
				super(null);
				this.data = p;
				this.next = n;
			}
		}
		
		/**
		 * Return the sink for invariant error messages
		 * @return current reporter
		 */
		public Consumer<String> getReporter() {
			return reporter;
		}
		
		

		/**
		 * Change the sink for invariant error messages.
		 * @param r where to send invariant error messages.
		 */
		public void setReporter(Consumer<String> r) {
			reporter = r;
		}

		/**
		 * Create a node for testing.
		 * @param p pixel, may be null
		 * @param n next node, may be null
		 * @return newly ceated test node
		 */
		public Node newNode(Pixel p, Node n) {
			return new Node(p, n);
		}
		
		/**
		 * CHange a node's next field
		 * @param n1 node to change, must not be null
		 * @param n2 node to point to, may be null
		 */
		public void setNext(Node n1, Node n2) {
			n1.next = n2;
		}
		
		/**
		 * Create an instance of the ADT with give data structure.
		 * This should only be used for testing.
		 * @param h head of linked list
		 * @param s size
		 * @param x current x
		 * @param y current y
		 * @return instance of DynamicRaster with the given field values.
		 */
		public DynamicRaster create(Node h, int s, Node p) {
			DynamicRaster result = new DynamicRaster(false);
			result.head = h;
			result.manyNodes = s;
			result.precursor = p;
			return result;
		}
		
		/**
		 * Return whether the wellFormed routine returns true for the argument
		 * @param s transaction seq to check.
		 * @return
		 */
		public boolean wellFormed(DynamicRaster s) {
			return s.wellFormed();
		}
	}
}
