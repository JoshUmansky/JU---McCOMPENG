import matplotlib.pyplot as plt

# Load the filter coefficients
coefficients = []
with open("filter_coefficients.txt", 'r') as file:
    for line in file:
        coefficients.append(float(line.strip()))

# Plot the impulse response
plt.figure(figsize=(10, 4))
plt.plot(coefficients, '.')
plt.title("h Coefficients of the Audio Filter: Mode 3 audio")
plt.xlabel("Sample")
plt.ylabel("Amplitude")
plt.savefig("impulse_response_plot3a.png", dpi=300)

