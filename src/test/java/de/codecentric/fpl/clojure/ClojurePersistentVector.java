package de.codecentric.fpl.clojure;

/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class ClojurePersistentVector implements Iterable<Object> {

	@SuppressWarnings("serial")
	public static class Node implements Serializable {
		transient public final AtomicReference<Thread> edit;
		public final Object[] array;

		public Node(AtomicReference<Thread> edit, Object[] array) {
			this.edit = edit;
			this.array = array;
		}

		Node(AtomicReference<Thread> edit) {
			this.edit = edit;
			this.array = new Object[32];
		}
	}

	final static AtomicReference<Thread> NOEDIT = new AtomicReference<Thread>(null);
	public final static Node EMPTY_NODE = new Node(NOEDIT, new Object[32]);

	final int cnt;
	public final int shift;
	public final Node root;
	public final Object[] tail;

	public final static ClojurePersistentVector EMPTY = new ClojurePersistentVector(0, 5, EMPTY_NODE, new Object[] {});

	static public ClojurePersistentVector adopt(Object[] items) {
		return new ClojurePersistentVector(items.length, 5, EMPTY_NODE, items);
	}

	ClojurePersistentVector(int cnt, int shift, Node root, Object[] tail) {
		this.cnt = cnt;
		this.shift = shift;
		this.root = root;
		this.tail = tail;
	}

	final int tailoff() {
		if (cnt < 32)
			return 0;
		return ((cnt - 1) >>> 5) << 5;
	}

	public Object[] arrayFor(int i) {
		if (i >= 0 && i < cnt) {
			if (i >= tailoff())
				return tail;
			Node node = root;
			for (int level = shift; level > 0; level -= 5)
				node = (Node) node.array[(i >>> level) & 0x01f];
			return node.array;
		}
		throw new IndexOutOfBoundsException();
	}

	public Object nth(int i) {
		Object[] node = arrayFor(i);
		return node[i & 0x01f];
	}

	public Object nth(int i, Object notFound) {
		if (i >= 0 && i < cnt)
			return nth(i);
		return notFound;
	}

	public int count() {
		return cnt;
	}

	public ClojurePersistentVector cons(Object val) {
		// room in tail?
		// if(tail.length < 32)
		if (cnt - tailoff() < 32) {
			Object[] newTail = new Object[tail.length + 1];
			System.arraycopy(tail, 0, newTail, 0, tail.length);
			newTail[tail.length] = val;
			return new ClojurePersistentVector(cnt + 1, shift, root, newTail);
		}
		// full tail, push into tree
		Node newroot;
		Node tailnode = new Node(root.edit, tail);
		int newshift = shift;
		// overflow root?
		if ((cnt >>> 5) > (1 << shift)) {
			newroot = new Node(root.edit);
			newroot.array[0] = root;
			newroot.array[1] = newPath(root.edit, shift, tailnode);
			newshift += 5;
		} else
			newroot = pushTail(shift, root, tailnode);
		return new ClojurePersistentVector(cnt + 1, newshift, newroot, new Object[] { val });
	}

	private Node pushTail(int level, Node parent, Node tailnode) {
		// if parent is leaf, insert node,
		// else does it map to an existing child? -> nodeToInsert = pushNode one more
		// level
		// else alloc new path
		// return nodeToInsert placed in copy of parent
		int subidx = ((cnt - 1) >>> level) & 0x01f;
		Node ret = new Node(parent.edit, parent.array.clone());
		Node nodeToInsert;
		if (level == 5) {
			nodeToInsert = tailnode;
		} else {
			Node child = (Node) parent.array[subidx];
			nodeToInsert = (child != null) ? pushTail(level - 5, child, tailnode)
					: newPath(root.edit, level - 5, tailnode);
		}
		ret.array[subidx] = nodeToInsert;
		return ret;
	}

	private static Node newPath(AtomicReference<Thread> edit, int level, Node node) {
		if (level == 0)
			return node;
		Node ret = new Node(edit);
		ret.array[0] = newPath(edit, level - 5, node);
		return ret;
	}

	@SuppressWarnings("rawtypes")
	Iterator rangedIterator(final int start, final int end) {
		return new Iterator() {
			int i = start;
			int base = i - (i % 32);
			Object[] array = (start < count()) ? arrayFor(i) : null;

			public boolean hasNext() {
				return i < end;
			}

			public Object next() {
				if (i < end) {
					if (i - base == 32) {
						array = arrayFor(i);
						base += 32;
					}
					return array[i++ & 0x01f];
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@SuppressWarnings("rawtypes")
	public Iterator iterator() {
		return rangedIterator(0, count());
	}


	public ClojurePersistentVector pop() {
		if (cnt == 0)
			throw new IllegalStateException("Can't pop empty vector");
		if (cnt == 1)
			return EMPTY;
		// if(tail.length > 1)
		if (cnt - tailoff() > 1) {
			Object[] newTail = new Object[tail.length - 1];
			System.arraycopy(tail, 0, newTail, 0, newTail.length);
			return new ClojurePersistentVector(cnt - 1, shift, root, newTail);
		}
		Object[] newtail = arrayFor(cnt - 2);

		Node newroot = popTail(shift, root);
		int newshift = shift;
		if (newroot == null) {
			newroot = EMPTY_NODE;
		}
		if (shift > 5 && newroot.array[1] == null) {
			newroot = (Node) newroot.array[0];
			newshift -= 5;
		}
		return new ClojurePersistentVector(cnt - 1, newshift, newroot, newtail);
	}

	private Node popTail(int level, Node node) {
		int subidx = ((cnt - 2) >>> level) & 0x01f;
		if (level > 5) {
			Node newchild = popTail(level - 5, (Node) node.array[subidx]);
			if (newchild == null && subidx == 0)
				return null;
			else {
				Node ret = new Node(root.edit, node.array.clone());
				ret.array[subidx] = newchild;
				return ret;
			}
		} else if (subidx == 0)
			return null;
		else {
			Node ret = new Node(root.edit, node.array.clone());
			ret.array[subidx] = null;
			return ret;
		}
	}


}