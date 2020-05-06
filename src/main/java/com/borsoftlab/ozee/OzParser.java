package com.borsoftlab.ozee;

public class OzParser{

    OzScanner scanner;
    int aheadLexeme = 0;
    byte[] mem = new byte [12];
    int pc = 0;

    /*
    * Type maintenance stack
    */
    private IntStack typeStack = new IntStack( 32 );


    public OzParser(){
    }
   
	public void compile(final OzScanner scanner) throws Exception {
        this.scanner = scanner;
        OzCompileError.reset();
        pc = 0;
        scanner.nextLexeme();
        stmtList();
        System.out.println(scanner.lexemeCount + " lexemes processed");
        System.out.println(scanner.text.loc.line + " lines compiled");
    }

    void stmtList() throws Exception {
        while( scanner.lookAheadLexeme != OzScanner.lexEOF ){
    //        System.out.print(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  ");
    //        System.out.printf("Maintenance type stack size is: %d\n", typeStack.size());
            stmt();
            match(OzScanner.lexSEMICOLON, "';'");
        }
    }

    void stmt() throws Exception {
        if( scanner.lookAheadLexeme == OzScanner.lexVARTYPE) {
            declareVarStmt();
        }
        else if( scanner.lookAheadLexeme == OzScanner.lexNAME) {
            assignStmt(); // TO DO
        }
        else {
            //OzCompileError.message(scanner, "unexpected symbol");
            expression(); // it will be not always
        } 
    }

    private void declareVarStmt() throws Exception {
        int type = varType();
        OzSymbols.Symbol symbol = newVariable(type);
        if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
            assignExpression(symbol);
        } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
            // empty
        } else {
            OzCompileError.expected(scanner, "'=' or ';'");
        }
    }

    private int varType() throws Exception {
        int type = scanner.symbol.varType;
        match(OzScanner.lexVARTYPE, "var type");
        return type;
    }

    private OzSymbols.Symbol newVariable(int type) throws Exception {
        OzSymbols.Symbol symbol = scanner.symbol;
        symbol.setType(type);
        match(OzScanner.lexNAME, "variable name");
        // allocateVariable(symbol);
        return symbol;
    }

    private OzSymbols.Symbol variable() throws Exception {
        OzSymbols.Symbol symbol = scanner.symbol;
        if( symbol.varType == OzScanner.VARTYPE_UNDEF ){
            OzCompileError.message(scanner, "variable '" + symbol.name + "' not defined");
        }
        match(OzScanner.lexNAME, "variable name");
        return symbol;
    }

    public void assignStmt() throws Exception {
        OzSymbols.Symbol symbol = variable();
        if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
            assignExpression(symbol);
        } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
            // empty
        } else {
            OzCompileError.expected(scanner, "'='");
        }
    }
    
    private void assignExpression(OzSymbols.Symbol symbol) throws Exception {
        match(OzScanner.lexASSIGN, "'='");
        expression();
        emit("push @" + symbol.name);
        emit("assign");
        //emitPullDir(symbol);
    }
   
    public void expression() throws Exception {
        term();
        while(true) {
            switch( scanner.lookAheadLexeme ){
                case OzScanner.lexPLUS:
                    sum();
                    break;
                case OzScanner.lexMINUS:
                    sub();
                    break;
                default:
                   return;
            }
        }
    }

    private void sum() throws Exception {
        match(OzScanner.lexPLUS, "'+'");
        term();
        emit("add");
//        System.out.printf("Maintenance type stack size is: %d\n", typeStack.size());
//        emitArithmeticOpCode(MachineCode.SUMF, MachineCode.SUMI);
    }

    private void sub() throws Exception {
        match(OzScanner.lexMINUS, "'-'");
        term();
        emit("sub");
//        emitArithmeticOpCode(MachineCode.SUBF, MachineCode.SUBI);
    }

    private void term() throws Exception {
        factor();
        while(true) {
            switch( scanner.lookAheadLexeme ){
                case OzScanner.lexMUL:
                    scanner.nextLexeme();
                    mul();
                    break;
                case OzScanner.lexDIV:
                    scanner.nextLexeme();
                    div();
                    break;
                default:
                    return;
            }
        }
    }    

    private void div() throws Exception {
        factor();
        emit("div");

//     emitArithmeticOpCode(MachineCode.DIVF, MachineCode.DIVI);
    }

    private void mul() throws Exception {
        factor();
        emit("mul");

//        emitArithmeticOpCode(MachineCode.MULF, MachineCode.MULI);
    }

    private void factor() throws Exception {
        boolean unaryMinus = false;
        if( scanner.lookAheadLexeme == OzScanner.lexMINUS){
            scanner.nextLexeme();;
            unaryMinus = true;
        };
        if( scanner.lookAheadLexeme == OzScanner.lexLPAREN){
            scanner.nextLexeme();
            expression();
            match(OzScanner.lexRPAREN, ")");
        } else {
            switch(scanner.lookAheadLexeme){
                case OzScanner.lexNUMBER:
                    scanner.nextLexeme();
                    typeStack.push(scanner.numberType);
                    if( scanner.numberType == OzScanner.VARTYPE_INT)
                        emit("push " + scanner.intNumber);
                    else
                        emit("push " + scanner.floatNumber);    
                    // emitPushImm(scanner.getNumberAsInt());
                    break;
                /*    
                case Scanner.TOKEN_BUILTIN:
                    match(Scanner.TOKEN_BUILTIN);
                    int builtinFunc = scanner.getSymbol().getType();
                    match(Scanner.TOKEN_LPAREN);
                    expression();
                    match(Scanner.TOKEN_RPAREN);
                    builtin(builtinFunc);
                    break;
                */    
                case OzScanner.lexNAME:
                    /*
                    scanner.nextLexeme();
                    OzSymbols.Symbol symbol = scanner.symbol;
                    int symbolType = symbol.varType;
                    */
                    OzSymbols.Symbol symbol = variable();
                    /*
                    switch (symbolType){
                        case OzScanner.VARTYPE_INT:
                        case OzScanner.VARTYPE_SHORT:
                        case OzScanner.VARTYPE_BYTE:
                        case OzScanner.VARTYPE_FLOAT:
                        case Scanner.DATA_TYPE_INT2:
                        case Scanner.DATA_TYPE_UINT1:
                        case Scanner.DATA_TYPE_UINT2:
                        symbolType = Scanner.DATA_TYPE_INT4;
                        break;
                    }
                    */
                    typeStack.push(symbol.varType);
                    emit("push @" + symbol.name);
                    emit("eval ");
//                    emitPushDir(symbol);
                    break;
                case OzScanner.lexEOF:
                break ;   
                default:
                    OzCompileError.expected(scanner, "expression");    
            }
        }
        if( unaryMinus ) {
  //          emitNegOpCode(MachineCode.NEGF, MachineCode.NEGI4);
        }
    }

    private void emit(String cmd) {
        System.out.println(cmd);
    }

    public byte[] getExecMemModule() {
        final int value = 1234567890;
        mem[pc++] = OzVm.OPCODE_PUSH;
        OzUtils.storeIntToByteArray(mem, pc, value);
        pc += 4;
        mem[pc++] = OzVm.OPCODE_STOP;
        return mem;
    }

    private void match(final int lexeme, final String msg) throws Exception {
        if( scanner.lookAheadLexeme == lexeme ){
            scanner.nextLexeme();
        } else {
            OzCompileError.expected(scanner, msg);
        }
    }     
}