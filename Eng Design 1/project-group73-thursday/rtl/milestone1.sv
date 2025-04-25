`timescale 1ns/100ps
`ifndef DISABLE_DEFAULT_NET
`default_nettype none
`endif

`include "define_state.h"



module Milestone1 (
   input logic            	CLOCK_50_I,
   input logic            	resetn,	
   input logic            	Enable,
	input logic [15:0]		SRAM_read_data,
   output logic [17:0]  	SRAM_address,
	output logic [15:0] 		SRAM_write_data,
	output logic SRAM_we_n

);

M1_State_type M1_state;
//address
logic [17:0] y_address, u_address, v_address, rgb_address;
logic [7:0] u_offset, v_offset, y_offset;
logic [17:0] rgb_offset;

//Value registers
logic [7:0] ubuffer1,ubuffer2,vbuffer1,vbuffer2;
logic [7:0] ujminus5,ujminus3,ujminus1,ujplus1,ujplus3,ujplus5;
logic [7:0] vjminus5,vjminus3,vjminus1,vjplus1,vjplus3,vjplus5;
logic [31:0] yeven1, yodd1, yeven2, yodd2, ueven1, ueven2, veven1, veven2;

//accumulators
logic signed [31:0] acc_u,acc_v;
//logic [31:0] U, V;

//multipliers
logic signed [31:0] multi1_op1,multi1_op2,multi2_op1, multi2_op2, multi3_op1, multi3_op2, multi4_op1,multi4_op2;
logic signed [63:0] multi1_a,multi2_a,multi3_a,multi4_a;

assign multi1_a = multi1_op1 * multi1_op2;
//assign multi1_a = {multi1_a[63],multi1_a[30:0]};

assign multi2_a = multi2_op1 * multi2_op2;
//assign multi2_a = {multi2_a[63],multi2_a[30:0]};

assign multi3_a = multi3_op1 * multi3_op2;
//assign multi3_a = {multi3_a[63],multi3_a[30:0]};

assign multi4_a = multi4_op1 * multi4_op2;
//assign multi4_a = {multi4_a[63],multi4_a[30:0]};

//Writing Registers
logic signed[31:0] Re1,Ge1,Be1,Ro1,Go1,Bo1;
logic signed[31:0] Re2,Ge2,Be2,Ro2,Go2,Bo2;

logic LO;

/*always_ff @(posedge CLOCK_50_I or negedge resetn) begin
	if(!resetn) begin
		Enable <= 1'b0;
	end else begin
		Enable <= ~Enable;
	end
end*/
always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin		
		M1_state <= S_LI_0;
		SRAM_address <= 18'd0;
		SRAM_write_data <= 18'd0;
		SRAM_we_n <= 1'd1;
		y_address <= Y_ADDRESS_START;
		u_address <= U_ADDRESS_START;
		v_address <= V_ADDRESS_START;
		rgb_address <= RGB_ADDRESS_START;
		yeven1 <= 32'b0;
		yodd1 <= 32'b0;
		yeven2 <= 32'b0;
		yodd2 <= 32'b0;
		ueven1 <= 32'b0;
		ueven2 <= 32'b0;
		veven1 <= 32'b0;
		veven2 <= 3'b0;
		ubuffer1 <= 8'b0;
		ubuffer2 <= 8'b0;
		vbuffer1 <= 8'b0;
		vbuffer2 <= 8'b0;
		ujminus5 <= 8'b0;
		ujminus3 <= 8'b0;
		ujminus1 <= 8'b0;
		ujplus1 <= 8'b0;
		ujplus3 <= 8'b0;
		ujplus5 <= 8'b0;
		vjminus5 <= 8'b0;
		vjminus3 <= 8'b0;
		vjminus1 <= 8'b0;
		vjplus1 <= 8'b0;
		vjplus3 <= 8'b0;
		vjplus5 <= 8'b0;
		y_offset <= 8'b0;
		u_offset <= 8'b0;
		v_offset <= 8'b0;
		acc_u <= 32'b0;
		acc_v <= 32'b0;
		multi1_op1 <= 32'b0;
		multi1_op2 <= 32'b0;
		multi2_op1 <= 32'b0;
		multi2_op2 <= 32'b0;
		multi3_op1 <= 32'b0;
		multi3_op2 <= 32'b0;
		multi4_op1 <= 32'b0;
		multi4_op2 <= 32'b0;
		Re1 <=32'b0;
		Ge1 <=32'b0;
		Be1 <=32'b0;
		Ro1 <=32'b0;
		Go1 <=32'b0;
		Bo1 <=32'b0;
		Re2 <=32'b0;
		Ge2 <=32'b0;
		Be2 <=32'b0;
		Ro2 <=32'b0;
		Go2 <=32'b0;
		Bo2 <=32'b0;
		LO <= 1'b0;
		rgb_offset <= 18'b0;						
	end else begin
	if(Enable) begin
		case (M1_state)
		S_LI_0: begin
		SRAM_address <= u_address + u_offset;//u0u1
		SRAM_we_n <= 1'b1;	
		u_offset <= u_offset + 1'd1;
		acc_u <= 32'd128;
		acc_v <= 32'd128;
		M1_state <= S_LI_1;
		end
		
		S_LI_1: begin
		SRAM_address <= u_address + u_offset;//u2u3		
		u_offset <= u_offset + 1'd1;
		SRAM_we_n <= 1'b1;		
		
		M1_state <= S_LI_2;
		end
		
		S_LI_2: begin
		SRAM_address <= v_address + v_offset; //v0v1
		SRAM_we_n <= 1'b1;	
		v_offset <= v_offset + 1'd1;
		
		M1_state <= S_LI_3;
		end
		S_LI_3: begin
		SRAM_address <= v_address + v_offset;//v2v3
		v_offset <= v_offset + 1'd1;
		SRAM_we_n <= 1'b1;
		
		ujminus5 <= SRAM_read_data[15:8];
		ujminus3 <= SRAM_read_data[15:8];
		ujminus1 <= SRAM_read_data[15:8];
		ujplus1 <= SRAM_read_data[7:0];
		
		ueven1 <= SRAM_read_data[15:8]; //ujminus1 
		
		M1_state <= S_LI_4;
		end
		S_LI_4: begin
		SRAM_address <= y_address;//y0y1
		SRAM_we_n <= 1'b1;				
		y_offset <= y_offset + 1'd1;	
		
		ujplus3 <= SRAM_read_data[15:8];
		ujplus5 <= SRAM_read_data[7:0];

		
		M1_state <= S_LI_5;
		end
		S_LI_5: begin
		SRAM_address <= y_address + y_offset;//y2y3
		y_offset <= y_offset + 1'd1;
		SRAM_we_n <= 1'b1;	
		
		vjminus5 <= SRAM_read_data[15:8];
		vjminus3 <= SRAM_read_data[15:8];
		vjminus1 <= SRAM_read_data[15:8];
		vjplus1 <= SRAM_read_data[7:0];
		
		veven1 <= SRAM_read_data[15:8];
		
		multi1_op1 <= ujminus5; 
		multi1_op2 <= k_5;
		
		multi2_op1 <= ujminus3;
		multi2_op2 <= k_3;
		
		multi3_op1 <= ujminus1;
		multi3_op2 <= k_1;
		
		multi4_op1 <= ujplus1;
		multi4_op2 <= k_1;
		
		M1_state <= S_LI_6;
		end
		S_LI_6: begin
		//no read/write
		vjplus3 <= SRAM_read_data[15:8];
		vjplus5 <= SRAM_read_data[7:0];
		
		acc_u <= acc_u + multi1_a + multi2_a + multi3_a + multi4_a;
		
		multi1_op1 <= ujplus3; 
		multi1_op2 <= k_3;
		
		multi2_op1 <= ujplus5;
		multi2_op2 <= k_5;
		
		multi3_op1 <= vjminus5;
		multi3_op2 <= k_5;
		
		multi4_op1 <= vjminus3;
		multi4_op2 <= k_3;
		
		M1_state <= S_LI_7;
		end
		S_LI_7: begin
		SRAM_address <= u_address + u_offset;
		u_offset <= u_offset + 1'd1;
		SRAM_we_n <= 1'b1;	
		
		yeven1 <= SRAM_read_data[15:8];
		yodd1 <= SRAM_read_data[7:0];
		
		acc_u <= acc_u + multi1_a + multi2_a;
		acc_v <= acc_v + multi3_a + multi4_a;
		
		multi1_op1 <= vjminus1; 
		multi1_op2 <= k_1;
		
		multi2_op1 <= vjplus1;
		multi2_op2 <= k_1;
		
		multi3_op1 <= vjplus3;
		multi3_op2 <= k_3;
		
		multi4_op1 <= vjplus5;
		multi4_op2 <= k_5;
		
		M1_state <= S_LI_8;
		end
		S_LI_8: begin
		SRAM_address <= v_address + v_offset;
		v_offset <= v_offset + 1'b1;
		SRAM_we_n <= 1'b1;
		
		yeven2 <= SRAM_read_data[15:8];
		yodd2 <= SRAM_read_data[7:0];
		
		acc_v <= acc_v + multi1_a + multi2_a + multi3_a + multi4_a;
		
		multi1_op1 <= veven1 - 9'd128; //Ea02
		multi1_op2 <= k_a02;
		
		multi2_op1 <= ueven1 - 9'd128; //Ea11
		multi2_op2 <= k_a11;
		
		multi3_op1 <= veven1 - 9'd128; //Ea12
		multi3_op2 <= k_a12;
		
		multi4_op1 <= ueven1 - 9'd128; //Ea21
		multi4_op2 <= k_a21;
		
		M1_state <= S_LI_9;
		end
		S_LI_9: begin
		//no read/write
		
		multi1_op1 <= {acc_v[31:8]} - 32'd128; //Oa02
		multi1_op2 <= k_a02;
		
		multi2_op1 <= {acc_u[31:8]} - 32'd128;; //Oa11
		multi2_op2 <= k_a11;
		
		multi3_op1 <= {acc_v[31:8]} - 32'd128; //Oa12
		multi3_op2 <= k_a12;
		
		multi4_op1 <= {acc_u[31:8]} - 32'd128; //Oa21
		multi4_op2 <= k_a21;
		
		Re1 <= multi1_a;
		Ge1 <= multi2_a + multi3_a;
		Be1 <= multi4_a;
		
		
		ujminus5 <= ujminus3;
		ujminus3 <= ujminus1;
		ujminus1 <= ujplus1;
		ujplus1 <= ujplus3;
		ujplus3 <= ujplus5;
		
		M1_state <= S_LI_10;
		end
		S_LI_10: begin
		SRAM_address <= y_address + y_offset; //y4y5
		y_offset <= y_offset + 1'd1;
		SRAM_we_n <= 1'b1;
		
		ubuffer1 <= SRAM_read_data[15:8];
		ubuffer2 <= SRAM_read_data[7:0];		
		
		ujplus5 <= SRAM_read_data[15:8];
		
		vjminus5 <= vjminus3;
		vjminus3 <= vjminus1;
		vjminus1 <= vjplus1;
		vjplus1 <= vjplus3;
		vjplus3 <= vjplus5;
		
		acc_u <= 32'd128;
		acc_v <= 32'd128;
		
		multi1_op1 <= yeven1 - 32'd16; //Ea00
		multi1_op2 <= k_a00;
		
		multi2_op1 <= yodd1 - 32'd16; //
		multi2_op2 <= k_a00;
		
		multi3_op1 <= ujminus5; //
		multi3_op2 <= k_5;
		
		multi4_op1 <= ujminus3; //
		multi4_op2 <= k_3;
		
		Ro1 <= multi1_a;
		Go1 <= multi2_a + multi3_a;
		Bo1 <= multi4_a;
		
		M1_state <= S_LI_11;
		end
		S_LI_11: begin
		SRAM_address <= u_address + u_offset; //u6u7
		u_offset <= u_offset + 1'b1;
		SRAM_we_n <= 1'b1;
		
		ueven2 <= ujminus1;
		veven2 <= vjminus1;
		
		vbuffer1 <= SRAM_read_data[15:8];
		vbuffer2 <= SRAM_read_data[7:0];
		vjplus5 <= SRAM_read_data[15:8];
		
		acc_u <= acc_u + multi3_a + multi4_a;
		
		multi1_op1 <= ujminus1; //
		multi1_op2 <= k_1;
		
		multi2_op1 <= ujplus1; //
		multi2_op2 <= k_1;
		
		multi3_op1 <= ujplus3; //
		multi3_op2 <= k_3;
		
		multi4_op1 <= ujplus5; //
		multi4_op2 <= k_5;
		
		Re1 <= Re1 + multi1_a;
		Ge1 <= Ge1 + multi1_a;
		Be1 <= Be1 + multi1_a;
		Ro1 <= Ro1 + multi2_a;
		Go1 <= Go1 + multi2_a;
		Bo1 <= Bo1 + multi2_a;
		
		M1_state <= S_LI_12;
		end
		S_LI_12: begin
		SRAM_address <= v_address + v_offset;
		v_offset <= v_offset + 1'b1;
		SRAM_we_n <= 1'b1;
		
		acc_u <= acc_u + multi1_a + multi2_a + multi3_a + multi4_a;
		
		multi1_op1 <= vjminus5; //
		multi1_op2 <= k_5;
		
		multi2_op1 <= vjminus3; //
		multi2_op2 <= k_3;
		
		multi3_op1 <= vjminus1; //
		multi3_op2 <= k_1;
		
		multi4_op1 <= vjplus1; //
		multi4_op2 <= k_1;
		
		M1_state <= S_LI_13;
		end
		S_LI_13: begin
		SRAM_address <= y_address + y_offset;
		y_offset <= y_offset + 1'd1;
		SRAM_we_n <= 1'b1;
		
		yeven1 <= SRAM_read_data[15:8];
		yodd1 <= SRAM_read_data[7:0];
		
		ujminus5 <= ujminus3;
		ujminus3 <= ujminus1;
		ujminus1 <= ujplus1;
		ujplus1 <= ujplus3;
		ujplus3 <= ujplus5;
		ujplus5 <= ubuffer2;
		
		acc_v <= acc_v + multi1_a + multi2_a + multi3_a + multi4_a;
		
		multi1_op1 <= vjplus3; //
		multi1_op2 <= k_3;
		
		multi2_op1 <= vjplus5; //
		multi2_op2 <= k_5;
		
		multi3_op1 <= yeven2 - 5'd16; //Ea00
		multi3_op2 <= k_a00;
		
		multi4_op1 <= yodd2 - 5'd16; //Oa00
		multi4_op2 <= k_a00;
		
		
		Re1 <= (Re1[31] == 1'b1) ? 32'd0 : (|Re1[30:24]) ? 8'd255 : Re1[23:16];
		Ge1 <= (Ge1[31] == 1'b1) ? 32'd0 : (|Ge1[30:24]) ? 8'd255 : Ge1[23:16];
		Be1 <= (Be1[31] == 1'b1) ? 32'd0 : (|Be1[30:24]) ? 8'd255 : Be1[23:16];
		Ro1 <= (Ro1[31] == 1'b1) ? 32'd0 : (|Ro1[30:24]) ? 8'd255 : Ro1[23:16];
		Go1 <= (Go1[31] == 1'b1) ? 32'd0 : (|Go1[30:24]) ? 8'd255 : Go1[23:16];
		Bo1 <= (Bo1[31] == 1'b1) ? 32'd0 : (|Bo1[30:24]) ? 8'd255 : Bo1[23:16];
		
		M1_state <= S_LI_14;
		end
		S_LI_14: begin
		SRAM_we_n <= 1'b1;
		veven1 <= vjplus1;
		
		ueven1 <= ujminus1;
		ubuffer1 <= SRAM_read_data[15:8];
		ubuffer2 <= SRAM_read_data[7:0];
		
		vjminus5 <= vjminus3;
		vjminus3 <= vjminus1;
		vjminus1 <= vjplus1;
		vjplus1 <= vjplus3;
		vjplus3 <= vjplus5;
		vjplus5 <= vbuffer2;
		
		acc_v <= acc_v + multi1_a + multi2_a;
		
		multi1_op1 <= veven2 - 9'd128; //Ea02
		multi1_op2 <= k_a02;
		
		multi2_op1 <= ueven2 - 9'd128; //Ea11
		multi2_op2 <= k_a11;
		
		multi3_op1 <= veven2 - 9'd128; //Ea12
		multi3_op2 <= k_a12;
		
		multi4_op1 <= ueven2 - 9'd128; //Ea21
		multi4_op2 <= k_a21;
		
		Re2 <= multi3_a;
		Ge2 <= multi3_a;
		Be2 <= multi3_a;
		Ro2 <= multi4_a;
		Go2 <= multi4_a;
		Bo2 <= multi4_a;
		
		M1_state <= S_LI_15;
		end
		S_LI_15: begin
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Re1[7:0],Ge1[7:0]};
		
		vbuffer1 <= SRAM_read_data[15:8];
		vbuffer2 <= SRAM_read_data[7:0];
		
		multi1_op1 <= {acc_v[31:8]} - 32'd128; //Oa02
		multi1_op2 <= k_a02;
		
		multi2_op1 <= {acc_u[31:8]} - 32'd128; //Oa11
		multi2_op2 <= k_a11;
		
		multi3_op1 <= {acc_v[31:8]} - 32'd128; //Oa12
		multi3_op2 <= k_a12;
		
		multi4_op1 <= {acc_u[31:8]} - 32'd128; //Oa21
		multi4_op2 <= k_a21;
		
		Re2 <= Re2 + multi1_a;
		Ge2 <= Ge2 + multi2_a + multi3_a;
		Be2 <= Be2 + multi4_a;
		
		
		M1_state <= S_CC_1;
		end
		S_CC_1: begin
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Be1[7:0],Ro1[7:0]};
		
		yeven2 <= SRAM_read_data[15:8];
		yodd2 <= SRAM_read_data[7:0];
		
		multi1_op1 <= ujminus5; 
		multi1_op2 <= k_5;
		
		multi2_op1 <= ujminus3;
		multi2_op2 <= k_3;
		
		multi3_op1 <= ujminus1;
		multi3_op2 <= k_1;
		
		multi4_op1 <= ujplus1;
		multi4_op2 <= k_1;
		
		Ro2 <= Ro2 + multi1_a;
		Go2 <= Go2 + multi2_a + multi3_a;
		Bo2 <= Bo2 + multi4_a;
		
		acc_u <= 32'd128;
		acc_v <= 32'd128;
		M1_state <= S_CC_2;
		end
		S_CC_2: begin		
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Go1[7:0],Bo1[7:0]};
		
		
		acc_u <= acc_u + multi1_a + multi2_a + multi3_a + multi4_a;
		
		multi1_op1 <= ujplus3; 
		multi1_op2 <= k_3;
		
		multi2_op1 <= ujplus5;
		multi2_op2 <= k_5;
		
		multi3_op1 <= vjminus5;
		multi3_op2 <= k_5;
		
		multi4_op1 <= vjminus3;
		multi4_op2 <= k_3;		
		
		Re2 <= (Re2[31] == 1'b1) ? 32'd0 : (|Re2[30:24]) ? 8'd255 : Re2[23:16];
		Ge2 <= (Ge2[31] == 1'b1) ? 32'd0 : (|Ge2[30:24]) ? 8'd255 : Ge2[23:16];
		Be2 <= (Be2[31] == 1'b1) ? 32'd0 : (|Be2[30:24]) ? 8'd255 : Be2[23:16];
		Ro2 <= (Ro2[31] == 1'b1) ? 32'd0 : (|Ro2[30:24]) ? 8'd255 : Ro2[23:16];
		Go2 <= (Go2[31] == 1'b1) ? 32'd0 : (|Go2[30:24]) ? 8'd255 : Go2[23:16];
		Bo2 <= (Bo2[31] == 1'b1) ? 32'd0 : (|Bo2[30:24]) ? 8'd255 : Bo2[23:16];
		
		M1_state <= S_CC_3;
		end
		S_CC_3: begin
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Re2[7:0],Ge2[7:0]};
		
		acc_u <= acc_u + multi1_a + multi2_a;
		acc_v <= acc_v + multi3_a + multi4_a;
		
		multi1_op1 <= vjminus1; 
		multi1_op2 <= k_1;
		
		multi2_op1 <= vjplus1;
		multi2_op2 <= k_1;
		
		multi3_op1 <= vjplus3;
		multi3_op2 <= k_3;
		
		multi4_op1 <= vjplus5;
		multi4_op2 <= k_5;
		
		M1_state <= S_CC_4;
		end
		S_CC_4: begin
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Be2[7:0],Ro2[7:0]};
		
		ujminus5 <= ujminus3;
		ujminus3 <= ujminus1;
		ujminus1 <= ujplus1;
		ujplus1 <= ujplus3;
		ujplus3 <= ujplus5;
		ujplus5 <= ubuffer1;
		
		vjminus5 <= vjminus3;
		vjminus3 <= vjminus1;
		vjminus1 <= vjplus1;
		vjplus1 <= vjplus3;
		vjplus3 <= vjplus5;
		vjplus5 <= vbuffer1;
		
		acc_v <= acc_v + multi1_a + multi2_a + multi3_a + multi4_a;
		
		multi1_op1 <= ueven1 - 9'd128; //Ea21
		multi1_op2 <= k_a21;
		
		multi2_op1 <= veven1 - 9'd128; //Ea02
		multi2_op2 <= k_a02;
		
		multi3_op1 <= ueven1 - 9'd128; //Ea11
		multi3_op2 <= k_a11;
		
		multi4_op1 <= veven1 - 9'd128; //Ea12
		multi4_op2 <= k_a12;
		
		M1_state <= S_CC_5;
		end
		S_CC_5: begin				
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Go2[7:0],Bo2[7:0]};
		
		ueven2 <= ujminus1;
		veven2 <= vjminus1;
		
		multi1_op1 <= acc_v[31:8] - 32'd128; //Oa12
		multi1_op2 <= k_a12;
		
		multi2_op1 <= acc_u[31:8] - 32'd128; //Oa21
		multi2_op2 <= k_a21;
		
		multi3_op1 <= acc_v[31:8] - 32'd128; //Oa02
		multi3_op2 <= k_a02;
		
		multi4_op1 <= acc_u[31:8] - 32'd128; //Oa11
		multi4_op2 <= k_a11;
		
		Re1 <= multi2_a;
		Ge1 <= multi3_a + multi4_a;
		Be1 <= multi1_a;
		
		M1_state <= S_CC_6;
		end
		S_CC_6: begin
		SRAM_address <= y_address + y_offset;		
		y_offset <= y_offset + 1'b1;
		SRAM_we_n <= 1'b1;
		
		multi1_op1 <= yeven1 - 5'd16; //Ea00
		multi1_op2 <= k_a00;
		
		multi2_op1 <= yodd1 - 5'd16; //Oa00
		multi2_op2 <= k_a00;
		
		multi3_op1 <= ujminus5;
		multi3_op2 <= k_5;
		
		multi4_op1 <= ujminus3; 
		multi4_op2 <= k_3;
		
		Ro1 <= multi3_a;
		Go1 <= multi1_a + multi4_a;
		Bo1 <= multi2_a;
		
		acc_u <= 32'd128;
		acc_v <= 32'd128;
		
		M1_state <= S_CC_7;
		end
		S_CC_7: begin
		SRAM_we_n <= 1'b1; 
		SRAM_address <= u_address + u_offset;
		u_offset <= u_offset + 1'b1;
		
		
		acc_u <= acc_u + multi3_a + multi4_a;
		
		multi1_op1 <= ujminus1; //
		multi1_op2 <= k_1;
		
		multi2_op1 <= ujplus1; //
		multi2_op2 <= k_1;
		
		multi3_op1 <= ujplus3; //
		multi3_op2 <= k_3;
		
		multi4_op1 <= ujplus5; //
		multi4_op2 <= k_5;
		
		Re1 <= Re1 + multi1_a;
		Ge1 <= Ge1 + multi1_a;
		Be1 <= Be1 + multi1_a;
		Ro1 <= Ro1 + multi2_a;
		Go1 <= Go1 + multi2_a;
		Bo1 <= Bo1 + multi2_a;	
		
		
		M1_state <= S_CC_8;
		end
		S_CC_8: begin
		SRAM_address <= v_address + v_offset;
		v_offset <= v_offset + 1'b1;
		SRAM_we_n <= 1'b1;
		
		acc_u <= acc_u + multi1_a + multi2_a + multi3_a + multi4_a;
		
		multi1_op1 <= vjminus5; //
		multi1_op2 <= k_5;
		
		multi2_op1 <= vjminus3; //
		multi2_op2 <= k_3;
		
		multi3_op1 <= vjminus1; //
		multi3_op2 <= k_1;
		
		multi4_op1 <= vjplus1; //
		multi4_op2 <= k_1;
		
		Re1 <= (Re1[31] == 1'b1) ? 32'd0 : (|Re1[30:24]) ? 8'd255 : Re1[23:16];
		Ge1 <= (Ge1[31] == 1'b1) ? 32'd0 : (|Ge1[30:24]) ? 8'd255 : Ge1[23:16];
		Be1 <= (Be1[31] == 1'b1) ? 32'd0 : (|Be1[30:24]) ? 8'd255 : Be1[23:16];
		Ro1 <= (Ro1[31] == 1'b1) ? 32'd0 : (|Ro1[30:24]) ? 8'd255 : Ro1[23:16];
		Go1 <= (Go1[31] == 1'b1) ? 32'd0 : (|Go1[30:24]) ? 8'd255 : Go1[23:16];
		Bo1 <= (Bo1[31] == 1'b1) ? 32'd0 : (|Bo1[30:24]) ? 8'd255 : Bo1[23:16];
		
		M1_state <= S_CC_9;
		end
		S_CC_9: begin
		SRAM_address <= y_address + y_offset;
		y_offset <= y_offset + 1'd1;
		SRAM_we_n <= 1'b1;
		
		yeven1 <= SRAM_read_data[15:8];
		yodd1 <= SRAM_read_data[7:0];
		
		ujminus5 <= ujminus3;
		ujminus3 <= ujminus1;
		ujminus1 <= ujplus1;
		ujplus1 <= ujplus3;
		ujplus3 <= ujplus5;
		ujplus5 <= ubuffer2;
		
		
		if(u_offset == 8'd81) LO = 1'b1;
		
		//else	LO = 1'b0;
		
		acc_v <= acc_v + multi1_a + multi2_a + multi3_a + multi4_a;
		
				
		multi1_op1 <= vjplus3; //
		multi1_op2 <= k_3;
		
		multi2_op1 <= vjplus5; //
		multi2_op2 <= k_5;
		
		multi3_op1 <= yeven2 - 5'd16; //Ea00
		multi3_op2 <= k_a00;
		
		multi4_op1 <= yodd2 - 5'd16; //Oa00
		multi4_op2 <= k_a00;
		
		M1_state <= S_CC_10;
		end
		S_CC_10: begin
		SRAM_we_n <= 1'b1;
		ueven1 <= ujminus1;
		
		if(LO == 1'b1) begin
			ubuffer1 <= ujplus5;
			ubuffer2 <= ujplus5;
		end else begin
		ubuffer1 <= SRAM_read_data[15:8];
		ubuffer2 <= SRAM_read_data[7:0];
		end		
		
		vjminus5 <= vjminus3;
		vjminus3 <= vjminus1;
		vjminus1 <= vjplus1;
		vjplus1 <= vjplus3;
		vjplus3 <= vjplus5;
		vjplus5 <= vbuffer2;
		
		acc_v <= acc_v + multi1_a + multi2_a;
		
		multi1_op1 <= veven2 - 9'd128; //Ea02
		multi1_op2 <= k_a02;
		
		multi2_op1 <= ueven2 - 9'd128; //Ea11
		multi2_op2 <= k_a11;
		
		multi3_op1 <= veven2 - 9'd128; //Ea12
		multi3_op2 <= k_a12;
		
		multi4_op1 <= ueven2 - 9'd128; //Ea21
		multi4_op2 <= k_a21;
		
		Re2 <= multi3_a;
		Ge2 <= multi3_a;
		Be2 <= multi3_a;
		Ro2 <= multi4_a;
		Go2 <= multi4_a;
		Bo2 <= multi4_a;
		
		if(y_offset > 8'd160) M1_state <= S_LO_1;
		else M1_state <= S_CC_11;
		end
		S_CC_11: begin
			SRAM_address <= rgb_address + rgb_offset;
			rgb_offset <= rgb_offset + 1'b1;
			SRAM_we_n <= 1'b0;
			SRAM_write_data <= {Re1[7:0],Ge1[7:0]};
		
		if(LO == 1'b1) begin
			vbuffer1 <= vjplus5;
			vbuffer2 <= vjplus5;
		end else begin
		vbuffer1 <= SRAM_read_data[15:8];
		vbuffer2 <= SRAM_read_data[7:0];
		end
		
		veven1 <= vjminus1;
		
		multi1_op1 <= acc_v[31:8] - 32'd128; //Oa02
		multi1_op2 <= k_a02;
		
		multi2_op1 <= acc_u[31:8] - 32'd128; //Oa11
		multi2_op2 <= k_a11;
		
		multi3_op1 <= acc_v[31:8] - 32'd128; //Oa12
		multi3_op2 <= k_a12;
		
		multi4_op1 <= acc_u[31:8] - 32'd128; //Oa21
		multi4_op2 <= k_a21;
		
		Re2 <= Re2 + multi1_a;
		Ge2 <= Ge2 + multi2_a + multi3_a;
		Be2 <= Be2 + multi4_a;
		
		M1_state <= S_CC_1;
		end
		S_LO_1: begin
		LO <= 1'b0;
		u_offset <= u_offset - 2'd3;
		v_offset <= v_offset - 2'd3;
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Re1[7:0],Ge1[7:0]};
		
		multi1_op1 <= acc_v[31:8] - 32'd128; //Oa02
		multi1_op2 <= k_a02;
		
		multi2_op1 <= acc_u[31:8] - 32'd128; //Oa11
		multi2_op2 <= k_a11;
		
		multi3_op1 <= acc_v[31:8] - 32'd128; //Oa12
		multi3_op2 <= k_a12;
		
		multi4_op1 <= acc_u[31:8] - 32'd128; //Oa21
		multi4_op2 <= k_a21;
		
		Re2 <= Re2 + multi1_a;
		Ge2 <= Ge2 + multi2_a + multi3_a;
		Be2 <= Be2 + multi4_a;
		
		M1_state <= S_LO_2;
		end
		S_LO_2: begin
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Be1[7:0],Ro1[7:0]};
		
		Ro2 <= Ro2 + multi1_a;
		Go2 <= Go2 + multi2_a + multi3_a;
		Bo2 <= Bo2 + multi4_a;
		
		M1_state <= S_LO_3;
		end
		S_LO_3: begin
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Go1[7:0],Bo1[7:0]};	
		
		Re2 <= (Re2[31] == 1'b1) ? 32'd0 : (|Re2[30:24]) ? 8'd255 : Re2[23:16];
		Ge2 <= (Ge2[31] == 1'b1) ? 32'd0 : (|Ge2[30:24]) ? 8'd255 : Ge2[23:16];
		Be2 <= (Be2[31] == 1'b1) ? 32'd0 : (|Be2[30:24]) ? 8'd255 : Be2[23:16];
		Ro2 <= (Ro2[31] == 1'b1) ? 32'd0 : (|Ro2[30:24]) ? 8'd255 : Ro2[23:16];
		Go2 <= (Go2[31] == 1'b1) ? 32'd0 : (|Go2[30:24]) ? 8'd255 : Go2[23:16];
		Bo2 <= (Bo2[31] == 1'b1) ? 32'd0 : (|Bo2[30:24]) ? 8'd255 : Bo2[23:16];
		
		M1_state <= S_LO_4;
		end
		S_LO_4: begin
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Re2[7:0],Ge2[7:0]};
		
		M1_state <= S_LO_5;
		end
		S_LO_5: begin
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Be2[7:0],Ro2[7:0]};
		M1_state <= S_LO_6;
		end
		S_LO_6: begin
		SRAM_address <= rgb_address + rgb_offset;
		rgb_offset <= rgb_offset + 1'b1;
		SRAM_we_n <= 1'b0;
		SRAM_write_data <= {Go2[7:0],Bo2[7:0]};
		
		y_address <= y_address + y_offset - 2'd2;
		y_offset <= 8'b0;
		u_address <= u_address + u_offset;
		u_offset <= 8'b0;
		v_address <= v_address + v_offset;
		v_offset <= 8'b0;
		
		
		M1_state <= S_LI_0;
		end
		default: M1_state <= S_LI_0;
		endcase;
		end

end
end
endmodule