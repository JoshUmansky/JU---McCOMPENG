%Question 6 Assignment 3 3SK (golden_section.m)
%Joshua Umansky
%400234265

%[returns] = function (inputs)
function [x_star, num_evals, epsilon_a] = golden_section(xl, xu, epsilon_s, Nmax)
    
    d = 0.668 * (xu - xl);
    x1 = xl + d;
    x2 = xu - d;
    f1 = eval_func(x1);
    f2 = eval_func(x2);
    num_evals = 2;
    
    for n = 1:Nmax
        if f1 > f2 %xl = x2, xu = xu
            xl = x2;
            x2 = x1;
            f2 = f1;
            d = 0.668 * (xu - xl);
            x1 = xl + d;
            f1 = eval_func(x1);
        else %xl = xl, xu = x1
            xu = x1;
            x1 = x2;
            f1 = f2;
            d = 0.668 * (xu - xl);
            x2 = xu - d;
            f2 = eval_func(x2);
        end
        num_evals = num_evals + 1;

        %Stopping
        if abs(xu - xl) < epsilon_s
            break;
        end
    end
    
    %Convergence
    if n >= Nmax
        x_star = NaN;
        epsilon_a = NaN;
    else
        x_star = (xl + xu) / 2;
        epsilon_a = abs((xu - xl) / x_star);
    end
end