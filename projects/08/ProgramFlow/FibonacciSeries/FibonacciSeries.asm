// Push argument 1
@ARG
D=M
@1
D=D+A
A=D
D=M
@SP
A=M
M=D
@SP
M=M+1
// Pop pointer 1
@SP
AM=M-1
D=M
@4
M=D
// Push constant 0
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
// Pop that 0
@THAT
D=M
@0
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
// Push constant 1
@1
D=A
@SP
A=M
M=D
@SP
M=M+1
// Pop that 1
@THAT
D=M
@1
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
// Push argument 0
@ARG
D=M
@0
D=D+A
A=D
D=M
@SP
A=M
M=D
@SP
M=M+1
// Push constant 2
@2
D=A
@SP
A=M
M=D
@SP
M=M+1
// sub command
@SP
AM=M-1
D=M
@SP
A=M-1
M=M-D
// Pop argument 0
@ARG
D=M
@0
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
// Label $MAIN_LOOP_START
($MAIN_LOOP_START)
// Push argument 0
@ARG
D=M
@0
D=D+A
A=D
D=M
@SP
A=M
M=D
@SP
M=M+1
// If $COMPUTE_ELEMENT
@SP
AM=M-1
D=M
@$COMPUTE_ELEMENT
D;JNE
// Goto $END_PROGRAM
@$END_PROGRAM
0;JMP
// Label $COMPUTE_ELEMENT
($COMPUTE_ELEMENT)
// Push that 0
@THAT
D=M
@0
D=D+A
A=D
D=M
@SP
A=M
M=D
@SP
M=M+1
// Push that 1
@THAT
D=M
@1
D=D+A
A=D
D=M
@SP
A=M
M=D
@SP
M=M+1
// add command
@SP
AM=M-1
D=M
@SP
A=M-1
M=M+D
// Pop that 2
@THAT
D=M
@2
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
// Push pointer 1
@R4
D=M
@SP
A=M
M=D
@SP
M=M+1
// Push constant 1
@1
D=A
@SP
A=M
M=D
@SP
M=M+1
// add command
@SP
AM=M-1
D=M
@SP
A=M-1
M=M+D
// Pop pointer 1
@SP
AM=M-1
D=M
@4
M=D
// Push argument 0
@ARG
D=M
@0
D=D+A
A=D
D=M
@SP
A=M
M=D
@SP
M=M+1
// Push constant 1
@1
D=A
@SP
A=M
M=D
@SP
M=M+1
// sub command
@SP
AM=M-1
D=M
@SP
A=M-1
M=M-D
// Pop argument 0
@ARG
D=M
@0
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
// Goto $MAIN_LOOP_START
@$MAIN_LOOP_START
0;JMP
// Label $END_PROGRAM
($END_PROGRAM)
