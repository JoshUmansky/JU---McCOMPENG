import numpy as np

if __name__ == "__main__":

    length = 32
    peak = 7
    print('\nRandom floating point array of length {0:d} with range from {1:2.2f} to {2:2.2f}\n' .format(length, -peak, peak))
    x_time_original = (np.random.rand(length)-0.5)*2*peak
    np.set_printoptions(precision=12, floatmode='fixed')
    print(x_time_original)

    print('\nFourier domain coefficients\n')
    X_freq = np.fft.fft(x_time_original)
    print(X_freq)

    print('\nAbsolute values of frequency domain coefficients\n')
    X_mag = np.abs(X_freq)
    print(X_mag)

    print('\nComplex time domain values obtained through inverse Fourier\n')
    x_time_complex = np.fft.ifft(X_freq)
    print(x_time_complex)

    print('\n Real time domain values recovered through inverse Fourier\n')
    x_time_real = np.real(np.fft.ifft(X_freq))
    print(x_time_real)

    print('\n Checks if original time domain signal equals the (real) recovered signal within a tolerance range\n')
    print(np.isclose(x_time_original, x_time_real, rtol=1e-15, atol=1e-15))