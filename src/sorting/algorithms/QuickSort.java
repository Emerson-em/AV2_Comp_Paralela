package sorting.algorithms;
import analysis.DatasetGenerator;
import java.util.ArrayList;
import java.util.List;

public class QuickSort {
    public static int[] sort(int[] arr) {
        if (arr.length <= 1) {
            return DatasetGenerator.copyArray(arr);
        }

        int pivot = arr[arr.length / 2];
        List<Integer> left = new ArrayList<>();
        List<Integer> middle = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        for (int value : arr) {
            if (value < pivot) {
                left.add(value);
            } else if (value == pivot) {
                middle.add(value);
            } else {
                right.add(value);
            }
        }

        int[] leftSorted = sort(listToArray(left));
        int[] rightSorted = sort(listToArray(right));

        return concatenate(leftSorted, listToArray(middle), rightSorted);
    }

    private static int[] listToArray(List<Integer> list) {
        return list.stream().mapToInt(i -> i).toArray();
    }

    private static int[] concatenate(int[]... arrays) {
        int totalLength = 0;
        for (int[] array : arrays) {
            totalLength += array.length;
        }

        int[] result = new int[totalLength];
        int currentIndex = 0;

        for (int[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }

        return result;
    }
}