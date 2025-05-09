/*
Comp Eng 3DY4 (Computer Systems Integration Project)

Copyright by Nicola Nicolici
Department of Electrical and Computer Engineering
McMaster University
Ontario, Canada
*/

#include <iostream>
#include <fstream>
#include <vector>
#include <iomanip>
#include <complex>
#include <cmath>
#include <cstdlib> // For system()


#define PI 3.14159265358979323846

// function for DFT (reused from previous experiment)
void DFT(const std::vector<float> &x, std::vector<std::complex<float>> &Xf)
{
	Xf.clear();
	Xf.resize(x.size(), std::complex<float>(0));
	for (int m = 0; m < (int)Xf.size(); m++) {
		for (int k = 0; k < (int)x.size(); k++) {
				std::complex<float> expval(0, -2*PI*(k*m) / x.size());
				Xf[m] += x[k] * std::exp(expval);
		}
	}
}
// idft function
void IDFT(const std::vector<std::complex<float>> &Xf, std::vector<float> &x) {

	// remove all the elements in the x vector before resizing
	x.clear();

	// allocate space for the vector holding the frequency bins
	// initialize all the values in the frequency vector to complex 0
	// only new elements are init (hence it is safe to clear before resizing)
	x.resize(Xf.size());

	for (int k = 0; k < (int)x.size(); k++) {
		for (int m = 0; m < (int)Xf.size(); m++) {
				// below is the declaration of a complex variable
				// that is initialized through its constructor (real, imag)
				std::complex<float> expval(0, 2*PI*(k*m) / Xf.size());
				// accumulate partial products to frequency bin "m"
				x[k] += std::real(Xf[m] * std::exp(expval));
		}
		x[k] /= Xf.size();
	}
}

// function to generate a sine with Fs samples per second (scaled by interval duration)
void generateSin(std::vector<float> &t, std::vector<float> &x, float Fs, float interval, float frequency, float amplitude, float phase)
{
	// we do NOT allocate memory space explicitly
	// for the time (t) vector and sample (x) vector
	t.clear(); x.clear();
	float dt = 1/Fs;
	for (float i = 0.0; i < interval; i += dt) {
		// vector size increases when pushing new elements into it
		t.push_back(i);
		x.push_back(amplitude*std::sin(2*PI*frequency*i+phase));
	}
}

// function to add an array of sines
void addSin(const std::vector<std::vector<float>> &sv, std::vector<float> &added)
{
	// assumes at least one sine passed to this function
	// assumes all input sines are of the same size
	for (int i = 0; i < (int)sv[0].size(); i ++) {
		float addval = 0.0;
		// note: sv.size() returns the number of sines (or rows in 2D repr)
		// sv[0].size() returns the number of samples in a sine (or cols in 2D repr)
		for (int k = 0; k < (int)sv.size(); k++)
			addval += sv[k][i];
		added.push_back(addval);
	}
}

// function to multiply an array of sines
void multiSin(const std::vector<std::vector<float>> &sv, std::vector<float> &added)
{
	// assumes at least one sine passed to this function
	// assumes all input sines are of the same size
	for (int i = 0; i < (int)sv[0].size(); i ++) {
		float addval = 1.0;
		// note: sv.size() returns the number of sines (or rows in 2D repr)
		// sv[0].size() returns the number of samples in a sine (or cols in 2D repr)
		for (int k = 0; k < (int)sv.size(); k++)
			addval *= sv[k][i];
		added.push_back(addval);
	}
}

// function to print a real vector (reused from previous experiment)
void printRealVector(const std::vector<float> &x)
{
	std::cout << "Printing float vector of size " << x.size() << "\n";
	for (int i = 0; i < (int)x.size(); i++)
		std::cout << x[i] << " ";
	std::cout << "\n";
}

// function to print a complex vector (reused from previous experiment)
void printComplexVector(const std::vector<std::complex<float>> &X)
{
	std::cout << "Printing complex vector of size " << X.size() << "\n";
	for (int i = 0; i < (int)X.size(); i++)
		std::cout << X[i] << " ";
	std::cout << "\n";
}

// function to record data in a format to be read by GNU plot
// the arguments are VERY specific to this usage in this experiment
// we have the time vector (t), a vector of sines (sv),
// input samples (x, i.e., added sines for this experiment),
// output samples (y, filtered input samples)
// frequency vectors (both Xf and Yf)
// note: the reference code does NOT do filtering, hence y and Yf are zeros by default

void plotaddedSinesSpectrum(const std::vector<float> &t, const std::vector<std::vector<float>> &sv, const std::vector<float> &x, const std::vector<float> &y, const std::vector<std::complex<float>> &Xf, const std::vector<std::complex<float>> &Yf)
{
	// write data in text format to be parsed by GNU plot
	const std::string filename = "../data/example.dat";
	std::fstream fd;  // file descriptor
	fd.open(filename, std::ios::out);
	fd << "#\tindex\tsine(0)\tsine(1)\tdata in\tdata out\tspectrum in\tspectrum out\n";
	for (int i = 0; i < (int)t.size(); i++) {
		fd << "\t " << i << "\t";
		for (int k = 0; k < (int)sv.size(); k++)
			fd << std::fixed << std::setprecision(3) << sv[k][i] << "\t ";
		fd << x[i] << "\t "<< y[i] << "\t\t ";
		fd << std::abs(Xf[i])/Xf.size() << "\t\t " << std::abs(Yf[i])/Yf.size() <<"\n";
	}
	std::cout << "Generated " << filename << " to be used by GNU plot\n";
	fd.close();
}

// function to compute the impulse response "h" based on the sinc function
// see pseudocode from previous lab that was implemented in Python
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

// function to compute the filtered output "y" by doing the convolution
// of the input data "x" with the impulse response "h"; this is based on
// your Python code from the take-home exercise from the previous lab
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

int main()
{

	float Fs = 512.0;                    // samples per second
	float interval = 1.0;                // number of seconds
	unsigned short int num_taps = 51;   // number of filter taps
	                    // cutoff frequency (in Hz)

	// declare a vector of vectors for multiple sines
	std::vector<std::vector<float>> sv;
	// declare time and sine vectors
	std::vector<float> t, sine;
	// note: there is no explicit memory allocation through vector resizing
	// vector memory space will increase via the push_back method
	//void generateSin(std::vector<float> &t, std::vector<float> &x, float Fs, float interval, float frequency = 7.0, float amplitude = 5.0, float phase = 0.0)
	// generate and store the first tone
	// check the function to understand the order of arguments
	float freq1 = 10 + (rand()%81);
	float freq2 = 10 + (rand()%81);
	
	float amp1 = 2 + (rand()% 3);
	float amp2 = 2 + (rand()% 3);
	
	float Fc = std::max(freq1,freq2); 
	
	generateSin(t, sine, Fs, interval, freq1, amp1, 0.0);
	sv.push_back(sine);
	// generate and store the second tone
	generateSin(t, sine, Fs, interval, freq2, amp2, 0.0);
	sv.push_back(sine);

	// declare the added sine vector and add the three tones
	std::vector<float> x;
	multiSin(sv, x);
	//printRealVector(x);

	// declare a vector of complex values for DFT; no memory is allocated for it
	std::vector<std::complex<float>> Xf;
	DFT(x, Xf);
	// printComplexVector(Xf);

	// generate the impulse response h
	// convolve it with the input data x
	// in order to produce the output data y
	std::vector<float> h;              // impulse response
	impulseResponseLPF(Fs, Fc, num_taps, h);
	std::vector<float> y;              // filter out
	convolveFIR(y, x, h);

	// compute DFT of the filtered data
	std::vector<std::complex<float>> Yf;
	DFT(y, Yf);

	// prepare the data for GNU plot
	plotaddedSinesSpectrum(t, sv, x, y, Xf, Yf);

	// naturally, you can comment the line below once you are comfortable to run GNU plot
	std::cout << "Running: gnuplot -e 'set terminal png size 1024,768' ../data/example.gnuplot > ../data/example.png\n";
	std::cout << "Freq1: " << freq1 << "\n";
	std::cout << "Freq2: " << freq2 << "\n";
	system("gnuplot -e 'set terminal png size 1024,768' ../data/example.gnuplot > ../data/example.png");
	system("xdg-open ../data/example.png");

	return 0;
}
