from math import *
import sys

def main():
    try:
        A = (int) (sys.argv[1])
        N = (int) (sys.argv[2])

        print("A = " + str(A) + " N = " + str(N) + "\nPb = " + str(Pb(N, A)))
    except:
        print("Inputs are not integers")

def sum(N, A):
    sum = 0
    for i in range(N+1):
        sum += pow(A, i)/factorial(i)
    return sum

def Pb(N, A):
    return pow(A,N)/factorial(N)/sum(N,A)

if __name__ == "__main__":
    main()