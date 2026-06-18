package github.boxiaolanya2008.kc_tool.shizuku;

interface IShizukuUserService {
    String executeCommand(String command);
    boolean checkRootAccess();
}