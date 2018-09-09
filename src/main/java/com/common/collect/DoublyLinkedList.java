package com.common.collect;

import static org.assertj.core.util.Preconditions.checkArgument;
import static org.assertj.core.util.Preconditions.checkNotNull;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import lombok.Data;
import lombok.NonNull;

/**
 * <p>A memory efficient version of Doubly Linked List. Instead of having two pointers on each node,
 * only one link is used for storing both pointers. See {@link DoublyLinkedList.Node#link} for more information on how this works</p>
 *
 * <p>This class should not be assumed to be universally superior to a common doubly Linked list implementation.
 * Generally speaking, this class reduces object allocation and memory
 * consumption at the price of moderately increased constant factors of CPU. Only use this class
 * when there is a specific reason to prioritize memory over CPU.</p>
 *
 * <p>Based on the implementation of google's CompactLinkedHashMap, but adapted to Linked list.</p>
 *
 * @author <a href="mailto:prperiscal@gmail.com">Pablo Rey Periscal</a>
 */
public class DoublyLinkedList<E> extends AbstractList<E> {

    /**
     * <p>The maximum size of array to allocate.</p>
     * <p>Some VMs reserve some header words in an array.</p>
     * <p>Attempts to allocate larger arrays may result in OutOfMemoryError: Requested array size exceeds VM limit</p>
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private static final int DEFAULT_CAPACITY = 10;

    /**
     * <p>Shared empty array instance used for default sized empty instances.</p>
     */
    private static final Node[] DEFAULT_CAPACITY_EMPTY_ELEMENT_DATA = {};

    /**
     * <p>Value for no set links.</p>
     */
    private static final int UNSET = -1;

    /**
     * <p>Only pointer to top o bottom of the stack.</p>
     */
    private static final int ENDPOINT = -2;

    /**
     * <p>Node model which contains the value and only one attribute as two way pointer.</p>
     */
    @Data
    public static class Node<E> {

        /**
         * <p>The actual value</p>
         */
        @NonNull
        private E value;

        /**
         * <p>Contains the link pointer corresponding with the node, in the range of [0, size()). The
         * high 32 bits of each long is the "prev" pointer, whereas the low 32 bits is the "succ" pointer
         * (pointing to the next entry in the linked list)</p>
         *
         * <p>A node with "prev" pointer equal to {@code ENDPOINT} is the first node in the linked list,
         * and a node with "next" pointer equal to {@code ENDPOINT} is the last node.</p>
         */
        @NonNull
        private long link;

    }

    /**
     * The array buffer into which the nodes of the DoublyList are stored.
     * The capacity of the DoublyList is the length of this array buffer. Any
     * empty DoublyList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
     * will be expanded to DEFAULT_CAPACITY when the first element is added.
     */
    private transient Node<E>[] nodes;

    /**
     * <p>Pointer to the first node in the linked list, or {@code null} if there are no nodes.</p>
     */
    private transient int firstNode;

    /**
     * <p>Pointer to the last node in the linked list, or {@code null} if there are no nodes.</p>
     */
    private transient int lastNode;

    /**
     * <p>Number of nodes in the array.</p>
     */
    private int size = 0;

    /**
     * <p>Constructs an empty list with an initial capacity of ten.</p>
     */
    public DoublyLinkedList() {
        nodes = DEFAULT_CAPACITY_EMPTY_ELEMENT_DATA;
        firstNode = UNSET;
        lastNode = UNSET;
    }

    private DoublyLinkedList(int expectedSize) {
        nodes = new Node[expectedSize];
        firstNode = UNSET;
        lastNode = UNSET;
    }

    /**
     * <p>Creates a {@code DoublyLinkedList} instance.</p>
     *
     * @param expectedSize the expected number of elements.
     *
     * @return a new, empty {@code DoublyLinkedList}.
     * @throws IllegalArgumentException if {@code expectedSize} is negative
     */
    public static <E> DoublyLinkedList<E> createWithExpectedSize(int expectedSize) {
        checkArgument(expectedSize >= 0, "Initial capacity must be non-negative");
        return new DoublyLinkedList<>(expectedSize);
    }

    private int getPredecessor(Node node) {
        return (int) (node.getLink() >>> 32);
    }

    private int getSuccessor(Node node) {
        return (int) (node.getLink());
    }

    private void setSuccessor(Node node, int succ) {
        long succMask = (~0L) >>> 32;
        node.setLink((node.getLink() & ~succMask) | (succ & succMask));
    }

    private void setPredecessor(Node node, int pred) {
        long predMask = ~0L << 32;
        node.setLink((node.getLink() & ~predMask) | ((long) pred << 32));
    }

    /**
     * <p>Inserts the given succ node after the given pred node.</p>
     * <p>Updates links for the given nodes, setting the "succ" node as the next node for the "pred".</p>
     * <p>Also updates the pre-updated successor of the pred node to point the give "succ" node as predecessor.</p>
     * <p>"pred" param can be null, meaning the "succ" to be the new first element.</p>
     *
     * @param pred This node will be updated to point the "succ" as successor, also the previous successor will be updated to point the given "succ" as "pred".
     * @param succ Node to be inserted as successor for the given "pred" node.
     */
    private void insertSucceeds(Node pred, Node succ) {
        //The node goes at the initial place
        if(pred == null) {//should be this succ the new first element?
            //update links so the new element succ points to the old first element
            setSuccessor(succ, firstNode);
            //and set predecessor to ENDPOINT
            setPredecessor(succ, ENDPOINT);
            //update the old first element prev pointer to link the new element, if any
            if(firstNode != UNSET) {
                setPredecessor(nodes[firstNode], size());
            }
            //the new node will be the first now
            firstNode = size();
            //If no lastNode, this has to be also de last one
            if(lastNode == UNSET) {
                setSuccessor(succ, ENDPOINT);
                lastNode = size();
            }
            //The node goes at the end
        } else if(getSuccessor(pred) == ENDPOINT) {
            //update links so the new element pred points to the end
            setSuccessor(succ, ENDPOINT);
            //and set the pred pointer to old last node
            setPredecessor(succ, lastNode);
            //update the old last element pred pointer to link the new element
            setSuccessor(pred, size());
            //the new node will be the first now
            lastNode = size();

        } else { //The node goes between two existing nodes.
            //Retrieve the actual successor node of the given predecessor node.
            //The new node (succ) has to be inserted between this both
            Node nextNode = nodes[getSuccessor(pred)];
            //Sets the predecessor of succ node to pred
            setPredecessor(succ, getPredecessor(nextNode));
            //sets the successor of the next node to succ
            setPredecessor(nextNode, size());
            //Set the successor of "succ" to the old successor of "pred"
            setSuccessor(succ, getSuccessor(pred));
            //Updates the pred to point the new node
            setSuccessor(pred, size());
        }
    }

    @Override
    public boolean add(E element) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!

        Node<E> newNode = new Node<>(element, UNSET);
        if(lastNode == UNSET) {
            insertSucceeds(null, newNode);
        } else {
            insertSucceeds(nodes[lastNode], newNode);
        }
        nodes[size++] = newNode;
        return true;
    }

    /**
     * <p>Inserts a new element as first element.</p>
     *
     * @param element Element to be inserted.
     */
    public void push(E element) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!

        Node<E> newNode = new Node<>(element, UNSET);
        insertSucceeds(null, newNode);
        nodes[size++] = newNode;
    }

    /**
     * <p>Inserts a new element after the given node.</p>
     *
     * @param node    {@link Node} where the given element will be inserted after
     * @param element Element to be inserted.
     */
    public void insertAfterNode(Node node, E element) {
        checkNotNull(node, "Node must no be null");
        Node predecessor = nodes[getPredecessor(node)];
        Node realNode = nodes[getSuccessor(predecessor)];
        checkArgument(realNode.equals(node), "The node given is not valid");

        ensureCapacityInternal(size + 1);  // Increments modCount!!

        Node<E> newNode = new Node<>(element, UNSET);
        insertSucceeds(realNode, newNode);
        nodes[size++] = newNode;
    }

    /**
     * <p>As an additional improve, the element will be fetched backwards if the index is over half of the side.
     * In this way the complexity will be O(n/2) in the worst case instead the O(n) if we try to fetched from the first element.</p>
     *
     * @param index index of the element to return
     *
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    @Override
    public E get(int index) {
        checkArgument(index >= 0 && index < size(), "Index is out of range");
        return getNodeByIndex(index).getValue();
    }

    /**
     * <p>Gets the node for the given index. As get, this performs an bidirectional search to improve complexity.</p>
     *
     * @param index index of the element to return
     *
     * @return {@link Node}
     */
    public Node<E> getNodeByIndex(int index) {
        checkArgument(index >= 0 && index < size(), "Index is out of range");

        if(index < size() / 2) {
            int count = 0;
            for(Iterator<Node<E>> itr = nodeIterator(); itr.hasNext(); ) {
                Node<E> node = itr.next();
                if(count++ == index) {
                    return node;
                }
            }
        } else {
            int count = size() - index;
            for(Iterator<Node<E>> itr = nodeBackwardsIterator(); itr.hasNext(); ) {
                Node<E> node = itr.next();
                if(count-- == 1) {
                    return node;
                }
            }
        }

        return null;
    }

    /**
     * <p>Gets the first node filtering by the element.</p>
     *
     * @param element to filter node by
     *
     * @return {@link Node} or {@code null} if there is no node with the givcn element
     */
    public Node<E> getNode(E element) {
        checkNotNull(element, "Value must not be null");
        for(Iterator<Node<E>> itr = nodeIterator(); itr.hasNext(); ) {
            Node<E> node = itr.next();
            if(node.getValue().equals(element)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public E remove(int index) {
        checkArgument(index >= 0 && index < size(), "Index is out of range");

        if(size() == 1) {
            clear();
            return null;
        }

        Node<E> nodeToRemove = getNodeByIndex(index);
        return remove(nodeToRemove);
    }

    /**
     * <p>Removes the given node.</p>
     *
     * @param node {@link Node} to be deleted
     */
    public E remove(Node<E> node) {
        int arrayIndex = 0;
        //If this is the first element, just change the successor
        if(getPredecessor(node) == ENDPOINT) {
            arrayIndex = firstNode;
            setPredecessor(nodes[getSuccessor(node)], ENDPOINT);
            firstNode = getSuccessor(node);
        } else if(getSuccessor(node) == ENDPOINT) {
            arrayIndex = lastNode;
            setSuccessor(nodes[getPredecessor(node)], ENDPOINT);
            lastNode = getPredecessor(node);
        } else {
            Node<E> successor = nodes[getSuccessor(node)];
            Node<E> predecessor = nodes[getPredecessor(node)];
            arrayIndex = getPredecessor(successor);
            setPredecessor(successor, getPredecessor(node));
            setSuccessor(predecessor, getSuccessor(node));
        }

        afterRemove(arrayIndex);
        return node.getValue();
    }

    /**
     * <p>Here is where the actual deletion is made. The last node will be copied into the one to delete and
     * indexes will be updated.</p>
     * <p>Be aware that we are dealing with the structure at {@link DoublyLinkedList#nodes} array. The index is the "real" index,
     * not the one global doublyLinkedList which is obtained through the pointers.</p>
     *
     * @param index from which the last node element inside nodes[] will be copied to. Therefore, the index of the element to be erase.
     */
    private void afterRemove(int index) {
        modCount++;

        if(index != size() - 1) {
            Node<E> nodeAtTheEndOfArray = nodes[size() - 1];
            nodes[index] = nodeAtTheEndOfArray;
            if(getSuccessor(nodeAtTheEndOfArray) == ENDPOINT) {
                lastNode = index;
            } else {
                setPredecessor(nodes[getSuccessor(nodeAtTheEndOfArray)], index);
            }
            if(getPredecessor(nodeAtTheEndOfArray) == ENDPOINT) {
                firstNode = index;
            } else {
                setSuccessor(nodes[getPredecessor(nodeAtTheEndOfArray)], index);
            }
        }

        nodes[--size] = null;
    }

    @Override
    public void clear() {
        nodes = DEFAULT_CAPACITY_EMPTY_ELEMENT_DATA;
        firstNode = UNSET;
        lastNode = UNSET;
    }

    @Override
    public E set(int index, E element) {
        checkArgument(index >= 0 && index < size(), "Index is out of range");

        getNodeByIndex(index).setValue(element);
        return element;
    }

    @Override
    public int size() {
        return size;
    }

    private void ensureCapacityInternal(int minCapacity) {
        if(nodes == DEFAULT_CAPACITY_EMPTY_ELEMENT_DATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if(minCapacity - nodes.length > 0) {
            grow(minCapacity);
        }
    }

    /**
     * <p>Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.</p>
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = nodes.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if(newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if(newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }
        // minCapacity is usually close to size, so this is a win:
        nodes = Arrays.copyOf(nodes, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if(minCapacity < 0) // overflow
        {
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    public Iterator<E> backwardIterator() {
        return new BackItr();
    }

    public void forEachBackward(Consumer<? super E> action) {
        checkNotNull(action);
        for(Iterator<E> itr = backwardIterator(); itr.hasNext(); ) {
            action.accept(itr.next());
        }
    }

    public Iterator<Node<E>> nodeIterator() {
        return new NodeItr();
    }

    public void forEachNode(Consumer<? super Node<E>> action) {
        checkNotNull(action);
        for(Iterator<Node<E>> itr = nodeIterator(); itr.hasNext(); ) {
            action.accept(itr.next());
        }
    }

    public Iterator<Node<E>> nodeBackwardsIterator() {
        return new BackNodeItr();
    }

    public void forEachNodeBackwards(Consumer<? super Node<E>> action) {
        checkNotNull(action);
        for(Iterator<Node<E>> itr = nodeBackwardsIterator(); itr.hasNext(); ) {
            action.accept(itr.next());
        }
    }

    private class Itr implements Iterator<E> {
        /**
         * <p>Index of element to be returned by subsequent call to next.</p>
         */
        int nextNodeIndex = firstNode;

        /**
         * <p>The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.</p>
         */
        int expectedModCount = modCount;

        public boolean hasNext() {
            return nextNodeIndex != ENDPOINT && nextNodeIndex != UNSET;
        }

        public E next() {
            checkForComodification();
            try {
                int index = nextNodeIndex;
                nextNodeIndex = getSuccessor(nodes[nextNodeIndex]);
                return (nodes[index]).getValue();
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        final void checkForComodification() {
            if(modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

    }

    private class BackItr extends Itr {

        int nextNodeIndex = lastNode;

        public boolean hasNext() {
            return nextNodeIndex != ENDPOINT && nextNodeIndex != UNSET;
        }

        public E next() {
            checkForComodification();
            try {
                int index = nextNodeIndex;
                nextNodeIndex = getPredecessor(nodes[nextNodeIndex]);
                return (nodes[index]).getValue();
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

    }

    private class NodeItr implements Iterator<Node<E>> {

        int nextNodeIndex = firstNode;

        int expectedModCount = modCount;

        public boolean hasNext() {
            return nextNodeIndex != ENDPOINT && nextNodeIndex != UNSET;
        }

        public Node<E> next() {
            checkForComodification();
            try {
                int index = nextNodeIndex;
                nextNodeIndex = getSuccessor(nodes[nextNodeIndex]);
                return (nodes[index]);
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        final void checkForComodification() {
            if(modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

    }

    private class BackNodeItr extends NodeItr {

        int nextNodeIndex = lastNode;

        public boolean hasNext() {
            return nextNodeIndex != ENDPOINT && nextNodeIndex != UNSET;
        }

        public Node<E> next() {
            checkForComodification();
            try {
                int index = nextNodeIndex;
                nextNodeIndex = getPredecessor(nodes[nextNodeIndex]);
                return nodes[index];
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

    }
}
