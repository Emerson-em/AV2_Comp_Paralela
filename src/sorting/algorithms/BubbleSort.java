package sorting.algorithms;
import analysis.DatasetGenerator;

public class BubbleSort {
    public static int[] sort(int[] arr) {
        int[] arrCopy = DatasetGenerator.copyArray(arr);
        int n = arrCopy.length;

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arrCopy[j] > arrCopy[j + 1]) {
                    // Troca os elementos
                    int temp = arrCopy[j];
                    arrCopy[j] = arrCopy[j + 1];
                    arrCopy[j + 1] = temp;
                }
            }
        }
        return arrCopy;
    }
}