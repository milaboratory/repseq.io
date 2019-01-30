/*
 * Copyright 2019 MiLaboratory, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.repseq.gen.dist;

/**
 * Arbitrary rank dense real-value (double) tensor
 */
public final class DoubleTensor {
    final int[] dimensions;
    final double[] data;

    public DoubleTensor(int... dimensions) {
        this.dimensions = dimensions;
        int dataLength = 1;
        for (int d : dimensions)
            dataLength *= d;
        this.data = new double[dataLength];
    }

    public int getDimension(int index) {
        return dimensions[index];
    }

    int idx(int... indices) {
        return idx(dimensions, indices);
    }

    int[] invIdx(int idx) {
        return invIdx(dimensions, idx);
    }

    public void set(double value, int... indices) {
        data[idx(indices)] = value;
    }

    public double get(int... indices) {
        return data[idx(indices)];
    }

    public double add(double value, int... indices) {
        return data[idx(indices)] += value;
    }

    public double div(double divider, int... indices) {
        return data[idx(indices)] /= divider;
    }

    public void divAll(double divider) {
        for (int i = 0; i < data.length; i++)
            data[i] /= divider;
    }

    public double sum() {
        double sum = 0.0;
        for (double d : data)
            sum += d;
        return sum;
    }

    static int idx(int[] dimensions, int[] indices) {
        if (indices.length != dimensions.length)
            throw new IllegalArgumentException("Wrong number of indices.");
        int idx = 0;
        for (int i = 0; i < dimensions.length; i++) {
            if (indices[i] < 0 || indices[i] >= dimensions[i])
                throw new IndexOutOfBoundsException();
            idx *= dimensions[i];
            idx += indices[i];
        }
        return idx;
    }

    static int[] invIdx(int[] dimensions, int idx) {
        int[] indices = new int[dimensions.length];
        for (int i = dimensions.length - 1; i >= 0; i--) {
            indices[i] = idx % dimensions[i];
            idx /= dimensions[i];
        }
        return indices;
    }
}
