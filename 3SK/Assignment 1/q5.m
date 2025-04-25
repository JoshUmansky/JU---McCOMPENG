%Joshua Umansky
%400234265
%Due Feb 06, 2025
xEval = 3;

realF = xEval^3 - 2*log(xEval);

f0 = @(x) x^3 - 2*log(x);
f1 = @(x) 3*x^2 -2./x;
f2 = @(x) 6*x + 2./(x^2);
f3 = @(x) 6 -4./(x^3);
f4 = @(x) 12./(x^4);

orders = 0:4;
taylor_est = zeros(1, 5);
errors = zeros(1, 5);

for n = orders
    term = 0;
    for k = 0:n
        switch k
            case 0
                term = term + f0(1); % 0th term
            case 1
                term = term + f1(1) * (xEval - 1);
            case 2
                term = term + f2(1) * (xEval - 1)^2 / factorial(2);
            case 3
                term = term + f3(1) * (xEval - 1)^3 / factorial(3);
            case 4
                term = term + f4(1) * (xEval - 1)^4 / factorial(4);
        end
    end
    taylor_est(n+1) = term;
    errors(n+1) = abs((realF - term)/realF);
end

for n = orders
    fprintf('Order:%d     Taylor Estimate: %.6f    Error: %.6f\n', n, taylor_est(n+1), errors(n+1) * 100);
end

figure;
plot(orders, errors * 100, '-ok', 'LineWidth', 2, 'MarkerSize', 8, 'MarkerEdgeColor', 'k', 'MarkerFaceColor', 'r');
xlabel('Order of Taylor Series', 'FontSize', 12, 'FontWeight', 'bold');
ylabel('Relative Error (%)', 'FontSize', 12, 'FontWeight', 'bold');
title('Relative Error vs. Taylor Series Order', 'FontSize', 14, 'FontWeight', 'bold');
grid on;

ax = gca;
ax.GridLineStyle = '-';
ax.GridColor = [0.1, 0.1, 0.1];
ax.GridAlpha = 0.5;

ax.XTick = orders;
ax.YTick = 0:5:max(errors*100);
ax.FontSize = 10;