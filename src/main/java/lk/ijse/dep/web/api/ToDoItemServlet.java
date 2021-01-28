package lk.ijse.dep.web.api;


import com.sun.xml.internal.ws.api.ha.StickyFeature;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet
public class ToDoItemServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        Jsonb jsonb = JsonbBuilder.create();
        if(req.getPathInfo()==null||req.getPathInfo().equals("/")){
            try(Connection connection =cp.getConnection()) {
                PreparedStatement pstm1= connection.prepareStatement("SELECT * FROM to_do_item WHERE username = ?");
                pstm1.setObject(1,req.getAttribute("username"));
                ResultSet rst1= pstm1.executeQuery();

                if()


            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }



    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
