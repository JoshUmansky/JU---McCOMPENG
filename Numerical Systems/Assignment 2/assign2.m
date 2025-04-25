%Part B
image = imread('lena.bmp');
A = single(image);
[U,S,V]=svd(A);

%Part C
ranks = [1,2,3,4,5,6,7,8,9,10,20,30,40,50];
for r = ranks
    Ar = U(:, 1:r) * S(1:r, 1:r) * V(:, 1:r)';
    figure;
    imshow(uint8(Ar));
    title(['r = ', num2str(r)]);
end

%Part E
Ar = U(:, 2:end) * S(2:end, 2:end) * V(:, 2:end)';
figure;
imshow(uint8(Ar));
title('Exclude 1st singular value');

%Part F
load('noisy-Lena.mat');
noisy_A=single(noisy_Lena);

sigma = diag(U' * noisy_A * V);
new_image = U * diag(sigma) * V';

imshow(uint8(new_image));

