// Push constant 3030
@3030
D=A
@R0
A=M
M=D
@R0
M=M+1
// Pop pointer 0
@R0
AM=M-1
D=M
@3
M=D
// Push constant 3040
@3040
D=A
@R0
A=M
M=D
@R0
M=M+1
// Pop pointer 1
@R0
AM=M-1
D=M
@4
M=D
// Push constant 32
@32
D=A
@R0
A=M
M=D
@R0
M=M+1
// Pop this 2
@R3
D=M
@2
D=D+A
@R13
M=D
@R0
AM=M-1
D=M
@R13
A=M
M=D
// Push constant 46
@46
D=A
@R0
A=M
M=D
@R0
M=M+1
// Pop that 6
@R4
D=M
@6
D=D+A
@R13
M=D
@R0
AM=M-1
D=M
@R13
A=M
M=D
// Push pointer 0
@R3
D=M
@R0
A=M
M=D
@R0
M=M+1
// Push pointer 1
@R4
D=M
@R0
A=M
M=D
@R0
M=M+1
// add command
@R0
AM=M-1
D=M
@R0
A=M-1
M=M+D
// Push this 2
@R3
D=M
@2
D=D+A
A=D
D=M
@R0
A=M
M=D
@R0
M=M+1
// sub command
@R0
AM=M-1
D=M
@R0
A=M-1
M=M-D
// Push that 6
@R4
D=M
@6
D=D+A
A=D
D=M
@R0
A=M
M=D
@R0
M=M+1
// add command
@R0
AM=M-1
D=M
@R0
A=M-1
M=M+D
