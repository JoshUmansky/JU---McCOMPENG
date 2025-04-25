%Question 6 Assignment 3 3SK (Main_Program.m)
%Joshua Umansky
%400234265

epsilon_s = 1e-9;
Nmax = 100;

%Intervals from Question
intervals = [0 2; 4 7; 2 7; 0 7];

% Loop through intervals
for i = 1:4
    [x_star, num_evals, epsilon_a] = golden_section(intervals(i, 1), intervals(i, 2), epsilon_s, Nmax);
    fprintf('Interval: [%d %d]\n', intervals(i, 1), intervals(i, 2));
    fprintf('Estimated solution x*: %.12f\n', x_star);
    fprintf('Number of evaluations: %d\n', num_evals);
    fprintf('Approximate relative error: %.12f%%\n', epsilon_a * 100);
end