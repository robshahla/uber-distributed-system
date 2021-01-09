import grpc.*;
import rest.host.RestMain;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        String port = s.next();
        String port_rest = s.next();
        String[] argss_grpc = {port};
        String[] argss_rest = {port_rest};
        GrpcMain.run(argss_grpc);
        RestMain.run(argss_rest);
    }
}
