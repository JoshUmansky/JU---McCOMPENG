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

module SRAM_BIST (
	input logic Clock,
	input logic Resetn,
	input logic BIST_start,
	
	output logic [17:0] BIST_address,
	output logic [15:0] BIST_write_data,
	output logic BIST_we_n,
	input logic [15:0] BIST_read_data,
	
	output logic BIST_finish,
	output logic BIST_mismatch
);

enum logic [3:0] {
	S_IDLE,
	S_DELAY_1_EVEN,
	S_DELAY_2_EVEN,
	S_WRITE_CYCLE_EVEN,
	S_READ_CYCLE_EVEN,
	S_DELAY_3_EVEN,
	S_DELAY_4_EVEN,
	S_DELAY_1_ODD,
	S_DELAY_2_ODD,
	S_WRITE_CYCLE_ODD,
	S_READ_CYCLE_ODD,
	S_DELAY_3_ODD,
	S_DELAY_4_ODD,
	S_END
} BIST_state;

logic BIST_start_buf;
logic [15:0] BIST_expected_data;

// write the 16 least significant bits of the address bus in each memory location
// 
// NOTE: this particular BACKGROUND pattern is specific to this BIST implementation
assign BIST_write_data[15:0] = BIST_address[15:0];

// based on the way how this particular BIST engine is implemented,
// the BIST expected data can be computed on-the-fly by
// decrementing the 16 least significant bits of the address 
//
// NOTE: the expected data must change if the memory is traversed in a different way
assign BIST_expected_data[15:0] = BIST_address[15:0]+16'd4;


always_ff @ (posedge Clock or negedge Resetn) begin
	if (Resetn == 1'b0) begin
		BIST_state <= S_IDLE;
		BIST_mismatch <= 1'b0;
		BIST_finish <= 1'b0;
		BIST_address <= 18'd0;
		BIST_we_n <= 1'b1;		
		BIST_start_buf <= 1'b0;
	end else begin
		BIST_start_buf <= BIST_start;
		
		case (BIST_state)
		S_IDLE: begin
			if (BIST_start & ~BIST_start_buf) begin
				// start the BIST engine
				BIST_address <= 18'd0;
				BIST_we_n <= 1'b0; // initiate first WRITE
				BIST_mismatch <= 1'b0;
				BIST_finish <= 1'b0;
				BIST_state <= S_WRITE_CYCLE_EVEN;
			end else begin
				BIST_address <= 18'd0;
				BIST_we_n <= 1'b1;
				BIST_finish <= 1'b1;				
			end
		end
		// a couple of delay states to initiate the first WRITE and first READ
		S_WRITE_CYCLE_EVEN: begin
			BIST_address <= BIST_address + 18'd2;
			if(BIST_address == 18'h3FFFE) begin
				BIST_we_n <= 1'b1;
				BIST_address <= 18'h3FFFE;
				BIST_state <= S_DELAY_1_EVEN;
				end
		end
		S_DELAY_1_EVEN: begin
			BIST_address <= BIST_address - 18'd2;
			BIST_state <= S_DELAY_2_EVEN;
		end
		S_DELAY_2_EVEN: begin
			BIST_address <= BIST_address - 18'd2;
			BIST_state <= S_READ_CYCLE_EVEN;
		end
		S_READ_CYCLE_EVEN: begin
			// complete the READ initiated two clock cycles earlier and perform comparison
			if (BIST_read_data != BIST_expected_data) 
				BIST_mismatch <= 1'b1;
			BIST_address <= BIST_address - 18'd2;
			if (BIST_address == 18'h0) begin				
				BIST_state <= S_DELAY_3_EVEN;
			end
		end
		S_DELAY_3_EVEN: begin
			if (BIST_read_data != BIST_expected_data) 
				BIST_mismatch <= 1'b1;
			//BIST_address <= 18'h1;
			BIST_address <= BIST_address - 18'd2;
			BIST_state <= S_DELAY_4_EVEN;			
		end
		S_DELAY_4_EVEN: begin
			// check for data mismatch
			if (BIST_read_data != BIST_expected_data)
				BIST_mismatch <= 1'b1;			
			BIST_we_n <= 1'b0;
			BIST_address<= 18'h1;
			BIST_state <= S_WRITE_CYCLE_ODD;
		end
		
		/*********************ODD******************************/
		
		S_WRITE_CYCLE_ODD: begin
			BIST_address <= BIST_address + 18'd2;
			if(BIST_address == 18'h3FFFF) begin
				BIST_we_n <= 1'b1;
				BIST_address <= 18'h3FFFF;
				BIST_state <= S_DELAY_1_ODD;
				end
		end
		S_DELAY_1_ODD: begin
			BIST_address <= BIST_address - 18'd2;
			BIST_state <= S_DELAY_2_ODD;
		end
		S_DELAY_2_ODD: begin
			BIST_address <= BIST_address - 18'd2;
			BIST_state <= S_READ_CYCLE_ODD;
		end
		S_READ_CYCLE_ODD: begin
			// complete the READ initiated two clock cycles earlier and perform comparison
			if (BIST_read_data != BIST_expected_data) 
				BIST_mismatch <= 1'b1;
			BIST_address <= BIST_address - 18'd2;
			if (BIST_address == 18'h1) begin				
				BIST_state <= S_DELAY_3_ODD;
			end
		end
		S_DELAY_3_ODD: begin
			if (BIST_read_data != BIST_expected_data) 
				BIST_mismatch <= 1'b1;
			BIST_address <= BIST_address - 18'd2;
			BIST_state <= S_DELAY_4_ODD;			
		end
		S_DELAY_4_ODD: begin
			// check for data mismatch
			if (BIST_read_data != BIST_expected_data)
				BIST_mismatch <= 1'b1;			
			BIST_state <= S_END;
		end
		S_END: begin
			BIST_state <= S_IDLE;
			BIST_finish <= 1'b1;		
		end
		default: BIST_state <= S_IDLE;
		endcase
	end
end

endmodule
