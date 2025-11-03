package sorting.algorithms;
import analysis.DatasetGenerator;

public class InsertionSort {
    public static int[] sort(int[] arr) {
        int[] arrCopy = DatasetGenerator.copyArray(arr);

        for (int i = 1; i < arrCopy.length; i++) {
            int key = arrCopy[i];
            int j = i - 1;

            while (j >= 0 && arrCopy[j] > key) {
                arrCopy[j + 1] = arrCopy[j];
                j--;
            }
            arrCopy[j + 1] = key;
        }
        return arrCopy;
    }
}