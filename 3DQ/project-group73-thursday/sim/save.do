
mem save -o SRAM.mem -f mti -data hex -addr hex -startaddress 0 -endaddress 262143 -wordsperline 8 /TB/SRAM_component/SRAM_data

if{[file exists $rtl/dual_port_RAM1.ver]}{
	file delete $rtl/dual_port_RAM1.ver
}
mem save -o RAM1.mem -f mti -data hex -addr decimal - wordsperline 1 /TB/UUT/Milestone2/altsyncram_component/m_default/altsyncram_inst/mem_data
if{[file exists $rtl/dual_port_RAM2.ver]}{
	file delete $rtl/dual_port_RAM2.ver
}
mem save -o RAM2.mem -f mti -data hex -addr decimal - wordsperline 1 /TB/UUT/Milestone2/altsyncram_component/m_default/altsyncram_inst/mem_data
if{[file exists $rtl/dual_port_RAM2.ver]}{
	file delete $rtl/dual_port_RAM2.ver
}
mem save -o RAM3.mem -f mti -data hex -addr decimal - wordsperline 1 /TB/UUT/Milestone2/altsyncram_component/m_default/altsyncram_inst/mem_data