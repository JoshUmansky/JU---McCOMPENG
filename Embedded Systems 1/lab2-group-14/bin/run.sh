#!/bin/bash

step=$1
trace_list="../bin/trace_file_list"
param_file="params.in"
benchmarks="../tools/benchmarks"

base_dir="../tools/benchmarks"

if [ "$step" -eq 1 ]; then # step 3.1
    for num in 1 2 3 4; do # sweeps from 1 to 4 for the large_width parameter
        echo "Running sim for large_width=${num}"
        
        param_name="large_width"
        new_value=$num
        sed -i "s/^$param_name .*/$param_name $new_value/" "$param_file"

        for subdir in "$base_dir"/*; do
            if [ -d "$subdir" ]; then  # Check if it's a directory
                benchm=$(basename "$subdir")
                bm_executable="${subdir}/BM"
                if [ -x "$bm_executable" ]; then  # Check if BM is executable
                    echo "running ${benchm}"
                    new_last_line=${subdir}/BM.txt
                    sed -i '$c\'"$new_last_line" "$trace_list"
                    ./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p' #'elapsed time:' changed to use cycles not time
                fi
            fi
        done
    done
elif [ "$step" -eq 2 ]; then # step 3.2
    sed -i "s/^large_width .*/large_width 4/" "$param_file" # set large_width to 4
    
    rates=("fsched_large_rate" "isched_large_rate" "msched_large_rate" "ssched_large_rate")
    sizes=("fsched_large_size" "isched_large_size" "msched_large_size" "ssched_large_size")

    for rate in "${rates[@]}"; do sed -i "s/^$rate .*/$rate 1/" "$param_file"; done # sets all rates to 1
    for size in "${sizes[@]}"; do sed -i "s/^$size .*/$size 32/" "$param_file"; done # sets all sizes to 32

    sed -i '$c\'"../tools/benchmarks/mergesort/BM.txt" "$trace_list"

    for rate in "${rates[@]}"; do # iterates through the different rates
        #if [[ "$rate" == "fsched_large_rate" || "$rate" == "isched_large_rate" ]]; then ## already did the first two
        #    continue
        #fi
        echo "iterating through $rate"
        for num in 1 2 4 8 16; do # sweeps through the rate values
            sed -i "s/^$rate .*/$rate $num/" "$param_file" # changes the rate value
            echo -n "${num}, "

            ./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p' #'elapsed time:' changed to use cycles not time
        done
        sed -i "s/^$rate .*/$rate 1/" "$param_file" # resets the rate paramter to 1
    done
elif [ "$step" -eq 3 ]; then # step 5.1
    
        # Iterate over the two scheduling modes: io and ooo
    for schedule_type in "io" "ooo"; do
        echo "Running simulations for large_core_schedule=${schedule_type}"

        # Update the large_core_schedule parameter in the param_file
        param_name="large_core_schedule"
        new_value=$schedule_type
        sed -i "s/^$param_name .*/$param_name $new_value/" "$param_file"

        # Iterate over all benchmark directories
        for subdir in "$base_dir"/*; do
            if [ -d "$subdir" ]; then  # Check if it's a directory
                benchm=$(basename "$subdir")
                bm_executable="${subdir}/BM"
                
                if [ -x "$bm_executable" ]; then  # Check if BM is executable
                    echo "Running benchmark ${benchm} with ${schedule_type} schedule"
                    
                    # Update the trace list with the benchmark's last line (if required)
                    new_last_line="${subdir}/BM.txt"
                    sed -i '$c'"$new_last_line" "$trace_list"
                    
                    # Run the simulation and extract the core execution cycles
                    ./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p'
                    # Output the results to a CSV file
                    #echo "${benchm};${schedule_type};${cycles}" >> pipeline_impact.csv
                fi
            fi
        done
    done
    #break
    elif [ "$step" -eq 4 ]; then # step 5.2
    #sed -i "s/^large_width .*/large_width 4/" "$param_file" # set large_width to 4
    
    rates=("io" "ooo")
    param_name="large_core_schedule"
    new_value=$schedule_type
    sed -i "s/^$param_name .*/$param_name $new_value/" "$param_file"
    #sizes=("fsched_large_size" "isched_large_size" "msched_large_size" "ssched_large_size")

    #for rate in "${rates[@]}"; do sed -i "s/^$rate .*/$rate 1/" "$param_file"; done # sets all rates to 1
    #for size in "${sizes[@]}"; do sed -i "s/^$size .*/$size 32/" "$param_file"; done # sets all sizes to 32

    sed -i '$c\'"../tools/benchmarks/mergesort/BM.txt" "$trace_list"

    for rate in "${rates[@]}"; do # iterates through the different rates
        #if [[ "$rate" == "fsched_large_rate" || "$rate" == "isched_large_rate" ]]; then ## already did the first two
        #    continue
        #fi
        sed -i "s/^$param_name .*/$param_name $rate/" "$param_file"
        echo "iterating through $rate"
        for num in 1 4 8 16 64 128 256 512; do # sweeps through the rate values
            sed -i "s/^rob_large_size .*/rob_large_size $num/" "$param_file" # changes the rate value
            echo -n "${num}, "

            ./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p' #'elapsed time:' changed to use cycles not time
        done
        sed -i "s/^rob_large_size .*/rob_large_size 1/" "$param_file" # resets the rate paramter to 1
    done

elif [ "$step" -eq 5 ]; then # step 5: pt 4.1
    echo "adding missing params to $param_file"
    # Define the parameters and their corresponding values
    declare -A required_params=(
        ["use_branch_prediction"]="1"
        ["bp_dir_mech"]="gshare"
        ["bp_hist_length"]="1"
        ["enable_btb"]="1"
        ["perfect_btb"]="0"
        ["btb_entries"]="1024"
        ["perfect_bp"]="1"
    )

    # Loop through the required parameters and check if each one exists in the param_file
    for param in "${!required_params[@]}"; do
        if ! grep -q "^$param" "$param_file"; then
            # If the parameter doesn't exist, append it with the predefined value
            echo "$param ${required_params[$param]}" >> "$param_file"
            echo "Added missing parameter: $param with value ${required_params[$param]}"
        else
            echo "$param already exists"
        fi
    done

    sizes=("fsched_large_size" "isched_large_size" "msched_large_size" "ssched_large_size")

    for size in "${sizes[@]}"; do sed -i "s/^$size .*/$size 32/" "$param_file"; done # sets all sizes to 32

    sed -i "s/^enable_btb .*/enable_btb 1/" "$param_file" # sets enable_btb to 1
    sed -i "s/^perfect_btb .*/perfect_btb 1/" "$param_file" # sets perfect_btb to 1


    echo "Updated enable_btb to 1 and perfect_btb to 0"

    sed -i '$c\'"../tools/benchmarks/mergesort/BM.txt" "$trace_list"
    
    # Now, read the updated values from the param_file
    enable_btb=$(grep "^use_branch_prediction" "$param_file" | awk '{print $2}')
    perfect_btb=$(grep "^perfect_bp" "$param_file" | awk '{print $2}')


    # If statements based on the enable_btb and perfect_btb values
    #if [ "$enable_btb" -eq 0 ]; then
     #   echo "BTB is disabled"
      #  ./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p' #'elapsed time:' changed to use cycles not time

    #elif [ "$enable_btb" -eq 1 ] && [ "$perfect_btb" -eq 1 ]; then  #comment/uncomment this and if above for changing the btb and per btb results
     #   echo "BTB is enabled and perfect"
      #  ./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p'

    if [ "$enable_btb" -eq 1 ] && [ "$perfect_btb" -eq 0 ]; then
        echo "BTB is enabled and imperfect"
        for nums in 2 4 8 16 32 64 128; do
            sed -i "s/^btb_entries .*/btb_entries $nums/" "$param_file" # changes the rate value
            echo "$nums"
            ./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p'
        done
        sed -i "s/^btb_entries .*/btb_entries 1024/" "$param_file" # changes the rate value
    fi

elif [ "$step" -eq 6 ]; then # step 5: pt 4.2
    echo "adding missing params to $param_file"
    # Define the parameters and their corresponding values
    declare -A required_params=(
        ["use_branch_prediction"]="1"
        ["bp_dir_mech"]="gshare"
        ["bp_hist_length"]="1"
        ["enable_btb"]="1"
        ["perfect_btb"]="0"
        ["btb_entries"]="1024"
        ["perfect_bp"]="1"
    )

    # Loop through the required parameters and check if each one exists in the param_file
    for param in "${!required_params[@]}"; do
        if ! grep -q "^$param" "$param_file"; then
            # If the parameter doesn't exist, append it with the predefined value
            echo "$param ${required_params[$param]}" >> "$param_file"
            echo "Added missing parameter: $param with value ${required_params[$param]}"
        else
            echo "$param already exists"
        fi
    done

    sizes=("fsched_large_size" "isched_large_size" "msched_large_size" "ssched_large_size")

    for size in "${sizes[@]}"; do sed -i "s/^$size .*/$size 32/" "$param_file"; done # sets all sizes to 32

    sed -i "s/^enable_btb .*/enable_btb 1/" "$param_file" # sets enable_btb to 1
    sed -i "s/^perfect_btb .*/perfect_btb 1/" "$param_file" # sets perfect_btb to 1

    sed -i "s/^use_branch_prediction .*/use_branch_prediction 1/" "$param_file" # sets enable_bp to 0
    sed -i "s/^perfect_bp .*/perfect_bp 0/" "$param_file" # sets perfect_bp to 1

    echo "Updated enable_btb to 1 and perfect_btb to 0"

    sed -i '$c\'"../tools/benchmarks/mergesort/BM.txt" "$trace_list"
    
    # Now, read the updated values from the param_file

    enable_bp=$(grep "^use_branch_prediction" "$param_file" | awk '{print $2}')
    perfect_bp=$(grep "^perfect_bp" "$param_file" | awk '{print $2}')

    # If statements based on the enable_btb and perfect_btb values
    if [ "$enable_bp" -eq 0 ]; then
        echo "BP is disabled"
        ./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p' #'elapsed time:' changed to use cycles not time

    elif [ "$enable_bp" -eq 1 ] && [ "$perfect_bp" -eq 1 ]; then
        echo "BP is enabled and perfect"
        ./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p'

    elif [ "$enable_bp" -eq 1 ] && [ "$perfect_bp" -eq 0 ]; then
        echo "BP is enabled and imperfect"
        #./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p'
        for nums in 1 2 4 8 16; do
            sed -i "s/^bp_hist_length .*/bp_hist_length $nums/" "$param_file" # changes the rate value
            echo "$nums"
            ./macsim | grep 'Core_Total  Finished:' | sed -n 's/.*cycles:\s*\([0-9]*\).*/\1/p'
        done
        sed -i "s/^bp_hist_length .*/bp_hist_length 1/" "$param_file" # changes the rate value
    fi

fi