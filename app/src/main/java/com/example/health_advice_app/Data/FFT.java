package com.example.health_advice_app.Data;

public class FFT {
    int n, m;

    double[] cos;
    double[] sin;

    public FFT(int n) {
        this.n = n;
        this.m = (int)(Math.log(n) / Math.log(2));

        cos = new double[n / 2];
        sin = new double[n / 2];

        for (int i = 0; i < n / 2; i++) {
            cos[i] = Math.cos(-2 * Math.PI * i / n);
            sin[i] = Math.sin(-2 * Math.PI * i / n);
        }
    }

    public void fft(double[] real, double[] imag) {
        int j = 0;
        int n2 = n / 2;
        for (int i = 1; i < n - 1; i++) {
            int bit = n2;
            while (j >= bit) {
                j -= bit;
                bit /= 2;
            }
            j += bit;

            if (i < j) {
                double tempReal = real[i];
                double tempImag = imag[i];
                real[i] = real[j];
                imag[i] = imag[j];
                real[j] = tempReal;
                imag[j] = tempImag;
            }
        }

        int step = 1;
        for (int k = 1; k <= m; k++) {
            int increment = step * 2;
            for (int i = 0; i < step; i++) {
                double wr = cos[i * n / increment];
                double wi = sin[i * n / increment];
                for (int j1 = i; j1 < n; j1 += increment) {
                    int j2 = j1 + step;
                    double tr = wr * real[j2] - wi * imag[j2];
                    double ti = wr * imag[j2] + wi * real[j2];
                    real[j2] = real[j1] - tr;
                    imag[j2] = imag[j1] - ti;
                    real[j1] += tr;
                    imag[j1] += ti;
                }
            }
            step = increment;
        }
    }
}
