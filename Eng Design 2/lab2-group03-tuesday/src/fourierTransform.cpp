/*
Comp Eng 3DY4 (Computer Systems Integration Project)

Copyright by Nicola Nicolici
Department of Electrical and Computer Engineering
McMaster University
Ontario, Canada
*/

#include <iostream>
#include <vector>
#include <complex>
#include <cmath>
#include <chrono>

#define PI 3.14159265358979323846

// input x is qualified as const in order not to be changed within the function
// although C++ is case sensitive it is "safer" to use Xf instead of X in order to avoid confusion
void DFT(const std::vector<float> &x, std::vector<std::complex<float>> &Xf) {

	// remove all the elements in the vector container before resizing
	Xf.clear();
	// allocate space for the vector holding the frequency bins
	// initialize all the values in the frequency vector to complex 0
	// only new elements are init (hence it is safe to clear before resizing)
	Xf.resize(x.size(), std::complex<float>(0));
	for (int m = 0; m < (int)Xf.size(); m++) {
		for (int k = 0; k < (int)x.size(); k++) {
				// below is the declaration of a complex variable
				// that is initialized through its constructor (real, imag)
				std::complex<float> expval(0, -2*PI*(k*m) / x.size());
				// accumulate partial products to frequency bin "m"
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


// function to generate N random float values
// x is the input/output vector
// N is the number of samples
// random values are between -max and max
// precision should be capped to 3 (in the context of this type of experiments)
void generateRandomSamples(std::vector<float> &x, unsigned int N, unsigned short int max, unsigned char precision)
{
	// allocate space for the vector with random values
	x.clear(); x.resize(N);
	int int_radom_max = 2*(max * int(pow(10,precision)));
	for (int i = 0; i < (int)x.size(); i++) {
		x[i] = int(std::rand() % int_radom_max);
		x[i] = (x[i] - (int_radom_max/2))/pow(10,precision);
	}
}

// function to print a real vector
void printRealVector(const std::vector<float> &x)
{
	std::cout << "Printing float vector of size " << x.size() << "\n";
	for (int i = 0; i < (int)x.size(); i++)
		std::cout << x[i] << " ";

	// starting from C++11 you can write
	// for (auto elem : x)
	//   std::cout << elem << " ";

	std::cout << "\n";
}

// function to print a complex vector
void printComplexVector(const std::vector<std::complex<float>> &X)
{
	std::cout << "Printing complex vector of size " << X.size() << "\n";
	for (int i = 0; i < (int)X.size(); i++)
		std::cout << X[i] << " ";
	std::cout << "\n";
}

int main()
{
	// initialize the seed for the random number generator using current time
	int seed = std::time(0x0);
	std::cout << "Starting from seed " << std::hex << seed << std::dec << "\n";

	// declare a vector of real values; no memory is allocated at this time
	std::vector<float> x;
	std::vector<float> x2;
	// generate 32 samples between -10 and 10; for extra flexibility
	// the last argument gives precision in terms of fraction digits
	// note: memory for x is allocated within the function called below
	generateRandomSamples(x, 1024, 10, 2);
	// print a vector of real numbers
	//printRealVector(x);

	// declare a vector of complex values; no memory is allocated for it
	std::vector<std::complex<float>> Xf;
	// perform DFT of x to produce Xf
	// we measure the execution time using the "chrono" class
	auto start_time = std::chrono::high_resolution_clock::now();
	DFT(x, Xf);

	// "auto" keyword is used for type deduction (or inference) in C++
	auto stop_time = std::chrono::high_resolution_clock::now();
	
	auto start_time1 = std::chrono::high_resolution_clock::now();
	IDFT(Xf, x2);
	auto stop_time1 = std::chrono::high_resolution_clock::now();
	std::chrono::duration<double, std::milli> DFT_run_time = stop_time-start_time;
	std::chrono::duration<double, std::milli> IDFT_run_time = stop_time1-start_time1;
	// print a vector of complex numbers
	// printComplexVector(Xf);
	std::cout << "DFT ran for " << DFT_run_time.count() << " milliseconds" << "\n";
	std::cout << "IDFT ran for " << IDFT_run_time.count() << " milliseconds" << "\n";


	//printRealVector(x);
	//printRealVector(x2);

	int error = 0;

	// Ensure that x2 is within 0.1% of x
	for (int i = 0; i < (int)x.size(); i++) {
		if (abs(x[i] - x2[i]) > 0.01) {
			error = 1;
			// print the abs difference between the two vectors
			std::cout << "Error at index " << i << " with value " << abs(x[i] - x2[i]) << "\n";
		}
	}

	if (error == 0) {
		std::cout << "IDFT ran successfully" << "\n";
	} else {
		std::cout << "IDFT failed" << "\n";
	}


	// finished!
	return 0;
}
