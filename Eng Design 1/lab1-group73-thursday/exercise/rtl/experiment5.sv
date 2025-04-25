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

// This is the top module
// It performs debouncing on the push buttons using a 1kHz clock, and a 10-bit shift register
// When PB0 is pressed, it will stop/start the counter
module experiment5 (
		/////// board clocks                      ////////////
		input logic CLOCK_50_I,                   // 50 MHz clock

		/////// pushbuttons/switches              ////////////
		input logic[3:0] PUSH_BUTTON_N_I,           // pushbuttons
		input logic[17:0] SWITCH_I,               // toggle switches

		/////// 7 segment displays/LEDs           ////////////
		output logic[6:0] SEVEN_SEGMENT_N_O[7:0], // 8 seven segment displays		
		output logic[8:0] LED_GREEN_O             // 9 green LEDs
);

parameter	MAX_1kHz_div_count = 24999,
		MAX_1Hz_div_count = 24999999;

logic resetn;

logic [15:0] clock_1kHz_div_count;
logic clock_1kHz, clock_1kHz_buf;

logic [24:0] clock_1Hz_div_count;
logic clock_1Hz, clock_1Hz_buf;

logic [9:0] debounce_shift_reg [3:0];
logic [3:0] push_button_status, push_button_status_buf;
logic [7:0] led_green;

logic [7:0] counter;
logic [6:0] value_7_segment0, value_7_segment1,value_7_segment2;
logic [6:0] value_7_segmenth0, value_7_segmenth1; //hex value segments
logic [6:0] value_7_segmento0, value_7_segmento1,value_7_segmento2; //oct value segments
logic stop_count; //0 = count, 1 = stop (default 0)
logic count_dir; //up = 1, down = 0 (default 1)
logic displaytype; //0=hex, 1=octal (default 0)

logic led0,led1,led2,led3,led4,led5,led6,led7,led8; //individual LED's

assign resetn = ~SWITCH_I[17];

// Clock division for 1kHz clock
always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		clock_1kHz_div_count <= 16'd0;
	end else begin
		if (clock_1kHz_div_count < MAX_1kHz_div_count) begin
			clock_1kHz_div_count <= clock_1kHz_div_count + 16'd1;
		end else 
			clock_1kHz_div_count <= 16'd0;
	end
end

always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		clock_1kHz <= 1'b1;
	end else begin
		if (clock_1kHz_div_count == 16'd0) 
			clock_1kHz <= ~clock_1kHz;
	end
end

always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		clock_1kHz_buf <= 1'b1;	
	end else begin
		clock_1kHz_buf <= clock_1kHz;
	end
end

// Clock division for 1Hz clock
always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		clock_1Hz_div_count <= 25'd0;
	end else begin
		if (clock_1Hz_div_count < MAX_1Hz_div_count) begin
			clock_1Hz_div_count <= clock_1Hz_div_count + 25'd1;
		end else 
			clock_1Hz_div_count <= 25'd0;		
	end
end

always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		clock_1Hz <= 1'b1;
	end else begin
		if (clock_1Hz_div_count == 25'd0) 
			clock_1Hz <= ~clock_1Hz;
	end
end

always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		clock_1Hz_buf <= 1'b1;	
	end else begin
		clock_1Hz_buf <= clock_1Hz;
	end
end

// Shift register for debouncing
always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		debounce_shift_reg[0] <= 10'd0;
		debounce_shift_reg[1] <= 10'd0;
		debounce_shift_reg[2] <= 10'd0;
		debounce_shift_reg[3] <= 10'd0;						
	end else begin
		if (clock_1kHz_buf == 1'b0 && clock_1kHz == 1'b1) begin
			debounce_shift_reg[0] <= {debounce_shift_reg[0][8:0], ~PUSH_BUTTON_N_I[0]};
			debounce_shift_reg[1] <= {debounce_shift_reg[1][8:0], ~PUSH_BUTTON_N_I[1]};
			debounce_shift_reg[2] <= {debounce_shift_reg[2][8:0], ~PUSH_BUTTON_N_I[2]};
			debounce_shift_reg[3] <= {debounce_shift_reg[3][8:0], ~PUSH_BUTTON_N_I[3]};
		end
	end
end

// push_button_status will contained the debounced signal
always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		push_button_status <= 4'h0;
		push_button_status_buf <= 4'h0;
	end else begin
		push_button_status_buf <= push_button_status;
		push_button_status[0] <= |debounce_shift_reg[0];
		push_button_status[1] <= |debounce_shift_reg[1];
		push_button_status[2] <= |debounce_shift_reg[2];
		push_button_status[3] <= |debounce_shift_reg[3];						
	end
end

// Push button status is checked here for controlling the counter
always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		led_green <= 4'h0;
		stop_count <= 1'b0;
		count_dir <= 1'b1;
		displaytype <= 1'b0;
	end else begin
		if (push_button_status_buf[0] == 1'b0 && push_button_status[0] == 1'b1) begin		
			stop_count <= ~stop_count;
		end
		if (push_button_status_buf[1] == 1'b0 && push_button_status[1] == 1'b1) begin
			count_dir <= 1'b1; //change count direction to up
		end
		if (push_button_status_buf[2] == 1'b0 && push_button_status[2] == 1'b1) begin
			count_dir <= 1'b0; //change count direction to down
		end		
		if (push_button_status_buf[3] == 1'b0 && push_button_status[3] == 1'b1) begin
			displaytype <= ~displaytype; //invert display when button pressed (hex<->octal)
		end		
	end
end

// Counter is incremented here
always_ff @ (posedge CLOCK_50_I or negedge resetn) begin
	if (resetn == 1'b0) begin
		counter <= 8'h00;
	end else begin
		if (clock_1Hz_buf == 1'b0 && clock_1Hz == 1'b1) begin
			if(stop_count == 1'b0) begin//counting
				if(count_dir) begin
					counter <= counter + 8'd1;
				end 
				else begin
					counter <= counter - 8'd1;
				end				
			end
		end
	end
end

//light controls
always_comb begin
	led8 = |SWITCH_I[16:10]; //right
	led7 = &SWITCH_I[16:10]; //right
	led6 = ~^SWITCH_I[16:10]; //right
	led5 = ~&SWITCH_I[9:3]; //right
	led4 = &SWITCH_I[9:3]; //right
	led3 = ^SWITCH_I[9:3]; //right
	led2 = (SWITCH_I[2] + SWITCH_I[1] + SWITCH_I[0])>=2; //right
	led1 = (SWITCH_I[2] + SWITCH_I[1] + SWITCH_I[0])<=2; //right
	led0 = (SWITCH_I[2] + SWITCH_I[1] + SWITCH_I[0])==2; //right
end
		

// Instantiate modules for converting hex number to 7-bit value for the 7-segment display

////////////////////////Hex Converting/////////////////////////////////
	convert_hex_to_seven_segment unit0( //first 4 bits to segment0
		.hex_value(counter[3:0]), 
		.converted_value(value_7_segmenth0)
	);

	convert_hex_to_seven_segment unit1( //2nd 4 bits to segment1
		.hex_value(counter[7:4]), 
		.converted_value(value_7_segmenth1)
	);
////////////////////////Octal Converting////////////////////////////
	convert_octal_to_seven_segment unit2 ( //first 3 bits to segment0
	.octal_value(counter[2:0]), 
	.converted_value(value_7_segmento0)
	);
	convert_octal_to_seven_segment unit3 ( //second 3 bits to segment1
		.octal_value(counter[5:3]), 
		.converted_value(value_7_segmento1)
	);
	convert_octal2bit_to_seven_segment unit4 ( //last 2 bits to segment2 *note different module to convert 2 bits to octal 0-3
		.octal_value(counter[7:6]), 
		.converted_value(value_7_segmento2)
	);
	//Max value 
	//Binary = 11111111
	//Hex = FF
	//Octal = 377
always_comb begin //Checking for which display to show on the 7 segment displays
	if(displaytype==1'b0)begin //hex
	value_7_segment0 = value_7_segmenth0;
	value_7_segment1 = value_7_segmenth1;
	value_7_segment2 = 7'h7f;
	end else begin //octal
	value_7_segment0 = value_7_segmento0;
	value_7_segment1 = value_7_segmento1;
	value_7_segment2 = value_7_segmento2;
	end
	
end

	assign	SEVEN_SEGMENT_N_O[0] = value_7_segment0, //assign the values from the if statement above to the 3 seven segment displays
			SEVEN_SEGMENT_N_O[1] = value_7_segment1,
			SEVEN_SEGMENT_N_O[2] = value_7_segment2,
			SEVEN_SEGMENT_N_O[3] = 7'h7f,
			SEVEN_SEGMENT_N_O[4] = 7'h7f,
			SEVEN_SEGMENT_N_O[5] = 7'h7f,
			SEVEN_SEGMENT_N_O[6] = 7'h7f,
			SEVEN_SEGMENT_N_O[7] = 7'h7f;


assign LED_GREEN_O = {led8,led7, led6, led5, led4, led3, led2, led1, led0}; //assign LED8-0

endmodule
