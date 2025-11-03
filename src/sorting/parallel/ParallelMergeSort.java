package sorting.parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import sorting.algorithms.MergeSort;
import analysis.DatasetGenerator;

public class ParallelMergeSort {

    public static int[] sort(int[] arr, int numThreads) {
        if (arr.length <= 1000 || numThreads <= 1) {
            return MergeSort.sort(arr);
        }

        try (ForkJoinPool pool = new ForkJoinPool(numThreads)) {
            return pool.invoke(new MergeSortTask(arr));
        }
    }

    private static class MergeSortTask extends RecursiveTask<int[]> {
        private final int[] arr;

        public MergeSortTask(int[] arr) {
            this.arr = arr;
        }

        @Override
        protected int[] compute() {
            if (arr.length <= 1000) {
                return MergeSort.sort(arr);
            }

            int mid = arr.length / 2;
            int[] leftArr = new int[mid];
            int[] rightArr = new int[arr.length - mid];

            System.arraycopy(arr, 0, leftArr, 0, mid);
            System.arraycopy(arr, mid, rightArr, 0, arr.length - mid);

            MergeSortTask leftTask = new MergeSortTask(leftArr);
            MergeSortTask rightTask = new MergeSortTask(rightArr);

            leftTask.fork();
            int[] rightResult = rightTask.compute();
            int[] leftResult = leftTask.join();

            return merge(leftResult, rightResult);
        }

        private int[] merge(int[] left, int[] right) {
            int[] result = new int[left.length + right.length];
            int i = 0, j = 0, k = 0;

            while (i < left.length && j < right.length) {
                if (left[i] <= right[j]) {
                    result[k++] = left[i++];
                } else {
                    result[k++] = right[j++];
                }
            }

            while (i < left.length) {
                result[k++] = left[i++];
            }

            while (j < right.length) {
                result[k++] = right[j++];
            }

            return result;
        }
    }
}