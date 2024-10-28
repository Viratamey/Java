package array.sortAlgos;

import java.util.Arrays;

public class SortAlgo {
    public static void main(String[] args) {
        int[] arr = {12,45,78,90,34,13,56,34,39,9,8,30,23,38,11,6,8,2,200,111,154};

//        selectionsort(arr);
//        insertionsort(arr);
//        mergesort(arr);
        quicksort(arr);
    }

    private static void quicksort(int[] arr) {
        int n = arr.length;
        quicksort(arr, 0, n-1);
        System.out.println(Arrays.toString(arr));
    }

    private static void quicksort(int[] arr, int low, int high) {
        if(low >= high || high<0 ) {
            return;
        }
        int pivot = arr[high];
        int i=low;
        for(int j=low; j<high; j++) {
            if(arr[j] <= pivot) {
                swap(arr, i, j);
                i++;
            }
        }
        swap(arr, i , high);
        quicksort(arr, low, i-1);
        quicksort(arr, i+1, high);
    }

    private static void mergesort(int[] arr) {
        int n = arr.length;
        mergesort(arr,0,n/2);
        mergesort(arr,(n/2)+1, n-1);

        merge(arr,0,(n/2)+1, n-1);
        System.out.println(Arrays.toString(arr));
    }

    private static void mergesort(int[] arr, int i, int j) {
        if(i+1>j) {
            return;
        } else{
            int mid = (i+j)/2;
            mergesort(arr,i,mid);
            mergesort(arr,mid+1,j);
            merge(arr, i, mid+1, j);
            return;
        }
    }

    private static void merge(int[] arr, int i, int j, int end) {
        int curr_i = i, curr_j =j;
        while(curr_i<j && curr_j<=end) {
            if(arr[curr_i]<arr[curr_j]) {
                curr_i++;
            } else{
                swap(arr, curr_i, curr_j);
                updateSecondArr(arr, curr_j, end);
                curr_i++;
            }
        }
    }

    private static void updateSecondArr(int[] arr, int curr_j, int end) {
        for(int i=curr_j; i<end && arr[i] > arr[i + 1]; ++i) {
            swap(arr, i, i+1);
        }
    }

    private static void selectionsort(int[] arr) {
        int n = arr.length;
        for(int curr=0; curr<n-1; curr++) {
            int min = Integer.MAX_VALUE;
            int min_id = curr;
            for(int it=curr; it<n; ++it) {
                if(min > arr[it]) {
                    min = arr[it];
                    min_id = it;
                }
            }
            if(min_id != curr) {
                swap(arr, min_id, curr);
            }
        }
        System.out.println(Arrays.toString(arr));
    }

    private static void swap(int[] arr, int i, int j) {
        if(i!=j) {
            arr[i] = arr[i] + arr[j];
            arr[j] = arr[i] - arr[j];
            arr[i] = arr[i] - arr[j];
        }
    }

    private static void insertionsort(int[] arr) {
        int n = arr.length;
        for(int curr=1; curr<n; curr++) {
            int it = curr;
            while(it>0 && arr[it-1] > arr[it]) {
                swap(arr, it-1, it);
                it--;
            }
        }
        System.out.println(Arrays.toString(arr));
    }
}
