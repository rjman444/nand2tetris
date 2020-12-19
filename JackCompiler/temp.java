
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
