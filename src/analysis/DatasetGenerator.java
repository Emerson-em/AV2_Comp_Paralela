package analysis;

import java.util.Random;
import java.util.Arrays;

public class DatasetGenerator {
    private static final Random random = new Random(42);

    public static int[] generateDataset(int size, String datasetType) {
        switch (datasetType) {
            case "random":
                return generateRandom(size);
            case "sorted":
                return generateSorted(size);
            case "reverse_sorted":
                return generateReverseSorted(size);
            case "partially_sorted":
                return generatePartiallySorted(size);
            case "duplicates":
                return generateDuplicates(size);
            default:
                throw new IllegalArgumentException("Tipo de dataset desconhecido: " + datasetType);
        }
    }

    private static int[] generateRandom(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(size * 10);
        }
        return arr;
    }

    private static int[] generateSorted(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = i;
        }
        return arr;
    }

    private static int[] generateReverseSorted(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = size - i;
        }
        return arr;
    }

    private static int[] generatePartiallySorted(int size) {
        int[] arr = generateSorted(size);
        // Inverte algumas seções
        for (int i = 0; i < size; i += size / 10) {
            if (i + 10 < size) {
                reverseSection(arr, i, i + 10);
            }
        }
        return arr;
    }

    private static int[] generateDuplicates(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(size / 10);
        }
        return arr;
    }

    private static void reverseSection(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start];
            arr[start] = arr[end - 1];
            arr[end - 1] = temp;
            start++;
            end--;
        }
    }

    public static boolean isSorted(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) {
                return false;
            }
        }
        return true;
    }

    public static int[] copyArray(int[] arr) {
        return Arrays.copyOf(arr, arr.length);
    }
}