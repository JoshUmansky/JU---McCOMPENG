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

module experiment4 (
		/////// board clocks                      ////////////
		input logic CLOCK_50_I,                   // 50 MHz clock

		/////// switches                          ////////////
		input logic[17:0] SWITCH_I,               // toggle switches

		/////// VGA interface                     ////////////
		output logic VGA_CLOCK_O,                 // VGA clock
		output logic VGA_HSYNC_O,                 // VGA H_SYNC
		output logic VGA_VSYNC_O,                 // VGA V_SYNC
		output logic VGA_BLANK_O,                 // VGA BLANK
		output logic VGA_SYNC_O,                  // VGA SYNC
		output logic[7:0] VGA_RED_O,              // VGA red
		output logic[7:0] VGA_GREEN_O,            // VGA green
		output logic[7:0] VGA_BLUE_O,              // VGA blue

		/////// PS2                               ////////////
		input logic PS2_DATA_I,                   // PS2 data
		input logic PS2_CLOCK_I                   // PS2 clock
);

`include "VGA_param.h"
parameter SCREEN_BORDER_OFFSET = 32;
parameter DEFAULT_MESSAGE_LINE = 280;
parameter DEFAULT_MESSAGE_START_COL = 360;
parameter KEYBOARD_MESSAGE_LINE = 320;
parameter KEYBOARD_MESSAGE_START_COL = 360;

logic resetn, enable;

logic [7:0] VGA_red, VGA_green, VGA_blue;
logic [9:0] pixel_X_pos;
logic [9:0] pixel_Y_pos;

logic [5:0] character_address;
logic rom_mux_output;

logic screen_border_on;

assign resetn = ~SWITCH_I[17];

logic [7:0] PS2_code, PS2_reg;
logic PS2_code_ready;

logic PS2_code_ready_buf;
logic PS2_make_code;

logic [7:0] run;
logic [9:0] letter;
logic [5:0] letteroffset;
logic [5:0] bcd1, bcd2;

// PS/2 controller
PS2_controller ps2_unit (
	.Clock_50(CLOCK_50_I),
	.Resetn(resetn),
	.PS2_clock(PS2_CLOCK_I),
	.PS2_data(PS2_DATA_I),
	.PS2_code(PS2_code),
	.PS2_code_ready(PS2_code_ready),
	.PS2_make_code(PS2_make_code)
);

// Putting the PS2 code into a register
always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		PS2_code_ready_buf <= 1'b0;
		PS2_reg <= 8'd0;
		letter <= 9'd0;
	end else begin
		PS2_code_ready_buf <= PS2_code_ready;
		if (PS2_code_ready && ~PS2_code_ready_buf && PS2_make_code) begin
			// scan code detected
			PS2_reg <= PS2_code;
			if(PS2_code == 8'h29) letter <= 9'b0;
		end
		if(PS2_code == 8'h3B) letter[9] <= 1'b1; //J
		if(PS2_code == 8'h43) letter[8] <= 1'b1; //I
		if(PS2_code == 8'h33) letter[7] <= 1'b1; //H
		if(PS2_code == 8'h34) letter[6] <= 1'b1; //G
		if(PS2_code == 8'h2B) letter[5] <= 1'b1; //F
		if(PS2_code == 8'h24) letter[4] <= 1'b1; //E
		if(PS2_code == 8'h23) letter[3] <= 1'b1; //D
		if(PS2_code == 8'h21) letter[2] <= 1'b1; //C
		if(PS2_code == 8'h32) letter[1] <= 1'b1; //B
		if(PS2_code == 8'h1C) letter[0] <= 1'b1; //A
		
		
	end	
end


VGA_controller VGA_unit(
	.clock(CLOCK_50_I),
	.resetn(resetn),
	.enable(enable),

	.iRed(VGA_red),
	.iGreen(VGA_green),
	.iBlue(VGA_blue),
	.oCoord_X(pixel_X_pos),
	.oCoord_Y(pixel_Y_pos),
	
	// VGA Side
	.oVGA_R(VGA_RED_O),
	.oVGA_G(VGA_GREEN_O),
	.oVGA_B(VGA_BLUE_O),
	.oVGA_H_SYNC(VGA_HSYNC_O),
	.oVGA_V_SYNC(VGA_VSYNC_O),
	.oVGA_SYNC(VGA_SYNC_O),
	.oVGA_BLANK(VGA_BLANK_O)
);

logic [2:0] delay_X_pos;

always_ff @(posedge CLOCK_50_I or negedge resetn) begin
	if(!resetn) begin
		delay_X_pos[2:0] <= 3'd0;
	end else begin
		delay_X_pos[2:0] <= pixel_X_pos[2:0];
	end
end

// Character ROM
char_rom char_rom_unit (
	.Clock(CLOCK_50_I),
	.Character_address(character_address),
	.Font_row(pixel_Y_pos[2:0]),
	.Font_col(delay_X_pos[2:0]),
	.Rom_mux_output(rom_mux_output)
);

// this experiment is in the 800x600 @ 72 fps mode
assign enable = 1'b1;
assign VGA_CLOCK_O = ~CLOCK_50_I;

always_comb begin
	screen_border_on = 0;
	if (pixel_X_pos == SCREEN_BORDER_OFFSET || pixel_X_pos == H_SYNC_ACT-SCREEN_BORDER_OFFSET)
		if (pixel_Y_pos >= SCREEN_BORDER_OFFSET && pixel_Y_pos < V_SYNC_ACT-SCREEN_BORDER_OFFSET)
			screen_border_on = 1'b1;
	if (pixel_Y_pos == SCREEN_BORDER_OFFSET || pixel_Y_pos == V_SYNC_ACT-SCREEN_BORDER_OFFSET)
		if (pixel_X_pos >= SCREEN_BORDER_OFFSET && pixel_X_pos < H_SYNC_ACT-SCREEN_BORDER_OFFSET)
			screen_border_on = 1'b1;
end

always_comb begin
	if(letter[9] == 1'b1) begin
		letteroffset = 6'd10;	
	end else if(letter[8] == 1'b1) begin
		letteroffset = 6'd9;
	end else if(letter[7] == 1'b1) begin
		letteroffset = 6'd8;
	end else if(letter[6] == 1'b1) begin
		letteroffset = 6'd7;
	end else if(letter[5] == 1'b1) begin
		letteroffset = 6'd6;
	end else if(letter[4] == 1'b1) begin
		letteroffset = 6'd5;
	end else if(letter[3] == 1'b1) begin
		letteroffset = 6'd4;
	end else if(letter[2] == 1'b1) begin
		letteroffset = 6'd3;
	end else if(letter[1] == 1'b1) begin
		letteroffset = 6'd2;
	end else if(letter[0] == 1'b1) begin
		letteroffset = 6'd1;
	end else
		letteroffset = 6'd32;
	
end
// Display text
always_comb begin

	character_address = 6'o40; // Show space by default
	
	// 8 x 8 characters
	if (pixel_Y_pos[9:3] == ((DEFAULT_MESSAGE_LINE) >> 3)) begin
		// Reach the section where the text is displayed
		case (pixel_X_pos[9:3])
			(DEFAULT_MESSAGE_START_COL >> 3) +  0: character_address = 6'o22; // R
			(DEFAULT_MESSAGE_START_COL >> 3) +  1: character_address = 6'o25; // U
			(DEFAULT_MESSAGE_START_COL >> 3) +  2: character_address = 6'o16; // N
			(DEFAULT_MESSAGE_START_COL >> 3) +  3: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) +  4: character_address = bcd1; // run number bcd 1		
			(DEFAULT_MESSAGE_START_COL >> 3) +  5: character_address = bcd2; // run number bcd 2
			(DEFAULT_MESSAGE_START_COL >> 3) +  6: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) +  7: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) +  8: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) +  9: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 10: character_address = 6'o40; // space		
			(DEFAULT_MESSAGE_START_COL >> 3) + 11: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 12: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 13: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 14: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 15: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 16: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 17: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 18: character_address = 6'o40; // space
			default: character_address = 6'o40; // space
		endcase
	end

	// 8 x 8 characters

	if (pixel_Y_pos[9:3] == ((KEYBOARD_MESSAGE_LINE) >> 3)) begin
		// Reach the section where the text is displayed
			case (pixel_X_pos[9:3])
			(DEFAULT_MESSAGE_START_COL >> 3) +  0: character_address = 6'o15; // M
			(DEFAULT_MESSAGE_START_COL >> 3) +  1: character_address = 6'o23; // S
			(DEFAULT_MESSAGE_START_COL >> 3) +  2: character_address = 6'o13; // K
			(DEFAULT_MESSAGE_START_COL >> 3) +  3: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) +  4: character_address = letteroffset; // letter		
			(DEFAULT_MESSAGE_START_COL >> 3) +  5: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) +  6: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) +  7: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) +  8: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) +  9: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 10: character_address = 6'o40; // space		
			(DEFAULT_MESSAGE_START_COL >> 3) + 11: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 12: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 13: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 14: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 15: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 16: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 17: character_address = 6'o40; // space
			(DEFAULT_MESSAGE_START_COL >> 3) + 18: character_address = 6'o40; // space
			default: character_address = 6'o40; // space
			endcase
	end
end

// RGB signals
always_comb begin
		VGA_red = 8'h00;
		VGA_green = 8'h00;
		VGA_blue = 8'h00;

		if (screen_border_on) begin
			// blue border
			VGA_blue = 8'hFF;
		end		
		if (rom_mux_output) begin
			// purple text
			VGA_red = 8'hFF;
			VGA_green = 8'h00;
			VGA_blue = 8'hFF;
		end
end

always_ff @(posedge CLOCK_50_I or negedge resetn) begin
	if(!resetn) begin
		run <= 7'd0;
	end else begin
	if (PS2_code_ready && ~PS2_code_ready_buf && PS2_make_code) begin
			// scan code detected
			PS2_reg <= PS2_code;
			if(PS2_code == 8'h29)begin
				if(run[3:0] == 4'd9) begin
					run[3:0] <= 4'd0;
					run[7:4] <= run[7:4] + 1'd1;
				end else begin
					run[3:0] <= run[3:0] + 1'd1;
				end
				if(run[7:4] == 4'd9 && run[3:0] == 4'd9) begin
					run<= 8'd0;
				end
		end
	end
	
	case (run[7:4])
		4'd0:   bcd1 = 6'o40; // space (should never show 0)
		4'd1:   bcd1 = 6'o61; // 1
		4'd2:   bcd1 = 6'o62; // 2
		4'd3:   bcd1 = 6'o63; // 3
		4'd4:   bcd1 = 6'o64; // 4
		4'd5:   bcd1 = 6'o65; // 5
		4'd6:   bcd1 = 6'o66; // 6
		4'd7:   bcd1 = 6'o67; // 7
		4'd8:   bcd1 = 6'o70; // 8
		4'd9:   bcd1 = 6'o71; // 9
		default: bcd1 = 6'o40; // 0
		endcase
	case (run[3:0])
		4'd0:   bcd2 = 6'o60; // 0
		4'd1:   bcd2 = 6'o61; // 1
		4'd2:   bcd2 = 6'o62; // 2
		4'd3:   bcd2 = 6'o63; // 3
		4'd4:   bcd2 = 6'o64; // 4
		4'd5:   bcd2 = 6'o65; // 5
		4'd6:   bcd2 = 6'o66; // 6
		4'd7:   bcd2 = 6'o67; // 7
		4'd8:   bcd2 = 6'o70; // 8
		4'd9:   bcd2 = 6'o71; // 9
		default: bcd2 = 6'o60; // 0
		endcase
end
end

endmodule
