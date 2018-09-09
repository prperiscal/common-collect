package com.common.collect;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import com.common.collect.DoublyLinkedList;
import org.junit.Test;

/**
 * @author <a href="mailto:prperiscal@gmail.com">Pablo Rey Periscal</a>
 */
public class DoublyLinkedListTest {

    @Test
    public void addAndReadListInOrderTest() throws Exception {
        List<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 100).forEach(doubleLinkedList::add);

        assertThat(doubleLinkedList.size()).isEqualTo(100);

        Integer expectedValue = 0;
        for(Integer value : doubleLinkedList) {
            assertThat(value).isEqualTo(expectedValue++);
        }
    }

    @Test
    public void addAndReadListBackwardsTest() throws Exception {
        DoublyLinkedList<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 100).forEach(doubleLinkedList::add);

        assertThat(doubleLinkedList.size()).isEqualTo(100);

        Integer expectedValue = 99;
        for(Iterator<Integer> itr = doubleLinkedList.backwardIterator(); itr.hasNext(); ) {
            assertThat(itr.next()).isEqualTo(expectedValue--);
        }
    }

    @Test
    public void pushTest() {
        DoublyLinkedList<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 100).forEach(doubleLinkedList::add);

        assertThat(doubleLinkedList.size()).isEqualTo(100);

        Integer firstElement = doubleLinkedList.get(0);
        assertThat(firstElement).isEqualTo(0);

        doubleLinkedList.push(3232);
        assertThat(doubleLinkedList.size()).isEqualTo(101);

        Integer firstElementLater = doubleLinkedList.get(0);
        assertThat(firstElementLater).isEqualTo(3232);
    }

    @Test
    public void getTest() throws Exception {
        List<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 1000).forEach(doubleLinkedList::add);

        Integer cero = doubleLinkedList.get(0);
        assertThat(cero).isEqualTo(0);

        Integer one = doubleLinkedList.get(1);
        assertThat(one).isEqualTo(1);

        Integer oneHundred = doubleLinkedList.get(100);
        assertThat(oneHundred).isEqualTo(100);

        Integer eightHundred = doubleLinkedList.get(800);
        assertThat(eightHundred).isEqualTo(800);

        Integer nienniennien = doubleLinkedList.get(999); // :D
        assertThat(nienniennien).isEqualTo(999);
    }

    @Test
    public void findNodeAndInsertAfterTest() throws Exception {
        DoublyLinkedList<Integer> doubleLinkedList = DoublyLinkedList.createWithExpectedSize(100);
        IntStream.range(0, 100).forEach(doubleLinkedList::add);

        assertThat(doubleLinkedList.size()).isEqualTo(100);

        DoublyLinkedList.Node<Integer> node = doubleLinkedList.getNode(20);
        assertThat(node).isNotNull();
        assertThat(node.getValue()).isEqualTo(20);

        doubleLinkedList.insertAfterNode(node, 3232);
        assertThat(doubleLinkedList.size()).isEqualTo(101);

        Integer newNode = doubleLinkedList.get(21);
        assertThat(newNode).isNotNull();
        assertThat(newNode).isEqualTo(3232);

        Integer nextNode = doubleLinkedList.get(22);
        assertThat(nextNode).isNotNull();
        assertThat(nextNode).isEqualTo(21);
    }

    @Test
    public void removeBetweenTest() {
        List<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 50).forEach(doubleLinkedList::add);

        doubleLinkedList.remove(1);
        assertThat(doubleLinkedList.size()).isEqualTo(49);

        Integer newValue = doubleLinkedList.get(1);
        assertThat(newValue).isEqualTo(2);
    }

    @Test
    public void removeFirstTest() {
        List<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 50).forEach(doubleLinkedList::add);

        doubleLinkedList.remove(0);
        assertThat(doubleLinkedList.size()).isEqualTo(49);

        Integer newValue = doubleLinkedList.get(0);
        assertThat(newValue).isEqualTo(1);
    }

    @Test
    public void removeLastTest() {
        List<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 50).forEach(doubleLinkedList::add);

        doubleLinkedList.remove(49);
        assertThat(doubleLinkedList.size()).isEqualTo(49);

        Integer newValue = doubleLinkedList.get(48);
        assertThat(newValue).isEqualTo(48);
    }

    @Test
    public void multipleOperationsTest() {
        DoublyLinkedList<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 10).forEach(doubleLinkedList::push);

        Integer previousValue = 9;
        for(Integer value : doubleLinkedList) {
            assertThat(value).isEqualTo(previousValue--);
        }

        doubleLinkedList.remove(3);
        doubleLinkedList.remove(3);
        doubleLinkedList.remove(3);

        DoublyLinkedList.Node<Integer> nodeThree = doubleLinkedList.getNodeByIndex(3);

        doubleLinkedList.insertAfterNode(nodeThree, 333);
        doubleLinkedList.insertAfterNode(nodeThree, 444);

        doubleLinkedList.add(555);
        doubleLinkedList.push(111);

        DoublyLinkedList.Node<Integer> node = doubleLinkedList.getNode(555);
        doubleLinkedList.remove(node);

        assertThat(doubleLinkedList).containsExactly(111, 9, 8, 7, 3, 444, 333, 2, 1, 0);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void addWhileLoopingTest() {
        DoublyLinkedList<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 10).forEach(doubleLinkedList::add);

        doubleLinkedList.forEach(value -> doubleLinkedList.add(2));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void removeWhileLoopingTest() {
        DoublyLinkedList<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 10).forEach(doubleLinkedList::add);

        doubleLinkedList.forEach(value -> doubleLinkedList.remove(2));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void pushWhileLoopingTest() {
        DoublyLinkedList<Integer> doubleLinkedList = new DoublyLinkedList<>();
        IntStream.range(0, 10).forEach(doubleLinkedList::add);

        doubleLinkedList.forEach(value -> doubleLinkedList.push(2));
    }

}