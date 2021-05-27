//saber cuando el programa termina
//Cambiar links relativos y añadir /index.html
//htm

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
    final ArrayList<String> urlsDescargados = new ArrayList<>();


    class Manejador implements Runnable{
        String dirActual;
        String url;

        public Manejador(String url, String dirActual) {
            this.url = url;
            this.dirActual = dirActual;
        }

        @Override
        public void run() {

            if(urlsDescargados.contains(url) || urlsDescargados.size()>900){
                System.out.println("El url ya fue descargado");
                return;
            }

            System.out.println("Iniciando la descarga");

            synchronized (urlsDescargados){
                urlsDescargados.add(url);
            }

            try {
                URL obj = new URL(url);
                HttpURLConnection conex = (HttpURLConnection) obj.openConnection();
                conex.setRequestMethod("GET");
                conex.setRequestProperty("User-Agent", "Mozilla/5.0");
                int responseCode = conex.getResponseCode();
                System.out.println("GET Response Code :: " + responseCode);

                if(responseCode == HttpURLConnection.HTTP_OK) { // success
                    String contentType = conex.getContentType();
                    System.out.println("Content type :: " + contentType);

                    if(contentType!=null && contentType.startsWith("text/html")){
                        String[] split =url.split("/");
                        File f = new File(dirActual+split[split.length-1]);
                        //crear carpeta con nombre url, en dirActual
                        f.mkdirs();
                        //modificar dir
                        dirActual=f.getAbsolutePath()+"\\";
                        //creaArchivo(conex);
                        leerArch(creaArchivo(conex));
                    }else{
                        creaArchivo(conex);
                    }

                } else {
                    System.out.println("GET request not worked");
                }
                conex.disconnect();
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("Hilo terminando");
        }

        public String creaArchivo(HttpURLConnection conex) throws IOException{
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            if(fileName.equals("")){
                fileName="index.html";
            }
            BufferedOutputStream bos= new BufferedOutputStream(new FileOutputStream(dirActual + fileName));

            InputStream inputStream = conex.getInputStream();
            byte[] buffer = new byte[1024];
            int bufferLength;
            int downloadedSize = 0;
            while ((bufferLength = inputStream.read ( buffer )) > 0) {

                bos.write ( buffer, 0, bufferLength );
                downloadedSize += bufferLength;
            }
            inputStream.close();
            bos.close();
            return fileName;
        }

        public void leerArch(String fileName) {
            Pattern MY_PATTERN = Pattern.compile("(href|src)=\".+?\"",Pattern.CASE_INSENSITIVE);

            try {
                FileReader fr = new FileReader(dirActual + fileName);//Nuevadir
                BufferedReader br = new BufferedReader(fr);
                String linea = br.readLine();
                StringBuilder htmlContent= new StringBuilder();
                while (linea != null) {
                    htmlContent.append(linea);
                    linea = br.readLine();
                }
                Matcher m = MY_PATTERN.matcher(htmlContent);
                while (m.find()) {
                    String relativeLink = m.group(0);
                    relativeLink = relativeLink.substring(relativeLink.indexOf("\"")+1,relativeLink.length()-1);
                    System.out.println("relative link = " + relativeLink);
                    if(!relativeLink.startsWith("?")){

                        if(!url.contains(relativeLink)){
                            if(relativeLink.startsWith("http")){
                                System.out.println("Descargando: " + relativeLink);
                                pool.execute(new Manejador(relativeLink,dirActual));
                            }
                            else {
                                System.out.println("Descargando: " + url + relativeLink);
                                pool.execute(new Manejador(url + relativeLink, dirActual));
                            }
                        }
                    }

                }
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public WebClient() {

        pool = Executors.newFixedThreadPool(10);

        //while (true){
        String url;
            System.out.println("Inserte la URL que desea descargar: \n>");
            Scanner aux= new Scanner(System.in);
            url=aux.nextLine();

            File f = new File("");

            pool.execute(new Manejador(url, f.getAbsolutePath() + "\\Descargas" + "\\"));
        //}
    }

    public static void main(String[] args) { new WebClient(); }
}
