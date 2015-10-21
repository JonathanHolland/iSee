img = imread('svgTest.png');
hold on

half = imresize(img,0.5);
double = imresize(half,2);
%imshow(double)

quarter = imresize(img,0.25);
quadruple = imresize(quarter,4);
%imshow(quadruple)

fifth = imresize(img, 0.2);
quintuple = imresize(fifth,5);
imshow(quintuple)