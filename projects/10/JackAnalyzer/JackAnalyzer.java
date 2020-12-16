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
                r.readLine();
                return hasMoreTokens();
            } else if (n == 10 || n == 13 || n == 32) {
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
                    if (kw.equals(sb.toString().toUpperCase())) {
                        tokenType = "KEYWORD";
                        keyWord = sb.toString().toUpperCase();
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
        private String[] keyWords = {"CLASS", "METHOD", "FUNCTION", "CONSTRUCTOR", "INT", "BOOLEAN", "CHAR", "VOID", "VAR", "STATIC", "FIELD", "LET", "DO", "IF", "ELSE", "WHILE", "RETURN", "TRUE", "FALSE", "NULL", "THIS"};
        private BufferedReader r;
        public char next;
    }

    private static class CompilationEngine {
        public CompilationEngine(FileInputStream i, FileOutputStream o) {
            tokenizer = new JackTokenizer(i);
            p = new PrintWriter(o);
        }

        public void CompileClass() throws Exception {
            p.println("<tokens>");
            char s;
            String sym;
            while(tokenizer.hasMoreTokens()) {
                tokenizer.advance();
                switch (tokenizer.tokenType()) {
                    case "SYMBOL":
                        s = tokenizer.symbol();
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
                            p.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                        }
                        break;
                    case "KEYWORD":
                        p.println("<keyword> " + tokenizer.keyWord() + " </keyword>");
                        break;
                    case "IDENTIFIER":
                        p.println("<identifier> " + tokenizer.identifier() + " </identifier>");
                        break;
                    case "INT_CONST":
                        p.println("<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
                        break;
                    case "STRING_CONST":
                        p.println("<stringConstant> " + tokenizer.stringVal() + " </stringConstant >");
                        break;
                }
            }
            p.println("</tokens>");
            p.close();
        }

        public  void CompileClassVarDec() {
        }

        public  void CompileSubRoutine() {
        }

        public  void CompileParameterList() {
        }

        public  void CompileVarDec() {
        }

        public  void CompileStatements() {
        }

        public void  CompileDo() {
        }

        public void  CompileLet() {
        }

        public void  CompileWhile() {
        }

        public void  CompileReturn() {
        }

        public void  CompileIf() {
        }

        public void  CompileExpression() {
        }

        public  void CompileTerm() {
        }

        public void CompileExpressionList() {
        }

        private JackTokenizer tokenizer;
        private PrintWriter p;
    }



    public static void main(String[] args) throws Exception {
        FileInputStream fi = new FileInputStream(new File("square.jack"));
        FileOutputStream fo = new FileOutputStream(new File("square.xml"));
        CompilationEngine ce = new CompilationEngine(fi, fo);
        ce.CompileClass();
    }
}


