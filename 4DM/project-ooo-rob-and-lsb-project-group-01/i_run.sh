#!/bin/bash

# Define the location for traces
TRACE_DIR=~/Octopus-Traces

# Define the benchmark list (modify if necessary)
BENCHMARKS=("3mm_trace.out" "doitgen_trace.out" "gem_trace.out" "gramschmidt_trace.out" "symm_trace.out" "convolution-2d_trace.out" "fdtd-apml_trace.out" "gemver_trace.out")

# Define the parameter sweeps
ROB_SIZES=(1 2 4 8 16 32)
IPC_SIZES=(1 2 4 8)
LSQ_SIZES=(1 2 4 8 16 32)

# Function to extract final cycle count from output
extract_cycles() {
    grep "Cpu 0 Simulation End @ processor cycle" | sed 's/.*processor cycle # //'
}

# Function to set default values for MAX_ENTRIES before starting the sweep
set_default_values() {
    # Set the default values for ROB and LSQ sizes (if not already set)
    sed -i "s/\(MAX_ENTRIES = \)[0-9]\+/\132/" ./src/MultiCoreSim/model/src/ROB.cc
    sed -i "s/\(MAX_ENTRIES = \)[0-9]\+/\18/" ./src/MultiCoreSim/model/src/LSQ.cc
    sed -i "s/\(IPC = \)[0-9]\+/\14/" ./src/MultiCoreSim/model/src/ROB.cc
}

# Function to run the sweep for a given variable and fixed values for the others
run_sweep() {
    local benchmark=$1
    local var_name=$2
    local var_values=("${!3}")

    for value in "${var_values[@]}"; do
        # Modify the config file for the given variable
        if [ "$var_name" == "ROB" ]; then
            # For ROB size sweep, update MAX_ENTRIES and keep the semi-colon
            sed -i "s/\(MAX_ENTRIES = \)[0-9]\+/\1$value/" ./src/MultiCoreSim/model/src/ROB.cc
        elif [ "$var_name" == "IPC" ]; then
            # For IPC sweep, update IPC and keep the semi-colon
            sed -i "s/\(IPC = \)[0-9]\+/\1$value/" ./src/MultiCoreSim/model/src/ROB.cc
        elif [ "$var_name" == "LSQ" ]; then
            # For LSQ size sweep, update MAX_ENTRIES and keep the semi-colon
            sed -i "s/\(MAX_ENTRIES = \)[0-9]\+/\1$value/" ./src/MultiCoreSim/model/src/LSQ.cc
        fi

        # Run the simulation and extract the final cycle count from the output
        cycles=$(./run.sh tools/x86_trace_generator/LAB3_Configurations/lab3_1_8KB_64B_1_RANDOM_32KB_64_2_LRU.xml $TRACE_DIR/ $benchmark 2>/dev/null | grep "Simulation End @ processor cycle" | awk '{print $NF}')

        # Output results in CSV format: Benchmark, Variable, Sweep Value, Cycle Count
        echo "$benchmark,$var_name,$value,$cycles"
    done
}

# Set default values for ROB and LSQ before sweeping
set_default_values

# Sweep IPC (ROB=32, LSQ=8)
for benchmark in "${BENCHMARKS[@]}"; do
    run_sweep $benchmark "IPC" IPC_SIZES[@]
done

set_default_values

# Sweep ROB Size (IPC=4, LSQ=8)
for benchmark in "${BENCHMARKS[@]}"; do
    run_sweep $benchmark "ROB" ROB_SIZES[@]
done

set_default_values

# Sweep LSQ Size (ROB=32, IPC=4)
for benchmark in "${BENCHMARKS[@]}"; do
    run_sweep $benchmark "LSQ" LSQ_SIZES[@]
done
