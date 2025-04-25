# activate waveform simulation

view wave

# format signal names in waveform

configure wave -signalnamewidth 1
configure wave -timeline 0
configure wave -timelineunits us

# add signals to waveform

add wave -divider -height 20 {Top-level signals}
add wave -bin UUT/CLOCK_50_I
add wave -bin UUT/resetn
add wave UUT/top_state
add wave -uns UUT/UART_timer

add wave -divider -height 10 {M2 signals}
add wave -uns UUT/M2/SRAM_address
add wave -bin UUT/M2/SRAM_we_n
add wave -hex UUT/M2/M2_state
add wave -uns UUT/M2/data_counter
add wave -uns UUT/M2/column_counter
add wave -uns UUT/M2/fetchs_offset
add wave -uns UUT/M2/RAM_S_address1
add wave -uns UUT/M2/ramS_offset
add wave -hex UUT/M2/write_S_1
add wave -decimal UUT/M2/sprime_buffer
add wave -decimal UUT/M2/SRAM_read_data

add wave -divider -height 10 {SRAM signals}
add wave -uns UUT/SRAM_address
add wave -hex UUT/SRAM_write_data
add wave -bin UUT/SRAM_we_n
add wave -hex UUT/SRAM_read_data

#add wave -divider -height 10 {M1 signals}
#add wave -uns UUT/M1/SRAM_address
#add wave -hex UUT/M1/SRAM_write_data
#add wave -hex UUT/SRAM_we_n
#add wave -bin UUT/M1/SRAM_we_n
#add wave -hex UUT/M1/M1_state
#add wave -hex UUT/M1/SRAM_read_data
#add wave -hex UUT/M1/M1_state
#add wave -uns UUT/M1/y_address
#add wave -uns UUT/M1/y_offset
#add wave -uns UUT/M1/u_address
#add wave -uns UUT/M1/u_offset
#add wave -uns UUT/M1/v_address
#add wave -uns UUT/M1/v_offset
#add wave -uns UUT/M1/rgb_address
#add wave -uns UUT/M1/rgb_offset
#add wave -bin UUT/M1/LO

#add wave -divider -height 10 {RGB Debugging}
#add wave -hex UUT/M1/Re2
#add wave -hex UUT/M1/Ge2
#add wave -hex UUT/M1/Be2
#add wave -hex UUT/M1/Ro2
#add wave -hex UUT/M1/Go2
#add wave -hex UUT/M1/Bo2
#add wave -hex UUT/M1/yeven1
#add wave -hex UUT/M1/yodd1
#add wave -hex UUT/M1/yeven2
#add wave -hex UUT/M1/yodd2

#add wave -divider -height 10 {U Registers}
#add wave -hex UUT/M1/M1_state
#add wave -uns UUT/M1/ujminus5
#add wave -uns UUT/M1/ujminus3
#add wave -uns UUT/M1/ujminus1
#add wave -uns UUT/M1/ujplus1
#add wave -uns UUT/M1/ujplus3
#add wave -uns UUT/M1/ujplus5

#add wave -divider -height 10 {V Registers}
#add wave -uns UUT/M1/vjminus5
#add wave -uns UUT/M1/vjminus3
#add wave -uns UUT/M1/vjminus1
#add wave -uns UUT/M1/vjplus1
#add wave -uns UUT/M1/vjplus3
#add wave -uns UUT/M1/vjplus5

#add wave -divider -height 10 {Multipliers}
#add wave -decimal UUT/M1/multi1_op1
#add wave -decimal UUT/M1/multi1_op2
#add wave -decimal UUT/M1/multi1_a
#add wave -decimal UUT/M1/multi2_op1
#add wave -decimal UUT/M1/multi2_op2
#add wave -decimal UUT/M1/multi2_a
#add wave -decimal UUT/M1/multi3_op1
#add wave -decimal UUT/M1/multi3_op2
#add wave -decimal UUT/M1/multi3_a
#add wave -decimal UUT/M1/multi4_op1
#add wave -decimal UUT/M1/multi4_op2
#add wave -decimal UUT/M1/multi4_a

#add wave -divider -height 10 {Accumulators}
#add wave -decimal UUT/M1/acc_u
#add wave -decimal UUT/M1/acc_v
#add wave -decimal UUT/M1/yodd1
#add wave -decimal UUT/M1/yodd2

#add wave -divider -height 10 {VGA signals}
#add wave -bin UUT/VGA_unit/VGA_HSYNC_O
#add wave -bin UUT/VGA_unit/VGA_VSYNC_O
#add wave -uns UUT/VGA_unit/pixel_X_pos
#add wave -uns UUT/VGA_unit/pixel_Y_pos
#add wave -hex UUT/VGA_unit/VGA_red
#add wave -hex UUT/VGA_unit/VGA_green
#add wave -hex UUT/VGA_unit/VGA_blue

