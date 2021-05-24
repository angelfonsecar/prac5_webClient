import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebClient {
    ExecutorService pool;
    ArrayList<String> urlsDescargados = new ArrayList<>();
    String dirActual;

    class Manejador implements Runnable{
        String url;
        public Manejador(String url) {
            this.url = url;
        }

        @Override
        public void run() {

            if(urlsDescargados.contains(url)){
                System.out.println("El url ya fue descargado");
                return;
            }

            System.out.println("Iniciando la descarga");

            synchronized (urlsDescargados){
                System.out.println("Accediendo al bloque syncro");
                urlsDescargados.add(url);
            }

            try {
                URL obj = new URL(url);
                HttpURLConnection conex = (HttpURLConnection) obj.openConnection();
                conex.setRequestMethod("GET");
                conex.setRequestProperty("User-Agent", "Mozilla/5.0");
                int responseCode = conex.getResponseCode();
                System.out.println("GET Response Code :: " + responseCode);

                String contentType = conex.getContentType();
                System.out.println("Content type :: " + contentType);

                if(responseCode == HttpURLConnection.HTTP_OK) { // success
                    creaArchivo(url, conex);

                    if(contentType.startsWith("text/html")){
                        System.out.println("Es un html, hacer analisis de links :'c");
                        leerArch();

                    }
                    conex.disconnect();
                    System.out.println("Hilo terminando");
                } else {
                    System.out.println("GET request not worked");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void creaArchivo(String url, HttpURLConnection conex) throws IOException{
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            if(fileName.equals("")){
                fileName="index.html";
            }
            System.out.println("FileName "+ fileName);

            File f = new File("");
            dirActual = f.getAbsolutePath() + "\\Descargas" + "\\";
            BufferedOutputStream bos= new BufferedOutputStream(new FileOutputStream(dirActual + fileName));

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

    }

/*    private  void sendGET(String url) throws IOException {
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

    }*/


    public WebClient() {

        pool = Executors.newFixedThreadPool(3);

        //while (true){
            String url;
            System.out.println("Inserte la URL que desea descargar: \n>");
            Scanner aux= new Scanner(System.in);
            url=aux.nextLine();

            pool.execute(new Manejador(url));
        //}
    }

    public static void main(String[] args) {
        new WebClient();
    }
}
