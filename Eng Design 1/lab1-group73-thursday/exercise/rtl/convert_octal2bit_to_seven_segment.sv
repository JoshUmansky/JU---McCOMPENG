/*
Copyright by Henry Ko and Nicola Nicolici
Department of Electrical and Computer Engineering
McMaster University
Ontario, Canada
*/

`timescale 1ns/100ps
`ifndef DISABLE_DEFAULT_NET
`default_nettype none
`endif

// This module is to convert a 3 bits octal number into a 7-bit value for 7-segment display
module convert_octal2bit_to_seven_segment (
	input logic [1:0] octal_value,
	output logic [6:0] converted_value
);

always_comb begin
	case(octal_value)
		4'o1: converted_value = 7'b1111001;
		4'o2: converted_value = 7'b0100100;
		4'o3: converted_value = 7'b0110000;
		4'o0: converted_value = 7'b1000000;
	endcase
end
	
endmodule
