import java.io.*;
import java.net.*;

public class server {
 
    /**
     * @param args
     */
	public static void main(String[] args) {
		System.out.printf("‹N“®\n");
		File file = new File("c:\\temp\\server.txt");
		try {
			file.delete();
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(10000);
			boolean runFlag = true;
			while(runFlag){
				Socket socket = serverSocket.accept();
				BufferedReader br =
						new BufferedReader(
								new InputStreamReader(socket.getInputStream()));
				String str;
				while( (str = br.readLine()) != null ){
					//file‚Ö‚Ì‘‚«‚İ
					try{
						FileWriter filewriter = new FileWriter(file,true);
						filewriter.write(str+"\r\n");
						filewriter.close();
					}catch(IOException e){
						System.out.println(e);
					}
					System.out.println(str);
					// exit‚Æ‚¢‚¤•¶š—ñ‚ğó‚¯æ‚Á‚½‚çI—¹‚·‚é
					if( "exit".equals(str)){
						runFlag = false;
					}
				}
				if( socket != null){
					socket.close();
					socket = null;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
 
		if( serverSocket != null){
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
}