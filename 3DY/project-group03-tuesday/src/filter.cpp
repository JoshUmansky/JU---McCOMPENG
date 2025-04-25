/*
Comp Eng 3DY4 (Computer Systems Integration Project)

Department of Electrical and Computer Engineering
McMaster University
Ontario, Canada
*/

#include "dy4.h"
#include "filter.h"
#include "iofunc.h"
#include <vector>
#include <cmath>
#include <iostream>
#include <fstream>
#include <iomanip>
#include <complex>

// function to compute the impulse response "h" based on the sinc function
void impulseResponseLPF(float Fs, float Fc, unsigned short int num_taps, std::vector<float> &h, int gain)
{
	// allocate memory for the impulse response
	h.clear(); h.resize(num_taps, 0.0);

	double Norm_cutoff = Fc/(Fs/2);
	double M = num_taps;
	double halfM = (M-1)/2.0;
	double piNormCutoff = PI * Norm_cutoff;
	double piOverM = PI / M;

	for (int i = 0; i < M; i++) {
		if (i == halfM)
			h[i] = Norm_cutoff;
		else
			h[i] = Norm_cutoff * (std::sin(piNormCutoff*(i-halfM)) / (piNormCutoff*(i-halfM)));
		h[i] = h[i] * std::pow(std::sin(piOverM*i), 2) * gain;
	}
}

// function to compute the impulse response "h" based on the sinc function
void impulseResponseBPF(float Fs, float Fb, float Fe, unsigned short int num_taps, std::vector<float> &h, int gain)
{
	// allocate memory for the impulse response
	h.clear(); h.resize(num_taps, 0.0);

	double Norm_center = ((Fe + Fb) / 2) / (Fs/2);
	double Norm_pass = (Fe - Fb) / (Fs/2);

	for (unsigned int i = 0; i < num_taps; i++) {
		if ((int) i == (num_taps-1)/2)
			h[i] = Norm_pass;
		else
			h[i] = Norm_pass *  (std::sin(PI*(Norm_pass/2)*(i-(num_taps-1)/2))) / (PI*(Norm_pass/2)*(i-(num_taps-1)/2));
		h[i] = h[i] * std::cos((i-(num_taps-1)/2)*PI*Norm_center) * gain; // Multiply by cos to shift the filter and become BPF
		h[i] = h[i] * std::pow(std::sin(PI*i/num_taps), 2);
	}
}

void fastResampler(std::vector<float> &convo_output, const std::vector<float> convo_input, const std::vector<float> h, std::vector<float> &state, int D, int U) {

    // Resize output vector
	convo_output.clear();
    convo_output.resize((int) (convo_input.size() * U)/D, 0.0);

    // Initialize state if empty
    if (state.empty()) {
        state.resize(h.size()-1, 1.0);
    }

	float Urecip = 1.0 / U;

    // Perform the convolution with upsampling and downsampling
    for (size_t n = 0; n < convo_output.size(); n++) {
		
		// Calculate the phase offset for the current element
		int phase = (int) ((D * n) % U);  

		int index_outer = (int) (n * D);

		// Start from the calculated phase and iterate over the impulse response array h with steps of size U
        for (size_t k = phase; k < h.size(); k += U) { 

			int index = (index_outer - (int) k) * Urecip; 

			if (index >= 0) {
				convo_output[n] += h[k] * convo_input[index];

			} else if (state.size() + index >= 0) { // Adjusted index for negative values
				convo_output[n] += h[k] * state[state.size() + index]; 
			}
        }
    }

     // Prepare state for the next call by clearing it
	state.clear();
	state.resize(h.size(), 0.0);

	for (size_t i = 0; i < h.size(); i++) {
		state[i] = convo_input[convo_input.size() - h.size() + i];
	}
}

void fmDemodFormula(const std::vector<float> I, const std::vector<float> Q, std::vector<float> &state, std::vector<float> &y){
	// resize y to be length of I
	y.resize(I.size(), 0);

	// Calculate the demodulated signal based on formula from Lab 3
	if(state[0] == 0 && state[1] == 0)
		y[0] = 0;
	else
		y[0] = ((state[0] * (Q[0] - state[1]) - (state[1] * (I[0] - state[0]))) / ((state[0]*state[0]) + (state[1]*state[1])));

	for (size_t k = 1; k < I.size(); k++){
       y[k] =((I[k] * (Q[k+1] - Q[k])) - (Q[k] * (I[k+1] - I[k]))) / ((I[k]*I[k]) + (Q[k]*Q[k]));
	}

	state[0] = I[I.size() - 1];
	state[1] = Q[Q.size() - 1];
}

void delayBlock(const std::vector<float> &input_block, std::vector<float> &state_block, std::vector<float> &output_block) {

	// Resize output vector
	output_block.clear();
	output_block = state_block;
	output_block.reserve(input_block.size());
	for(unsigned int n = 0; n<(input_block.size()-state_block.size());n++){
		output_block.push_back(input_block[n]);
	}

	std::vector<float> temp(&input_block[input_block.size()-state_block.size()], &input_block[input_block.size()]);
	state_block = temp;

}

void PLL (std::vector<float> &plln, std::vector<float> &ncoOut, float Fs, float freq, float ncoScale, float phaseAdjust, float normBandwidth, float (&pllData)[5], int &trigOffset) {
	float Cp = 2.666;
	float Ci = 3.555;

	float Kp = normBandwidth * Cp;
	float Ki = normBandwidth * normBandwidth * Ci;

	if(ncoOut.size() != plln.size() + 1)
		ncoOut.resize(plln.size() + 1, 0.0);
	
	float integrator = pllData[0];
	float phaseEst = pllData[1];
	float feedbackI = pllData[2];
	float feedbackQ = pllData[3];
	ncoOut[0] = pllData[4];

	for (int i = 0; i < (int) plln.size(); i++) {
		
		// Phase detector
		float errorI = plln[i] * (+feedbackI);
		float errorQ = plln[i] * (-feedbackQ);
		float errorD = std::atan2(errorQ, errorI);

		// Loop filter
		integrator = integrator + Ki * errorD;
		phaseEst = phaseEst + Kp * errorD + integrator;

		// NCO
		trigOffset = trigOffset + 1.0;
		float trigArg = 2*PI*(freq/Fs)*(trigOffset) + phaseEst;

		feedbackI = std::cos(trigArg);
		feedbackQ = std::sin(trigArg);

		ncoOut[i+1] = std::cos((trigArg*ncoScale + phaseAdjust));

	}

	pllData[0] = integrator;
	pllData[1] = phaseEst;
	pllData[2] = feedbackI;
	pllData[3] = feedbackQ;
	pllData[4] = ncoOut[ncoOut.size() - 1];
}

void PLL_RDS (std::vector<float> &plln, std::vector<float> &ncoOutI, std::vector<float> &ncoOutQ, float Fs, float freq, float ncoScale, float phaseAdjust, float normBandwidth, float (&pllData)[6], int &trigOffset) {
	float Cp = 2.666;
	float Ci = 3.555;

	float Kp = normBandwidth * Cp;
	float Ki = normBandwidth * normBandwidth * Ci;

	if(ncoOutI.size() != plln.size() + 1)
		ncoOutI.resize(plln.size() + 1, 0.0);
	if(ncoOutQ.size() != plln.size() + 1)
		ncoOutQ.resize(plln.size() + 1, 0.0);

	float integrator = pllData[0];
	float phaseEst = pllData[1];
	float feedbackI = pllData[2];
	float feedbackQ = pllData[3];
	ncoOutI[0] = pllData[4];
	ncoOutQ[0] = pllData[5];

	for (int i = 0; i < (int) plln.size(); i++) {
		
		// Phase detector
		float errorI = plln[i] * (+feedbackI);
		float errorQ = plln[i] * (-feedbackQ);
		float errorD = std::atan2(errorQ, errorI);

		// Loop filter
		integrator = integrator + Ki * errorD;
		phaseEst = phaseEst + Kp * errorD + integrator;

		// NCO
		trigOffset = trigOffset + 1.0;
		float trigArg = 2*PI*(freq/Fs)*(trigOffset) + phaseEst;

		feedbackI = std::cos(trigArg);
		feedbackQ = std::sin(trigArg);

		ncoOutI[i+1] = std::cos((trigArg*ncoScale + phaseAdjust));
		ncoOutQ[i+1] = std::sin((trigArg*ncoScale + phaseAdjust));
		//std::cerr << phaseEst << std::endl;

	}

	pllData[0] = integrator;
	pllData[1] = phaseEst;
	pllData[2] = feedbackI;
	pllData[3] = feedbackQ;
	pllData[4] = ncoOutI[ncoOutI.size() - 1];
	pllData[5] = ncoOutQ[ncoOutQ.size() - 1];
}

std::vector<int> multiply_with_parity_check(const std::vector<int>& bit_vector, const std::vector<std::vector<int>>& matrix) {
    std::vector<int> result(matrix[0].size(), 0); // Initialize the result vector with zeros based on the number of columns
    for (size_t j = 0; j < matrix[0].size(); ++j) { // Iterate over columns of the matrix
        for (size_t i = 0; i < matrix.size(); ++i) { // Iterate over rows of the matrix
            result[j] ^= bit_vector[i] & matrix[i][j]; // Perform bitwise multiplication and XOR
        }
    }
    return result;
}

void impulseResponseRootRaisedCosine(float Fs, unsigned short int num_taps, std::vector<float> &h) {
	float T_symbol = 1.0 / 2375.0;

	float beta = 0.9;

	h.clear(); h.resize(num_taps, 0.0);

	for (int k = 0; k < num_taps; k++) {
		float t = (float)((k-num_taps/2))/Fs;

		// Calculate the impulse response based on the formula
		if (t == 0.0) {
			h[k] = 1.0 + beta*((4.0/PI)-1);
		} else if(t == -T_symbol/(4.0*beta) || t == T_symbol/(4.0*beta)) {
			h[k] = (beta/std::sqrt(2))*(((1+2.0/PI)*(std::sin(PI/(4.0*beta)))) + ((1-2.0/PI)*(std::cos(PI/(4.0*beta)))));
		} else {
			h[k] = (std::sin(PI*t*(1-beta)/T_symbol) + 4*beta*(t/T_symbol)*std::cos(PI*t*(1+beta)/T_symbol))/(PI*t*(1-(4*beta*t/T_symbol)*(4*beta*t/T_symbol))/T_symbol);
		}
	}
}
