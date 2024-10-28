package tree.priorityQueue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;


public class PriorityQueueImpl {

    public static final int SIZE = 50;

    int arr[] = new int[SIZE];
    int n = 0;

    public static void main(String[] args) {
        PriorityQueueImpl pq = new PriorityQueueImpl();
        pq.insert(17);
        pq.insert(26);
        pq.insert(96);
        pq.insert(Arrays.asList(47,34,32,29,45));
        System.out.println(pq.get());
        System.out.println(pq.get());
        System.out.println(pq.get());
        System.out.println(pq.get());
        System.out.println(pq.get());
        System.out.println(pq.get());
        System.out.println(pq.get());
        System.out.println(pq.get());
        System.out.println(pq.get());
        System.out.println(pq.get());
        System.out.println(pq.get());
        System.out.println(pq.get());

    }

    private int get() {
        if(n==0) {
            return -1;
        }
        n--;
        int val = arr[0];
        arr[0] = arr[n];
        shiftDown();
        return val;
    }

    private void shiftDown() {
        int i=0;
        while(2*i+1<n) {
            int left = arr[2*i];
            int right = arr[2*i+1];
            int larger = left > right ? 2*i : 2*i+1;
            if(arr[larger] > arr[i]) {
                swap(larger, i , arr);
                i = larger;
            }
            else{
                return;
            }

        }
        if(2*i<n && arr[2*i] < arr[i]) {
            swap(2*i, i ,arr);
        }
    }

    private void insert(int i) {
        arr[n] = i;
        shiftUp(n);
        n++;
        return;
    }



    private void shiftUp(int k) {
        while(k>0) {
            int parent = k/2;
            if(arr[k] > arr[parent]) {
                swap(k, parent, arr);
                k = parent;
            }
            else{
                return;
            }
        }
    }

    private void swap(int i, int i1, int[] array) {
        array[i] = array[i] + array[i1];
        array[i1] = array[i] - array[i1];
        array[i] = array[i] - array[i1];
    }

    private void insert(List<Integer> list) {
        for(int i: list) {
            insert(i);
        }
    }
}
