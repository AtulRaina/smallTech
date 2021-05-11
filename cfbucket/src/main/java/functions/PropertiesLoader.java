package functions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    public Properties prop;
    public PropertiesLoader() throws IOException {
        InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream("config.properties");
        prop = new Properties();
        prop.load(input);
    }
}
