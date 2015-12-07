package sort;

import tools.ListGenerator;

import java.util.List;

/**
 * Created by cheng on 2015/12/7.
 */
public class Test {
	static ListGenerator listGenerator=new ListGenerator();
    public static void main(String[] args){
        
        List<Integer> array=listGenerator.randLinkedList(-10,10,40);
        //List<Integer> array=listGenerator.randArrayList(-10,10,40);
        System.out.println(array);
        //
        Sorter sorter =new MergeSorter();
        //Sorter sorter =new InsertionSorter();
        sorter.sort(array);
        //
        System.out.println(array);

    }
}
