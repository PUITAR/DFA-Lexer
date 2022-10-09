/*
 * @author: puitar
 * @Description: lexer
 * @Date: 2022-10-09 09:06:35
 */

import java.io.FileInputStream;
/* 
 * DFA(q0, S, F, delta)
 */
public class DFALexer {
    private static enum State {
        START, NOTATION, NUM, ID, ASSIGN, DONE, ERROR;
    }
    private String word;
    private State cur;
    private FileInputStream in;
    private boolean rollback;

    public DFALexer(String path) {
        rollback = false;
        word = new String();
        cur = State.START;
        try {
            in = new FileInputStream(path);
        }
        catch (Exception e) {
            throw new Error("[NONE TINY FILE]");
        }
    }

    private State start(int c) {
        if      (c == ' ')              return State.START;
        else if (c == '{')              return State.NOTATION;
        else if (c == ':')              return State.ASSIGN;
        else if (Character.isDigit(c))  return State.NUM;
        else if (Character.isLetter(c)) return State.ID;
        else                            return State.DONE;
    }
    private State notation(int c) {
        switch (c) {
            case '}':                   return State.START;
            default :                   return State.NOTATION;
        }
    }
    private State num(int c) {
        if (Character.isDigit(c))       return State.NUM;
        else                            return State.DONE;
    }
    private State id(int c) {
        if (Character.isLetter(c))      return State.ID;
        else                            return State.DONE;
    }
    private State assign(int c) {
        if (c == '=')                   return State.DONE;
        else                            throw new Error("[Syntax Error of ':']");
    }

    private State nextState(int c) { 
        switch (cur) {
            case START      : return start(c);
            case NOTATION   : return notation(c);
            case NUM        : return num(c);
            case ID         : return id(c);
            case ASSIGN     : return assign(c);
            // case DONE    : never get into this state
            default         : break;
        }
        return State.ERROR;
    }

    // get next char. if rollback was set, will rollback
    private int nextChar() {
        try {
            return in.read();
        }
        catch (Exception e) {
            throw new Error("[Next Character Error]");
        }
    }

    private void formatPrint(String type) {
        System.out.println(word + " % " + type);
    }

    private boolean wordIsNotation() {
        int len = word.length();
        if (len >= 2 && word.charAt(0) == '{' && word.charAt(len - 1) == '}')
            return true;
        return false;
    }

    // main function parser
    public void scan() {
        int c = 0;
        while (true) {
            if (rollback) rollback = false;
            else c = nextChar();
            if (c == -1) {
                System.out.println("[all works done]");
                return ;
            }
            State temp = cur;
            cur = nextState(c);
            if (cur == State.ERROR) throw new Error("[Syntax Error]");
            else if (cur == State.DONE) {
                if (temp == State.ID) {
                    if      (word.equals("if"))          formatPrint("IF");  
                    else if (word.equals("then"))        formatPrint("THEN");
                    else if (word.equals("else"))        formatPrint("ELSE");
                    else if (word.equals("end"))         formatPrint("END");
                    else if (word.equals("repeat"))      formatPrint("REPEAT");
                    else if (word.equals("until"))       formatPrint("UNTIL");
                    else if (word.equals("read"))        formatPrint("READ");
                    else if (word.equals("write"))       formatPrint("WRITE");
                    else                                          formatPrint("ID");
                    if (c != ' ') rollback = true;
                }
                else if (temp == State.NUM)                       formatPrint("NUM"); 
                else if (temp == State.ASSIGN) {
                    word = ":=";                                  formatPrint("ASSIGN");
                }                   
                else { // temp == State.START     
                    if (wordIsNotation());
                    else {
                        word += (char) c;
                        if      (c == '=')                        formatPrint("EQ");
                        else if (c == '<')                        formatPrint("LT");
                        else if (c == '+')                        formatPrint("PLUS");
                        else if (c == '-')                        formatPrint("MINUS");
                        else if (c == '*')                        formatPrint("TIMES");
                        else if (c == '/')                        formatPrint("OVER");
                        else if (c == '(')                        formatPrint("LPARENT");
                        else if (c == ')')                        formatPrint("RPARENT");
                        else if (c == ';')                        formatPrint("SEMI");       
                        else if (c == ' ' || c == '\t' || c == 13 || c == 10); // CR: 13; NL: 10
                        else throw new Error("[Syntax Error]");           
                    }                                         
                }  
                word = "";
                cur = State.START;
            }
            else {
                if (c != ' ' && c != '\t' && c != 13 && c != 10)
                    word += (char) c;
            }
        }
    }

    public static void main(String[] args) {
        DFALexer parser = new DFALexer(args[0]);
        // DFALexer parser = new DFALexer("sample.tny");
        parser.scan();
    }
}