package sort;

import java.util.List;

public class MergeSorter implements Sorter{

	@Override
	public void sort(List<Integer> list) {
		mergeSort(list, 0, list.size()-1);
		
	}
	
	private void mergeSort(List<Integer> list, int start, int end){
		if(start<end){
			mergeSort(list, start, (start+end)/2);
			mergeSort(list, (start+end)/2+1, end);
			merge(list, start, (start+end)/2, end);
		}
	}
	private void merge(List<Integer> list, int start, int mid, int end){
		int flag1=start, flag2=mid+1;
		while(flag1<=mid&&flag2<=end){
			int i1=list.get(flag1), i2=list.get(flag2);
			if(i1<i2){
				flag1++;
			}
			else if(i1==i2){
				int temp=list.remove(flag2);
				list.add(flag1+1, temp);
				flag1=flag1+2;
				flag2++;
				mid++;
			}
			else{
				int temp=list.remove(flag2);
				list.add(flag1, temp);
				flag2++;
				flag1++;
				mid++;
			}
		}
	}

}
