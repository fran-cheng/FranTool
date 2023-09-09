package com.fran.proguard;

public class SmaliProguardCallback extends SmaliParser.Callback{
    private boolean mIsAnonymous;
    private String mClassName;
    private String mSupreClassName;
    private int mFieldCount;
    private static String[] SS = new String[]{
            "test",
            "testtest",
            "testtesttest",
            "Lorem ipsum dolor sit amet",
            "consectetur adipiscing elit",
            "testtesttesttest",
            "testtesttesttesttest",
            "testtesttesttesttesttest",
            "incididunt ut labore et",
            "Ut enim ad minim veniam",
            "quis nostrud exercitation ullamco",
            "Duis aute irure dolor in reprehenderit in",
            "voluptate velit esse cillum dolore",
            "Excepteur sint occaecat",
            "sunt in culpa qui officia deserunt",
            "testtesttesttesttesttesttest",
            "testtesttesttesttesttesttesttest",
            "testtesttesttesttesttesttesttesttest"
    };
    private int mCounter;

    public boolean onKeyword(String keyword, SmaliParser lx) {
        if("class".equals(keyword)){
            if(mClassName == null){
                lx.append(".class");
                String line = lx.skipLine();
                mClassName = line.substring(line.indexOf('L'), line.lastIndexOf(';')+1);
                mIsAnonymous = mClassName.contains("$");
                return true;
            }
        }
        if("super".equals(keyword)){
            if(mSupreClassName == null){
                lx.append(".super");
                String line = lx.skipLine();
                mSupreClassName = line.substring(line.indexOf('L'), line.lastIndexOf(';')+1);
                return true;
            }
        }

        else if("method".equals(keyword)){
            lx.append(".method");
            String line = lx.skipLine();
            if(line.contains("constructor <init>")) {
                if(mIsAnonymous)
                    return true;
                if(lx.skipTo(".locals", ".end")){
                    lx.skipWhitespace();
                    int args = Math.max(Integer.parseInt(lx.nextIdentifier()), 1);
                    lx.append(Integer.toString(args));
                    lx.skipToLineEnd();
                    // �������췽��
                    lx.skipTo(mSupreClassName+"-><init>", ".end");
                    lx.skipToLineEnd();
                    for(int i=1;i<=mFieldCount;i++){
                        lx.append("const-string v0, \""+rand()+"\"\n");
                        lx.append("iput-object v0, p0, "+mClassName+"->mTest"+i+":Ljava/lang/String;\n");
                    }
                }
            }
            else{
                if(Util.random(0, 2) == 0){
                    if(
                    lx.skipTo(".locals", ".end")){
                        lx.skipWhitespace();
                        int c = Util.random(1, 2);
                        int args = Math.max(Integer.parseInt(lx.nextIdentifier()), c);
                        lx.append(Integer.toString(args));
                        lx.skipToLineEnd();
                        for(int i=0;i<c;i++){
                            lx.append("const-string v"+i+", \""+rand()+"\"\n");
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean onDirective(String directive, SmaliParser lx) {
        return false;
    }

    public boolean onComment(SmaliParser lx) {
        lx.append("#");
        String comment = lx.skipLine();
        if(comment.contains("static fields")){
            lx.append(".field static final _TEST"+(++mCounter)+":Ljava/lang/String; = \""+rand()+"\"\n");
        }
        else if(comment.contains("instance fields")){
            if(!mIsAnonymous){
                if(mFieldCount == 0){
                    mFieldCount = Util.random(1, 3);
                    for(int i=1;i<=mFieldCount;i++){
                        lx.append(".field private mTest"+i+":Ljava/lang/String;\n");
                    }
                }
            }
        }
        return true;
    }

    private static final String rand(){
        return SS[Util.random(0, SS.length-1)];
    }
}
