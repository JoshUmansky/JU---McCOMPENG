%Joshua Umansky
%400234265
%Due Feb 06, 2025
%%%%%%%%%%%%%%%Part A%%%%%%%%%%%%%%%%%%%%%
e = 1;
x = 1;
while(1+e) > 1
    min_e = e;
    e = e/2;
end

fprintf('Epsilon Machine value = %.3e\n', min_e);
%%%%%%%%%%%%%%%%Part B%%%%%%%%%%%%%%%%%%%%%
while x>0    
    min_x = x;
    x = x/2;
end

fprintf('X Min that can be represented = %.3e\n', min_x);

