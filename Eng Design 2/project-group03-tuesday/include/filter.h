/*
Comp Eng 3DY4 (Computer Systems Integration Project)

Department of Electrical and Computer Engineering
McMaster University
Ontario, Canada
*/

#ifndef DY4_FILTER_H
#define DY4_FILTER_H

// add headers as needed
#include <iostream>
#include <vector>

// declaration of a function prototypes
void impulseResponseLPF(float Fs, float Fc, unsigned short int num_taps, std::vector<float> &h, int gain);
void impulseResponseBPF(float Fs, float Fb, float Fe, unsigned short int num_taps, std::vector<float> &h, int gain);
void PLL (std::vector<float> &plln, std::vector<float> &ncoOut, float Fs, float freq, float ncoScale, float phaseAdjust, float normBandwidth, float (&pllData)[5], int &trigOffset);
void PLL_RDS (std::vector<float> &plln, std::vector<float> &ncoOutI, std::vector<float> &ncoOutQ, float Fs, float freq, float ncoScale, float phaseAdjust, float normBandwidth, float (&pllData)[6], int &trigOffset);
void fastResampler(std::vector<float> &y, const std::vector<float> x, const std::vector<float> h, std::vector<float> &state, int D, int U);
void fmDemodFormula(const std::vector<float> I, const std::vector<float> Q, std::vector<float> &state, std::vector<float> &y);
void RFFrontEnd(std::vector<float> &y, std::vector<float> &data, std::vector<float> &RFdemod_state, std::vector<float> &i_state, std::vector<float> &q_state, int Ds, float RfFs);
void delayBlock(const std::vector<float> &input_block, std::vector<float> &state_block, std::vector<float> &output_block);
void impulseResponseRootRaisedCosine(float Fs, unsigned short int num_taps, std::vector<float> &h);
std::vector<int> multiply_with_parity_check(const std::vector<int>& bit_vector, const std::vector<std::vector<int>>& matrix);
#endif // DY4_FILTER_H
