package app;

import com.beust.jcommander.Parameter;

import java.util.List;
public class Commands {
    @Parameter(names = {"-h", "--help"}, description = "Help Info", help = true)
    public boolean help;

    @Parameter(names = {"-s", "--superclass"}, description = "JAVA SuperClass")
    public String superclass;

    @Parameter(names = {"--static"}, description = "try do static")
    public boolean isstatic;

    @Parameter(names = {"--jar"}, description = "use base rt.jar")
    public boolean isjar;

    @Parameter(names = {"--debug"}, description = "make debug")
    public boolean isdedug;

    @Parameter(names = {"-j", "--targetpath"}, description = "JAVA Target PATH")
    public String jarpath;

    @Parameter(names = {"-i", "--interface"}, description = "JAVA InterFace Name")
    public List<String> interfaces;

    @Parameter(names = {"-m", "--method"}, description = "JAVA Method Name")
    public String method;

    @Parameter(names = {"-p", "--paramter"}, description = "JAVA Method Paramter Type")
    public String paramter;

    @Parameter(names = {"-r", "--return"}, description = "JAVA Method Return Paramter Type")
    public String returnparamter;

}
