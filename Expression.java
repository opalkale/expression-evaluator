package apps;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Scanner;
import java.util.StringTokenizer;

import structures.Stack;

public class Expression {

    /**
     * Expression to be evaluated
     */
    String expr;

    /**
     * Scalar symbols in the expression
     */
    ArrayList<ScalarSymbol> scalars;

    /**
     * Array symbols in the expression
     */
    ArrayList<ArraySymbol> arrays;

    /**
     * Positions of opening brackets
     */
    ArrayList<Integer> openingBracketIndex;

    /**
     * Positions of closing brackets
     */
    ArrayList<Integer> closingBracketIndex;

    /**
     * String containing all delimiters (characters other than variables and constants),
     * to be used with StringTokenizer
     */
    public static final String delims = " \t*+-/()[]";

    /**
     * Initializes this Expression object with an input expression. Sets all other
     * fields to null.
     *
     * @param expr Expression
     */
    public Expression(String expr) {
        this.expr = expr;
        scalars = null;
        arrays = null;
        openingBracketIndex = null;
        closingBracketIndex = null;
    }

    /**
     * Matches parentheses and square brackets. Populates the openingBracketIndex and
     * closingBracketIndex array lists in such a way that closingBracketIndex[i] is
     * the position of the bracket in the expression that closes an opening bracket
     * at position openingBracketIndex[i]. For example, if the expression is:
     * <pre>
     *    (a+(b-c))*(d+A[4])
     * </pre>
     * then the method would return true, and the array lists would be set to:
     * <pre>
     *    openingBracketIndex: [0 3 10 14]
     *    closingBracketIndex: [8 7 17 16]
     * </pre>
     *
     * @return True if brackets are matched correctly, false if not
     */
    public boolean isLegallyMatched() {
        openingBracketIndex = new ArrayList<Integer>();
        closingBracketIndex  = new ArrayList<Integer>();
        // COMPLETE THIS METHOD
        Stack<Bracket> brackets = new Stack<Bracket>();
        Stack<Bracket> close = new Stack<Bracket>();
        for (int i = 0; i < expr.length(); i++)
        {
            if (expr.charAt(i) == '(' || expr.charAt(i) == '[')
            {
                    brackets.push(new Bracket(expr.charAt(i), i));
                    openingBracketIndex.add(i);
            }

            else if (expr.charAt(i) == ')')
            {
            	if (brackets.isEmpty())
            		return false;
                if (brackets.peek().ch == '(')
                {
                    brackets.pop();
                    if (brackets.isEmpty())
                    {
                        closingBracketIndex.add(i);
                        while (!close.isEmpty())
                        {
                            closingBracketIndex.add(close.pop().pos);
                        }
                    }
                    else
                        close.push(new Bracket(expr.charAt(i), i));
                }
                else
                    return false;
            }
            else if (expr.charAt(i) == ']')
            {
            	if (brackets.isEmpty())
            		return false;
                if (brackets.peek().ch == '[')
                {
                    brackets.pop();
                    if (brackets.isEmpty())
                    {
                        closingBracketIndex.add(i);
                        while (!close.isEmpty())
                        {
                            closingBracketIndex.add(close.pop().pos);
                        }
                    }
                    else
                        close.push(new Bracket(expr.charAt(i), i));
                }
                else
                    return false;
            }
            else
                continue;
        }
       
        return brackets.isEmpty();
        // FOLLOWING LINE ADDED TO MAKE COMPILER HAPPY

    }

    /**
     * Populates the scalars and arrays lists with symbols for scalar and array
     * variables in the expression. For every variable, a SINGLE symbol is created and stored,
     * even if it appears more than once in the expression.
     * At this time, values for all variables are set to
     * zero - they will be loaded from a file in the loadSymbolValues method.
     */
    public void buildSymbols() {
        arrays = new ArrayList<ArraySymbol>();
        scalars = new ArrayList<ScalarSymbol>();

        String temp = "";
        for (int i = 0; i < expr.length(); i++)
        {
            temp = temp + expr.charAt(i);
            if (expr.charAt(i) == '[')
            {
                temp = temp + "~";
            }
        }
        StringTokenizer str = new StringTokenizer(temp, " \t*+-/()]~");

        while (str.hasMoreElements())
        {
            String x = str.nextToken();
            if (x.charAt(x.length()-1) == '[')
            {
                arrays.add(new ArraySymbol(x.substring(0, x.length()-1)));
            }
            else
            {
                if (!Character.isLetter(x.charAt(0)))
                    continue;
                else
                    scalars.add(new ScalarSymbol(x));
            }

        }


    }
    

    /**
     * Loads values for symbols in the expression
     *
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input
     */
    public void loadSymbolValues(Scanner sc)
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String sym = st.nextToken();
            ScalarSymbol ssymbol = new ScalarSymbol(sym);
            ArraySymbol asymbol = new ArraySymbol(sym);
            int ssi = scalars.indexOf(ssymbol);
            int asi = arrays.indexOf(asymbol);
            if (ssi == -1 && asi == -1) {
                continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                scalars.get(ssi).value = num;
            } else { // array symbol
                asymbol = arrays.get(asi);
                asymbol.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    asymbol.values[index] = val;
                }
            }
        }
    }

    /**
     * Evaluates the expression, using RECURSION to evaluate subexpressions and to evaluate array
     * subscript expressions.
     *
     * @return Result of evaluation
     */
    public float evaluate() {

        String last = evaluate_recur(expr);
         
        return Float.parseFloat(last);
    }
    
    //Recursive Solution
    private String evaluate_recur(String expression)
    {
        for (int i = 0; i < expression.length(); i++)
        {

            if (expression.charAt(i) == '(' && i != expression.length()-1) //handles parenthesis
            {
                String before = expression.substring(0, i);
                String after = expression.substring(findEndIndex(expression)+1, expression.length());
                String s = evaluate_recur(expression.substring(i+1, findEndIndex(expression)));//recursive call
                String output = before + s + after;
                if (after.trim().length() > 1)
                	return evaluate_recur("" + output); //recursive call
                else
                	return "" + baseEvaluate(output); // evaluates expression using numbers


            }
            if (expression.charAt(i) == '[' && i != expression.length()-1) //handles square brackets
            {
                String before = expression.substring(0, i+1);
                String after = expression.substring(findEndIndex(expression), expression.length());
                String s = evaluate_recur(expression.substring(i+1, findEndIndex(expression))); //recursive call
                String output = before + s + after;
                if (after.trim().length() > 1)
                	return evaluate_recur(Math.floor(baseEvaluate(before + s + after.charAt(0))) + after.substring(1)); //recursive call
                else
                	return "" + Math.floor(baseEvaluate(output)); // evaluates expression using numbers
            }

        }
        return "" + baseEvaluate(expression);
    }
    private Integer findEndIndex(String expression)
    {
        ArrayList<Integer> opening = getOpeningBracketIndexList(expression);
        ArrayList<Integer> closing = getClosingBracketIndexList(expression);

        return closing.get(0);
    }


    private float baseEvaluate(String expression) //converts expressions that have variables into an expression with just numbers 
    {
        String translated = "";
        for (int i = 0; i < expression.trim().length(); i++)
        {
            String temp = "";
            if (expression.charAt(i) == '.') {
                translated = translated + expression.charAt(i);
                continue;
            }
            if (expression.charAt(i) == ')' || expression.charAt(i) == '(')
            	continue;
            if (Character.isLetter(expression.charAt(i)))

            {
            	boolean loop = true;
                while (loop){
                	temp = temp + expression.charAt(i);;
                    if (i != expression.length()-1 && Character.isLetter(expression.charAt(i+1)))
                    {
                        loop=true;
                    }
                    else
                    {
                        loop = false;
                        break;
                    }
                    i++;
                }



                boolean found = false;
                int index = 0;
                ScalarSymbol scale;
                while (!found && index < scalars.size())
                {
                    scale = scalars.get(index);
                    if (temp.equals(scale.name))
                    {
                        translated = translated + scale.value;
                        found = true;
                    }
                    else
                        index++;
                }

                boolean found2 = false;
                int index2 = 0;
                int j = 0;
                ArraySymbol arr;
                while (!found2 && index2 < arrays.size())
                {
                    arr = arrays.get(index2);
                        if (temp.equals(arr.name))
                        {
                            String ind = "";
                            int a = i+1;
                            while (a < expression.length())
                            {
                                if (Character.isDigit(expression.charAt(a)))
                                {
                                    ind = "" + expression.charAt(a);
                                    break;
                                }
                                a++;
                            }
                            j = a;
                            Integer indy = Integer.parseInt(ind);
                            translated = translated + arr.values[indy];
                            found2 = true;
                        }
                        else
                            index2++;
                }
                if (found2)
                {
                    while (j < expression.length())
                    {
                        if (expression.charAt(j) == ']')
                        {
                            i = j;
                            break;
                        }
                        j++;

                    }
                }
            }
            else
                translated = translated + expression.charAt(i);
        }


        return postfixToSolution(infixToPostfix(translated));



    }
    private String infixToPostfix (String str) //converts expression of numbers from infix to postfix notation using stacks
    {
        Stack <String> postfix = new Stack<String>();
        String pst = "";
        int i = 0;


        while (i < str. length())
        {
                String op = "";
                op = op + str.charAt(i);
                if (Character.isDigit(str.charAt(i)) || str.charAt(i) == '.')
                {
                    boolean loop = true;
                    while (loop){
                        pst = pst + str.charAt(i);
                        if (i != str.length()-1 && (Character.isDigit(str.charAt(i+1)) || str.charAt(i+1) == '.'))
                        {
                            i++;
                            continue;
                        }
                        else
                        {
                            pst = pst + "~";
                            loop = false;
                        }
                    }
                    i++;
                    continue;
                }

                else if (str.charAt(i) == ' ')
                {
                    i++;
                    continue;
                }



                else
                {
                    if (postfix.isEmpty())
                    {
                        postfix.push(op);
                    }
                    else
                    {
                        boolean loop = true;
                        while (loop)
                        {
                            if (postfix.isEmpty() == true)
                            {
                                postfix.push(op);
                                break;
                            }

                            String topStackValue = postfix.peek();
                            char scannedChar = str.charAt(i);
                            boolean higherPrecedence = false;
                            //handles precedence of operators 
                            if ((topStackValue.equals("*") || topStackValue.equals ("/")) && (scannedChar == '+' || scannedChar == '-'))
                                higherPrecedence = true;
                            else if ((topStackValue.equals("+") || topStackValue.equals("-")) && (scannedChar == '+' || scannedChar == '-'))
                                higherPrecedence = true;
                            else if ((topStackValue.equals("*") || topStackValue.equals("/")) && (scannedChar == '*' || scannedChar == '/'))
                                higherPrecedence = true;
                            else
                                higherPrecedence = false;

                            if (higherPrecedence == true)
                            {
                                String x = postfix.pop();
                                pst = pst + x + "~";

                            }
                            else
                            {
                                postfix.push("" + scannedChar);
                                loop = false;
                            }
                        }
                    }
                }
                i++;
        } // }
        while (!(postfix.isEmpty()))
        {
            pst = pst + postfix.peek() + "~";
            postfix.pop();
        }

        return pst;
    }



    
    private float postfixToSolution (String str) //converts postfix notation to a value 
    {
        Stack<Float> operand = new Stack<Float>();
        Stack<Character> operand_excep= new Stack<Character>();
        StringTokenizer str_token = new StringTokenizer(str, "~");

        while (str_token.hasMoreElements())
        {
            String op = str_token.nextToken();

            if (Character.isDigit(op.charAt(0)))
            {
                Float x = Float.parseFloat(op);
                operand.push(x);
            }
            else
            {
            	if (operand.size()==1)
            	{
            		if (op.charAt(0) == '-')
            		{
            			Float operand1 = operand.peek();
            			operand.pop();
            			Float operand2 = (float)0;
            			char operator = op.charAt(0);
            			Float retValue = findValue(operand1, operand2, operator);
            			operand.push(retValue);
            		}
            		else
            		{
            			operand_excep.push(op.charAt(0));
            		}
            	}
            	else if (!operand_excep.isEmpty())
            	{
            		char operator = operand_excep.pop();
            		Float operand1 = operand.peek();
            		operand.pop();
            		Float operand2 = operand.peek();
            		Float retValue = findValue(operand1, operand2, operator);
            		operand.pop();
            		if (op.charAt(0)=='-')
            			operand.push(0-retValue);
            		else
            			operand.push(0-retValue);;
            	}
            	else
            	{
            		Float operand1 = operand.peek();
            		operand.pop();
            		Float operand2 = operand.peek();
            		char operator = op.charAt(0);
            		Float retValue = findValue(operand1, operand2, operator);
            		operand.pop();
            		operand.push(retValue);
            	}
            }
        }

        return operand.peek();
    }

    private Float findValue(Float a, Float b, char x)
    {
        if (x == '+')
            return a + b;
        else if (x == '-')
            return b - a;
        else if (x == '*')
            return a * b;
        else
            return b / a;
    }

    private ArrayList<Integer> getOpeningBracketIndexList(String expression) { //utility method 
        ArrayList<Integer> openingIndex = new ArrayList<Integer>();
        ArrayList<Integer> closingIndex = new ArrayList<Integer>();
        // COMPLETE THIS METHOD
        Stack<Bracket> brackets = new Stack<Bracket>();
        Stack<Bracket> close = new Stack<Bracket>();
        for (int i = 0; i < expression.length(); i++)
        {
            if (expression.charAt(i) == '(' || expression.charAt(i) == '[')
            {
                    brackets.push(new Bracket(expression.charAt(i), i));
                    openingIndex.add(i);
            }

            else if (expression.charAt(i) == ')')
            {

                if (brackets.peek().ch == '(')
                {
                    brackets.pop();
                    if (brackets.isEmpty())
                    {
                        closingIndex.add(i);
                        while (!close.isEmpty())
                        {
                            closingIndex.add(close.pop().pos);
                        }
                    }
                    else
                        close.push(new Bracket(expression.charAt(i), i));
                }

            }
            else if (expression.charAt(i) == ']')
            {

                if (brackets.peek().ch == '[')
                {
                    brackets.pop();
                    if (brackets.isEmpty())
                    {
                        closingIndex.add(i);
                        while (!close.isEmpty())
                        {
                            closingIndex.add(close.pop().pos);
                        }
                    }
                    else
                        close.push(new Bracket(expression.charAt(i), i));
                }

            }
            else
                continue;
        }

        return openingIndex;
        // FOLLOWING LINE ADDED TO MAKE COMPILER HAPPY

    }



    private ArrayList<Integer> getClosingBracketIndexList(String expression) { //utility method
        ArrayList<Integer> openingIndex = new ArrayList<Integer>();
        ArrayList<Integer> closingIndex = new ArrayList<Integer>();
        // COMPLETE THIS METHOD
        Stack<Bracket> brackets = new Stack<Bracket>();
        Stack<Bracket> close = new Stack<Bracket>();
        for (int i = 0; i < expression.length(); i++)
        {
            if (expression.charAt(i) == '(' || expression.charAt(i) == '[')
            {
                    brackets.push(new Bracket(expression.charAt(i), i));
                    openingIndex.add(i);
            }

            else if (expression.charAt(i) == ')')
            {

                if (brackets.peek().ch == '(')
                {
                    brackets.pop();
                    if (brackets.isEmpty())
                    {
                        closingIndex.add(i);
                        while (!close.isEmpty())
                        {
                            closingIndex.add(close.pop().pos);
                        }
                    }
                    else
                        close.push(new Bracket(expression.charAt(i), i));
                }

            }
            else if (expression.charAt(i) == ']')
            {

                if (brackets.peek().ch == '[')
                {
                    brackets.pop();
                    if (brackets.isEmpty())
                    {
                        closingIndex.add(i);
                        while (!close.isEmpty())
                        {
                            closingIndex.add(close.pop().pos);
                        }
                    }
                    else
                        close.push(new Bracket(expression.charAt(i), i));
                }

            }
            else
                continue;
        }

        return closingIndex;
        // FOLLOWING LINE ADDED TO MAKE COMPILER HAPPY

    }



    public void printScalars() {
        for (ScalarSymbol ss: scalars) {
            System.out.println(ss);
        }
    }

    /**
     * Utility method, prints the symbols in the arrays list
     */
    public void printArrays() {
        for (ArraySymbol as: arrays) {
            System.out.println(as);
        }
    }

}
