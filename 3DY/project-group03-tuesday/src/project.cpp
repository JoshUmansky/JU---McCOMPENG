/*
Comp Eng 3DY4 (Computer Systems Integration Project)

Copyright by Nicola Nicolici
Department of Electrical and Computer Engineering
McMaster University
Ontario, Canada
*/

#include "dy4.h"
#include "filter.h"
#include "fourier.h"
#include "genfunc.h"
#include "iofunc.h"
#include "logfunc.h"
#include "queue.cpp"
#include "RDS_helper.h"
#include <vector>
#include <stdio.h>
#include <iostream>
#include <thread>
#include <mutex>
#include <chrono>

void frontEnd(Queue& queue, int block_size, int IF_D, int IQ_Fs, int num_taps);
void frontEnd_RDS(Queue& audio_Queue, Queue& RDS_Queue, int block_size, int IF_D, int RF_Fs, int IF_Fs, int num_taps, int SPS);
void monoAudioProcessing(Queue& queue, int D, int U, int AudioFs, int num_taps);
void stereoAudioProcessing(Queue& queue, int D, int U, int AudioFs, int num_taps, int block_size);
void RDS(Queue& queue, int Fs, int num_taps, int SPS, int U, int D);

int main(int argc, char *argv[]){

	std::cerr << "STARTING MAIN FUNCTION" << std::endl;
	char path = 'm';
	int mode = 0;

	if(argc > 3){
		std::cerr << "More than two parameters given" << std::endl;
		exit(1);
	} else{

		mode = *argv[1] - 48; // Convert from char to int
		if (mode < 0 || mode > 3) {
			std::cerr << "Incorrect mode given" << std::endl;
			exit(1);
		}

		path = *argv[2];
		if (path != 'm' && path != 's' && path != 'r') {
			std::cerr << "Incorrect path given" << std::endl;
			exit(1);
		}
	}

	int block_size, IF_D, U, D, RF_Fs, IF_Fs, SPS, RDS_U, RDS_D;
	if (mode == 0) {
		std::cerr << "Path : " << path << " Mode : " <<  mode << std::endl;
		block_size = BLOCK_SIZEM0;
		IF_D = 10;
		U = 1;
		D = 5;
		RF_Fs = 2400000;
		IF_Fs = 240000;
		SPS = 26;
		RDS_U = 247;
		RDS_D = 960;
	} 
	else if (mode == 1) {
		std::cerr << "Path : " << path << " Mode : " <<  mode << std::endl;
		block_size = BLOCK_SIZEM1;
		IF_D = 9;
		U = 1;
		D = 4;
		RF_Fs = 1152000;
		IF_Fs = 128000;
	} 
	else if (mode == 2) {
		std::cerr << "Path : " << path << " Mode : " <<  mode << std::endl;
		block_size = BLOCK_SIZEM2;
		IF_D = 10;
		U = 147;
		D = 800;
		RF_Fs = 2400000;
		IF_Fs = 240000;
		SPS = 45;
		RDS_U = 57;
		RDS_D = 128;
	} 
	else if (mode == 3) {
		std::cerr << "Path : " << path << " Mode : " <<  mode << std::endl;
		block_size = BLOCK_SIZEM3;
		IF_D = 5;
		U = 147;
		D = 960;
		RF_Fs = 1440000;
		IF_Fs = 288000;
	}

	int num_taps = 101;

	Queue audio_Queue(5);
	Queue RDS_Queue(5);

	if (path == 'm') {
		std::thread frontEndThread(frontEnd, std::ref(audio_Queue), block_size, IF_D, RF_Fs, num_taps);
		std::thread audioProcessingThread(monoAudioProcessing, std::ref(audio_Queue), D, U, IF_Fs, num_taps);

		audioProcessingThread.join();
		frontEndThread.join();
	}
	else if (path == 's') {
		std::thread frontEndThread(frontEnd, std::ref(audio_Queue), block_size, IF_D, RF_Fs, num_taps);
		std::thread audioProcessingThread(stereoAudioProcessing, std::ref(audio_Queue), D, U, IF_Fs, num_taps, block_size);

		audioProcessingThread.join();
		frontEndThread.join();
	}
	else if (path == 'r') {
		std::thread frontEndThread(frontEnd_RDS, std::ref(audio_Queue), std::ref(RDS_Queue), block_size, IF_D, RF_Fs, IF_Fs, num_taps, SPS);
		std::thread audioProcessingThread(stereoAudioProcessing, std::ref(audio_Queue), D, U, IF_Fs, num_taps, block_size);
		std::thread RDSProcessingThread(RDS, std::ref(RDS_Queue), IF_Fs, num_taps, SPS, RDS_U, RDS_D);

		RDSProcessingThread.join();
		audioProcessingThread.join();
		frontEndThread.join();
	}
}

void frontEnd(Queue& queue, int block_size, int IF_D, int RF_Fs, int num_taps) {

    std::vector<float> i_state, q_state;
    std::vector<float> RFdemod_state = {0, 0};
	std::vector<float> IQfilter;
	std::vector<float> data_i;
	std::vector<float> data_q;

	float Fc = std::min((RF_Fs *  (float) IF_D) / 2 , (float) 100000);
	//auto startLPF = std::chrono::high_resolution_clock::now();
	impulseResponseLPF(RF_Fs, Fc, num_taps, IQfilter, 1);
	/*auto endLPF = std::chrono::high_resolution_clock::now();
	std::chrono::duration<double> elapsedLPF = endLPF - startLPF;
	std::chrono::duration<double> elapsedread;
	std::chrono::duration<double> elapsedconv;
	std::chrono::duration<double> elapsedfmdemod;
	std::chrono::duration<double> elapsedreadp;
	std::chrono::duration<double> elapsedconvp;
	std::chrono::duration<double> elapsedfmdemodp;
	double lastblockid = 0;*/
	//std::chrono::duration<double> elapsedRfFrontEnd;
	//std::chrono::duration<double> elapsedRfFrontEndp;
    for (unsigned int block_id = 0; ; block_id++) {

		// ------------------------------------------- RF FRONT END -------------------------------------------
		//auto RfFrontEndStart = std::chrono::high_resolution_clock::now();
        std::vector<float> block_data(block_size);
		std::vector<float> fmDemod;

		// Read RF data from stdin
		//auto startread = std::chrono::high_resolution_clock::now();
        readStdinBlockData(block_size, block_id, block_data);
		//auto endread = std::chrono::high_resolution_clock::now();
		
        if ((std::cin.rdstate()) != 0) {
            std::cerr << "End of input stream reached" << std::endl;
            exit(1); 
        }

		// Split data into I and Q
		data_i.clear();
		data_q.clear();
		for (size_t i = 0; i < block_data.size(); ++i) {
			if (i % 2 == 0) {
				data_i.push_back(block_data[i]);
			} else {
				data_q.push_back(block_data[i]);
			}
		}

		// Resample data
		std::vector<float> block_i (data_i.size(), 0), block_q(data_q.size(), 0);
		//auto startconv = std::chrono::high_resolution_clock::now();
		fastResampler(block_i, data_i, IQfilter, i_state, IF_D, 1);
		//auto endconv = std::chrono::high_resolution_clock::now();
		fastResampler(block_q, data_q, IQfilter, q_state, IF_D, 1);

		// Demodulate data
		//auto startfmdemod = std::chrono::high_resolution_clock::now();
		fmDemodFormula(block_i, block_q, RFdemod_state, fmDemod);
		//auto endfmdemod = std::chrono::high_resolution_clock::now();

		/*elapsedread += endread - startread;
		elapsedconv += endconv - startconv;
		elapsedfmdemod += endfmdemod - startfmdemod;
		lastblockid = block_id+1;
		elapsedreadp = elapsedread / lastblockid;
		elapsedconvp = elapsedconv / lastblockid;
		elapsedfmdemodp = elapsedfmdemod / lastblockid;
		std::cerr << "Average time for reading data: " << elapsedreadp.count() << " s" << std::endl;
		std::cerr << "Average time for convolution: " << elapsedconvp.count() << " s" << std::endl;
		std::cerr << "Average time for FM demodulation: " << elapsedfmdemodp.count() << " s" << std::endl;
		std::cerr << "Time for filter generation: " << elapsedLPF.count() << " s" << std::endl;*/
		//auto RfFrontEndEnd = std::chrono::high_resolution_clock::now();
		//elapsedRfFrontEnd += RfFrontEndEnd - RfFrontEndStart;
		//elapsedRfFrontEndp = elapsedRfFrontEnd / (block_id + 1);
		//std::cerr << "Average time for RF Front End: " << elapsedRfFrontEndp.count() << " s" << std::endl;
		// Push data to queue
        queue.produce(fmDemod);
    }
	

	// Signal that no more data will be produced
    queue.stopProducing();
}


void frontEnd_RDS(Queue& audio_Queue, Queue& RDS_Queue, int block_size, int IF_D, int RF_Fs, int IF_Fs, int num_taps, int SPS) {

    std::vector<float> i_state, q_state;
    std::vector<float> RFdemod_state = {0, 0};
	std::vector<float> IQfilter;
	std::vector<float> data_i;
	std::vector<float> data_q;

	std::vector<float> channel_state, delay_state((num_taps-1)/2, 0.0), narrow_state;
	float pll_state[6] = {0.0, 0.0, 1.0, 0.0, 1.0, 0.0};
	int trigOffset = 0;
	std::vector<float> hNarrow, hChannel;
	
	// Generate filters for RDS carrier processing
	float Fc = std::min((RF_Fs *  (float) IF_D) / 2 , (float) 100000);
	//auto startLPF = std::chrono::high_resolution_clock::now();
	impulseResponseLPF(RF_Fs, Fc, num_taps, IQfilter, 1);
	//auto endLPF = std::chrono::high_resolution_clock::now();

	//auto startBPF1 = std::chrono::high_resolution_clock::now();
	impulseResponseBPF(IF_Fs, 113500, 114500, num_taps, hNarrow, 1);
	//auto endBPF1 = std::chrono::high_resolution_clock::now();

	//auto startBPF2 = std::chrono::high_resolution_clock::now();
	impulseResponseBPF(IF_Fs, 54000, 60000, num_taps, hChannel, 1);
	//auto endBPF2 = std::chrono::high_resolution_clock::now();

	/*std::chrono::duration<double> elapsedLPF = endLPF - startLPF;
	std::chrono::duration<double> elapsedBPF1 = endBPF1 - startBPF1;
	std::chrono::duration<double> elapsedBPF2 = endBPF2 - startBPF2;
	int lastblock = 0;

	std::chrono::duration<double> elapsedconv1;
	std::chrono::duration<double> elapseddelay;
	std::chrono::duration<double> elapsedsquare;
	std::chrono::duration<double> elapsedconv2;
	std::chrono::duration<double> elapsedpll;
	std::chrono::duration<double> elapsedmix;

	std::chrono::duration<double> elapsedconv1p;
	std::chrono::duration<double> elapseddelayp;
	std::chrono::duration<double> elapsedsquarep;
	std::chrono::duration<double> elapsedconv2p;
	std::chrono::duration<double> elapsedpllp;
	std::chrono::duration<double> elapsedmixp;
	std::chrono::duration<double> elapsedextraction;
	std::chrono::duration<double> elapsedrecovery;
	std::chrono::duration<double> elapsedextractionp;
	std::chrono::duration<double> elapsedrecoveryp;*/

    for (unsigned int block_id = 0; ; block_id++) {

		// ------------------------------------------- RF FRONT END -------------------------------------------

        std::vector<float> block_data(block_size), fmDemod;

		// Read RF data from stdin
        readStdinBlockData(block_size, block_id, block_data);
		
        if ((std::cin.rdstate()) != 0) {
            std::cerr << "End of input stream reached" << std::endl;
            exit(1); 
        }

		// Split data into I and Q
		data_i.clear();
		data_q.clear();
		for (size_t i = 0; i < block_data.size(); ++i) {
			if (i % 2 == 0) {
				data_i.push_back(block_data[i]);
			} else {
				data_q.push_back(block_data[i]);
			}
		}

		// Resample data
		std::vector<float> block_i (data_i.size(), 0), block_q(data_q.size(), 0);
		fastResampler(block_i, data_i, IQfilter, i_state, IF_D, 1);
		fastResampler(block_q, data_q, IQfilter, q_state, IF_D, 1);

		// Demodulate data
		fmDemodFormula(block_i, block_q, RFdemod_state, fmDemod);

		// Push data to queue
        audio_Queue.produce(fmDemod); 

		// ------------------------------------------- RDS CHANNEL EXTRACTION -------------------------------------------
		//auto startextraction = std::chrono::high_resolution_clock::now();
		std::vector<float> RDS_extracted1;
		//auto startconv1 = std::chrono::high_resolution_clock::now();
		fastResampler(RDS_extracted1, fmDemod, hChannel, channel_state, 1, 1); // <---- Ask about up and down sample
		//auto endconv1 = std::chrono::high_resolution_clock::now();
		// All pass filter match delay for bottom branch
		std::vector<float> delayed_RDS; 
		//auto startdelay = std::chrono::high_resolution_clock::now();
		delayBlock(RDS_extracted1, delay_state, delayed_RDS);
		//auto enddelay = std::chrono::high_resolution_clock::now();
		//auto endextraction = std::chrono::high_resolution_clock::now();
		// ------------------------------------------- RDS PILOT EXTRACTION -------------------------------------------
		//auto startrecovery = std::chrono::high_resolution_clock::now();
		// Squaring nonlinearity
		std::vector<float> RDS_extracted(RDS_extracted1.size());
		for (int i = 0; i < (int)RDS_extracted1.size(); i++) {
			RDS_extracted[i] = RDS_extracted1[i] * RDS_extracted1[i];
		}
		//auto endsquare = std::chrono::high_resolution_clock::now();
		// BPF 113.5kHz - 114.5kHz
		std::vector<float> recovery_pll_input; 
		//auto startconv2 = std::chrono::high_resolution_clock::now();
		fastResampler(recovery_pll_input, RDS_extracted, hNarrow, narrow_state, 1, 1);
		//auto endconv2 = std::chrono::high_resolution_clock::now();

		// PLL 114 kHz (NCO 57 kHz)
		std::vector<float> NCO_outputI, NCO_outputQ;
		std::vector<float> Imixed, Qmixed;
		//auto startpll = std::chrono::high_resolution_clock::now();
		PLL_RDS(recovery_pll_input, NCO_outputI, NCO_outputQ, IF_Fs, 114000, 0.5, 0, 0.001, pll_state, trigOffset); // <---- phase adjust?? phase adjust is for flipped polarity, 0 or -pi
		//auto endpll = std::chrono::high_resolution_clock::now();
		//auto endrecovery = std::chrono::high_resolution_clock::now();
		// ------------------------------------------- RDS CARRIER RECOVERY -------------------------------------------
		//auto startmix = std::chrono::high_resolution_clock::now();
		Imixed.resize(NCO_outputI.size());
		Qmixed.resize(NCO_outputQ.size());
		for (int i = 0; i < (int)Imixed.size(); i++) {
			Imixed[i] = NCO_outputI[i] * delayed_RDS[i]; // <---- confirm whether should be multiplied by constant like *2
			Qmixed[i] = NCO_outputQ[i] * delayed_RDS[i];
		}
		//auto endmix = std::chrono::high_resolution_clock::now();
		// Push data to queue
		RDS_Queue.produce(Imixed);

		//timing
		/*elapsedextraction += endextraction - startextraction;
		elapsedrecovery += endrecovery - startrecovery;
		elapsedextractionp = elapsedextraction / (block_id + 1);
		elapsedrecoveryp = elapsedrecovery / (block_id + 1);
		std::cerr << "Average time for RDS extraction: " << elapsedextractionp.count() << " s" << std::endl;
		std::cerr << "Average time for RDS recovery: " << elapsedrecoveryp.count() << " s" << std::endl;
		elapsedconv1 += endconv1 - startconv1;
		elapseddelay += enddelay - startdelay;
		elapsedsquare += endsquare - startsquare;
		elapsedconv2 += endconv2 - startconv2;
		elapsedpll += endpll - startpll;
		elapsedmix += endmix - startmix;
		elapsedconv1p = elapsedconv1 / (lastblock + 1);
		elapseddelayp = elapseddelay / (lastblock + 1);
		elapsedsquarep = elapsedsquare / (lastblock + 1);
		elapsedconv2p = elapsedconv2 / (lastblock + 1);
		elapsedpllp = elapsedpll / (lastblock + 1);
		elapsedmixp = elapsedmix / (lastblock + 1);

		//print filter timing
		std::cerr << "Time for LPF: " << elapsedLPF.count() << " s" << std::endl;
		std::cerr << "Time for BPF1: " << elapsedBPF1.count() << " s" << std::endl;
		std::cerr << "Time for BPF2: " << elapsedBPF2.count() << " s" << std::endl;
		//print processing timing
		std::cerr << "Average time for convolution1: " << elapsedconv1p.count() << " s" << std::endl;
		std::cerr << "Average time for delay: " << elapseddelayp.count() << " s" << std::endl;
		std::cerr << "Average time for squaring: " << elapsedsquarep.count() << " s" << std::endl;
		std::cerr << "Average time for convolution2: " << elapsedconv2p.count() << " s" << std::endl;
		std::cerr << "Average time for PLL: " << elapsedpllp.count() << " s" << std::endl;
		std::cerr << "Average time for mixing: " << elapsedmixp.count() << " s" << std::endl;
		lastblock += 1;*/
    }
	// Signal that no more data will be produced
    audio_Queue.stopProducing(); 
	RDS_Queue.stopProducing(); 
	
}

void monoAudioProcessing(Queue& queue, int D, int U, int AudioFs, int num_taps) {

    num_taps *= U;
    std::vector<float> h;
    std::vector<float> audio_state(num_taps - 1, 0);
	
    float Fc = std::min((AudioFs * (float)U / D) / 2, (float)16000);
	//auto startLPF = std::chrono::high_resolution_clock::now();
    impulseResponseLPF(AudioFs * U, Fc, num_taps, h, U);
	/*auto endLPF = std::chrono::high_resolution_clock::now();
	std::chrono::duration<double> elapsedLPF = endLPF - startLPF;
	std::chrono::duration<double> elapsedconv;
	std::chrono::duration<double> elapsedconvp;
	double lastblockid = 0;*/
	//std::chrono::duration<double> elapsedmono;
	//std::chrono::duration<double> elapsedmonop;
	//double lastblockid = 0;
    while (queue.isProducing()) {
		//auto startmono = std::chrono::high_resolution_clock::now();
		// ------------------------------------------- MONO PROCESSING -------------------------------------------

        std::vector<float> fmDemod = queue.consume();
        std::vector<float> audio_mono(fmDemod.size());

		// Resample data using specified U and D values
		//start time
		//auto startconv = std::chrono::high_resolution_clock::now();
        fastResampler(audio_mono, fmDemod, h, audio_state, D, U);
		//auto endconv = std::chrono::high_resolution_clock::now();
		// Convert to short int
        std::vector<short int> audio_data(audio_mono.size());
        for (unsigned int k = 0; k < audio_mono.size(); k++) {
            audio_data[k] = std::isnan(audio_mono[k]) ? 0 : static_cast<short int>(audio_mono[k] * 8192);
        }
        fwrite(&audio_data[0], sizeof(short int), audio_data.size(), stdout);
		//auto endmono = std::chrono::high_resolution_clock::now();
		/*elapsedconv += endconv - startconv;
		lastblockid += 1;
		elapsedconvp = elapsedconv / lastblockid;
		std::cerr << "Average time for convolution: " << elapsedconvp.count() << " s" << std::endl;
		std::cerr << "Time for filter generation: " << elapsedLPF.count() << " s" << std::endl;*/
		//elapsedmono += endmono - startmono;
		//lastblockid += 1;
		//elapsedmonop = elapsedmono / lastblockid;
		//std::cerr << "Average time for mono processing: " << elapsedmonop.count() << " s" << std::endl;
    }
}

void stereoAudioProcessing(Queue& queue, int D, int U, int IF_Fs, int num_taps, int block_size) {

	std::vector<float> block_data(block_size);
	float StereoFb = 22000;
	float StereoFe = 54000;
	float StereoSignalTone1 = 18500;
	float StereoSignalTone2 = 19500;

	std::vector<float> htone, hchannel, stereo_filter;
	std::vector<float> mono_state, pilot_state, channel_state;
	std::vector<float> audio_stereo(block_size);
	std::vector<float> stereo_carrier(block_size);
	std::vector<float> tone_stereo;
	int trigOffset = 0;
	float pll_state[5] = {0.0, 0.0, 1.0, 0.0, 1.0};

	std::vector<float> delay_state((num_taps-1)/2, 0);
	std::vector<short int> mono_data;
	std::vector<float> mono_data_delayed;

	std::vector<short int> out_data;

    std::vector<float> audio_state(num_taps - 1, 0);

    float Fc_Audio = std::min((IF_Fs * (float)U / D) / 2, (float)20000);

	/*auto startLPF = std::chrono::high_resolution_clock::now();
	auto endLPF = std::chrono::high_resolution_clock::now();
	std::chrono::duration<double> elapsedLPF = endLPF - startLPF;
	std::chrono::duration<double> elapsedconv;
	std::chrono::duration<double> elapsedconvp;
	
	*/
	//Pilot tone Filter
	//auto startBPF1 = std::chrono::high_resolution_clock::now();
	impulseResponseBPF(IF_Fs, StereoSignalTone1, StereoSignalTone2, num_taps, htone, 1);
	//auto endBPF1 = std::chrono::high_resolution_clock::now();

	// Channel Filter
	//auto startBPF2 = std::chrono::high_resolution_clock::now();			
	impulseResponseBPF(IF_Fs, StereoFb, StereoFe, num_taps, hchannel, 1);
	//auto endBPF2 = std::chrono::high_resolution_clock::now();

	// Audio Filter
	num_taps *= U;
	//auto startLPF = std::chrono::high_resolution_clock::now();
	impulseResponseLPF(IF_Fs*U, Fc_Audio, num_taps, stereo_filter, U);
	//auto endLPF = std::chrono::high_resolution_clock::now();

	/*std::chrono::duration<double> elapsedBPF1 = endBPF1 - startBPF1;
	std::chrono::duration<double> elapsedBPF2 = endBPF2 - startBPF2;
	std::chrono::duration<double> elapsedLPF = endLPF - startLPF;

	std::chrono::duration<double> elapsedconv1;
	std::chrono::duration<double> elapsedconv2;
	std::chrono::duration<double> elapsedconv3;
	std::chrono::duration<double> elapsedconv4;
	std::chrono::duration<double> elapseddelay;
	std::chrono::duration<double> elapsedcombine;
	std::chrono::duration<double> elapsedpll;

	std::chrono::duration<double> elapsedconv1p;
	std::chrono::duration<double> elapsedconv2p;
	std::chrono::duration<double> elapsedconv3p;
	std::chrono::duration<double> elapsedconv4p;
	std::chrono::duration<double> elapseddelayp;
	std::chrono::duration<double> elapsedcombinep;
	std::chrono::duration<double> elapsedpllp;


	
	double lastblockid = 0;
	std::chrono::duration<double> elapsedextract;
	std::chrono::duration<double> elapsedrecovery;
	std::chrono::duration<double> elapsedprocess;

	std::chrono::duration<double> elapsedextractp;
	std::chrono::duration<double> elapsedrecoveryp;
	std::chrono::duration<double> elapsedprocessp;

	double lastblockid = 0;*/

    while (queue.isProducing()) {
		
        std::vector<float> fmDemod = queue.consume();

		// ------------------------------------------- STEREO EXTRACTION -------------------------------------------

		// Extract Channel
		//auto startextract = std::chrono::high_resolution_clock::now();
		fastResampler(audio_stereo, fmDemod, hchannel, channel_state, 1, 1);
		//auto endextract = std::chrono::high_resolution_clock::now();
		// Extract Pilot Tone
		//auto startrecovery = std::chrono::high_resolution_clock::now();
		fastResampler(tone_stereo, fmDemod, htone, pilot_state, 1, 1);
		//auto endconv2 = std::chrono::high_resolution_clock::now();

		//auto startpll = std::chrono::high_resolution_clock::now();
		PLL(tone_stereo, stereo_carrier, IF_Fs, 19000, 2, 0, 0.01, pll_state, trigOffset);				
		//auto endrecovery = std::chrono::high_resolution_clock::now();

		// Mixer to recover from dsb-sc
		//auto startprocess = std::chrono::high_resolution_clock::now();
		for (unsigned int i = 0; i < audio_stereo.size(); i++) {
			audio_stereo[i] = audio_stereo[i] * stereo_carrier[i];
		}

		std::vector<float> stereo_filtered;
		//auto startconv3 = std::chrono::high_resolution_clock::now();
		fastResampler(stereo_filtered, audio_stereo, stereo_filter, audio_state, D, U);
		//auto endconv3 = std::chrono::high_resolution_clock::now();

		for(unsigned int k=0; k<stereo_filtered.size(); k++){
			if(std::isnan(stereo_filtered[k])) stereo_filtered[k]=0;						
			else stereo_filtered[k] = static_cast<short int>(stereo_filtered[k]*8192);
		}

		// ------------------------------------------- MONO PROCESSING -------------------------------------------
		//auto startdelay = std::chrono::high_resolution_clock::now();
		delayBlock(fmDemod, delay_state, mono_data_delayed);
		//auto enddelay = std::chrono::high_resolution_clock::now();

		std::vector<float> mono_data(block_size);
		//auto startconv4 = std::chrono::high_resolution_clock::now();
		fastResampler(mono_data, mono_data_delayed, stereo_filter, mono_state, D, U);
		//auto endconv4 = std::chrono::high_resolution_clock::now();	

		for(unsigned int k=0; k<mono_data.size(); k++){
			if(std::isnan(mono_data[k])) mono_data[k]=0;						
			else mono_data[k] = static_cast<short int>(mono_data[k]*8192);
		}	

		// ------------------------------------------- COMBINING -------------------------------------------

		out_data.resize(mono_data.size() * 2);

		// Calculate left and right and interweave
		//auto startcombine = std::chrono::high_resolution_clock::now();
		for(unsigned int i=0; i < mono_data.size(); i++){ 
			out_data[i*2] = mono_data[i] + 2*stereo_filtered[i];
			out_data[(i*2)+1] = mono_data[i] - 2*stereo_filtered[i];
		}
		//auto endcombine = std::chrono::high_resolution_clock::now();
		
		fwrite(&out_data[0], sizeof(short int), out_data.size(), stdout);
		//auto endprocess = std::chrono::high_resolution_clock::now();
		/*elapsedconv1 += endconv1 - startconv1;
		elapsedconv2 += endconv2 - startconv2;
		elapsedpll += endpll - startpll;
		elapsedconv3 += endconv3 - startconv3;
		elapseddelay += enddelay - startdelay;
		elapsedconv4 += endconv4 - startconv4;
		elapsedcombine += endcombine - startcombine;

		lastblockid += 1;
		elapsedconv1p = elapsedconv1 / lastblockid;
		elapsedconv2p = elapsedconv2 / lastblockid;
		elapsedpllp = elapsedpll / lastblockid;
		elapsedconv3p = elapsedconv3 / lastblockid;
		elapseddelayp = elapseddelay / lastblockid;
		elapsedconv4p = elapsedconv4 / lastblockid;
		elapsedcombinep = elapsedcombine / lastblockid;

		std::cerr << "Time for BPF Pilot Tone: " << elapsedBPF1.count() << " s" << std::endl;
		std::cerr << "Time for BPF Channel: " << elapsedBPF2.count() << " s" << std::endl;
		std::cerr << "Time for LPF Audio: " << elapsedLPF.count() << " s" << std::endl;
		std::cerr << "Average time for convolution (Channel): " << elapsedconv1p.count() << " s" << std::endl;
		std::cerr << "Average time for convolution (Pilot Tone): " << elapsedconv2p.count() << " s" << std::endl;
		std::cerr << "Average time for PLL: " << elapsedpllp.count() << " s" << std::endl;
		std::cerr << "Average time for convolution (Audio): " << elapsedconv3p.count() << " s" << std::endl;
		std::cerr << "Average time for delay: " << elapseddelayp.count() << " s" << std::endl;
		std::cerr << "Average time for convolution (Mono): " << elapsedconv4p.count() << " s" << std::endl;
		std::cerr << "Average time for combining: " << elapsedcombinep.count() << " s" << std::endl;

		elapsedextract += endextract - startextract;
		elapsedrecovery += endrecovery - startrecovery;
		elapsedprocess += endprocess - startprocess;
		lastblockid += 1;
		elapsedextractp = elapsedextract / lastblockid;
		elapsedrecoveryp = elapsedrecovery / lastblockid;
		elapsedprocessp = elapsedprocess / lastblockid;

		std::cerr << "Average time for stereo extraction: " << elapsedextractp.count() << " s" << std::endl;
		std::cerr << "Average time for stereo recovery: " << elapsedrecoveryp.count() << " s" << std::endl;
		std::cerr << "Average time for stereo processing: " << elapsedprocessp.count() << " s" << std::endl;*/

    }
}

void RDS(Queue& queue, int Fs, int num_taps, int SPS, int U, int D){
	
	float RDSFs = SPS * 2375;
	std::vector<float> lowpass_state_I;
	std::vector<float> lowpass_state_Q;
	std::vector<float> RRC_state_I;
	std::vector<float> RRC_state_Q;
	int clock_offset = 0;
	int block_id = 0;

	/*auto startLPF = std::chrono::high_resolution_clock::now();
	auto endLPF = std::chrono::high_resolution_clock::now();
	std::chrono::duration<double> elapsedLPF = endLPF - startLPF;
	std::chrono::duration<double> elapsedconv;
	std::chrono::duration<double> elapsedconvp;
	
	*/

	// Low-pass 3kHz
	std::vector<float> hLow;
	//auto startLPF = std::chrono::high_resolution_clock::now();
	impulseResponseLPF(Fs*U, 3000, num_taps*U, hLow, U);
	//auto endLPF = std::chrono::high_resolution_clock::now();

	// Root-raised cosine filter
	std::vector<float> hRRC;
	//auto startrrcf = std::chrono::high_resolution_clock::now();
	impulseResponseRootRaisedCosine(RDSFs, num_taps, hRRC);
	//auto endrrcf = std::chrono::high_resolution_clock::now();

	/*std::chrono::duration<double> elapsedLPF = endLPF - startLPF;
	std::chrono::duration<double> elapsedrrcf = endrrcf - startrrcf;
	std::chrono::duration<double> elapsedconv1;
	std::chrono::duration<double> elapsedconv1p;
	std::chrono::duration<double> elapsedconv2;
	std::chrono::duration<double> elapsedconv2p;
	std::chrono::duration<double> elapsedcdr;
	std::chrono::duration<double> elapsedcdrp;
	std::chrono::duration<double> elapsedmanchester;
	std::chrono::duration<double> elapsedmanchesterp;
	std::chrono::duration<double> elapseddiff;
	std::chrono::duration<double> elapseddiffp;
	std::chrono::duration<double> elapsedframe;
	std::chrono::duration<double> elapsedframep;

	std::chrono::duration<double> elapseddemod;
	std::chrono::duration<double> elapsedprocess;
	std::chrono::duration<double> elapseddemodp;
	std::chrono::duration<double> elapsedprocessp;*/

	while (queue.isProducing()) {

		std::vector<float> Imixed = queue.consume();

		//auto startdemod = std::chrono::high_resolution_clock::now();
		// ------------------------------------------- RDS DEMODULATION ------------------------------------------

		// Low-pass 3kHz
		std::vector<float> RDS_lowpass_out_I;
		// Rational resampler (and low-pass filter)
		//auto startconv1 = std::chrono::high_resolution_clock::now();
		fastResampler(RDS_lowpass_out_I, Imixed, hLow, lowpass_state_I, D, U);
		//auto endconv1 = std::chrono::high_resolution_clock::now();
		// Root-raised cosine filter
		std::vector<float> RRC_output_I;
		//auto startconv2 = std::chrono::high_resolution_clock::now();
		fastResampler(RRC_output_I, RDS_lowpass_out_I, hRRC, RRC_state_I, 1, 1);
		//auto endconv2 = std::chrono::high_resolution_clock::now();
		//auto enddemod = std::chrono::high_resolution_clock::now();
		// ------------------------------------------- RDS DATA PROCESSING -------------------------------------------
		//auto startprocess = std::chrono::high_resolution_clock::now();
		// Clock and Data Recovery
		//auto startcdr = std::chrono::high_resolution_clock::now();
		if (block_id == 0) { // First block
			float max = -1.0;
			for (int i = 0; i < SPS; i++) {
				if(std::abs(RRC_output_I[i+8*SPS]) > max) { // Chose an offset 7*SPS cause weird start
					clock_offset = i;
					max = std::abs(RRC_output_I[i+8*SPS]);
				}
			}
		}
		
		// Data Recovery
		std::vector<int> symbolsI;
		// Extract symbols
		for (int i = clock_offset; i+SPS < (int)RRC_output_I.size(); i+=SPS) {
			// Skip first block
			if (block_id == 0 && i < clock_offset+7*SPS)
				continue;
			symbolsI.push_back((RRC_output_I[i] > 0) ? 1 : 0);
		}
		//auto endcdr = std::chrono::high_resolution_clock::now();
		
		// Manchester Decoding
		std::vector<int> manchester_decoded;
		//auto startmanchester = std::chrono::high_resolution_clock::now();
		for (int i = 0; i < (int) symbolsI.size(); i += 2) {
			if (i + 1 < (int) symbolsI.size()) { 
				int current = symbolsI[i];
				int next = symbolsI[i + 1];

				if (current == 0 && next == 1) { 		// LO -> HI = 0
					manchester_decoded.push_back(0);
				} else if (current == 1 && next == 0) { // HI -> LO = 1
					manchester_decoded.push_back(1);
				} else { // If an error is detected, adjust phase
					i--;
				}
			}
		}
		//auto endmanchester = std::chrono::high_resolution_clock::now();

		// Differential Decoding
		std::vector<int> differential_decoded(manchester_decoded.size());
		//auto startdiff = std::chrono::high_resolution_clock::now();
		differential_decoded[0] = manchester_decoded[0]; 
		for (int i = 1; i < (int) manchester_decoded.size(); ++i) {
			differential_decoded[i] = (manchester_decoded[i] ^ manchester_decoded[i - 1]); // XOR
		}
		//auto enddiff = std::chrono::high_resolution_clock::now();

		// Frame Syncronization and Error Detection
		//auto startframe = std::chrono::high_resolution_clock::now();
		for (int i = 0; i < (int) differential_decoded.size(); i++) {
			std::vector<int>send;
			for (int j = 0; j < 26; j++) {
				send.push_back(differential_decoded[i+j]);
			}

			// Parity check matrix
			std::vector<int> parity_check_result = multiply_with_parity_check(send, parity_check_matrix);
			for (int j = 0; j < (int) parity_check_result.size(); j++) {
			}

			// Check for syndrome match
			bool match = true;
			for (int j = 0; j < (int) syndrome_words.size(); j++) {
				std::string syndrome = syndrome_words[j];
				for (int k = 0; k < (int) syndrome.size(); k++) {
					if (syndrome[k] - '0' != parity_check_result[k]) {
						match = false;
						break;
					}
				}
				if (match) {
					std::cerr << "Match found for syndrome word: " << syndrome_type[j] << ". At index: " << i << std::endl;
					break;
				}
			}
		}
		//auto endprocess = std::chrono::high_resolution_clock::now();
		//auto endframe = std::chrono::high_resolution_clock::now();
		
		//timing
		/*elapseddemod += enddemod - startdemod;
		elapsedprocess += endprocess - startprocess;
		elapseddemodp = elapseddemod / block_id;
		elapsedprocessp = elapsedprocess / block_id;
		std::cerr << "Average time for RDS demodulation: " << elapseddemodp.count() << " s" << std::endl;
		std::cerr << "Average time for RDS processing: " << elapsedprocessp.count() << " s" << std::endl;
		block_id++;
		//timing
		elapsedconv1 += endconv1 - startconv1;
		elapsedconv2 += endconv2 - startconv2;
		elapsedcdr += endcdr - startcdr;
		elapsedmanchester += endmanchester - startmanchester;
		elapseddiff += enddiff - startdiff;
		elapsedframe += endframe - startframe;

		elapsedconv1p = elapsedconv1 / block_id;
		elapsedconv2p = elapsedconv2 / block_id;
		elapsedcdrp = elapsedcdr / block_id;
		elapsedmanchesterp = elapsedmanchester / block_id;
		elapseddiffp = elapseddiff / block_id;
		elapsedframep = elapsedframe / block_id;

		//printing
		std::cerr << "Time for Lowpass filter Generation " << elapsedLPF.count() << " s" << std::endl;
		std::cerr << "Time for RRC filter Generation " << elapsedrrcf.count() << " s" << std::endl;
		std::cerr << "Average time for convolution (Lowpass): " << elapsedconv1p.count() << " s" << std::endl;
		std::cerr << "Average time for convolution (RRC): " << elapsedconv2p.count() << " s" << std::endl;
		std::cerr << "Average time for CDR: " << elapsedcdrp.count() << " s" << std::endl;
		std::cerr << "Average time for Manchester Decoding: " << elapsedmanchesterp.count() << " s" << std::endl;
		std::cerr << "Average time for Differential Decoding: " << elapseddiffp.count() << " s" << std::endl;
		std::cerr << "Average time for Frame Syncronization: " << elapsedframep.count() << " s" << std::endl;*/
	}
}