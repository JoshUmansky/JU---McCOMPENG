#include <fstream>
#include <iostream>
#include <vector>
#include <string>
#include <sstream>

std::vector<std::vector<int>> parity_check_matrix = {
    {1,0,0,0,0,0,0,0,0,0},
    {0,1,0,0,0,0,0,0,0,0},
    {0,0,1,0,0,0,0,0,0,0},
    {0,0,0,1,0,0,0,0,0,0},
    {0,0,0,0,1,0,0,0,0,0},
    {0,0,0,0,0,1,0,0,0,0},
    {0,0,0,0,0,0,1,0,0,0},
    {0,0,0,0,0,0,0,1,0,0},
    {0,0,0,0,0,0,0,0,1,0},
    {0,0,0,0,0,0,0,0,0,1},
    {1,0,1,1,0,1,1,1,0,0},
    {0,1,0,1,1,0,1,1,1,0},
    {0,0,1,0,1,1,0,1,1,1},
    {1,0,1,0,0,0,0,1,1,1},
    {1,1,1,0,0,1,1,1,1,1},
    {1,1,0,0,0,1,0,0,1,1},
    {1,1,0,1,0,1,0,1,0,1},
    {1,1,0,1,1,1,0,1,1,0},
    {0,1,1,0,1,1,1,0,1,1},
    {1,0,0,0,0,0,0,0,0,1},
    {1,1,1,1,0,1,1,1,0,0},
    {0,1,1,1,1,0,1,1,1,0},
    {0,0,1,1,1,1,0,1,1,1},
    {1,0,1,0,1,0,0,1,1,1},
    {1,1,1,0,0,0,1,1,1,1},
    {1,1,0,0,0,1,1,0,1,1}
};

std::vector<std::string> syndrome_words = {
    "1111011000",
    "1111010100",
    "1001011100",
    "1111010100",
    "1001011000"
};

std::vector<std::string> syndrome_type = {
    "A",
    "B",
    "C",
    "C'",
    "D"
};

// Adjusted function to multiply a 1x26 vector by a 26x10 matrix
// The resulting vector will be of size 1x10
std::vector<int> multiply_with_parity_check(const std::vector<int>& bit_vector, const std::vector<std::vector<int>>& matrix) {
    std::vector<int> result(matrix[0].size(), 0); // Initialize the result vector with zeros based on the number of columns
    for (size_t j = 0; j < matrix[0].size(); ++j) { // Iterate over columns of the matrix
        for (size_t i = 0; i < matrix.size(); ++i) { // Iterate over rows of the matrix
            result[j] ^= bit_vector[i] & matrix[i][j]; // Perform bitwise multiplication and XOR
        }
    }
    return result;
}

int main() {
    std::ifstream inputFile("input_data.dat");
    if (!inputFile) {
        std::cerr << "Failed to open file." << std::endl;
        return 1;
    }

    // Skip the header
    std::string header;
    std::getline(inputFile, header);

    std::vector<double> x_axis;
    std::vector<double> y_axis;

    std::string line;
    while (std::getline(inputFile, line)) {
        std::istringstream iss(line);
        double x, y;
        if (!(iss >> x >> y)) { break; } // error

        x_axis.push_back(x);
        y_axis.push_back(y);
    }

    inputFile.close();

    std::vector<int> bits;

    // Output the read values to check
    for(size_t i = 0; i < x_axis.size(); ++i) {
        if (y_axis[i] > 0) {
            bits.push_back(1);
        } else {
            bits.push_back(0);
        }
    }

    std::vector<int> manchester_decoded;

    // Perform Manchester & Differential Decoding///////////////////////////////////////
    for (int i = 0; i < bits.size(); i += 2) {
        if (i + 1 < bits.size()) { 
            int current = bits[i];
            int next = bits[i + 1];

            if (current == 0 && next == 1) {
                manchester_decoded.push_back(0);
            } else if (current == 1 && next == 0) {
                manchester_decoded.push_back(1);
            }
        }
    }

    // output the manchester decoded bits
    for (int i = 0; i < manchester_decoded.size(); i++) {
        std::cerr << manchester_decoded[i];
    }



    // This vector will store the final result after differential decoding
    std::vector<int> differential_decoded;
    differential_decoded.push_back(manchester_decoded[0]); // The first element is used as is
    // Perform Differential decoding based on the Manchester result
    for (int i = 1; i < manchester_decoded.size(); ++i) {
        differential_decoded.push_back(manchester_decoded[i] ^ manchester_decoded[i - 1]);
    }
    
    // Perform parity check
    for (int i = 0; i < differential_decoded.size(); i++) {
        std::vector<int>send;
        for (int j = 0; j < 26; j++) {
            send.push_back(differential_decoded[i+j]);
        }
        std::vector<int> parity_check_result = multiply_with_parity_check(send, parity_check_matrix);
        for (int j = 0; j < parity_check_result.size(); j++) {
            //std::cerr << parity_check_result[j];
        }

        bool match = true;
        for (int j = 0; j < syndrome_words.size(); j++) {
            std::string syndrome = syndrome_words[j];
            for (int k = 0; k < syndrome.size(); k++) {
                if (syndrome[k] - '0' != parity_check_result[k]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                std::cerr << "Match found for syndrome type " << syndrome_type[j] << std::endl;
                break;
            }
        }

        if (!match) {
            //std::cerr << "No match found for any syndrome type" << std::endl;
        }

        
    }
    



    inputFile.close();
    return 0;
}