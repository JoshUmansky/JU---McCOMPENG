/*
Comp Eng 3DY4 (Computer Systems Integration Project)

Department of Electrical and Computer Engineering
McMaster University
Ontario, Canada
*/

// source code for Fourier-family of functions
#include "dy4.h"
#include "fourier.h"
#include <vector>
#include <complex>
#include <cmath> 

// just DFT function (no FFT yet)
void DFT(const std::vector<float> &x, std::vector<std::complex<float>> &Xf) {
	Xf.clear(); Xf.resize(x.size(), std::complex<float>(0));
	for (int m = 0; m < (int)Xf.size(); m++) {
		for (int k = 0; k < (int)x.size(); k++) {
				std::complex<float> expval(0, -2*PI*(k*m) / x.size());
				Xf[m] += x[k] * std::exp(expval);
		}
	}
}

// function to compute the magnitude values in a complex vector
void computeVectorMagnitude(const std::vector<std::complex<float>> &Xf, std::vector<float> &Xmag)
{
	// only the positive frequencies
	Xmag.clear(); Xmag.resize(Xf.size(), float(0));
	for (int i = 0; i < (int)Xf.size(); i++) {
		Xmag[i] = std::abs(Xf[i])/Xf.size();
	}
}

// add your own code to estimate the PSD

void estimatePSD(const std::vector<float> &samples, const int freq_bins, const float Fs, std::vector<float> &freq, std::vector<float> &psd_est) {
	
	double df = Fs/freq_bins;
	psd_est.resize(freq_bins / 2, 0); // Initialize with zeros.

	// create an incrementing arrayfor plotting the psd thats evenly spaced out and incrementing
	for (double f = 0; f < (Fs / 2); f += df) {
		freq.push_back(f);
	}
	
	std::vector<double> hann;
	for (int i = 0; i < freq_bins; i++) {
		hann.push_back(std::pow(std::sin(i*PI/freq_bins),2));
	}
	
	std::vector<double> psd_list;
	
	int no_segments = std::floor(samples.size() / static_cast<double>(freq_bins));

	for (int k = 0; k < no_segments; k++) {
		
		std::vector<float> windowed_samples(freq_bins);
		
		for (int i = 0; i < freq_bins; ++i) {
			windowed_samples[i] = samples[(k*freq_bins)+i] * hann[i];
		}
		
		std::vector<std::complex<float>> Xf;
		DFT(windowed_samples, Xf);
		
		Xf.resize(freq_bins / 2);
		std::vector<double> psd_seg(freq_bins / 2);
		for (int i = 0; i < (freq_bins / 2); ++i) {
			psd_seg[i] = (1.0 / (Fs * (freq_bins / 2))) * std::pow(std::abs(Xf[i]), 2);
			psd_seg[i] *= 2; // Compensate for the energy in the negative frequency bins
		}
		
		for (int i = 0; i < (int) psd_seg.size(); ++i) {
		
			psd_seg[i] = 10*std::log10(psd_seg[i]);
		
		}
		
		psd_list.insert(psd_list.end(), psd_seg.begin(), psd_seg.end());
		
	}
	
	for (int k = 0; k < (int) freq_bins/2; k++) {
		for (int l = 0; l < no_segments; l++) {
			psd_est[k] += psd_list[k + l * ((int) freq_bins/2)];
		}
		psd_est[k] /= no_segments;
	}
}
