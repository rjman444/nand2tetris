import java.lang.*;
import java.util.*;
import java.io.*;

public class VMTranslator {

    enum CTYPE {
        C_ARITHMETIC,
        C_PUSH,
        C_POP,
        C_GOTO,
        C_IF,
        C_LABEL,
        C_FUNCTION,
        C_RETURN,
        C_CALL
    }

    private static class Command {
        private static CTYPE Type;
        private static String arg1;
        private static int arg2;
        
        public Command(CTYPE T, String A1, int A2){
            Type = T;
            arg1 = A1;
            arg2 = A2;
        }
    }


    private static class Parser {
        public Parser(FileInputStream fs) {
            r = new BufferedReader(new InputStreamReader(fs));
        }

        public boolean hasMoreCommands() {
            return read();
        }

        public void advance() {
            if (line.isEmpty() || line.charAt(0) == '/' ) {
                if (hasMoreCommands()){
                    advance();
                }
            } else {
                String[] cmd = line.split("\\s+");
                
                // Arithmetic Commands
                if (cmd[0].equals("add")){
                    command = new Command(CTYPE.C_ARITHMETIC, "add", 0);
                } else if (cmd[0].equals("sub")){
                    command = new Command(CTYPE.C_ARITHMETIC, "sub", 0);
                } else if (cmd[0].equals("neg")){
                    command = new Command(CTYPE.C_ARITHMETIC, "neg", 0);
                } else if (cmd[0].equals("lt")){
                    command = new Command(CTYPE.C_ARITHMETIC, "lt", 0);
                } else if (cmd[0].equals("gt")){
                    command = new Command(CTYPE.C_ARITHMETIC, "gt", 0);
                } else if (cmd[0].equals("eq")){
                    command = new Command(CTYPE.C_ARITHMETIC, "eq", 0);
                } else if (cmd[0].equals("and")){
                    command = new Command(CTYPE.C_ARITHMETIC, "and", 0);
                } else if (cmd[0].equals("or")){
                    command = new Command(CTYPE.C_ARITHMETIC, "or", 0);
                } else if (cmd[0].equals("not")){
                    command = new Command(CTYPE.C_ARITHMETIC, "not", 0);
                }

                // Push and Pop Commands
                if (cmd[0].equals("pop")){
                    command = new Command(CTYPE.C_POP, cmd[1], Integer.parseInt(cmd[2]));
                } else if (cmd[0].equals("push")){
                    command = new Command(CTYPE.C_PUSH, cmd[1], Integer.parseInt(cmd[2]));
                }

                // Branching Commands
                if (cmd[0].equals("label")) {
                    command = new Command(CTYPE.C_LABEL, cmd[1], 0);
                } else if (cmd[0].equals("goto")) {
                    command = new Command(CTYPE.C_GOTO, cmd[1], 0);
                } else if (cmd[0].equals("if-goto")) {
                    command = new Command(CTYPE.C_IF, cmd[1], 0);
                }

                // Function Commands
                if (cmd[0].equals("function")) {
                    command = new Command(CTYPE.C_FUNCTION, cmd[1],Integer.parseInt(cmd[2]));
                } else if (cmd[0].equals("return")) {
                    command = new Command(CTYPE.C_RETURN, "", 0);
                }  else if (cmd[0].equals("call")) {
                    command = new Command(CTYPE.C_CALL, cmd[1], Integer.parseInt(cmd[2]));
                }
                

            }
           
        }

        public CTYPE commandType(){
            return command.Type;
        }

        public String arg1(){
            return command.arg1;
        }

        public int arg2(){
            return command.arg2;
        }

        private boolean read(){
            try {
                line = r.readLine();
                if (line == null) return false;
            } catch (IOException e) { e.printStackTrace(); }
            return true;
        }

        private Command command;
        private String line = "";
        private static BufferedReader r;
    }

    private static class CodeWriter{

        private int labelCount = 0;
        private String fileName;

        public CodeWriter(FileOutputStream ofs){
            p = new PrintWriter(ofs);
        }

        public void setFileName(String fn) {
            fileName = fn;
        }
            

        public void writeArithmetic(String cmd){
            p.println("// " + cmd + " command");
            p.println("@SP"); // Access stack pointer
            
            if (cmd.equals("not")) {
                p.println("A=M-1");
                p.println("M=!M");
            } else if (cmd.equals("neg")) {
                p.println("A=M-1");
                p.println("M=-M");
            } else {
                p.println("AM=M-1"); // Point to top element of stack and go there
                p.println("D=M"); // Get y
                p.println("@SP");
                p.println("A=M-1"); // Get x
            }

            if (cmd.equals("add")) {
                p.println("M=M+D");
            } else if (cmd.equals("sub")) {
                p.println("M=M-D");
            } else if (cmd.equals("and")) {
                p.println("M=M&D");
            } else if (cmd.equals("or")) {
                p.println("M=M|D");
            }

           if (cmd.equals("eq") || cmd.equals("gt") || cmd.equals("lt")) {
                String n = Integer.toString(labelCount);
                p.println("D=M-D");
                p.println("@TRUE:" + n);// Prepare for comparison

                if (cmd.equals("eq")) {
                    p.println("D;JEQ");
                } else if (cmd.equals("gt")) {
                    p.println("D;JGT");
                } else if (cmd.equals("lt")) {
                    p.println("D;JLT");
                }

                p.println("@SP");
                p.println("A=M-1");
                p.println("M=0");
                p.println("@END:" + n); 
                p.println("0;JMP");
                p.println("(TRUE:" + n + ")");
                p.println("@SP");
                p.println("A=M-1");
                p.println("M=-1");
                p.println("(END:" + n + ")");

                labelCount += 1;
            }

        }

        public void writePushPop(CTYPE cmd, String segment, int index) {
            String i = Integer.toString(index);

            if (cmd == CTYPE.C_PUSH) {

                p.println("// Push " + segment + " " + i);
                
                if (segment.equals("constant")) {
                    p.println("@" + i);
                    p.println("D=A");
                } else if (segment.equals("temp")) {
                    p.println("@" + Integer.toString(5+index));
                    p.println("D=M");
                } else if (segment.equals("static")) {
                    p.println("@" + fileName + "." + i);
                    p.println("D=M");
                } else if (segment.equals("pointer")) {
                    p.println("@R" + Integer.toString(3+index));
                    p.println("D=M");
                }else {

                    if (segment.equals("local")) {
                        p.println("@LCL");
                    } else if (segment.equals("argument")) {
                         p.println("@ARG");
                    } else if (segment.equals("this")) {
                        p.println("@THIS");
                    } else if (segment.equals("that")) {
                        p.println("@THAT");
                    }
                        p.println("D=M");
                        p.println("@" + i);
                        p.println("D=D+A");
                        p.println("A=D");
                        p.println("D=M");
                }

                

                p.println("@SP");
                p.println("A=M");
                p.println("M=D");
                p.println("@SP");
                p.println("M=M+1");
            
            
        } else if (cmd == CTYPE.C_POP) {
            p.println("// Pop " + segment + " " + i);
            if (segment.equals("temp")) { 
                p.println("@SP");
                p.println("AM=M-1");
                p.println("D=M");
                p.println("@" + Integer.toString(5+index));
                p.println("M=D");
            } else if (segment.equals("pointer")) {
                p.println("@SP");
                p.println("AM=M-1");
                p.println("D=M");
                p.println("@" + Integer.toString(3+index));
                p.println("M=D");
            } else if (segment.equals("static")) {
                p.println("@SP");
                p.println("AM=M-1");
                p.println("D=M");
                p.println("@" + fileName + "." + i);
                p.println("M=D");
                p.println("D=A");
            } else {
                
                if (segment.equals("local")) {
                    p.println("@LCL");
                } else if (segment.equals("argument")) {
                    p.println("@ARG");
                } else if (segment.equals("this")) {
                    p.println("@THIS");
                } else if (segment.equals("that")) {
                    p.println("@THAT");
                }
                
                p.println("D=M");
                p.println("@" + i);
                p.println("D=D+A");
                p.println("@R13");
                p.println("M=D");
                p.println("@SP");
                p.println("AM=M-1");
                p.println("D=M");
                p.println("@R13");
                p.println("A=M");
                p.println("M=D");
            }

            }
        }
        
        public void writeInit() {
            p.println("// Init");
            p.println("@256");
            p.println("D=A");
            p.println("@SP");
            p.println("M=D");
            writeCall("Sys.init", 0);
        }
            
        public void writeLabel(String label) {
            p.println("// Label " + label);
            p.println("(" + label + ")");
        }

        public void writeGoto(String label) {
            p.println("// Goto " + label);
            p.println("@" + label);
            p.println("0;JMP");
        }

        public void writeIf(String label) {
            p.println("// If " + label);
            p.println("@SP");
            p.println("AM=M-1");
            p.println("D=M");
            p.println("@" + label);
            p.println("D;JNE");
        }

        public void writeCall(String fnName, int numArgs) {
            p.println("// Call " + fnName);
            p.println("@ret:" + Integer.toString(r)); // Push Return Address
            p.println("D=A");
            p.println("@SP");
            p.println("A=M");
            p.println("M=D");
            p.println("@SP");
            p.println("M=M+1");
            
            p.println("@LCL"); // Save LCL of caller
            p.println("D=M");
            p.println("@SP");
            p.println("A=M");
            p.println("M=D");
            p.println("@SP");
            p.println("M=M+1"); 
        
            p.println("@ARG"); // Save ARG of caller
            p.println("D=M");
            p.println("@SP");
            p.println("A=M");
            p.println("M=D");
            p.println("@SP");
            p.println("M=M+1");

            p.println("@THIS"); // Save THIS of caller
            p.println("D=M");
            p.println("@SP");
            p.println("A=M");
            p.println("M=D");
            p.println("@SP");
            p.println("M=M+1");

            p.println("@THAT"); // Save THAT of caller
            p.println("D=M");
            p.println("@SP");
            p.println("A=M");
            p.println("M=D");
            p.println("@SP");
            p.println("M=M+1");
            p.println("D=M");
            
            p.println("@" + Integer.toString(numArgs)); // Reposition ARG
            p.println("D=D-A");
            p.println("@5");
            p.println("D=D-A");
            p.println("@ARG");
            p.println("M=D");
            

            p.println("@SP"); // Reposition LCL
            p.println("D=M");
            p.println("@LCL");
            p.println("M=D");

            writeGoto(fnName); // Go to callee

            p.println("(ret:" + Integer.toString(r) + ")"); // Set return address

            r += 1;
        }

        public void writeReturn() {
            p.println("// Return");

            p.println("@LCL"); // Save LCL in R13
            p.println("D=M");
            p.println("@R13");
            p.println("M=D");

            p.println("@5"); // Save return address in R14
            p.println("D=D-A");
            p.println("A=D");
            p.println("D=M");
            p.println("@R14");
            p.println("M=D");

            p.println("@SP"); // Reposition return value
            p.println("AM=M-1");
            p.println("D=M");
            p.println("@ARG");
            p.println("A=M");
            p.println("M=D");

            p.println("D=A+1"); // Restore SP of caller
            p.println("@SP");
            p.println("M=D");

            p.println("@R13"); // Restore THAT of caller
            p.println("AM=M-1");
            p.println("D=M");
            p.println("@THAT");
            p.println("M=D"); 

            p.println("@R13"); // Restore THIS of caller
            p.println("AM=M-1");
            p.println("D=M");
            p.println("@THIS");
            p.println("M=D");

            p.println("@R13"); // Restore ARG of caller
            p.println("AM=M-1");
            p.println("D=M");
            p.println("@ARG");
            p.println("M=D");
            
            p.println("@R13"); // Restore LCL of caller
            p.println("AM=M-1");
            p.println("D=M");
            p.println("@LCL");
            p.println("M=D");

            p.println("@R14"); // Go to return address in caller
            p.println("A=M");
            p.println("0;JMP");

        }

        public void writeFunction(String fnName, int numLocals) {
            p.println("// Function " + fnName);
            p.println("(" + fnName + ")");
            for (int i = 0; i < numLocals; i++) {
                writePushPop(CTYPE.C_PUSH, "constant", 0);
            }
        }

        public void close(){
            p.close();
        }

        private static PrintWriter p;
        private static int r = 0; 

    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Error: wrong number of arguments");
            System.exit(1);
        }

        String fileName = args[0];
        File input = new File(fileName);
        
        if (input.isDirectory()) {
            
            File[] fileList = input.listFiles();
            CodeWriter c = new CodeWriter(new FileOutputStream(fileName + "/" + fileName + ".asm"));

            if (fileList != null) {

                c.writeInit();

                for (File child: fileList) {
                    String childName = child.getName();
                    if (childName.endsWith(".vm")){
                            Parser p = new Parser(new FileInputStream(fileName + "/" + childName));
                            String currentFunction = "";
                            c.setFileName(childName.substring(0, childName.length() - 3)); 
                            while (p.hasMoreCommands()) {
                                p.advance();
                                if (p.commandType() == CTYPE.C_ARITHMETIC) {
                                    c.writeArithmetic(p.arg1());
                                } else if (p.commandType() == CTYPE.C_POP || p.commandType() == CTYPE.C_PUSH) {
                                    c.writePushPop(p.commandType(), p.arg1(), p.arg2());
                                } else if (p.commandType() == CTYPE.C_FUNCTION) {
                                    currentFunction = p.arg1();
                                    c.writeFunction(p.arg1(), p.arg2());
                                } else if (p.commandType() == CTYPE.C_CALL) {
                                    c.writeCall(p.arg1(), p.arg2());
                                } else if (p.commandType() == CTYPE.C_RETURN) {
                                    c.writeReturn();
                                } else if (p.commandType() == CTYPE.C_IF) {
                                    c.writeIf(currentFunction + "$" + p.arg1());
                                } else if (p.commandType() == CTYPE.C_GOTO) {
                                    c.writeGoto(currentFunction + "$" + p.arg1());
                                } else if (p.commandType() == CTYPE.C_LABEL) {
                                    c.writeLabel(currentFunction + "$" + p.arg1());
                                }
                            }
                        }
                    }
                }

            c.close();
            }  else if (input.isFile()) {
                    String justName = fileName.substring(0, fileName.length() - 3);
                    CodeWriter c = new CodeWriter(new FileOutputStream(justName + ".asm"));

                    Parser p = new Parser(new FileInputStream(fileName));
                    String currentFunction = "";
                    c.setFileName(justName); 
                    while (p.hasMoreCommands()) {
                        p.advance();
                        if (p.commandType() == CTYPE.C_ARITHMETIC) {
                            c.writeArithmetic(p.arg1());
                        } else if (p.commandType() == CTYPE.C_POP || p.commandType() == CTYPE.C_PUSH) {
                            c.writePushPop(p.commandType(), p.arg1(), p.arg2());
                        } else if (p.commandType() == CTYPE.C_FUNCTION) {
                            currentFunction = p.arg1();
                            c.writeFunction(p.arg1(), p.arg2());
                        } else if (p.commandType() == CTYPE.C_CALL) {
                            c.writeCall(p.arg1(), p.arg2());
                        } else if (p.commandType() == CTYPE.C_RETURN) {
                            c.writeReturn();
                        } else if (p.commandType() == CTYPE.C_IF) {
                            c.writeIf(currentFunction + "$" + p.arg1());
                        } else if (p.commandType() == CTYPE.C_GOTO) {
                            c.writeGoto(currentFunction + "$" + p.arg1());
                        } else if (p.commandType() == CTYPE.C_LABEL) {
                            c.writeLabel(currentFunction + "$" + p.arg1());
                        }
                    }

                    c.close();


            }

        }
}
