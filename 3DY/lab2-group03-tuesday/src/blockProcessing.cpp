/*
Comp Eng 3DY4 (Computer Systems Integration Project)

Copyright by Nicola Nicolici
Department of Electrical and Computer Engineering
McMaster University
Ontario, Canada
*/

#include <iostream>
#include <iomanip>
#include <fstream>
#include <vector>
#include <cmath>

#define PI 3.14159265358979323846

// function for computing the impulse response (reuse from previous experiment)
void impulseResponseLPF(float Fs, float Fc, unsigned short int num_taps, std::vector<float> &h)
{
	// allocate memory for the impulse response
	h.clear(); h.resize(num_taps, 0.0);
	
	float norm_cut = Fc / (Fs / 2);
	int tapsminus1 = num_taps - 1;
	for(int i = 0; i < num_taps; i++){
		if (i == tapsminus1 / 2){
			h[i] = norm_cut;
		}
		else{
			//  H[i] = NormCut * (math.sin (math.pi * NormCut*(i-(tapMinus1/2))) / (math.pi * NormCut *(i-(tapMinus1/2))))
			h[i] = norm_cut * (sin(PI * norm_cut * (i - (tapsminus1 / 2))) / (PI * norm_cut * (i - (tapsminus1 / 2))));
		}
		h[i] = h[i] * sin((i*PI) / num_taps * sin((i * PI) / num_taps));
	}
}

// function for computing the impulse response (reuse from previous experiment)
void convolveFIR(std::vector<float> &y, const std::vector<float> &x, const std::vector<float> &h)
{
	// allocate memory for the output (filtered) data
	y.clear(); y.resize(x.size()+h.size()-1, 0.0);

	// the rest of the code in this function is to be completed by you
	// based on your understanding and the Python code from the first lab
	
	for(int i=0;i<int(x.size());i++){
		y[i] = 0;
		for(int j=0;j<int(h.size());j++){
			if(i-j >= 0 and i-j < int(x.size())){
				y[i] += x[i-j] * h[j];
			}
		}		
	}
}

void blockconvolveFIR(std::vector<float> &y, const std::vector<float> &x, const std::vector<float> &h, const int block_num)
{
	// allocate memory for the output (filtered) data
	y.clear(); y.resize(x.size()+h.size()-1, 0.0);
	
	std::vector<float> state;
	std::vector<float> block;
	
	int block_amount = static_cast<int>(x.size()/block_num) + 1;
	
	for(int k=0; k < block_amount; k++){
		
		std::cout << "Block " << k << " start \n";
		
		int start_idx = k * block_num; 
		int end_idx = std::min((k+1) * block_num, int(x.size()));

		block.clear(); 
		block.reserve(block_num); // Reserve space for the block

		// Insert the relevant portion of x into the block
		block.insert(block.end(), x.begin() + start_idx, x.begin() + end_idx);

		// Pad the rest of the block with zeros if needed
		int padding_size = block_num - (end_idx - start_idx);
		if (padding_size > 0) {
			block.insert(block.end(), padding_size, 0.0);
		}

		if(k!=0){ 
			block.insert(block.begin(), state.begin(), state.end());
		} 
		
		for(int i=0; i<int(block.size()); i++){
			y[i + k*block_num] = 0;
			for(int j=0; j<int(h.size()); j++){
				if(i-j >= 0 and i-j < int(block.size())){
					y[i + k*block_num] += block[i-j] * h[j];
				}
			}		
		}
		
		int state_start_idx = std::max(0, int(block.size()) - int(h.size()) + 1);
		int state_end_idx = block.size();
		
		state.clear(); state.resize(h.size()-1, 0.0);
		state = std::vector<float>(block.begin() + state_start_idx, block.begin() + state_end_idx);
		
		for (const auto& value : state) {
			std::cout << value << " ";
		}
	}
}

// function to read audio data from a binary file that contains raw samples
// represented as 32-bit floats; we also assume two audio channels
// note: check the Python script that can prepare this type of files
// directly from .wav files
void read_audio_data(const std::string in_fname, std::vector<float> &audio_data)
{
	// file descriptor for the input to be read
	std::ifstream fdin(in_fname, std::ios::binary);
	if(!fdin) {
		std::cout << "File " << in_fname << " not found ... exiting\n";
		exit(1);
	} else {
		std::cout << "Reading raw audio from \"" << in_fname << "\"\n";
	}
	// search for end of file to count the number of samples to be read
	fdin.seekg(0, std::ios::end);
	// we assume the Python script has written data in 32-bit floats
	const unsigned int num_samples = fdin.tellg() / sizeof(float);

	// allocate memory space to store all the samples
	audio_data.clear(); audio_data.resize(num_samples);
	// back to the beginning of the file to read all samples at once
	fdin.seekg(0, std::ios::beg);
	// do a single read for audio data from the input file stream
	fdin.read(reinterpret_cast<char*>(&audio_data[0]), \
						num_samples*sizeof(float));
	// close the input file
	fdin.close();
}

// function to split an audio data where the left channel is in even samples
// and the right channel is in odd samples
void split_audio_into_channels(const std::vector<float> &audio_data, std::vector<float> &audio_left, std::vector<float> &audio_right)
{
	for (int i=0; i<(int)audio_data.size(); i++) {
		if (i%2 == 0)
			audio_left.push_back(audio_data[i]);
		else
			audio_right.push_back(audio_data[i]);
	}
}

// function to write audio data to a binary file that contains raw samples
// represented as 32-bit floats; we also assume two audio channels
// note: check the python script that can read this type of files
// and then reformat them to .wav files to be run on third-party players
void write_audio_data(const std::string out_fname, const std::vector<float> &audio_left, const std::vector<float> &audio_right)
{
	// file descriptor for the output to be written
	if (audio_left.size() != audio_right.size()) {
		std::cout << "Something got messed up with audio channels\n";
		std::cout << "They must have the same size ... exiting\n";
		exit(1);
	} else {
		std::cout << "Writing raw audio to \"" << out_fname << "\"\n";
	}
	std::ofstream fdout(out_fname, std::ios::binary);
	for (int i=0; i<(int)audio_left.size(); i++) {
		// we assume we have handled a stereo audio file
		// hence, we must interleave the two channels
		// (change as needed if testing with mono files)
		fdout.write(reinterpret_cast<const char*>(&audio_left[i]),\
								sizeof(audio_left[i]));
		fdout.write(reinterpret_cast<const char*>(&audio_right[i]),\
								sizeof(audio_right[i]));
	}
	fdout.close();
}

int main()
{
	// assume the wavio.py script was run beforehand to produce a binary file
	const std::string in_fname = "../data/float32samples.bin";
	// declare vector where the audio data will be stored
	std::vector<float> audio_data;
	// note: we allocate memory for audio_data from within this read function
	read_audio_data(in_fname, audio_data);

	// set up the filtering flow
	float Fs = 44100.0;	// sample rate for our "assumed" audio (change as needed for 48 ksamples/sec audio files)
	float Fc = 20000.0;	// cutoff frequency (explore ... but up-to Nyquist only!)
	// number of FIR filter taps (feel free to explore ...)
	unsigned short int num_taps = 51;

	// impulse response (reuse code from the previous experiment)
	std::vector<float> h;
	impulseResponseLPF(Fs, Fc, num_taps, h);
	// note: memory for the impulse response vector and output data vectors
	// should be allocated from within the corresponding functions
	// (as for the previous experiment, from where you should reuse your code)

	// there is one more point before filtering is done:
	// recall we assume there are two channels in the audio data
	// the channels must be handled separately by your DSP functions, hence
	// split the audio_data into two channels (audio_left and audio_right)

	// declare vectors where the audio left/right channels will be stored
	std::vector<float> audio_left, audio_right;
	// note: we allocate the memory for the left/right channels
	// from within the split function that is called in the code below
	split_audio_into_channels(audio_data, audio_left, audio_right);

	// convolution code for filtering (reuse from the previous experiment)
	std::vector<float> single_pass_left, single_pass_right;
	//convolveFIR(single_pass_left, audio_left, h);
	//convolveFIR(single_pass_right, audio_right, h);
	std::cout << "starting convolution \n";
	blockconvolveFIR(single_pass_left, audio_left, h, 1000);
	blockconvolveFIR(single_pass_right, audio_right, h, 1000);
	// note: by default the above convolution produces zero on the output stream
	// YOU will need to update the convolveFIR and impulseResponseLPF functions

	// create a binary file to be read by wavio.py script to produce a .wav file
	// note: small adjustments will need to be made to wavio.py, i.e., you should
	// match the filenames, no need for self-checks as default Python code, ...
	const std::string out_fname = "../data/float32filtered.bin";
	write_audio_data(out_fname, single_pass_left,	single_pass_right);

	return 0;
}
