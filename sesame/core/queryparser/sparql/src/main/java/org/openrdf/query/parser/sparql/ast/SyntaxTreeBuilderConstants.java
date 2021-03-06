/* Generated By:JJTree&JavaCC: Do not edit this line. SyntaxTreeBuilderConstants.java */
package org.openrdf.query.parser.sparql.ast;

public interface SyntaxTreeBuilderConstants {

  int EOF = 0;
  int WS_CHAR = 1;
  int WHITESPACE = 2;
  int SINGLE_LINE_COMMENT = 3;
  int LPAREN = 4;
  int RPAREN = 5;
  int LBRACE = 6;
  int RBRACE = 7;
  int LBRACK = 8;
  int RBRACK = 9;
  int SEMICOLON = 10;
  int COMMA = 11;
  int DOT = 12;
  int EQ = 13;
  int NE = 14;
  int GT = 15;
  int LT = 16;
  int LE = 17;
  int GE = 18;
  int NOT = 19;
  int OR = 20;
  int AND = 21;
  int PLUS = 22;
  int MINUS = 23;
  int STAR = 24;
  int SLASH = 25;
  int DT_PREFIX = 26;
  int NIL = 27;
  int ANON = 28;
  int IS_A = 29;
  int BASE = 30;
  int PREFIX = 31;
  int SELECT = 32;
  int CONSTRUCT = 33;
  int DESCRIBE = 34;
  int ASK = 35;
  int DISTINCT = 36;
  int REDUCED = 37;
  int FROM = 38;
  int NAMED = 39;
  int WHERE = 40;
  int ORDER = 41;
  int BY = 42;
  int ASC = 43;
  int DESC = 44;
  int LIMIT = 45;
  int OFFSET = 46;
  int OPTIONAL = 47;
  int GRAPH = 48;
  int UNION = 49;
  int FILTER = 50;
  int STR = 51;
  int LANG = 52;
  int LANGMATCHES = 53;
  int DATATYPE = 54;
  int BOUND = 55;
  int SAMETERM = 56;
  int IS_IRI = 57;
  int IS_BLANK = 58;
  int IS_LITERAL = 59;
  int REGEX = 60;
  int TRUE = 61;
  int FALSE = 62;
  int Q_IRI_REF = 63;
  int PNAME_NS = 64;
  int PNAME_LN = 65;
  int BLANK_NODE_LABEL = 66;
  int VAR1 = 67;
  int VAR2 = 68;
  int LANGTAG = 69;
  int INTEGER = 70;
  int INTEGER_POSITIVE = 71;
  int INTEGER_NEGATIVE = 72;
  int DECIMAL = 73;
  int DECIMAL1 = 74;
  int DECIMAL2 = 75;
  int DECIMAL_POSITIVE = 76;
  int DECIMAL_NEGATIVE = 77;
  int DOUBLE = 78;
  int DOUBLE1 = 79;
  int DOUBLE2 = 80;
  int DOUBLE3 = 81;
  int EXPONENT = 82;
  int DOUBLE_POSITIVE = 83;
  int DOUBLE_NEGATIVE = 84;
  int STRING_LITERAL1 = 85;
  int STRING_LITERAL2 = 86;
  int STRING_LITERAL_LONG1 = 87;
  int STRING_LITERAL_LONG2 = 88;
  int SAFE_CHAR1 = 89;
  int SAFE_CHAR2 = 90;
  int SAFE_CHAR_LONG1 = 91;
  int SAFE_CHAR_LONG2 = 92;
  int ECHAR = 93;
  int HEX = 94;
  int ALPHA = 95;
  int NUM = 96;
  int PN_CHARS_BASE = 97;
  int PN_CHARS_U = 98;
  int PN_START_CHAR = 99;
  int VAR_CHAR = 100;
  int PN_END_CHAR = 101;
  int PN_CHAR = 102;
  int PN_PREFIX = 103;
  int PN_LOCAL = 104;
  int VARNAME = 105;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "<WS_CHAR>",
    "<WHITESPACE>",
    "<SINGLE_LINE_COMMENT>",
    "\"(\"",
    "\")\"",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\";\"",
    "\",\"",
    "\".\"",
    "\"=\"",
    "\"!=\"",
    "\">\"",
    "\"<\"",
    "\"<=\"",
    "\">=\"",
    "\"!\"",
    "\"||\"",
    "\"&&\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"^^\"",
    "<NIL>",
    "<ANON>",
    "\"a\"",
    "\"base\"",
    "\"prefix\"",
    "\"select\"",
    "\"construct\"",
    "\"describe\"",
    "\"ask\"",
    "\"distinct\"",
    "\"reduced\"",
    "\"from\"",
    "\"named\"",
    "\"where\"",
    "\"order\"",
    "\"by\"",
    "\"asc\"",
    "\"desc\"",
    "\"limit\"",
    "\"offset\"",
    "\"optional\"",
    "\"graph\"",
    "\"union\"",
    "\"filter\"",
    "\"str\"",
    "\"lang\"",
    "\"langmatches\"",
    "\"datatype\"",
    "\"bound\"",
    "\"sameTerm\"",
    "<IS_IRI>",
    "\"isBlank\"",
    "\"isLiteral\"",
    "\"regex\"",
    "\"true\"",
    "\"false\"",
    "<Q_IRI_REF>",
    "<PNAME_NS>",
    "<PNAME_LN>",
    "<BLANK_NODE_LABEL>",
    "<VAR1>",
    "<VAR2>",
    "<LANGTAG>",
    "<INTEGER>",
    "<INTEGER_POSITIVE>",
    "<INTEGER_NEGATIVE>",
    "<DECIMAL>",
    "<DECIMAL1>",
    "<DECIMAL2>",
    "<DECIMAL_POSITIVE>",
    "<DECIMAL_NEGATIVE>",
    "<DOUBLE>",
    "<DOUBLE1>",
    "<DOUBLE2>",
    "<DOUBLE3>",
    "<EXPONENT>",
    "<DOUBLE_POSITIVE>",
    "<DOUBLE_NEGATIVE>",
    "<STRING_LITERAL1>",
    "<STRING_LITERAL2>",
    "<STRING_LITERAL_LONG1>",
    "<STRING_LITERAL_LONG2>",
    "<SAFE_CHAR1>",
    "<SAFE_CHAR2>",
    "<SAFE_CHAR_LONG1>",
    "<SAFE_CHAR_LONG2>",
    "<ECHAR>",
    "<HEX>",
    "<ALPHA>",
    "<NUM>",
    "<PN_CHARS_BASE>",
    "<PN_CHARS_U>",
    "<PN_START_CHAR>",
    "<VAR_CHAR>",
    "<PN_END_CHAR>",
    "<PN_CHAR>",
    "<PN_PREFIX>",
    "<PN_LOCAL>",
    "<VARNAME>",
  };

}
