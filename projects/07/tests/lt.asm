@R0
AM=M-1
D=M
@R0
A=M-1
D=M-D
@TRUE:n
D;JEQ
@R0
A=M-1
M=0
@END:n
0;JMP
(TRUE:n)
@R0
A=M-1
M=-1
(END:n)