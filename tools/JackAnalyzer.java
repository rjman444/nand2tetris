import java.util.*;
import java.lang.*;
import java.io.*;



public class JackAnalyzer {


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
                    while((n = r.read()) != -1){
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
            p = new PrintWriter(o);
        }
            
        private void parse() throws Exception {
            char s;
            String sym;
            switch (t.tokenType()) {
                case "SYMBOL":
                    s = t.symbol();
                    if (s == '<'){
                        sym = "&lt;";
                        p.println("<symbol> " + sym + " </symbol>");
                    } else if (s == '>'){
                        sym = "&gt;";
                        p.println("<symbol> " + sym + " </symbol>");
                    } else if (s == '"') { 
                        sym = "&quote;"; 
                        p.println("<symbol> " + sym + " </symbol>");
                    } else if (s == '&') { 
                        sym = "&amp;";
                        p.println("<symbol> " + sym + " </symbol>");
                    } else {
                        p.println("<symbol> " + t.symbol() + " </symbol>");
                    }
                    break;
                case "KEYWORD":
                    p.println("<keyword> " + t.keyWord() + " </keyword>");
                    break;
                case "IDENTIFIER":
                    p.println("<identifier> " + t.identifier() + " </identifier>");
                    break;
                case "INT_CONST":
                    p.println("<integerConstant> " + t.intVal() + " </integerConstant>");
                    break;
                case "STRING_CONST":
                    p.println("<stringConstant> " + t.stringVal() + " </stringConstant>");
                    break;
                }
            }
     

        private boolean next() throws Exception {
            if (t.hasMoreTokens()) {
                t.advance();
                return true;
            } else {
                return false;
            }
        }

        private void pn() throws Exception{
            next();
            parse();
        }

        public void po() throws Exception {
            while (t.hasMoreTokens()) {
                t.advance();
                parse();
            }
            p.close();
        }

        public void CompileClass() throws Exception {
            p.println("<class>");

            pn(); // Compile class keyword
            pn(); // Compile className
            pn(); // Compile { symbol
            next();

            while (t.tokenType().equals("KEYWORD") && (t.keyWord().equals("static") || t.keyWord().equals("field"))) {
                CompileClassVarDec(); 
                next();
            }

            while (t.tokenType().equals("KEYWORD") && (t.keyWord().equals("constructor") || t.keyWord().equals("function") || t.keyWord().equals("method"))) {
                CompileSubRoutine();
                next();
            }

            parse(); // Compile } symbol 
            p.println("</class>");
            p.close();
        }

        public  void CompileClassVarDec() throws Exception{
            p.println("<classVarDec>");
            parse(); // static or field
            pn(); // Type
            pn(); // varName 
            next();
            while (t.tokenType().equals("SYMBOL") && t.symbol() == ',') {
                parse(); // ,
                pn(); // varName
                next();
            }
            parse(); // ;
            p.println("</classVarDec>");
        }

        public  void CompileSubRoutine() throws Exception{
            p.println("<subroutineDec>");

            parse(); // constructor or function or method
            pn(); // void or type
            pn(); // subroutine name
            pn(); // (
            CompileParameterList();
            parse(); // )

            // Subroutine Body
            p.println("<subroutineBody>");
            pn(); // {

            // Var Decs
            next();

            if (t.keyWord().equals("var")) {
                CompileVarDec();
            }

        
            // Statements
            CompileStatements();

    
            parse(); // }

            p.println("</subroutineBody>");
            p.println("</subroutineDec>");
            
        }

        public  void CompileParameterList() throws Exception {
            p.println("<parameterList>");
            next();
            if (t.tokenType().equals("SYMBOL")) {
                ;
            } else {
                parse(); // type
                pn(); // varName
                next();
                while (t.tokenType().equals("SYMBOL") && t.symbol() == ',') {
                    parse(); // ,
                    pn(); // type
                    pn(); // varName
                    next();
                }
            }
            p.println("</parameterList>");
        }

        public  void CompileVarDec() throws Exception {
            while (t.tokenType().equals("KEYWORD") && t.keyWord().equals("var")) {
                p.println("<varDec>");
                parse(); // var
                pn(); // type
                pn(); // varName 

                next();

                while (t.tokenType().equals("SYMBOL") && t.symbol() == ',') {
                    parse(); // ,
                    pn(); // varName
                    next();
                }
                parse(); // ;
                next();
                p.println("</varDec>");
            }

        }

        public  void CompileStatements() throws Exception {
            p.println("<statements>");
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
            p.println("</statements>");
        }

        public void  CompileDo() throws Exception {
            p.println("<doStatement>");
            parse(); // do
            pn(); // subroutineName or className or varName
            next();
            if (t.tokenType().equals("SYMBOL") && t.symbol() == '(') {
                parse(); // (
                CompileExpressionList();
                parse(); // )
            } else if (t.symbol() == '.') {
                parse(); // .
                pn(); // subroutineName
                pn(); // (
                CompileExpressionList();
                parse(); // )
            }
            pn(); // ;
            p.println("</doStatement>");


        }

        public void  CompileLet() throws Exception{
            p.println("<letStatement>");
            parse(); // let
            pn(); // varName
            next();
            if (t.symbol() == '[') {
                parse(); // [
                next();
                CompileExpression();
                parse(); // ] possible bug
                next();
            } 

            parse(); // =
            next();
            CompileExpression();
            parse(); // ;

            p.println("</letStatement>"); 
        }
        

        public void  CompileWhile() throws Exception {
            p.println("<whileStatement>");
            parse(); // while
            pn(); // (
            next();
            CompileExpression();
            parse(); // )
            pn(); // {
            next();
            CompileStatements();
            parse(); // }
            p.println("</whileStatement>");
        }


        public void  CompileReturn() throws Exception {
            p.println("<returnStatement>"); 
            parse(); // return
            next(); 
            if (!t.tokenType().equals("SYMBOL")) {
                CompileExpression();
            }
            parse(); // ;
            p.println("</returnStatement>");
            
        }

        public void  CompileIf() throws Exception {
            p.println("<ifStatement>");
            parse(); // If
            pn(); // (
            next();
            CompileExpression();
            parse(); // )
            pn(); // {
            next();
            CompileStatements();
            parse(); // }
            next();
            if (t.tokenType().equals("KEYWORD") && t.keyWord().equals("else")) {
                parse(); // else
                pn(); // {
                next();
                CompileStatements();
                parse(); // }
                next();
            }
            p.println("</ifStatement>");
            
        }

        public void  CompileExpression() throws Exception {
            p.println("<expression>");
            CompileTerm();
            while (t.tokenType().equals("SYMBOL") && op.contains(Character.toString(t.symbol()))) {
                parse(); // op
                next();
                CompileTerm();
            }
                 
            p.println("</expression>");
        }

        public  void CompileTerm() throws Exception {
            
            p.println("<term>");
            if (t.tokenType().equals("SYMBOL")) {
                if (t.symbol() == '(') {
                    parse();
                    next();
                    CompileExpression();
                    parse();
                    next();
                } else if (t.symbol == '~' || t.symbol == '-') {
                    parse();
                    next();
                    CompileTerm();
                }
            } else {
                    parse(); 
                    next();
                    if (t.tokenType().equals("SYMBOL")) {
                        if (t.symbol() == '[') {
                            parse();
                            next();
                            CompileExpression();
                            parse();
                            next();
                        } else if (t.symbol() == '(') {
                            parse();
                            CompileExpressionList();
                            parse();
                            next();
                        } else if (t.symbol() == '.') {
                            parse();
                            pn();
                            pn();
                            CompileExpressionList();
                            parse();
                            next();
                        }
                    }
                        
                }
            p.println("</term>");
        }
         

        public void CompileExpressionList() throws Exception {
            p.println("<expressionList>");
            next();
            if (!t.tokenType().equals("SYMBOL") || t.symbol() != ')') {
                CompileExpression();
                while (t.symbol() == ',') {
                    parse(); // ,
                    next();
                    CompileExpression();
                }
            }
            p.println("</expressionList>");
            

        }

        private String op = "+-*/&|<>=";
        private String unary = "-~";

        private JackTokenizer t;
        private PrintWriter p;
    }



    public static void main(String[] args) throws Exception {
        File input = new File(args[0]);

        if (input.isDirectory()) {
            for (File child: input.listFiles()) {
                if (child.getName().endsWith(".jack")) {
                    FileInputStream fi = new FileInputStream(child);
                    FileOutputStream fo = new FileOutputStream(args[0] + "/" + child.getName().substring(0, child.getName().length() - 5) + ".xml");
                    CompilationEngine ce = new CompilationEngine(fi, fo);
                    ce.CompileClass();
                }
            }
        } else if (input.isFile()) {

            FileInputStream fi = new FileInputStream(args[0]);
            FileOutputStream fo  = new FileOutputStream(args[0].substring(0, args[0].length() - 5) + ".xml");
            CompilationEngine ce = new CompilationEngine(fi, fo);
            ce.CompileClass();
        }

    }
}


