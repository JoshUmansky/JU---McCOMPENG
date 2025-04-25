#
# Comp Eng 3DY4 (Computer Systems Integration Project)
#
# Copyright by Nicola Nicolici
# Department of Electrical and Computer Engineering
# McMaster University
# Ontario, Canada
#

import matplotlib.pyplot as plt
import numpy as np
import cmath, math
import sys
from scipy import fft, ifft, signal

def plotSpectrum(x, Fs, type = 'FFT'):

    n = len(x)             # length of the signal
    df = Fs/n              # frequency increment (width of freq bin)

    # compute Fourier transform, its magnitude and normalize it before plotting
    if type == 'FFT':
        Xfreq = np.fft.fft(x)
        #print(type(Xfreq))
        
    elif type == 'DFT':
        Xfreq = DFT(x)
        #numpyfreq = np.fft.fft(x)
        #print(type(Xfreq))
        
    elif type == 'IDFT':
        Xfreq = IDFT(x)
        #print(type(Xfreq))
    
    XMag = abs(Xfreq)/n
    #numpyMag = abs(numpyfreq)/n
    
    # Note: because x is real, we keep only the positive half of the spectrum
    # Note also: half of the energy is in the negative half (not plotted)
    XMag = XMag[0:int(n/2)]
    #numpyMag = numpyMag[0:int(n/2)]

    # freq vector up to Nyquist freq (half of the sample rate)
    freq = np.arange(0, Fs/2, df)

    fig, ax = plt.subplots()
    ax.plot(freq, XMag)
    #ax.plot(freq, numpyMag)
    ax.set(xlabel='Frequency (Hz)', ylabel='Magnitude',
    title='Frequency domain plot')
    # fig.savefig("freq.png")
    plt.show()

def plotTime(x, time):
    fig, ax = plt.subplots()
    ax.plot(time, x.real)
    ax.set(xlabel='Time (sec)', ylabel='Amplitude',
            title='Time domain plot')
    # fig.savefig("time.png")
    #plt.show()

def generateSin(Fs, interval, frequency = 7.0, amplitude = 5.0, phase = 0.0):

    dt = 1.0/Fs                          # sampling period (increment in time)
    time = np.arange(0, interval, dt)    # time vector over interval

    # generate the sin signal
    x = amplitude*np.sin(2*math.pi*frequency*time+phase)

    return time, x

def generateSquare(Fs, interval, frequency = 7.0, amplitude = 5.0, dutyCycle = 0.5):

    dt = 1.0/Fs                          # sampling period (increment in time)
    time = np.arange(0, interval, dt)    # time vector over interval

    # generate the sin signal
    x = amplitude*signal.square(time*2*math.pi*frequency, dutyCycle)

    return time, x

def DFT(x):
    X = [None] * len(x)
    for m in range(len(x)):
        X[m] = 0       
        for k in range(len(x)):
            X[m] += x[k]*cmath.exp(-2j * math.pi * k*m/len(x))
    return np.array(X)

def IDFT(X):
    x = [None] * len(X)
    for m in range(len(X)):
        x[m] = 0       
        for k in range(len(X)):
            x[m] += X[k]*cmath.exp(2j * math.pi * k*m/len(X))
        x[m] = x[m] / len(X)
    return np.array(x)

def cli_error_msg():

    # error message to provide the correct command line interface (CLI) arguments
    print('Valid arguments:')
    print('\trc:  reference code')
    print('\til1: in-lab 1')
    print('\til2: in-lab 2')
    print('\til3: in-lab 3')
    print('\tth:  take-home')
    sys.exit()

if __name__ == "__main__":

    if len(sys.argv[0:]) != 2:
        cli_error_msg()

    Fs = 100.0          # sampling rate
    interval = 1.0      # set up to one full second

    if (sys.argv[1] == 'rc'): # runs the reference code (rc)

        print('Reference code for the Fourier transform')

        # generate the user-defined sin function
        time, x = generateSin(Fs, interval)
        # plot the signal in time domain
        plotTime(x, time)
        # plot the signal in frequency domain
        plotSpectrum(x, Fs, type = 'FFT')

    elif (sys.argv[1] == 'il1'):

        print('In-lab experiment 1 for the Fourier transform')
        time, x = generateSin(Fs, interval)
        
        plotSpectrum(x,Fs,type="DFT")
        plotTime(IDFT(DFT(x)), time)
        plotTime(np.fft.ifft(np.fft.fft(x)), time)
        plt.show()
        
        if (np.allclose(DFT(x),np.fft.fft(x)) == True):
            print("DFT GOOD")
        else:
            print("DFT BAD")
            
        if (np.allclose(IDFT(DFT(x)),np.fft.ifft(np.fft.fft(x))) == True):
            print("IDFT GOOD")
        else:
            print("IDFT BAD")
        
        # compute the spectrum with your own DFT
        # you can use cmath.exp() for complex exponentials
        # plotSpectrum(x, Fs, type = 'your DFT name')

        # confirm DFT/IDFT correctness by checking if x == IDFT(DFT(x))

        # for further details, if any, check the lab document

    elif (sys.argv[1] == 'il2'):

        print('In-lab experiment 2 for the Fourier transform')

        # use np.random.randn() for randomization
        # we can owverwrie the default values
        # frequency =  8.0                     # frequency of the signal
        # amplitude =  3.0                     # amplitude of the signal
        # phase = 1.0                          # phase of the signal
        # time, x = generateSin(Fs, interval, frequency, amplitude, phase)

        # You should also numerically check if the signal energy
        # in time and frequency domains is identical

        # for further details, if any, check the lab document
        
        x = np.random.randn(1000)
        x = np.interp(x, (x.min(), x.max()), (-10, 10))
        
        sumx = 0
        sumX = 0
        
        for i in x:
            sumx += abs(i) ** 2
        for i in DFT(x):
            sumX += abs(i) ** 2
        
        sumX /= len(x)
            
        print(sumx, " : ", sumX)
        if np.allclose(sumx, sumX):
            print("CORRECT")
        else:
            print("INCORRECT")
        
        
                 
    elif (sys.argv[1] == 'il3'):

        print('In-lab experiment 3 for the Fourier transform')

        # generate randomized multi-tone signals
        # plot them in both time and frequency domain

        # for further details, if any, check the lab document
                
        # generate the user-defined sin function
        time, x = generateSin(Fs, interval,frequency = 7.0, amplitude = 5.0, phase = 0.0)
        x += generateSin(Fs, interval,frequency = 2.0, amplitude = 20.0, phase = 2.0)[1]
        x += generateSin(Fs, interval,frequency = 10.0, amplitude = 1.0, phase = 8.0)[1]
        
        # plot the signal in time domain
        plotTime(x, time)
        # plot the signal in frequency domain
        plotSpectrum(x, Fs, type = 'FFT')
        

    elif (sys.argv[1] == 'th'):

        print('Take-home exercise for the Fourier transform')

        # for specific details check the lab document
        time, x = generateSquare(Fs, interval)
        # plot the signal in time domain
        plotTime(x, time)
        # plot the signal in frequency domain
        plotSpectrum(x, Fs, type = 'FFT')
        

    else:

        cli_error_msg()

    plt.show()
