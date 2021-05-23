import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class WebClient {

    private String dirActual;
    private String FileName;

    private  void sendGET(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection conex = (HttpURLConnection) obj.openConnection();
        conex.setRequestMethod("GET");
        conex.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = conex.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);

        String contentType = conex.getContentType();
        System.out.println("Content type :: " + contentType);

        if(responseCode == HttpURLConnection.HTTP_OK) { // success
            if(contentType.startsWith("text/html")){
                System.out.println("Es un html, hacer analisis de links :'c");
                //buscar link
                //sengGET(nuevo link)

            }
            else{
                creaArchivo(url, conex);
            }
        } else {
            System.out.println("GET request not worked");
        }

    }

    public void creaArchivo(String url, HttpURLConnection conex) throws IOException{
        FileName= url.substring(url.lastIndexOf("/")+1);
        System.out.println("FileName "+FileName);

        File f = new File("");
        dirActual = f.getAbsolutePath()+"\\Descargas"+"\\";
        BufferedOutputStream bos= new BufferedOutputStream(new FileOutputStream(dirActual+FileName));

        InputStream inputStream = conex.getInputStream();
        byte[] buffer = new byte[1024];
        int bufferLength = 0;
        int downloadedSize = 0;
        while ((bufferLength = inputStream.read ( buffer )) > 0) {

            bos.write ( buffer, 0, bufferLength );
            downloadedSize += bufferLength;
        }
        System.out.println("Archivo recibido");

        bos.close();
    }

    public static void main(String[] args) throws IOException {
        WebClient wbcl = new WebClient();

        while (true){
            String url;
            System.out.println("Inserte la URL que desea descargar: \n>");
            Scanner aux= new Scanner(System.in);
            url=aux.nextLine();
            wbcl.sendGET(url);
        }
    }
}
