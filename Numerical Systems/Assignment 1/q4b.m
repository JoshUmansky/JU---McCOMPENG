%Joshua Umansky
%400234265
%Due Feb 06, 2025

x = [pi/3, pi/6]; %Values of X
values_x = ["pi/3", "pi/6"]; %Makes it look nicer to have a loop
Rel_Error = 10^-9; %Value looking to achieve

for i = 1:length(x)
    trueCos = cos(x(i));
    cos_McLaurin = 0;
    term = 1;
    k = 0;
    relErr = 1;

    while relErr > Rel_Error
        cos_McLaurin = cos_McLaurin + term;
        k = k+1;
        term = (-1)^k * (x(i)^(2*k))/factorial(2*k);
        relErr = abs((trueCos - cos_McLaurin) / trueCos);
    end

    fprintf('======%s======\n', values_x(i));
    fprintf('Cos (McLaurin) %.6f\n', cos_McLaurin);
    fprintf('Cos(%s) = %.6f\n', values_x(i), cos(x(i)));
    fprintf('Num of trials to achieve error =  %d\n', k);

end

