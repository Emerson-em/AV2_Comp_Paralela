package sorting.parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import sorting.algorithms.QuickSort;
import analysis.DatasetGenerator;

public class ParallelQuickSort {

    public static int[] sort(int[] arr, int numThreads) {
        if (arr.length <= 1000 || numThreads <= 1) {
            return QuickSort.sort(arr);
        }

        try (ForkJoinPool pool = new ForkJoinPool(numThreads)) {
            return pool.invoke(new QuickSortTask(arr));
        }
    }

    private static class QuickSortTask extends RecursiveTask<int[]> {
        private final int[] arr;

        public QuickSortTask(int[] arr) {
            this.arr = arr;
        }

        @Override
        protected int[] compute() {
            if (arr.length <= 1000) {
                return QuickSort.sort(arr);
            }

            int pivot = arr[arr.length / 2];
            java.util.List<Integer> left = new java.util.ArrayList<>();
            java.util.List<Integer> middle = new java.util.ArrayList<>();
            java.util.List<Integer> right = new java.util.ArrayList<>();

            for (int value : arr) {
                if (value < pivot) {
                    left.add(value);
                } else if (value == pivot) {
                    middle.add(value);
                } else {
                    right.add(value);
                }
            }

            QuickSortTask leftTask = new QuickSortTask(listToArray(left));
            QuickSortTask rightTask = new QuickSortTask(listToArray(right));

            leftTask.fork();
            int[] rightResult = rightTask.compute();
            int[] leftResult = leftTask.join();

            return concatenate(leftResult, listToArray(middle), rightResult);
        }

        private int[] listToArray(java.util.List<Integer> list) {
            return list.stream().mapToInt(i -> i).toArray();
        }

        private int[] concatenate(int[]... arrays) {
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
}