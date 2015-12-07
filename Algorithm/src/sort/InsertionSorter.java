package sort;

import java.util.List;

/**
 * Created by cheng on 2015/12/7.
 */
public class InsertionSorter implements Sorter {

    @Override
    public void sort(List<Integer> array) {
        for(int i=1;i<array.size();i++){
            int key=array.get(i);
            int j=i-1;
            while(j>=0&&key<array.get(j)){
                array.set(j+1, array.get(j));
                j--;
            }
            array.set(j+1, key);
        }
    }

    public InsertionSorter(){}
}
