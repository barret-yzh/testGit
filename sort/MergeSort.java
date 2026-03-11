package sort;

import java.util.Arrays;

public class MergeSort {

    public static void mergeSort(int[] arr) {
        split(arr, 0, arr.length - 1);
    }

    private static void split(int[] arr, int s, int e) {
        if (s >= e) {
            return;
        }
        int mid = s + (e - s) / 2;
        split(arr, s, mid);
        split(arr, mid + 1, e);
        merge(arr, s, mid, e);
    }

    // 合并：将[start, mid]和[mid+1, end]两个有序子数组合并为一个有序数组
    private static void merge(int[] arr, int s, int mid, int e) {
        // 创建临时数组，存放合并后的结果
        int[] temp = new int[e - s + 1];
        int i = s; // 左子数组的起始指针
        int j = mid + 1; // 右子数组的起始指针
        int k = 0; // 临时数组的起始指针
        while (i <= mid && j <= e) {
            if (arr[i] <= arr[j]) {
                temp[k] = arr[i];
                i++;
            } else {
                temp[k] = arr[j];
                j++;
            }
            k++;
        }
        while (i <= mid) {
            temp[k] = arr[i];
            i++;
            k++;
        }
        while (j <= e) {
            temp[k] = arr[j];
            j++;
            k++;
        }
        for (int m = 0; m < temp.length; m++) {
            arr[s + m] = temp[m];
        }
    }

    // 测试
    public static void main(String[] args) {
        int[] arr = {38, 27, 43, 3, 9, 82, 10};
        System.out.println("排序前：" + Arrays.toString(arr));
        mergeSort(arr);
        System.out.println("排序后：" + Arrays.toString(arr));
    }
}