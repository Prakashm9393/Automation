package utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.io.FileUtils;

public class UploadFile {
	
	public static boolean uploadReportIntoServer(String src, String dest){
		boolean bReturn = false;
		File client = new File(src);
		File server = new File(dest);
		try {
			System.out.println("Uploading...");
		    FileUtils.copyDirectory(client, server);		    
		    bReturn = true;
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return bReturn;
	}
	
	public static String serverIpAddress(){
		String serverIp = null;
		try {
			InetAddress IP=InetAddress.getLocalHost();
			serverIp = IP.getHostAddress();
		} catch (UnknownHostException e) {			
			e.printStackTrace();
		}
		return serverIp;
	}
	
	public static void execute(){
		if (serverIpAddress().equals(Constant.Server_IP)) {
			if (uploadReportIntoServer(Constant.Client_Path, Constant.Server_Path)) {
				System.out.println("Report was successfully uploaded into server.");
			} else {
				throw new RuntimeException("Unable to upload report into server.");
			} 
		}else{
			System.err.println("Note: This is the client system, so unable to upload test report automatically.");
			System.err.println("      Kindly upload report file by manually into the server.");
		}
	}	
	
}
