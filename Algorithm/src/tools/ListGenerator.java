package tools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by cheng on 2015/12/7.
 */
public class ListGenerator {
	public ArrayList<Integer> randArrayList( int start, int end, int size) {
		ArrayList<Integer> array = new ArrayList<Integer>();
		Random rand = new Random();
		for (int i = 0; i < size; i++) {
			array.add(rand.nextInt(end - start+1) + start);
		}
		return array;
	}

	public LinkedList<Integer> randLinkedList(int start, int end, int size ){
		LinkedList<Integer> array = new LinkedList<Integer>();
		Random rand = new Random();
		for (int i = 0; i < size; i++) {
			array.add(rand.nextInt(end - start+1) + start);
		}
		return array;
	}
}
