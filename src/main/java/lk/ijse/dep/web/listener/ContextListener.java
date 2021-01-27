package lk.ijse.dep.web.listener;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.Properties;

@WebListener
public class ContextListener implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing connection pool");
        Properties properties= new Properties();
        try{

            properties.load(this.getClass().getResourceAsStream("/application.properties"));






        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
