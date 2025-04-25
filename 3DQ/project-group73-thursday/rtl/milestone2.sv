`timescale 1ns/100ps
`ifndef DISABLE_DEFAULT_NET
`default_nettype none
`endif

`include "define_state.h"



module Milestone2 (
   input logic            	CLOCK_50_I,
   input logic            	resetn,	
   input logic            	Enable,
	input logic [15:0]		SRAM_read_data,
   output logic [17:0]  	SRAM_address,
	output logic [15:0] 		SRAM_write_data,
	output logic SRAM_we_n

);


M2_State_type M2_state;

//address
logic [17:0] fetchs_address;
logic [17:0] fetchs_offset;
logic [6:0] ramS_offset;
logic [6:0] RAM_S_address1,RAM_S_address2,RAM_TRAM_E_address1,RAM_TRAM_E_address2,RAM_TRAM_O_address1,RAM_TRAM_O_address2,t_address,t_offset,start_coffset;

//RAMS
logic RAM_S_we_1,RAM_S_we_2,RAM_TRAM_E_we_1,RAM_TRAM_E_we_2,RAM_TRAM_O_we_1,RAM_TRAM_O_we_2;
logic signed[31:0] read_TRAM_E1,read_TRAM_E2,read_TRAM_O1,read_TRAM_O2,read_S_1,read_S_2; //use this as op1 for S calculations (TRAM E1/2 and TRAM O1/2) and op2 will be the C value but inverse the way you iterate through 
logic signed[31:0] write_TRAM_E1,write_TRAM_E2,write_TRAM_O1,write_TRAM_O2,write_S_1,write_S_2;

//flags
logic [7:0] data_counter,column_counter;

//buffer
logic signed[16:0] sprime_buffer,S_buffer,sram_offset,sram_buffer;

//accumulators
logic signed [31:0] acc_t1, acc_t2, acc_t3, acc_t4,acc_s1, acc_s2, acc_s3, acc_s4;

//C declaration
logic signed [31:0] C1,C2,C3,C4,CT;
logic signed [5:0] C_matrix; 

//multipliers
logic signed [31:0] multi1_op1,multi1_op2,multi2_op1, multi2_op2, multi3_op1, multi3_op2, multi4_op1,multi4_op2;
logic signed [63:0] multi1_a,multi2_a,multi3_a,multi4_a;

assign multi1_a = multi1_op1 * multi1_op2;

assign multi2_a = multi2_op1 * multi2_op2;

assign multi3_a = multi3_op1 * multi3_op2;

assign multi4_a = multi4_op1 * multi4_op2;

//
logic [7:0] row, column, start_offset;

dual_port_RAM1 TRAM_E (
	.address_a ( RAM_TRAM_E_address1 ),
	.address_b ( RAM_TRAM_E_address2 ),
	.clock ( CLOCK_50_I ),
	.data_a ( write_TRAM_E1 ),
	.data_b ( write_TRAM_E2 ),
	.wren_a ( RAM_TRAM_E_we_1 ),
	.wren_b ( RAM_TRAM_E_we_2 ),
	.q_a ( read_TRAM_E1 ),
	.q_b ( read_TRAM_E2 )
	);	
	dual_port_RAM2 TRAM_O (
	.address_a ( RAM_TRAM_O_address1 ),
	.address_b ( RAM_TRAM_O_address2 ),
	.clock ( CLOCK_50_I ),
	.data_a ( write_TRAM_O1 ),
	.data_b ( write_TRAM_O2 ),
	.wren_a ( RAM_TRAM_O_we_1 ),
	.wren_b ( RAM_TRAM_O_we_2 ),
	.q_a ( read_TRAM_O1 ),
	.q_b ( read_TRAM_O2 )
	);	
	dual_port_RAM3 RAM_S (
	.address_a ( RAM_S_address1 ),
	.address_b ( RAM_S_address2 ),
	.clock ( CLOCK_50_I ),
	.data_a ( write_S_1 ),
	.data_b ( write_S_2 ),
	.wren_a ( RAM_S_we_1 ),
	.wren_b ( RAM_S_we_2 ),
	.q_a ( read_S_1 ),
	.q_b ( read_S_2 )
	);
always_comb begin
case (C_matrix) 
0: CT = 32'd1448;
1: CT = 32'd2008;
2: CT = 32'd1892;
3: CT = 32'd1702;
4: CT = 32'd1448;
5: CT = 32'd1137;
6: CT = 32'd783;
7: CT = 32'd399;
8: CT = 32'd1448;
9: CT = 32'd1702;
10: CT = 32'd783;
11: CT = -32'sd399;
12: CT = -32'sd1448;
13: CT = -32'sd2008;
14: CT = -32'sd1892;
15: CT = -32'sd1137;
16: CT = 32'd1448;
17: CT = 32'd1137;
18: CT = -32'sd783;
19: CT = -32'sd2008;
20: CT = -32'sd1448;
21: CT = 32'd399;
22: CT = 32'd1892;
23: CT = 32'd1702;
24: CT = 32'd1448;
25: CT = 32'd399;
26: CT = -32'sd1892;
27: CT = -32'sd1137;
28: CT = 32'd1448;
29: CT = 32'd1702;
30: CT = -32'sd783;
31: CT = -32'sd2008;
32: CT = 32'd1448;
33: CT = -32'sd399;
34: CT = -32'sd1892;
35: CT = 32'd1137;
36: CT = 32'd1448;
37: CT = -32'sd1702;
38: CT = -32'sd783;
39: CT = 32'd2008;
40: CT = 32'd1448;
41: CT = -32'sd1137;
42: CT = -32'sd783;
43: CT = -32'sd2008;
44: CT = -32'sd1448;
45: CT = -32'sd399;
46: CT = 32'd1892;
47: CT = -32'sd1702;
48: CT = 32'd1448;
49: CT = -32'sd1702;
50: CT = 32'd783;
51: CT = 32'd399;
52: CT = -32'sd1448;
53: CT = 32'd2008;
54: CT = -32'sd1892;
55: CT = 32'd1137;
56: CT = 32'd1448;
57: CT = -32'sd2008;
58: CT = 32'd1892;
59: CT = -32'sd1702;
60: CT = 32'd1448;
61: CT = -32'sd1137;
62: CT = 32'd783;
63: CT = -32'sd399;
default: CT = 32'd0;

endcase
end
	
always_comb begin //put the c1c2c3c4 if statement in here
if (row == 7'd0 && column == 1'd0) begin
	C1 = 32'd1448;
	C2 = 32'd1448;
	C3 = 32'd1448;
	C4 = 32'd1448;
	end else if(row == 7'd0 && column == 1'd1) begin
			C1 = 32'd1448; 
			C2 = 32'd1448; 
			C3 = 32'd1448; 
			C4 = 32'd1448;
			end else if(row == 7'd1 && column == 1'd0) begin
				C1 = 32'd2008;
				C2 = 32'd1702;
				C3 = 32'd1137;
				C4 = 32'd399;
					end else if(row == 7'd1 && column == 1'd1) begin
					C1 = -32'sd399;
					C2 = -32'sd1137;
					C3 = -32'sd1702;
					C4 = -32'sd2008;
						end else if(row == 7'd2 && column == 1'd0) begin
						C1 = 32'd1892;
						C2 = 32'd783;
						C3 = -32'sd783;
						C4 = -32'sd1892;
							end else if(row == 7'd2 && column == 1'd1) begin
							C1 = -32'sd1862;
							C2 = -32'sd783;
							C3 = 32'd783;
							C4 = 32'd1892;
								end else if(row == 7'd3 && column == 1'd0) begin
								C1 = 32'd1702;
								C2 = -32'sd399;
								C3 = -32'sd2008;
								C4 = -32'sd1137;
									end else if(row == 7'd3 && column == 1'd1) begin
									C1 = 32'd1137;
									C2 = 32'd2008;
									C3 = 32'd399;
									C4 = -32'sd1702;
										end else if(row == 7'd4 && column == 1'd0) begin
										C1 = 32'd1448;
										C4 = 32'd1448;
										C2 = -32'sd1448;
										C3 = -32'sd1448;	
											end else if(row == 7'd4 & column == 1'd1) begin
											C1 = 32'd1448;
											C4 = 32'd1448;
											C2 = -32'sd1448;
											C3 = -32'sd1448;
												end else if(row == 7'd5 && column == 1'd0) begin
												C1 = 32'd1137;
												C2 = -32'sd2008;
												C3 = 32'd399;
												C4 = 32'd1702;
													end else if(row == 7'd5 && column == 1'd1) begin
													C1 = -32'sd1702;
													C2 = -32'sd399;
													C3 = 32'd2008;
													C4 = -32'sd1137;
														end else if(row == 7'd6 && column == 1'd0) begin
														C1 = 32'd783;
														C2 = -32'sd1892;
														C3 = 32'd1892;
														C4 = -32'sd783;
															end else if(row == 7'd6 && column == 1'd1) begin
															C1 = -32'sd783;
															C2 = 32'd1892;
															C3 = -32'sd1892;
															C4 = 32'd783;
																end else if(row == 7'd7 && column == 1'd0) begin
																C1 = 32'd399;
																C2 = -32'sd1137;
																C3 = 32'd1702;
																C4  = -32'sd2008;
																	end else if(row == 7'd7 && column == 1'd1) begin
																	C1 = 32'd2008;
																	C2 = -32'sd1702;
																	C3 = 32'd1137;
																	C4 = -32'sd399;
																	end else begin
																	C1 = 32'd0;
																	C2 = 32'd0;
																	C3 = 32'd0;
																	C4 = 32'd0;
																	end

end
always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin		
		SRAM_address <= 18'd0;
		SRAM_write_data <= 18'd0;
		SRAM_we_n <= 1'd1;
		fetchs_address <= YUV_PRE_START;
		M2_state <= S_FetchS_0;
		data_counter <= 8'd0;
		fetchs_offset <= 8'd0;
		column_counter <= 8'd0;
		RAM_S_address1 <= 7'd0;
		RAM_S_address2 <= 7'd0;
		RAM_TRAM_E_address1 <= 7'd0;
		RAM_TRAM_E_address2 <= 7'd0;
		RAM_TRAM_O_address1 <= 7'd0;
		RAM_TRAM_O_address2 <= 7'd0;
		RAM_S_we_1 <= 1'b0;
		RAM_S_we_2 <= 1'b0;
		RAM_TRAM_E_we_1 <= 1'b0;
		RAM_TRAM_E_we_2 <= 1'b0;
		RAM_TRAM_O_we_1 <= 1'b0;
		RAM_TRAM_O_we_2 <= 1'b0;
		ramS_offset <= 5'd0;
		sprime_buffer <= 32'd0;
		RAM_S_we_1 <= 1'b0;
		RAM_S_we_2 <= 1'b0;
		C_matrix <= 1'd0;
		row <= 7'd0;
		column <= 1'b0;
		start_offset <= 8'b0;
	end else begin
	if(Enable) begin
	case (M2_state)	
	S_FetchS_0: begin
		SRAM_address <= fetchs_address;
		SRAM_we_n <= 1'd1;
		data_counter <= data_counter + 1'd1;
		column_counter <= column_counter + 1'd1;
		fetchs_offset <= fetchs_offset + 1'd1;
		M2_state <= S_FetchS_1;
	end
	S_FetchS_1: begin
		SRAM_address <= fetchs_address + fetchs_offset;
		SRAM_we_n <= 1'd1;
		data_counter <= data_counter + 1'd1;
		column_counter <= column_counter + 1'd1;
		fetchs_offset <= fetchs_offset + 1'd1;
		M2_state <= S_FetchS_2;
	end
	S_FetchS_2: begin
		SRAM_address <= fetchs_address + fetchs_offset;
		SRAM_we_n <= 1'd1;
		data_counter <= data_counter + 1'd1;
		column_counter <= column_counter + 1'd1;
		fetchs_offset <= fetchs_offset + 1'd1;
		M2_state <= S_FetchS_3;
	end
	S_FetchS_3: begin
		SRAM_address <= fetchs_address + fetchs_offset;
		SRAM_we_n <= 1'd1;
		data_counter <= data_counter + 1'd1;
		column_counter <= column_counter + 1'd1;
		fetchs_offset <= fetchs_offset + 1'd1;		
		
		sprime_buffer <= SRAM_read_data;
		M2_state <= S_FetchS_4;
	end
	S_FetchS_4: begin
		SRAM_address <= fetchs_address + fetchs_offset;
		SRAM_we_n <= 1'd1;
		data_counter <= data_counter + 1'd1;
		column_counter <= column_counter + 1'd1;
		fetchs_offset <= fetchs_offset + 1'd1;
		
		RAM_S_address1 <= ramS_offset;
		RAM_S_we_1 <= 1'b1;
		write_S_1 <= {sprime_buffer,SRAM_read_data};
		ramS_offset <= ramS_offset + 1'b1;
		
		M2_state <=S_FetchS_5;
	end
	S_FetchS_5: begin		
		SRAM_address <= fetchs_address + fetchs_offset;
		SRAM_we_n <= 1'd1;
		data_counter <= data_counter + 1'd1;
		column_counter <= (column_counter == 4'd7) ? 8'd0 : column_counter + 1'd1;
		fetchs_offset <= fetchs_offset + ((column_counter == 4'd7) ? 9'd313 : 1'd1);		
		
		sprime_buffer <= SRAM_read_data;
		M2_state <= (data_counter == 9'd63) ? S_FetchS_6 : S_FetchS_4;
	end
	S_FetchS_6: begin
		RAM_S_address1 <= ramS_offset;
		RAM_S_we_1 <= 1'b1;
		write_S_1 <= {sprime_buffer,SRAM_read_data};
		ramS_offset <= ramS_offset + 1'b1;
		M2_state <= S_FetchS_7;
	end
	S_FetchS_7: begin
		sprime_buffer <= SRAM_read_data;
		M2_state <= S_FetchS_8;
	end
	S_FetchS_8: begin
		RAM_S_address1 <= ramS_offset;
		RAM_S_we_1 <= 1'b1;
		write_S_1 <= {sprime_buffer,SRAM_read_data};
		ramS_offset <= 7'b0;
		M2_state <= S_COMPUTE_T0;
	end
	S_COMPUTE_T0: begin
		//this would be start of calc T
		row <= 7'd0;
		column <= 1'd0;
		start_offset <= ramS_offset;
		RAM_S_address1 <= ramS_offset;//S0S1
		ramS_offset <= ramS_offset + 1'b1;
		RAM_S_we_1 <= 1'b0;
		if(row != 7'd0) begin
		  RAM_TRAM_E_address1 <= t_address;
		  RAM_TRAM_E_we_1 <= 1'b1;
		  write_TRAM_E1 <= acc_t1 + multi1_a;

		  RAM_TRAM_O_address1 <= t_address;
		  RAM_TRAM_O_we_1 <= 1'b1;
		  write_TRAM_O1 <= acc_t2 + multi2_a;

		  RAM_TRAM_E_address2 <= t_address + 1'b1;
		  RAM_TRAM_E_we_2 <= 1'b1;
		  write_TRAM_E2 <= acc_t3 + multi3_a;

		  RAM_TRAM_O_address2 <= t_address + 1'b1;
		  RAM_TRAM_O_we_2 <= 1'b1;
		  write_TRAM_O2 <= acc_t4 + multi4_a;
		  
		 end
		M2_state <= S_COMPUTE_T1;
		
	end
	S_COMPUTE_T1: begin
		row <= 7'd1;
		column <= 1'd0;
		S_buffer <= read_S_1[7:0];
		
		multi1_op1 <= read_S_1[15:8]; 
		multi1_op2 <= C1;
		
		multi2_op1 <= read_S_1[15:8];
		multi2_op2 <= C2;
		
		multi3_op1 <= read_S_1[15:8];
		multi3_op2 <= C3;
		
		multi4_op1 <= read_S_1[15:8];
		multi4_op2 <= C4;
		M2_state <= S_COMPUTE_T2;
		
	end
	S_COMPUTE_T2: begin
		row <= 7'd2;
		column <= 1'd0;
		RAM_S_address1 <= ramS_offset;//S2S3
		ramS_offset <= ramS_offset + 1'b1;
		acc_t1 <= multi1_a;
		acc_t2 <= multi2_a;
		acc_t3 <= multi3_a;
		acc_t4 <= multi4_a;
		
		multi1_op1 <= S_buffer; 
		multi1_op2 <= C1;
		
		multi2_op1 <= S_buffer;
		multi2_op2 <= C2;
		
		multi3_op1 <= S_buffer;
		multi3_op2 <= C3;
		
		multi4_op1 <= S_buffer;
		multi4_op2 <= C4;
		M2_state <= S_COMPUTE_T3;
	end
	S_COMPUTE_T3: begin
		row <= 7'd3;
		column <= 1'd0;
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
		S_buffer <= read_S_1[7:0];
		
		multi1_op1 <= read_S_1[15:8]; 
		multi1_op2 <= C1;
		
		multi2_op1 <= read_S_1[15:8];
		multi2_op2 <= C2;
		
		multi3_op1 <= read_S_1[15:8];
		multi3_op2 <= C3;
		
		multi4_op1 <= read_S_1[15:8];
		multi4_op2 <= C4;	
		M2_state <= S_COMPUTE_T4;
	end	
	S_COMPUTE_T4: begin
		row <= 7'd4;
		column <= 1'd0;
		RAM_S_address1 <= ramS_offset;//S4S5
		ramS_offset <= ramS_offset + 1'b1;
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
		
		multi1_op1 <= S_buffer; 
		multi1_op2 <= C1;
		
		multi2_op1 <= S_buffer;
		multi2_op2 <= C2;
		
		multi3_op1 <= S_buffer;
		multi3_op2 <= C3;
		
		multi4_op1 <= S_buffer;
		multi4_op2 <= C4;	
		M2_state <= S_COMPUTE_T5;
	end
	S_COMPUTE_T5: begin
		row <= 7'd5;
		column <= 1'd0;
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
		S_buffer <= read_S_1[7:0];
		
		multi1_op1 <= read_S_1[15:8]; 
		multi1_op2 <= C1;
		
		multi2_op1 <= read_S_1[15:8];
		multi2_op2 <= C2;
		
		multi3_op1 <= read_S_1[15:8];
		multi3_op2 <= C3;
		
		multi4_op1 <= read_S_1[15:8];
		multi4_op2 <= C4;	
		M2_state <= S_COMPUTE_T6;
	end
	S_COMPUTE_T6: begin
		row <= 7'd6;
		column <= 1'd0;
		RAM_S_address1 <= ramS_offset;//S6S7
		ramS_offset <= ramS_offset + 1'b1;
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
		
		multi1_op1 <= S_buffer; 
		multi1_op2 <= C1;
		
		multi2_op1 <= S_buffer;
		multi2_op2 <= C2;
		
		multi3_op1 <= S_buffer;
		multi3_op2 <= C3;
		
		multi4_op1 <= S_buffer;
		multi4_op2 <= C4;		
		M2_state <= S_COMPUTE_T7;
	end
	S_COMPUTE_T7: begin
		row <= 7'd7;
		column <= 1'd0;
		ramS_offset <= start_offset;
		S_buffer <= read_S_1[7:0];
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
		
		multi1_op1 <= read_S_1[15:8]; 
		multi1_op2 <= C1;
		
		multi2_op1 <= read_S_1[15:8];
		multi2_op2 <= C2;
		
		multi3_op1 <= read_S_1[15:8];
		multi3_op2 <= C3;
		
		multi4_op1 <= read_S_1[15:8];
		multi4_op2 <= C4;		
		M2_state <= S_COMPUTE_T8;
		ramS_offset <= 1'b0; //reset offset to go to next row
	end
	S_COMPUTE_T8: begin
		row <= 7'd0;
		column <= 1'd1;
		//ramS_offset <= start_offset;
		RAM_S_address1 <= ramS_offset;//S0S1
		ramS_offset <= ramS_offset + 1'b1;
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
		
		multi1_op1 <= S_buffer; 
		multi1_op2 <= C1;
		
		multi2_op1 <= S_buffer;
		multi2_op2 <= C2;
		
		multi3_op1 <= S_buffer;
		multi3_op2 <= C3;
		
		multi4_op1 <= S_buffer;
		multi4_op2 <= C4;		
		M2_state <= S_COMPUTE_T9;
	end
	S_COMPUTE_T9: begin //write T values to ram write in T9 because thats after both even and odd pair of T calculated
	  row <= 7'd1;
	  column <= 1'd1;
	  RAM_TRAM_E_address1 <= t_address;
     RAM_TRAM_E_we_1 <= 1'b1;
     write_TRAM_E1 <= acc_t1 + multi1_a;

     RAM_TRAM_O_address1 <= t_address;
     RAM_TRAM_O_we_1 <= 1'b1;
     write_TRAM_O1 <= acc_t2 + multi2_a;

     RAM_TRAM_E_address2 <= t_address + 1'b1;
     RAM_TRAM_E_we_2 <= 1'b1;
     write_TRAM_E2 <= acc_t3 + multi3_a;

     RAM_TRAM_O_address2 <= t_address + 1'b1;
     RAM_TRAM_O_we_2 <= 1'b1;
     write_TRAM_O2 <= acc_t4 + multi4_a;

     t_address <= t_address + 1'b1;

		M2_state <= S_COMPUTE_T10;
	end
	S_COMPUTE_T10: begin
		row <= 7'd2;
		column <= 1'd1;
		//ramS_offset <= start_offset;
		RAM_S_address1 <= ramS_offset;//S2S3
		ramS_offset <= ramS_offset + 1'b1;
		acc_t1 <= multi1_a;
		acc_t2 <= multi2_a;
		acc_t3 <= multi3_a;
		acc_t4 <= multi4_a;
		
		multi1_op1 <= S_buffer; 
		multi1_op2 <= C1;
		
		multi2_op1 <= S_buffer;
		multi2_op2 <= C2;
		
		multi3_op1 <= S_buffer;
		multi3_op2 <= C3;
		
		multi4_op1 <= S_buffer;
		multi4_op2 <= C4;		
		M2_state <= S_COMPUTE_T11;
	end
	S_COMPUTE_T11: begin
	row <= 7'd3;
	column <= 1'd1;
	acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
			S_buffer <= read_S_1[7:0];
		
		multi1_op1 <= read_S_1[15:8]; 
		multi1_op2 <= C1;
		
		multi2_op1 <= read_S_1[15:8];
		multi2_op2 <= C2;
		
		multi3_op1 <= read_S_1[15:8];
		multi3_op2 <= C3;
		
		multi4_op1 <= read_S_1[15:8];
		multi4_op2 <= C4;
		M2_state <= S_COMPUTE_T12;
	end
	S_COMPUTE_T12: begin
	row <= 7'd4;
	column <= 1'd1;
	//ramS_offset <= start_offset;
		RAM_S_address1 <= ramS_offset;//S4S5
		ramS_offset <= ramS_offset + 1'b1;
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
		
		multi1_op1 <= S_buffer; 
		multi1_op2 <= C1;
		
		multi2_op1 <= S_buffer;
		multi2_op2 <= C2;
		
		multi3_op1 <= S_buffer;
		multi3_op2 <= C3;
		
		multi4_op1 <= S_buffer;
		multi4_op2 <= C4;		
		M2_state <= S_COMPUTE_T13;
	end
	S_COMPUTE_T13: begin
	row <= 7'd5;
	column <= 1'd1;
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
			S_buffer <= read_S_1[7:0];
		
		multi1_op1 <= read_S_1[15:8]; 
		multi1_op2 <= C1;
		
		multi2_op1 <= read_S_1[15:8];
		multi2_op2 <= C2;
		
		multi3_op1 <= read_S_1[15:8];
		multi3_op2 <= C3;
		
		multi4_op1 <= read_S_1[15:8];
		multi4_op2 <= C4;
		M2_state <= S_COMPUTE_T14;
	end
	S_COMPUTE_T14: begin
	row <= 7'd6;
	column <= 1'd1;
	//ramS_offset <= start_offset;
		RAM_S_address1 <= ramS_offset;//S6S7
		ramS_offset <= ramS_offset + 1'b1;
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
		
		multi1_op1 <= S_buffer; 
		multi1_op2 <= C1;
		
		multi2_op1 <= S_buffer;
		multi2_op2 <= C2;
		
		multi3_op1 <= S_buffer;
		multi3_op2 <= C3;
		
		multi4_op1 <= S_buffer;
		multi4_op2 <= C4;		
		M2_state <= S_COMPUTE_T15;
	end
	S_COMPUTE_T15: begin
	row <= 7'd7;
	column <= 1'd1;
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
		S_buffer <= read_S_1[7:0];
		
		multi1_op1 <= read_S_1[15:8]; 
		multi1_op2 <= C1;
		
		multi2_op1 <= read_S_1[15:8];
		multi2_op2 <= C2;
		
		multi3_op1 <= read_S_1[15:8];
		multi3_op2 <= C3;
		
		multi4_op1 <= read_S_1[15:8];
		multi4_op2 <= C4;
		M2_state <= S_COMPUTE_T16;
	end
	S_COMPUTE_T16: begin
		//ramS_offset <= ramS_offset + 1'b1;
		//RAM_S_address1 <= ramS_offset;//S320S321
		acc_t1 <= acc_t1 + multi1_a;
		acc_t2 <= acc_t2 + multi2_a;
		acc_t3 <= acc_t3 + multi3_a;
		acc_t4 <= acc_t4 + multi4_a;
		
		multi1_op1 <= S_buffer; 
		multi1_op2 <= C1;
		
		multi2_op1 <= S_buffer;
		multi2_op2 <= C2;
		
		multi3_op1 <= S_buffer;
		multi3_op2 <= C3;
		
		multi4_op1 <= S_buffer;
		multi4_op2 <= C4;		
		M2_state <= (ramS_offset == 6'd63)? S_COMPUTE_S0 : S_COMPUTE_T0;
	end
	//how do we want to set up the first compute S state?
	S_COMPUTE_S0: begin
		start_coffset <= C_matrix; 
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'd1;
		RAM_TRAM_O_address2 <= t_offset + 1'd1;
		t_offset <= t_offset + 1'd2;
		RAM_TRAM_E_we_1 <= 1'b0;
		RAM_TRAM_O_we_1 <= 1'b0;
		RAM_TRAM_E_we_2 <= 1'b0;
		RAM_TRAM_O_we_2 <= 1'b0;
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		M2_state <= S_COMPUTE_S1;		
	end
	
	S_COMPUTE_S1: begin
		if(C_matrix != 6'd0) begin
		SRAM_address <= sram_offset;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {acc_s1[7:0],acc_s2[7:0]};
		sram_offset <= sram_offset + 1'b1;
		sram_buffer <= {acc_s3[7:0],acc_s4[7:0]};
		end
		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
		
		C_matrix <= C_matrix + 1'd1;//increment this everytime you are going to do a calculation. We want to start at 0
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;

		M2_state <= S_COMPUTE_S2;
		
	end
	S_COMPUTE_S2: begin
	//since the size of the data is different, what changes need to be made concenring the buffer and are there half the amount of common cases because we are only pulling 1 T value instead of 2?
		if (C_matrix != 6'd1) begin
		SRAM_address <= sram_offset;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= sram_buffer;
		sram_offset <= sram_offset + 1'b1;
		end
		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
				
		acc_s1 <= multi1_a;
		acc_s2 <= multi2_a;
		acc_s3 <= multi3_a;
		acc_s4 <= multi4_a;
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;
		C_matrix <= C_matrix + 1'd1;

		M2_state <= S_COMPUTE_S3;

	end
	S_COMPUTE_S3: begin
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		C_matrix <= C_matrix + 1'd1;//increment this everytime you are going to do a calculation. We want to start at 0
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;

		M2_state <= S_COMPUTE_S4;
		
		end
		S_COMPUTE_S4: begin
		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
				
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;
		C_matrix <= C_matrix + 1'd1;

		M2_state <= S_COMPUTE_S5;
		end
		
		S_COMPUTE_S5: begin
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		C_matrix <= C_matrix + 1'd1;//increment this everytime you are going to do a calculation. We want to start at 0
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;

		M2_state <= S_COMPUTE_S6;
		
		end	
		
		S_COMPUTE_S6: begin
		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
				
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;
		C_matrix <= C_matrix + 1'd1;

		M2_state <= S_COMPUTE_S7;
		end
		
		S_COMPUTE_S7: begin		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		C_matrix <= C_matrix + 1'd1;//increment this everytime you are going to do a calculation. We want to start at 0
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;

		M2_state <= S_COMPUTE_S8;
		
		end	
		
		S_COMPUTE_S8: begin
		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
		C_matrix <= start_coffset; //compute S8		
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;
		C_matrix <= C_matrix + 1'd1;

		M2_state <= S_COMPUTE_S9;
		end
		
		S_COMPUTE_S9: begin
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
		
		C_matrix <= C_matrix + 1'd1;//increment this everytime you are going to do a calculation. We want to start at 0
		
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;

		M2_state <= S_COMPUTE_S10;
		
		end	
	
		S_COMPUTE_S10: begin
		SRAM_address <= sram_offset;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {acc_s1[7:0],acc_s2[7:0]};
		sram_offset <= sram_offset + 1'b1;
		sram_buffer <= {acc_s3[7:0],acc_s4[7:0]};		
		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		
		t_offset <= t_offset + 1'd2;
				
		acc_s1 <=multi1_a;
		acc_s2 <=multi2_a;
		acc_s3 <=multi3_a;
		acc_s4 <=multi4_a;
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;
		C_matrix <= C_matrix + 1'd1;

		M2_state <= S_COMPUTE_S11;
		end
	
		S_COMPUTE_S11: begin
		SRAM_address <= sram_offset;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= sram_buffer;
		sram_offset <= sram_offset + 1'b1;
		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		C_matrix <= C_matrix + 1'd1;//increment this everytime you are going to do a calculation. We want to start at 0
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;

		M2_state <= S_COMPUTE_S12;
		
		end		
	
		S_COMPUTE_S12: begin
		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
				
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;
		C_matrix <= C_matrix + 1'd1;

		M2_state <= S_COMPUTE_S13;
		end
		
		
		S_COMPUTE_S13: begin
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
		
		C_matrix <= C_matrix + 1'd1;//increment this everytime you are going to do a calculation. We want to start at 0
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;

		M2_state <= S_COMPUTE_S14;
		
		end	
	
		S_COMPUTE_S14: begin
		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
				
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;
		C_matrix <= C_matrix + 1'd1;

		M2_state <= S_COMPUTE_S15;
		end	
	
	
		S_COMPUTE_S15: begin
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;		
		C_matrix <= C_matrix + 1'd1;//increment this everytime you are going to do a calculation. We want to start at 0
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;

		M2_state <= S_COMPUTE_S16;
		
		end	
		
		S_COMPUTE_S16: begin
		
		RAM_TRAM_E_address1 <= t_offset;
		RAM_TRAM_O_address1 <= t_offset;
		RAM_TRAM_E_address2 <= t_offset + 1'b1;
		RAM_TRAM_O_address2 <= t_offset + 1'b1;
		t_offset <= t_offset + 1'd2;
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		
		multi1_op1 <= read_TRAM_E1; 
		multi1_op2 <= CT;
		
		multi2_op1 <= read_TRAM_O1;
		multi2_op2 <= CT;
		
		multi3_op1 <= read_TRAM_E2;
		multi3_op2 <= CT;
		
		multi4_op1 <= read_TRAM_O2;
		multi4_op2 <= CT;
		C_matrix <= C_matrix + 1'd1;

		M2_state <= (C_matrix == 6'd63) ? S_S_FINISH : S_COMPUTE_S0;
		end
		
		S_S_FINISH: begin		
		acc_s1 <= acc_s1 + multi1_a;
		acc_s2 <= acc_s2 + multi2_a;
		acc_s3 <= acc_s3 + multi3_a;
		acc_s4 <= acc_s4 + multi4_a;
		M2_state <= S_Write_S1;
		end
		S_Write_S1: begin
		SRAM_address <= sram_offset;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {acc_s1[7:0],acc_s2[7:0]};
		sram_offset <= sram_offset + 1'b1;
		sram_buffer <= {acc_s3[7:0],acc_s4[7:0]};
		M2_state <= S_Write_S2;
	end
		S_Write_S2: begin		
		SRAM_address <= sram_offset;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= sram_buffer;
		sram_offset <= sram_offset + 1'b1;
		M2_state <= S_FetchS_0;
	end
			
		
	//start_coffset <= C_matrix;
	//C_matrix <= start_coffset; //compute S8
	//C_matrix <= C_matrix + 1'b1; //compute S 16
	default: M2_state <= S_FetchS_0;
	endcase
	end













end

end


endmodule