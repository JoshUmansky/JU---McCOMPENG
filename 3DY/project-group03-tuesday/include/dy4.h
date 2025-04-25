/*
Comp Eng 3DY4 (Computer Systems Integration Project)

Department of Electrical and Computer Engineering
McMaster University
Ontario, Canada
*/

#ifndef DY4_DY4_H
#define DY4_DY4_H
#include <vector>
#include <string>

// some general and reusable stuff
// our beloved PI constant
#define PI 3.14159265358979323846

// although we use DFT (no FFT ... yet), the number of points for a
// Fourier transform is defined as NFFT (same as matplotlib)
#define NFFT 512
#define BLOCK_SIZEM0 102400 //1024 * 10 * 5 * 2
#define BLOCK_SIZEM1 73728 // 1024 * 9 * 4 * 2
#define BLOCK_SIZEM2 160000 // 10 * 10 * 800 * 2
#define BLOCK_SIZEM3 192000 // 10 * 10 * 960 * 2

#endif // DY4_DY4_H
