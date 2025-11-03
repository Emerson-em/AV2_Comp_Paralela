package sorting.parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import sorting.algorithms.InsertionSort;
import analysis.DatasetGenerator;

public class ParallelInsertionSort {

    public static int[] sort(int[] arr, int numThreads) {
        if (arr.length <= 1000) {
            return InsertionSort.sort(arr);
        }

        try (ForkJoinPool pool = new ForkJoinPool(numThreads)) {
            return pool.invoke(new InsertionSortTask(arr, 0, arr.length));
        }
    }

    private static class InsertionSortTask extends RecursiveTask<int[]> {
        private final int[] arr;
        private final int start;
        private final int end;
        private static final int THRESHOLD = 500;

        public InsertionSortTask(int[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        @Override
        protected int[] compute() {
            int length = end - start;

            if (length <= THRESHOLD) {
                return sequentialInsertionSort();
            }

            int mid = start + length / 2;
            InsertionSortTask leftTask = new InsertionSortTask(arr, start, mid);
            InsertionSortTask rightTask = new InsertionSortTask(arr, mid, end);

            leftTask.fork();
            int[] rightResult = rightTask.compute();
            int[] leftResult = leftTask.join();

            return merge(leftResult, rightResult);
        }

        private int[] sequentialInsertionSort() {
            int[] chunk = new int[end - start];
            System.arraycopy(arr, start, chunk, 0, chunk.length);
            return InsertionSort.sort(chunk);
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