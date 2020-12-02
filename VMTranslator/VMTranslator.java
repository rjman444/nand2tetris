import java.lang.*;
import java.util.*;
import java.io.*;

public class VMTranslator {

    enum CTYPE {
        C_ARITHMETIC,
        C_PUSH,
        C_POP
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
                String[] cmd = line.split(" ");
                
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
            p.println("@R0"); // Access stack pointer
            
            if (cmd.equals("not")) {
                p.println("A=M-1");
                p.println("M=!M");
            } else if (cmd.equals("neg")) {
                p.println("A=M-1");
                p.println("M=-M");
            } else {
                p.println("AM=M-1"); // Point to top element of stack and go there
                p.println("D=M"); // Get y
                p.println("@R0");
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

                p.println("@R0");
                p.println("A=M-1");
                p.println("M=0");
                p.println("@END:" + n); 
                p.println("0;JMP");
                p.println("(TRUE:" + n + ")");
                p.println("@R0");
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
                        p.println("@R1");
                    } else if (segment.equals("argument")) {
                         p.println("@R2");
                    } else if (segment.equals("this")) {
                        p.println("@R3");
                    } else if (segment.equals("that")) {
                        p.println("@R4");
                    }
                        p.println("D=M");
                        p.println("@" + i);
                        p.println("D=D+A");
                        p.println("A=D");
                        p.println("D=M");
                }

                

                p.println("@R0");
                p.println("A=M");
                p.println("M=D");
                p.println("@R0");
                p.println("M=M+1");
            
            
        } else if (cmd == CTYPE.C_POP) {
            p.println("// Pop " + segment + " " + i);
            if (segment.equals("temp")) { 
                p.println("@R0");
                p.println("AM=M-1");
                p.println("D=M");
                p.println("@" + Integer.toString(5+index));
                p.println("M=D");
            } else if (segment.equals("pointer")) {
                p.println("@R0");
                p.println("AM=M-1");
                p.println("D=M");
                p.println("@" + Integer.toString(3+index));
                p.println("M=D");
            } else if (segment.equals("static")) {
                p.println("@R0");
                p.println("AM=M-1");
                p.println("D=M");
                p.println("@" + fileName + "." + i);
                p.println("M=D");
                p.println("D=A");
            } else {
                
                if (segment.equals("local")) {
                    p.println("@R1");
                } else if (segment.equals("argument")) {
                    p.println("@R2");
                } else if (segment.equals("this")) {
                    p.println("@R3");
                } else if (segment.equals("that")) {
                    p.println("@R4");
                }
                
                p.println("D=M");
                p.println("@" + i);
                p.println("D=D+A");
                p.println("@R13");
                p.println("M=D");
                p.println("@R0");
                p.println("AM=M-1");
                p.println("D=M");
                p.println("@R13");
                p.println("A=M");
                p.println("M=D");
            }

            }
        }

        public void close(){
            p.close();
        }

        private static PrintWriter p;

    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Error: wrong number of arguments");
            System.exit(1);
        }

        String fileName = args[0];
        String outputName = fileName.substring(0, fileName.length() - 3);
        Parser p = new Parser(new FileInputStream(fileName));
        CodeWriter c = new CodeWriter(new FileOutputStream(outputName + ".asm"));
        c.setFileName(outputName); 
        while (p.hasMoreCommands()) {
            p.advance();
            if (p.commandType() == CTYPE.C_ARITHMETIC) {
                c.writeArithmetic(p.arg1());
            } else if (p.commandType() == CTYPE.C_POP || p.commandType() == CTYPE.C_PUSH) {
                c.writePushPop(p.commandType(), p.arg1(), p.arg2());
            }
        }

        c.close();

        

    }
}
