package sorting.parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import sorting.algorithms.BubbleSort;
import analysis.DatasetGenerator;

public class ParallelBubbleSort {

    public static int[] sort(int[] arr, int numThreads) {
        if (arr.length <= 1000) {
            return BubbleSort.sort(arr);
        }

        try (ForkJoinPool pool = new ForkJoinPool(numThreads)) {
            return pool.invoke(new BubbleSortTask(arr, 0, arr.length));
        }
    }

    private static class BubbleSortTask extends RecursiveTask<int[]> {
        private final int[] arr;
        private final int start;
        private final int end;
        private static final int THRESHOLD = 500;

        public BubbleSortTask(int[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        @Override
        protected int[] compute() {
            int length = end - start;

            if (length <= THRESHOLD) {
                return sequentialBubbleSort();
            }

            int mid = start + length / 2;
            BubbleSortTask leftTask = new BubbleSortTask(arr, start, mid);
            BubbleSortTask rightTask = new BubbleSortTask(arr, mid, end);

            leftTask.fork();
            int[] rightResult = rightTask.compute();
            int[] leftResult = leftTask.join();

            return merge(leftResult, rightResult);
        }

        private int[] sequentialBubbleSort() {
            int[] chunk = new int[end - start];
            System.arraycopy(arr, start, chunk, 0, chunk.length);
            return BubbleSort.sort(chunk);
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