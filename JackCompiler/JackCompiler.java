import java.util.*;
import java.lang.*;
import java.io.*;



public class JackCompiler {

    private static class SymbolTable {
        

        public SymbolTable() {
            ctb = new HashMap<>();
            srtb = new HashMap<>();
            argCount = varCount = staticCount = fieldCount = 0;
            }

       public void startSubroutine() {
           srtb = new HashMap<>();
           argCount = varCount = 0;
       }

       public void Define(String name, String type, String kind) {
           kind = kind.toUpperCase();
           if (kind.equals("STATIC")) {
               Symbol S = new Symbol(type, kind, staticCount++);
               ctb.put(name, S);
           } else if (kind.equals("FIELD")) {
               Symbol S = new Symbol(type, kind, fieldCount++);
               ctb.put(name, S);
           } else if (kind.equals("ARG")) {
               Symbol S = new Symbol(type, kind, argCount++);
               srtb.put(name, S);
           } else if (kind.equals("VAR")) {
               Symbol S = new Symbol(type, kind, varCount++);
               srtb.put(name, S);
           } else {
               System.out.println("ERROR: Wrong kind given to symbol table");
           }

       }

       public int VarCount(String kind) {
           if (kind.equals("STATIC")) {
               return staticCount + 1;
           } else if (kind.equals("FIELD")) {
               return fieldCount + 1;
           } else if (kind.equals("ARG")) {
               return argCount + 1;
           } else if (kind.equals("VAR")) {
               return varCount + 1;
           } else {
               System.out.println("ERROR: Wrong kind given to VarCount");
               return 0;
           }
       }

       public String KindOf(String name) {
           Symbol S;
           S = srtb.get(name);
           if (S == null) {
               S = ctb.get(name);
           }

           if (S == null) {
               return "NONE";
           } else {
               return S.kind;
           }
       }

       public String TypeOf(String name) {
           Symbol S;
           S = srtb.get(name);
           if (S == null) {
               S = ctb.get(name);
           }

           if (S == null) {
               return "NONE";
           } else {
               return S.type;
           }
       }

       public int IndexOf(String name) {
           Symbol S;
           S = srtb.get(name);
           if (S == null) {
               S = ctb.get(name);
           }

           if (S == null) {
               return -1;
           } else {
               return S.index;
           }
       }
        

        private int argCount, varCount, staticCount, fieldCount;
        private HashMap<String, Symbol> ctb, srtb;

        private class Symbol {

            public int index;
            public String type;
            public String kind;

            public Symbol(String t, String k, int i) {
                type = t;
                kind = k;
                index = i;
            }
        }

    }


    private static class VMWriter {

        public VMWriter(OutputStream o) {
            p = new PrintWriter(o);
        }

        public void writePush(String segment, int index) {
            String i = Integer.toString(index);
            switch (segment) {
                case "CONST":
                   p.println("push constant " + i);
                   break;
                case "ARG":
                   p.println("push argument " + i);
                   break;
                case "LOCAL":
                   p.println("push local " + i);
                   break;
                case "STATIC":
                   p.println("push static " + i);
                   break;
                case "THIS":
                   p.println("push this " + i);
                   break;
                case "THAT":
                   p.println("push that " + i);
                   break;
                case "POINTER":
                   p.println("push pointer " + i);
                   break;
                case "TEMP":
                   p.println("push temp " + i);
                   break;
            }
        }
        
        public void writePop(String segment, int index) {
            String i = Integer.toString(index);
            switch (segment) {
                case "CONST":
                   p.println("pop constant " + i);
                   break;
                case "ARG":
                   p.println("pop argument " + i);
                   break;
                case "LOCAL":
                   p.println("pop local " + i);
                   break;
                case "STATIC":
                   p.println("pop static " + i);
                   break;
                case "THIS":
                   p.println("pop this " + i);
                   break;
                case "THAT":
                   p.println("pop that " + i);
                   break;
                case "POINTER":
                   p.println("pop pointer " + i);
                   break;
                case "TEMP":
                   p.println("pop temp " + i);
                   break;
            }
        }

        public void writeArithmetic(String command) {
           p.println(command.toLowerCase());
        }

        public void writeLabel(String label) {
           p.println("label " + label);
        }

        public void writeGoto(String label) {
           p.println("goto " + label);
        }

        public void writeIf(String label) {
           p.println("if-goto " + label);
        }

        public void writeCall(String name, int nArgs) {
           p.println("call " + name + " " + Integer.toString(nArgs));
        }

        public void writeFunction(String name, int nLocals) {
           p.println("function " + name + " " + Integer.toString(nLocals));
        }

        public void writeReturn() {
           p.println("return");
        }

        public void close() {
            p.close();
        }

        private PrintWriter p;
    }

     

    private static class JackTokenizer {

        public JackTokenizer(InputStream i) {
            r = new BufferedReader(new InputStreamReader(i));
        }
        
        public boolean hasMoreTokens() throws Exception {

            if (pause) {
                pause = false;
                return true;
            }

            int n = r.read();

            if (n == 47) {
                n = r.read();
                if (n == 47) {
                    r.readLine();
                    return hasMoreTokens();
                } else if (n == 42) {
                    r.read();
                    while ((n = r.read()) != -1) {
                        if (n == 42 && r.read() == 47)  break;
                    } 
                    r.read();
                    return hasMoreTokens();
                } else {
                    next = (char) 47;
                    return true;
                }
            } else if (n == 9 || n == 10 || n == 13 || n == 32) {
                return hasMoreTokens();
            } else if (n == -1) {
                return false;
            } else {
                next = (char) n;
                return true;
            }
        }
        
        public void advance() throws Exception {
            if (symbols.contains(Character.toString(next))) { // If next is symbol
                tokenType = "SYMBOL";
                symbol = next; 
            } else if (Character.isLetter(next)) { // If next is keyword or identifier
                sb = new StringBuilder();
                sb.append(next);
                int n;
                while((n = r.read()) != -1 ) {
                    next = (char) n;
                    if (Character.isLetter(next) || Character.isDigit(next)) {
                        sb.append(next);
                    } else if (n == 32){
                        break;
                    } else {
                        pause = true;
                        break;
                    }
                }

                boolean isKeyWord = false;

                for (String kw: keyWords) {
                    if (kw.equals(sb.toString().toLowerCase())) {
                        tokenType = "KEYWORD";
                        keyWord = kw;
                        isKeyWord = true;
                        break; 
                    }
                }

                if (!isKeyWord) {
                    tokenType = "IDENTIFIER";
                    identifier = sb.toString();
                }
            } else if (next == '"') { // If next is string const
                sb = new StringBuilder();
                int n;
                while((n = r.read()) != -1 ) {
                    next = (char) n;
                    if (next != '"') {
                        sb.append(next);
                    } else {
                        break;
                    }
                }
                tokenType = "STRING_CONST";
                stringVal = sb.toString();
            } else if (Character.isDigit(next)) { // If next is int const
                sb = new StringBuilder();
                sb.append(next);
                int n;
                while((n = r.read()) != -1 ) {
                    next = (char) n;
                    if (Character.isDigit(next)) {
                        sb.append(next);
                    } else if (n == 32) {
                        break;
                    } else {
                        pause = true;
                        break;
                    }
                }
                tokenType = "INT_CONST";
                intVal = Integer.parseInt(sb.toString());
            } 


        }


        public String tokenType() {
            return tokenType;
        }

        public String keyWord() {
            return keyWord;
        }

        public char symbol() {
            return symbol;
        }

        public String identifier() {
            return identifier;
        }

        public int intVal() {
            return intVal;
        }

        public String stringVal() {
            return stringVal;
        }
            
            
        private boolean pause = false;
        private StringBuilder sb;
        private String tokenType;
        private char symbol;
        private String identifier;
        private int intVal;
        private String stringVal;
        private String keyWord;
        private String symbols = "{}()[].,;+-*/&|<>=~"; 
        private String[] keyWords = {"class", "method", "function", "constructor", "int", "boolean", "char", "void", "var", "static", "field", "let", "do", "if", "else", "while", "return", "true", "false", "null", "this"};
        private BufferedReader r;
        public char next;
    }

    private static class CompilationEngine {
        public CompilationEngine(FileInputStream i, FileOutputStream o) {
            t = new JackTokenizer(i);
            v = new VMWriter(o);
            st = new SymbolTable();
        }
            
        private void next() throws Exception {
            if (t.hasMoreTokens()) {
                t.advance();
            }
        }

        private void currentToken() throws Exception {
            switch (t.tokenType()) {
                case "IDENTIFIER":
                    System.out.println("Current Token is Identifier: " + t.identifier());
                    break;
                case "KEYWORD":
                    System.out.println("Current Token is Keyword: " + t.keyWord());
                    break;
                case "SYMBOL":
                    System.out.println("Current Token is Symbol: " + t.symbol());
                    break;
                case "INT_CONST":
                    System.out.println("Current Token is Int Constant: " + t.intVal());
                    break;
                case "STRING_CONST":
                    System.out.println("Current Token is String Constant " + t.stringVal());
                    break;
            }
        }

        public void CompileClass() throws Exception {

            next(); // Compile class keyword
            next(); // Compile className
            next(); // Compile { symbol
            next();

            while (t.tokenType().equals("KEYWORD") && (t.keyWord().equals("static") || t.keyWord().equals("field"))) {
                CompileClassVarDec(); 
                next();
            }

            while (t.tokenType().equals("KEYWORD") && (t.keyWord().equals("constructor") || t.keyWord().equals("function") || t.keyWord().equals("method"))) {
                CompileSubRoutine();
                next();
            }

            next(); // Compile } symbol 
            v.close();
        }

        public void CompileClassVarDec() throws Exception{
            String kind = t.keyWord();
            String type = "";
            next();
            if (t.tokenType().equals("KEYWORD")) {
                type = t.keyWord();
            } else {
                type = t.identifier();
            }
            next();
            String name = t.identifier(); 
            st.Define(name, type, kind); 
            System.out.println(name + " " + st.KindOf(name) + " " + st.TypeOf(name) + " " + st.IndexOf(name));
            next();
            while (t.tokenType().equals("SYMBOL") && t.symbol() == ',') {
                next();
                name = t.identifier();
                st.Define(name, type, kind);
                System.out.println(name + " " + st.KindOf(name) + " " + st.TypeOf(name) + " " + st.IndexOf(name));
                next();
            }
        }

        public void CompileSubRoutine() throws Exception {
            st.startSubroutine();
            
            next(); // constructor or function or method
            next(); // void or type
            System.out.println("Compiling " + t.identifier());
            String subRoutineName = t.identifier();
            next(); // subroutine name

            int locals = CompileParameterList();
            next();

            v.writeFunction(subRoutineName, locals);

            // Subroutine Body
            next(); // {


            // Var Decs

            while (t.tokenType().equals("KEYWORD") && t.keyWord().equals("var")) {
                CompileVarDec();
                next();
            }

            // Statements
            CompileStatements();
        }

        public int CompileParameterList() throws Exception {
            int locals = 0;
            next();
            if (t.tokenType().equals("SYMBOL")) {
                ;
            } else {
                String type = t.tokenType().equals("KEYWORD") ? t.keyWord() : t.identifier(); 
                next(); 
                String name = t.identifier();
                next(); 
                st.Define(name, type, "ARG");
                locals++;
                System.out.println(name + " " + st.KindOf(name) + " " + st.TypeOf(name) + " " + st.IndexOf(name));

                while (t.tokenType().equals("SYMBOL") && t.symbol() == ',') {
                    next(); 
                    type = t.tokenType().equals("KEYWORD") ? t.keyWord() : t.identifier(); 
                    next(); 
                    name = t.identifier();
                    next();
                    st.Define(name, type, "ARG");
                    locals++;
                    System.out.println(name + " " + st.KindOf(name) + " " + st.TypeOf(name) + " " + st.IndexOf(name));
                }
            }

            return locals;
        }

        public void CompileVarDec() throws Exception {
                next(); // var
                String type = t.tokenType().equals("KEYWORD") ? t.keyWord() : t.identifier();
                next(); // type
                String name = t.identifier();
                st.Define(name, type, "VAR");
                System.out.println(name + " " + st.KindOf(name) + " " + st.TypeOf(name) + " " + st.IndexOf(name));
                next(); // varName 

                while (t.tokenType().equals("SYMBOL") && t.symbol() == ',') {
                    next(); // ,
                    name = t.identifier();
                    st.Define(name, type, "VAR");
                    System.out.println(name + " " + st.KindOf(name) + " " + st.TypeOf(name) + " " + st.IndexOf(name));
                    next();
                }
        }

        public  void CompileStatements() throws Exception {
            while (t.tokenType().equals("KEYWORD")) {
                if (t.keyWord().equals("let")) {
                    CompileLet();
                    next();
                } else if (t.keyWord().equals("if")) {
                    CompileIf();
                } else if (t.keyWord().equals("while")) {
                    CompileWhile();
                    next();
                } else if (t.keyWord().equals("do")) {
                    CompileDo();
                    next();
                } else if (t.keyWord().equals("return")) {
                    CompileReturn();
                    next();
                } else {
                    System.out.println("ERR");
                    break;
                }
            }
        }

        public void  CompileDo() throws Exception {
            next(); // do
            next(); // subroutineName or className or varName
            next();
            if (t.tokenType().equals("SYMBOL") && t.symbol() == '(') {
                next(); // (
                CompileExpressionList();
                next(); // )
            } else if (t.symbol() == '.') {
                next(); // .
                next(); // subroutineName
                next(); // (
                CompileExpressionList();
                next(); // )
            }
            next(); // ;


        }

        public void  CompileLet() throws Exception{
            next(); // let
            String varName = t.identifier();
            System.out.println("Compiling Let " + varName); 
            String segment;
            if (st.KindOf(varName).equals("FIELD")) {
                segment = "THIS";
            } else if (st.KindOf(varName).equals("VAR")) {
                segment = "LOCAL";
            } else {
                segment = st.KindOf(varName);
            }
            int index = st.IndexOf(varName);
            next(); 
            if (t.symbol() == '[') {
                next(); // [
                next();
                CompileExpression();
                next(); // ] possible bug
                next();
            } 

            next();
            CompileExpression();
            currentToken();
            v.writePop(segment, index);

        }
        

        public void  CompileWhile() throws Exception {
            next(); // while
            next(); // (
            next();
            CompileExpression();
            next(); // )
            next(); // {
            next();
            CompileStatements();
            next(); // }
        }


        public void  CompileReturn() throws Exception {
            next(); // return
            next(); 
            if (!t.tokenType().equals("SYMBOL")) {
                CompileExpression();
            }
            next(); // ;
            
        }

        public void  CompileIf() throws Exception {
            next(); // If
            next(); // (
            next();
            CompileExpression();
            next(); // )
            next(); // {
            next();
            CompileStatements();
            next(); // }
            next();
            if (t.tokenType().equals("KEYWORD") && t.keyWord().equals("else")) {
                next(); // else
                next(); // {
                next();
                CompileStatements();
                next(); // }
                next();
            }
            
        }

        public void  CompileExpression() throws Exception {
            CompileTerm();
            while (t.tokenType().equals("SYMBOL") && op.contains(Character.toString(t.symbol()))) {
                char o = t.symbol();
                next(); 
                CompileTerm();
                switch (o) {
                    case '+':
                        v.writeArithmetic("ADD");
                        break;
                    case '-':
                        v.writeArithmetic("SUB");
                        break;
                    case '=':
                        v.writeArithmetic("EQ");
                        break;
                    case '>':
                        v.writeArithmetic("GT");
                        break;
                    case '<':
                        v.writeArithmetic("LT");
                        break;
                    case '&':
                        v.writeArithmetic("AND");
                        break;
                    case '|':
                        v.writeArithmetic("OR");
                        break;
                    case '*':
                        v.writeCall("Math.Multiply", 2);
                        break;
                }
            }
                 
        }

        public void CompileTerm() throws Exception {
            if (t.tokenType().equals("INT_CONST")) {
                v.writePush("CONST", t.intVal);
                next();
            } 

            if (t.tokenType().equals("IDENTIFIER")) {
                String name = t.identifier();
                next();
                if (t.symbol == '[') {
                    ; // Array
                } else if (t.symbol == '.') {
                    next();
                    String m = t.identifier();
                    next(); // (
                    next();
                    int args = CompileExpressionList();
                    v.writeCall(name + "." + m, args);
                    ; // Subroutine Call with Class
                } else if (t.symbol == '(') {
                    next();
                    int args = CompileExpressionList();
                    v.writeCall(name, args);
                } else {
                    System.out.println("Correct");
                    String segment;
                    if (st.KindOf(name).equals("FIELD")) {
                        segment = "THIS";
                    } else if (st.KindOf(name).equals("VAR")) {
                        segment = "LOCAL";
                    } else {
                        segment = st.KindOf(name);
                    }
                    int index = st.IndexOf(name);
                    v.writePush(segment, index);
                }
            }
            
            if (t.tokenType().equals("SYMBOL")) {
                if (t.symbol() == '(') {
                    next();
                    CompileExpression();
                    next();
                } else if (t.symbol == '~') {
                    next();
                    CompileTerm();
                    v.writeArithmetic("NOT");
                } else if (t.symbol == '-') {
                    next();
                    CompileTerm();
                    v.writeArithmetic("NEG");
                }
            } 

        }
         

        public int CompileExpressionList() throws Exception {
            int args = 0;
            if (!t.tokenType().equals("SYMBOL") || t.symbol != ')') {
                CompileExpression();
                args++;
                while (t.symbol() == ',') {
                    next(); // ,
                    CompileExpression();
                    args++;
                }
            }
            next(); // )
            return args;
        }
            
        private String op = "+-*/&|<>=";
        private String unary = "-~";

        private JackTokenizer t;
        private VMWriter v;
        private SymbolTable st;
    }



    public static void main(String[] args) throws Exception {
        File input = new File(args[0]);

        if (input.isDirectory()) {
            for (File child: input.listFiles()) {
                if (child.getName().endsWith(".jack")) {
                    FileInputStream fi = new FileInputStream(child);
                    FileOutputStream fo = new FileOutputStream(args[0] + "/" + child.getName().substring(0, child.getName().length() - 5) + ".vm");
                    CompilationEngine ce = new CompilationEngine(fi, fo);
                    ce.CompileClass();
                }
            }
        } else if (input.isFile()) {

            FileInputStream fi = new FileInputStream(args[0]);
            FileOutputStream fo  = new FileOutputStream(args[0].substring(0, args[0].length() - 5) + ".vm");
            CompilationEngine ce = new CompilationEngine(fi, fo);
            ce.CompileClass();
        }

    }
}


