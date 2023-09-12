package nkp.pspValidator.web.backend.cli;

public class Version {

    public String version = "1.0.0";
    public String build;


    //TODO:fix
    /*public Version() {
        URL url = Version.class.getResource("");
        if (url != null) {
            String basePath = url.getPath();
            System.out.println("Base Path: " + basePath);
        } else {
            System.err.println("Base Path not found.");
        }

        try (InputStream input = Version.class.getClassLoader().getResourceAsStream("version.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            this.version = properties.getProperty("app.version");
            this.build = properties.getProperty("build.timestamp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
