// Push constant 111
@111
D=A
@R0
A=M
M=D
@R0
M=M+1
// Push constant 333
@333
D=A
@R0
A=M
M=D
@R0
M=M+1
// Push constant 888
@888
D=A
@R0
A=M
M=D
@R0
M=M+1
// Pop static 8
@R0
AM=M-1
D=M
@StaticTest.8
M=D
D=A
// Pop static 3
@R0
AM=M-1
D=M
@StaticTest.3
M=D
D=A
// Pop static 1
@R0
AM=M-1
D=M
@StaticTest.1
M=D
D=A
// Push static 3
@StaticTest.3
D=M
@R0
A=M
M=D
@R0
M=M+1
// Push static 1
@StaticTest.1
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
// Push static 8
@StaticTest.8
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
