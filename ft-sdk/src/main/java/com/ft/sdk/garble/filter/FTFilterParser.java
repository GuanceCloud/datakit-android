package com.ft.sdk.garble.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

class FTFilterParser {

    static final Object MISSING = new Object();
    private static final Object NIL = new Object();

    interface Values {
        Object get(String key);
    }

    interface Expression {
        boolean eval(Values values);
    }

    static List<Expression> parseConditions(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return new Parser(input).parseConditions();
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }

    private enum TokenType {
        EOF,
        LEFT_BRACE,
        RIGHT_BRACE,
        LEFT_PAREN,
        RIGHT_PAREN,
        LEFT_BRACKET,
        RIGHT_BRACKET,
        COMMA,
        SEMICOLON,
        IDENTIFIER,
        STRING,
        NUMBER,
        BOOLEAN,
        NIL,
        REGEX,
        AND,
        OR,
        EQ,
        NEQ,
        GT,
        GTE,
        LT,
        LTE,
        IN,
        NOT_IN,
        MATCH,
        NOT_MATCH
    }

    private static class Token {
        final TokenType type;
        final String text;
        final Object value;

        Token(TokenType type, String text) {
            this(type, text, null);
        }

        Token(TokenType type, String text, Object value) {
            this.type = type;
            this.text = text;
            this.value = value;
        }
    }

    private static class Parser {
        private final Lexer lexer;
        private Token current;

        Parser(String input) {
            lexer = new Lexer(input);
            current = lexer.next();
        }

        List<Expression> parseConditions() {
            List<Expression> conditions = new ArrayList<>();
            while (current.type != TokenType.EOF) {
                if (match(TokenType.SEMICOLON) || match(TokenType.COMMA)) {
                    continue;
                }
                if (!match(TokenType.LEFT_BRACE)) {
                    advance();
                    continue;
                }
                if (match(TokenType.RIGHT_BRACE)) {
                    conditions.add(new AlwaysExpression());
                    continue;
                }
                Expression expression = parseOr();
                while (match(TokenType.COMMA)) {
                    expression = new BinaryExpression(TokenType.AND, expression, parseOr());
                }
                expect(TokenType.RIGHT_BRACE);
                if (expression != null) {
                    conditions.add(expression);
                }
            }
            return conditions;
        }

        private Expression parseOr() {
            Expression expression = parseAnd();
            while (match(TokenType.OR)) {
                expression = new BinaryExpression(TokenType.OR, expression, parseAnd());
            }
            return expression;
        }

        private Expression parseAnd() {
            Expression expression = parseCompare();
            while (match(TokenType.AND)) {
                expression = new BinaryExpression(TokenType.AND, expression, parseCompare());
            }
            return expression;
        }

        private Expression parseCompare() {
            ValueExpression left = parseValue();
            TokenType op = current.type;
            switch (op) {
                case EQ:
                case NEQ:
                case GT:
                case GTE:
                case LT:
                case LTE:
                    advance();
                    return new CompareExpression(op, left, parseValue());
                case IN:
                case NOT_IN:
                case MATCH:
                case NOT_MATCH:
                    advance();
                    List<ValueExpression> values = parseList();
                    if (op == TokenType.MATCH || op == TokenType.NOT_MATCH) {
                        values = compileRegexList(values);
                    }
                    return new ListExpression(op, left, values);
                default:
                    return left;
            }
        }

        private List<ValueExpression> compileRegexList(List<ValueExpression> values) {
            List<ValueExpression> regexValues = new ArrayList<>();
            for (ValueExpression value : values) {
                if (!(value instanceof LiteralExpression)) {
                    continue;
                }
                Object rawValue = ((LiteralExpression) value).rawValue();
                if (!(rawValue instanceof String)) {
                    continue;
                }
                try {
                    regexValues.add(new LiteralExpression(new RegexValue((String) rawValue)));
                } catch (PatternSyntaxException ignored) {
                    // cliutils/filter drops invalid regex entries from MATCH/NOTMATCH lists.
                }
            }
            return regexValues;
        }

        private List<ValueExpression> parseList() {
            expect(TokenType.LEFT_BRACKET);
            List<ValueExpression> values = new ArrayList<>();
            while (current.type != TokenType.RIGHT_BRACKET && current.type != TokenType.EOF) {
                if (match(TokenType.COMMA)) {
                    continue;
                }
                values.add(parseValue());
                match(TokenType.COMMA);
            }
            expect(TokenType.RIGHT_BRACKET);
            return values;
        }

        private ValueExpression parseValue() {
            Token token = current;
            switch (token.type) {
                case LEFT_PAREN:
                    advance();
                    Expression expression = parseOr();
                    expect(TokenType.RIGHT_PAREN);
                    return new NestedValueExpression(expression);
                case IDENTIFIER:
                    advance();
                    return new IdentifierExpression(token.text);
                case STRING:
                case NUMBER:
                case BOOLEAN:
                case REGEX:
                    advance();
                    return new LiteralExpression(token.value);
                case NIL:
                    advance();
                    return new LiteralExpression(NIL);
                default:
                    throw new IllegalArgumentException("Unexpected token " + token.type);
            }
        }

        private boolean match(TokenType type) {
            if (current.type == type) {
                advance();
                return true;
            }
            return false;
        }

        private void expect(TokenType type) {
            if (!match(type)) {
                throw new IllegalArgumentException("Expected " + type + " but got " + current.type);
            }
        }

        private void advance() {
            current = lexer.next();
        }
    }

    private interface ValueExpression extends Expression {
        Object value(Values values);
    }

    private static class AlwaysExpression implements Expression {
        @Override
        public boolean eval(Values values) {
            return true;
        }
    }

    private static class NestedValueExpression implements ValueExpression {
        private final Expression expression;

        NestedValueExpression(Expression expression) {
            this.expression = expression;
        }

        @Override
        public Object value(Values values) {
            return eval(values);
        }

        @Override
        public boolean eval(Values values) {
            return expression != null && expression.eval(values);
        }
    }

    private static class IdentifierExpression implements ValueExpression {
        private final String key;

        IdentifierExpression(String key) {
            this.key = key;
        }

        @Override
        public Object value(Values values) {
            return values == null ? MISSING : values.get(key);
        }

        @Override
        public boolean eval(Values values) {
            Object value = value(values);
            return value instanceof Boolean && (Boolean) value;
        }
    }

    private static class LiteralExpression implements ValueExpression {
        private final Object value;

        LiteralExpression(Object value) {
            this.value = value;
        }

        Object rawValue() {
            return value;
        }

        @Override
        public Object value(Values values) {
            return value;
        }

        @Override
        public boolean eval(Values values) {
            return value instanceof Boolean && (Boolean) value;
        }
    }

    private static class BinaryExpression implements Expression {
        private final TokenType op;
        private final Expression left;
        private final Expression right;

        BinaryExpression(TokenType op, Expression left, Expression right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean eval(Values values) {
            if (op == TokenType.AND) {
                return left.eval(values) && right.eval(values);
            }
            return left.eval(values) || right.eval(values);
        }
    }

    private static class CompareExpression implements Expression {
        private final TokenType op;
        private final ValueExpression left;
        private final ValueExpression right;

        CompareExpression(TokenType op, ValueExpression left, ValueExpression right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean eval(Values values) {
            Object leftValue = left.value(values);
            Object rightValue = right.value(values);
            if (leftValue == MISSING && rightValue == MISSING && right instanceof IdentifierExpression) {
                return false;
            }
            return compare(op, leftValue, rightValue);
        }
    }

    private static class ListExpression implements Expression {
        private final TokenType op;
        private final ValueExpression left;
        private final List<ValueExpression> items;

        ListExpression(TokenType op, ValueExpression left, List<ValueExpression> items) {
            this.op = op;
            this.left = left;
            this.items = items;
        }

        @Override
        public boolean eval(Values values) {
            if ((op == TokenType.MATCH || op == TokenType.NOT_MATCH) && items.isEmpty()) {
                return false;
            }
            Object leftValue = left.value(values);
            boolean matched = false;
            for (ValueExpression item : items) {
                Object itemValue = item.value(values);
                if (op == TokenType.MATCH || op == TokenType.NOT_MATCH) {
                    if (matches(leftValue, itemValue)) {
                        matched = true;
                        break;
                    }
                } else if (compare(TokenType.EQ, leftValue, itemValue)) {
                    matched = true;
                    break;
                }
            }

            return op == TokenType.NOT_IN || op == TokenType.NOT_MATCH ? !matched : matched;
        }
    }

    private static class RegexValue {
        final Pattern pattern;

        RegexValue(String regex) {
            pattern = Pattern.compile(regex);
        }
    }

    private static boolean compare(TokenType op, Object left, Object right) {
        if (right instanceof RegexValue && (op == TokenType.EQ || op == TokenType.NEQ)) {
            boolean matched = matches(left, right);
            return op == TokenType.NEQ ? !matched : matched;
        }

        if (left == MISSING) {
            left = NIL;
        }
        if (right == MISSING) {
            right = NIL;
        }

        switch (op) {
            case EQ:
                return equal(left, right);
            case NEQ:
                return !equal(left, right);
            case GT:
            case GTE:
            case LT:
            case LTE:
                return compareOrdered(op, left, right);
            default:
                return false;
        }
    }

    private static boolean equal(Object left, Object right) {
        if (left == NIL || right == NIL) {
            return left == NIL && right == NIL;
        }
        if (left instanceof Number && right instanceof Number) {
            return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue()) == 0;
        }
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return left.equals(right);
    }

    private static boolean compareOrdered(TokenType op, Object left, Object right) {
        int result;
        if (left instanceof Number && right instanceof Number) {
            result = Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue());
        } else if (left instanceof String && right instanceof String) {
            result = ((String) left).compareTo((String) right);
        } else {
            return false;
        }

        switch (op) {
            case GT:
                return result > 0;
            case GTE:
                return result >= 0;
            case LT:
                return result < 0;
            case LTE:
                return result <= 0;
            default:
                return false;
        }
    }

    private static boolean matches(Object left, Object right) {
        if (!(left instanceof String)) {
            return false;
        }
        if (right instanceof RegexValue) {
            return ((RegexValue) right).pattern.matcher((String) left).find();
        }
        if (right instanceof String) {
            try {
                return Pattern.compile((String) right).matcher((String) left).find();
            } catch (PatternSyntaxException e) {
                return false;
            }
        }
        return false;
    }

    private static class Lexer {
        private final String input;
        private int pos;

        Lexer(String input) {
            this.input = input;
        }

        Token next() {
            skipWhitespaceAndComments();
            if (pos >= input.length()) {
                return new Token(TokenType.EOF, "");
            }

            char c = input.charAt(pos);
            switch (c) {
                case '{':
                    pos++;
                    return new Token(TokenType.LEFT_BRACE, "{");
                case '}':
                    pos++;
                    return new Token(TokenType.RIGHT_BRACE, "}");
                case '(':
                    pos++;
                    return new Token(TokenType.LEFT_PAREN, "(");
                case ')':
                    pos++;
                    return new Token(TokenType.RIGHT_PAREN, ")");
                case '[':
                    pos++;
                    return new Token(TokenType.LEFT_BRACKET, "[");
                case ']':
                    pos++;
                    return new Token(TokenType.RIGHT_BRACKET, "]");
                case ',':
                    pos++;
                    return new Token(TokenType.COMMA, ",");
                case ';':
                    pos++;
                    return new Token(TokenType.SEMICOLON, ";");
                case '\'':
                case '"':
                case '`':
                    return readString(c);
                case '=':
                    pos++;
                    if (peek('=')) {
                        pos++;
                    }
                    return new Token(TokenType.EQ, "=");
                case '!':
                    pos++;
                    if (peek('=')) {
                        pos++;
                        return new Token(TokenType.NEQ, "!=");
                    }
                    throw new IllegalArgumentException("Unexpected '!'");
                case '>':
                    pos++;
                    if (peek('=')) {
                        pos++;
                        return new Token(TokenType.GTE, ">=");
                    }
                    return new Token(TokenType.GT, ">");
                case '<':
                    pos++;
                    if (peek('=')) {
                        pos++;
                        return new Token(TokenType.LTE, "<=");
                    }
                    return new Token(TokenType.LT, "<");
                case '&':
                    if (peekNext('&')) {
                        pos += 2;
                        return new Token(TokenType.AND, "&&");
                    }
                    break;
                case '|':
                    if (peekNext('|')) {
                        pos += 2;
                        return new Token(TokenType.OR, "||");
                    }
                    break;
                default:
                    break;
            }

            if (isNumberStart(c)) {
                return readNumber();
            }
            if (isIdentifierStart(c)) {
                return readIdentifier();
            }
            throw new IllegalArgumentException("Unexpected char " + c);
        }

        private void skipWhitespaceAndComments() {
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (Character.isWhitespace(c)) {
                    pos++;
                } else if (c == '#') {
                    while (pos < input.length() && input.charAt(pos) != '\n') {
                        pos++;
                    }
                } else {
                    break;
                }
            }
        }

        private Token readIdentifier() {
            int start = pos;
            pos++;
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '-') {
                    pos++;
                } else {
                    break;
                }
            }
            String text = input.substring(start, pos);
            String keyword = text.toLowerCase(Locale.US);
            if ("re".equals(keyword) && peek('(')) {
                return readRegexFunction();
            }
            if ("identifier".equals(keyword) && peek('(')) {
                return readIdentifierFunction();
            }
            if ("and".equals(keyword)) {
                return new Token(TokenType.AND, text);
            } else if ("or".equals(keyword)) {
                return new Token(TokenType.OR, text);
            } else if ("in".equals(keyword)) {
                return new Token(TokenType.IN, text);
            } else if ("notin".equals(keyword) || "not_in".equals(keyword)) {
                return new Token(TokenType.NOT_IN, text);
            } else if ("match".equals(keyword)) {
                return new Token(TokenType.MATCH, text);
            } else if ("notmatch".equals(keyword)) {
                return new Token(TokenType.NOT_MATCH, text);
            } else if ("true".equals(keyword) || "false".equals(keyword)) {
                return new Token(TokenType.BOOLEAN, text, Boolean.valueOf(keyword));
            } else if ("nil".equals(keyword) || "null".equals(keyword)) {
                return new Token(TokenType.NIL, text, NIL);
            }
            return new Token(TokenType.IDENTIFIER, text);
        }

        private Token readIdentifierFunction() {
            expectChar('(');
            skipWhitespaceAndComments();
            if (pos >= input.length()) {
                throw new IllegalArgumentException("Unexpected end of identifier");
            }
            char quote = input.charAt(pos);
            if (quote != '\'' && quote != '"' && quote != '`') {
                throw new IllegalArgumentException("Identifier expects string");
            }
            String identifier = (String) readString(quote).value;
            skipWhitespaceAndComments();
            expectChar(')');
            return new Token(TokenType.IDENTIFIER, identifier, identifier);
        }

        private Token readRegexFunction() {
            expectChar('(');
            skipWhitespaceAndComments();
            if (pos >= input.length()) {
                throw new IllegalArgumentException("Unexpected end of regex");
            }
            char quote = input.charAt(pos);
            if (quote != '\'' && quote != '"' && quote != '`') {
                throw new IllegalArgumentException("Regex expects string");
            }
            String regex = (String) readString(quote).value;
            skipWhitespaceAndComments();
            expectChar(')');
            try {
                return new Token(TokenType.REGEX, regex, new RegexValue(regex));
            } catch (PatternSyntaxException e) {
                return new Token(TokenType.REGEX, regex, new RegexValue("a^"));
            }
        }

        private Token readString(char quote) {
            StringBuilder sb = new StringBuilder();
            pos++;
            while (pos < input.length()) {
                char c = input.charAt(pos++);
                if (c == quote) {
                    return new Token(TokenType.STRING, sb.toString(), sb.toString());
                }
                if (c == '\\' && pos < input.length()) {
                    char next = input.charAt(pos++);
                    switch (next) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        default:
                            sb.append(next);
                            break;
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("Unterminated string");
        }

        private Token readNumber() {
            int start = pos;
            if (input.charAt(pos) == '-') {
                pos++;
            }
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                pos++;
            }
            boolean isFloat = false;
            if (pos < input.length() && input.charAt(pos) == '.') {
                isFloat = true;
                pos++;
                while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                    pos++;
                }
            }
            String text = input.substring(start, pos);
            Object value = isFloat ? Double.parseDouble(text) : Long.parseLong(text);
            return new Token(TokenType.NUMBER, text, value);
        }

        private boolean isIdentifierStart(char c) {
            return Character.isLetter(c) || c == '_';
        }

        private boolean isNumberStart(char c) {
            return Character.isDigit(c) || c == '-';
        }

        private boolean peek(char c) {
            return pos < input.length() && input.charAt(pos) == c;
        }

        private boolean peekNext(char c) {
            return pos + 1 < input.length() && input.charAt(pos) == c && input.charAt(pos + 1) == c;
        }

        private void expectChar(char c) {
            if (!peek(c)) {
                throw new IllegalArgumentException("Expected char " + c);
            }
            pos++;
        }
    }
}
