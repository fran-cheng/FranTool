package com.fran.proguard;

import java.io.File;

public class SmaliParser {
    private int mIdx = -1;
    private int mLen;
    private int mCol = 0;
    private String mSource;
    private StringBuilder mBuff;
    private int mLine;
    private Callback mCallback;

    public SmaliParser(String source, Callback callback) {
        this.mSource = source;
        this.mLen = source.length();
        this.mBuff = new StringBuilder();
        this.mCallback = callback;
    }

    public int previewChar() {
        int i = this.mIdx + 1;
        return i == this.mLen ? -1 : this.mSource.charAt(i);
    }

    public int nextChar() {
        int prevch = this.previewChar();
        if (prevch == -1) {
            return -1;
        } else {
            ++this.mIdx;
            ++this.mCol;
            if (prevch == 10) {
                ++this.mLine;
            }

            return prevch;
        }
    }

    public void skipWhitespace() {
        int ch;
        while((ch = this.previewChar()) != -1 && Character.isWhitespace(ch) && ch != 10) {
            ++this.mIdx;
            this.mBuff.append((char)ch);
        }

    }

    public void skipToLineEnd() {
        while(true) {
            int ch = this.nextChar();
            if (ch != -1) {
                this.mBuff.append((char)ch);
                if (ch != 10) {
                    continue;
                }
            }

            return;
        }
    }

    public boolean skipTo(String string, String endString) {
        if (this.mIdx == this.mLen - 1) {
            return false;
        } else {
            int len = string.length();
            if (len == 0) {
                return true;
            } else {
                int endx = this.mSource.indexOf(endString, this.mIdx + 1);
                int idx = this.mSource.indexOf(string, this.mIdx + 1);
                if (idx >= 0 && idx < endx) {
                    int end = idx + len;
                    this.mBuff.append(this.mSource, this.mIdx + 1, end);
                    this.mIdx = end - 1;
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public String readLine() {
        int start = this.mIdx + 1;

        while(true) {
            int ch = this.previewChar();
            if (ch == -1) {
                return start > this.mIdx ? "" : this.mSource.substring(start, this.mIdx);
            }

            if (ch == 10) {
                return this.mSource.substring(start, this.mIdx + 1);
            }

            this.nextChar();
        }
    }

    public String skipLine() {
        String line = this.readLine();
        this.mBuff.append(line);
        this.skipToLineEnd();
        return line;
    }

    private void skipBlock(String name) {
        this.skipToLineEnd();

        while(true) {
            this.skipWhitespace();
            int ch = this.previewChar();
            if (ch == -1) {
                throw new RuntimeException("No " + name + " end.");
            }

            if (ch == 46) {
                ++this.mIdx;
                String keyword = this.nextIdentifier();
                this.mBuff.append(".").append(keyword);
                if ("end".equals(keyword)) {
                    this.skipWhitespace();
                    keyword = this.nextIdentifier();
                    this.mBuff.append(keyword);
                    if (name.equals(keyword)) {
                        this.skipToLineEnd();
                        return;
                    }
                }
            }

            this.skipToLineEnd();
        }
    }

    public SmaliParser append(String s) {
        this.mBuff.append(s);
        return this;
    }

    public static final boolean isIdentityChar(int c) {
        return c >= 97 && c <= 122 || c >= 65 && c <= 90 || c >= 48 && c <= 57 || c == 45 || c == 47 || c == 59 || c == 36;
    }

    public String nextIdentifier() {
        int start = this.mIdx + 1;

        while(true) {
            int ch = this.previewChar();
            if (!isIdentityChar(ch)) {
                if (!Character.isWhitespace(ch) && ch != -1) {
                    throw new ParseException("δʶ���ʶ����" + (char)ch + ",�� " + this.mLine + ",�� " + this.mCol);
                } else {
                    return this.mSource.substring(start, this.mIdx + 1);
                }
            }

            ++this.mIdx;
        }
    }

    public String nextString() {
        StringBuilder sb = new StringBuilder();
        ++this.mIdx;

        while(true) {
            while(true) {
                int ch = this.nextChar();
                if (ch == 92) {
                    sb.append((char)this.nextChar());
                } else {
                    if (ch == 34) {
                        return sb.toString();
                    }

                    if (ch == -1) {
                        throw new ParseException("��Ч�ַ�����ʽ��" + (char)ch + ",�� " + this.mLine + ",�� " + this.mCol);
                    }

                    sb.append((char)ch);
                }
            }
        }
    }

    public String parse() {
        while(true) {
            skipWhitespace();
            int ch = this.previewChar();
            if (ch == 10) {
                this.mBuff.append((char)ch);
                ++this.mIdx;
            } else {
                if (ch == -1) {
                    return this.mBuff.toString();
                }

                String identifier;
                if (ch == 46) {
                    ++this.mIdx;
                    identifier = this.nextIdentifier();
                    if ("annotation".equals(identifier)) {
                        this.mBuff.append(".").append(identifier);
                        this.skipBlock("annotation");
                    } else if (!this.mCallback.onKeyword(identifier, this)) {
                        this.mBuff.append(".").append(identifier);
                        this.skipToLineEnd();
                    }
                } else if (ch == 35) {
                    ++this.mIdx;
                    if (!this.mCallback.onComment(this)) {
                        this.mBuff.append("#");
                        this.skipToLineEnd();
                    }
                } else if (isIdentityChar(ch)) {
                    identifier = this.nextIdentifier();
                    if (!this.mCallback.onDirective(identifier, this)) {
                        this.mBuff.append(identifier);
                        this.skipToLineEnd();
                    }
                } else {
                    this.skipToLineEnd();
                }
            }
        }
    }

    public static void main(String[] args) {
        File f = new File("C:\\Data\\Work\\APK\\GP\\Dating\\Dating_in_my_area\\smali\\datingapp\\datingcod\\datinginregion\\activities\\AuthGenerateActivityDAF.smali");
        String s = Util.read(f, "utf-8");
        SmaliParser sl = new SmaliParser(s, new Callback());
        String r = null;

        try {
            r = sl.parse();
        } catch (ParseException var6) {
            var6.printStackTrace();
        }

        System.out.println(r);
        Util.writeFile(f.getAbsolutePath(), r, "utf-8");
    }

    public static class Callback {
        public Callback() {
        }

        public boolean onKeyword(String keyword, SmaliParser lx) {
            return false;
        }

        public boolean onDirective(String directive, SmaliParser lx) {
            return false;
        }

        public boolean onComment(SmaliParser lx) {
            return false;
        }
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String ex) {
            super(ex);
        }
    }
}
